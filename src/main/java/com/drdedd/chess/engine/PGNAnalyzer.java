package com.drdedd.chess.engine;

import com.drdedd.chess.api.data.AnalysisData;
import com.drdedd.chess.api.data.AnalysisReport;
import com.drdedd.chess.engine.stockfish.EngineLine;
import com.drdedd.chess.engine.stockfish.Stockfish;
import com.drdedd.chess.engine.stockfish.StockfishOption;
import com.drdedd.chess.game.ParsedGame;
import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.game.gameData.ChessAnnotation;
import com.drdedd.chess.game.pgn.PGN;
import com.drdedd.chess.game.pgn.PGNData;
import com.drdedd.chess.game.pgn.PGNParser;
import com.drdedd.chess.misc.Log;
import com.drdedd.chess.misc.MiscMethods;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PGNAnalyzer {
    public static final int NO_LIMIT = -1, MAX_DEPTH = 30, MIN_DEPTH = 15;
    private static final float blunderThreshold = 2.5f, mistakeThreshold = 1.2f, inaccuracyThreshold = 0.6f;
    private static final int greatMoveThreshold = 5, maxCP = 15300;
    private final ArrayList<Integer> winPercentage, accuracy;
    private final ArrayList<String> evaluations, annotations;
    private final int initialEvaluationTime, evaluationTime, evaluationDepth;
    private double whiteCPLoss, blackCPLoss;
    private int whiteGreatMoves, blackGreatMoves, whiteInaccuracies, blackInaccuracies, whiteMistakes, blackMistakes, whiteBlunders, blackBlunders, whiteCPMoves, blackCPMoves, totalWhiteMoves, totalBlackMoves;
    private boolean startsWithWhite;
    private String pgnString;
    private Stockfish stockfish;
    private PGN pgn;
    private ArrayList<String> FENs;
    private PGNData pgnData;
    private ArrayList<String> moves;
    @Getter
    private AnalysisReport whiteReport, blackReport;

    public PGNAnalyzer(int depth, int timeLimit) {
        initialEvaluationTime = 15000;
        evaluationDepth = depth < 0 ? MIN_DEPTH : Math.min(depth, MAX_DEPTH);
        evaluationTime = timeLimit;

        evaluations = new ArrayList<>();
        annotations = new ArrayList<>();
        moves = new ArrayList<>();
        winPercentage = new ArrayList<>();
        accuracy = new ArrayList<>();
        whiteReport = new AnalysisReport();
        blackReport = new AnalysisReport();
    }

    public AnalysisData analyzePGN(String pgnContent, boolean includeFENs) {
        System.out.println("Analyzing PGN:");
        System.out.printf("%n%s%n%n", pgnContent);
        evaluations.clear();
        annotations.clear();
        winPercentage.clear();
        accuracy.clear();
        whiteReport = new AnalysisReport();
        blackReport = new AnalysisReport();

        AnalysisData data = new AnalysisData();
        data.setSuccess(false);

        try {
            // Parse PGN
            PGNParser pgnParser = new PGNParser(pgnContent);
            pgnParser.parse();
            ParsedGame parsedGame = pgnParser.getParsedGame();
            pgn = parsedGame.pgn();
            pgnData = pgn.getPGNData();
            FENs = new ArrayList<>(parsedGame.FENs());
            moves = new ArrayList<>(pgn.getUCIMoves());
            LinkedHashMap<String, String> tagsMap = pgnData.getTagsMap();
            startsWithWhite = !tagsMap.containsKey(PGN.TAG_FEN) || tagsMap.get(PGN.TAG_FEN).contains(" w ");

            // Initialize engine
            HardwareInfo hardwareInfo = new HardwareInfo();
            stockfish = new Stockfish(String.valueOf(hardwareInfo.maximumSafeThreads()));

            data.setEngine(stockfish.getStockfishVersion());

            // Start analysis
            analyze();

            pgn.addTag(PGN.TAG_ANALYZED_BY, "%s, depth %d, %s".formatted(stockfish.getStockfishVersion(), evaluationDepth, MiscMethods.formatNanoseconds(evaluationTime * 1000000L)));
            pgn.addTag(PGN.TAG_ANNOTATOR, "?");
            pgnString = pgn.toString();

            // Generate report
            computeReport();
            data.setMessage("Analysis successful");
            data.setSuccess(true);
            data.setPgn(pgnString);
            data.setDepth(evaluationDepth);
            data.setWhiteAnalysis(whiteReport);
            data.setBlackAnalysis(blackReport);
            data.setEvaluations(evaluations);
            data.setAnnotations(annotations);
            if (includeFENs) data.setFens(new ArrayList<>(parsedGame.FENs()));
        } catch (Exception e) {
            System.err.println("Error while analyzing PGN!");
            e.printStackTrace(System.err);
        }
        return data;
    }

    private void computeReport() {
        long whiteACPL = Math.round(whiteCPLoss / whiteCPMoves);
        long blackACPL = Math.round(blackCPLoss / blackCPMoves);

        String whiteName = pgnData.getTag(PGN.TAG_WHITE, "White");
        String blackName = pgnData.getTag(PGN.TAG_BLACK, "Black");

        long whiteAccuracy, blackAccuracy;
        whiteAccuracy = computeAverageAccuracy(true);
        blackAccuracy = computeAverageAccuracy(false);

        whiteReport.setName(whiteName);
        whiteReport.setGreat(whiteGreatMoves);
        whiteReport.setInaccuracy(whiteInaccuracies);
        whiteReport.setMistake(whiteMistakes);
        whiteReport.setBlunder(whiteBlunders);
        whiteReport.setAcpl((int) whiteACPL);
        whiteReport.setAccuracy((int) whiteAccuracy);

        blackReport.setName(blackName);
        blackReport.setGreat(blackGreatMoves);
        blackReport.setInaccuracy(blackInaccuracies);
        blackReport.setMistake(blackMistakes);
        blackReport.setBlunder(blackBlunders);
        blackReport.setAcpl((int) blackACPL);
        blackReport.setAccuracy((int) blackAccuracy);
    }

    /**
     * Analyzes the parsed PGN with each move and generate game report
     */
    private void analyze() {
        System.out.printf("%nAnalyzing with depth: %s, time: %s%n", evaluationDepth == -1 ? "?" : evaluationDepth, evaluationTime == -1 ? "?" : evaluationTime + " ms");
        boolean whiteToMove = startsWithWhite;
        long start, end;
        whiteCPLoss = blackCPLoss = whiteCPMoves = blackCPMoves = totalWhiteMoves = totalBlackMoves = 0;
        String FEN;
        ArrayList<EngineLine> currentLines, previousLines;
        System.out.printf("PGN moves: %s%n%n", pgn.getPGNMoves());
        try {
            start = System.nanoTime();

            FEN = FENs.getFirst();
            stockfish.setOption(StockfishOption.optionMultiPV, "2");

            // Initial evaluation
            currentLines = stockfish.getEngineLines(FEN, "", initialEvaluationTime, evaluationDepth, NO_LIMIT);

            evaluations.add(currentLines.getFirst().getEval());
            annotations.add("");
            winPercentage.add(computeWinPercent(currentLines.getFirst().getScore(), whiteToMove));

            System.out.println("Initial eval: " + evaluations.getFirst());

            whiteToMove = !whiteToMove;

            String previousEvaluation, currentEvaluation;
            previousEvaluation = evaluations.getFirst();
            boolean whitesMove = startsWithWhite;

            for (int i = 1; i <= moves.size(); i++) {
                String move = moves.get(i - 1);

                if (!move.matches(Regexes.uciRegex))
                    throw new Exception("Invalid UCI move: %s, Move no: %d, Position: %s".formatted(move, (i - 1) / 2, FEN));

                previousLines = currentLines;
                currentLines = stockfish.getEngineLines(FEN, move, evaluationTime, evaluationDepth, NO_LIMIT);

                evaluations.add(currentLines.getFirst().getEval());

                int winPercent = computeWinPercent(currentLines.getFirst().getScore(), whiteToMove);
                int acc = computeAccuracy(whiteToMove ? 100 - winPercentage.get(i - 1) : winPercentage.get(i - 1), whiteToMove ? 100 - winPercent : winPercent);

                winPercentage.add(winPercent);
                accuracy.add(acc);

                FEN = stockfish.getFEN();
                System.out.printf("Move %3d: %-5s eval: %-8s depth %3s %8sn/s%n", (i - 1) / 2 + 1, move, currentLines.getFirst().getEval(), currentLines.getFirst().getDepth(), MiscMethods.convertNumber(Long.parseLong(currentLines.getFirst().getNps())));

                whiteToMove = !whiteToMove;

                if (whitesMove) totalWhiteMoves++;
                else totalBlackMoves++;
                currentEvaluation = evaluations.get(i);
                ChessAnnotation chessAnnotation = getAnnotation(move, previousLines, previousEvaluation, currentEvaluation, whitesMove);
                if (chessAnnotation != null) {
                    pgnData.addAnnotation(i - 1, chessAnnotation);
                    annotations.add(chessAnnotation.name().toLowerCase());
                } else annotations.add("");
                pgnData.addComment(i - 1, "{ [%%eval %s] }".formatted(evaluations.get(i)));
                whitesMove = !whitesMove;
                previousEvaluation = currentEvaluation;
            }
            end = System.nanoTime();
            Log.printTime("analyzing PGN", end - start);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            stockfish.quitEngine();
        }
    }

    /**
     * Computes win percentage with the following formula:<br>
     * Win% = 50 + 50 x (2 / (1 + e<sup>-0.00368208 x centipawns</sup>) - 1)
     *
     * @param score       Score generated by the engine
     * @param whiteToMove White to play the next move
     * @return <code>int</code> - Win percentage for white
     */
    private int computeWinPercent(String score, boolean whiteToMove) {
        int cp = Integer.parseInt(score.split(" ")[1]);
        if (!whiteToMove) cp = -cp;
        if (score.contains("mate")) {
            if (cp >= 0) cp = maxCP;
            if (cp < 0) cp = -maxCP;
        }
        return (int) Math.round(50 + 50 * (2 / (1 + Math.exp(-0.00368208f * cp)) - 1));
    }

    /**
     * Computes accuracy of the move with the following formula:<br>
     * Accuracy% = 103.1668 x e<sup>-0.04354 x (winPercentBefore - winPercentAfter)</sup> - 3.1669
     *
     * @param winPercentBefore Win percentage before the move
     * @param winPercentAfter  Win percentage after the move
     * @return <code>int</code> - Accuracy of the move
     */
    private static int computeAccuracy(double winPercentBefore, double winPercentAfter) {
//        System.out.printf("winPercentBefore = %.0f, winPercentAfter = %.0f%n", winPercentBefore, winPercentAfter);
        return (int) (103.1668 * Math.exp(-0.04354 * (winPercentBefore - winPercentAfter)) - 3.1669);
    }

    /**
     * @param white Player to compute accuracy
     * @return <code>long</code> - Average of the accuracy of the player
     */
    private long computeAverageAccuracy(boolean white) {
        double totalAccuracy = 0;
//        System.out.printf("%n%s accuracy%n", white ? "White" : "Black");
        for (int i = startsWithWhite == white ? 0 : 1; i < accuracy.size(); i += 2) {
//            System.out.println(accuracy.get(i));
            totalAccuracy += accuracy.get(i);
        }
        return Math.round(totalAccuracy / (white ? totalWhiteMoves : totalBlackMoves));
    }

    /**
     * Computes analysis difference and gives annotation for the move
     *
     * @param previousEvaluation Analysis of previous position
     * @param currentEvaluation  Analysis of current position
     * @param whitesMove         Whether the current move was made by white
     * @return <code>String</code> - Chess move annotation <code>(??|?|?!)</code>
     */
    private ChessAnnotation getAnnotation(String move, ArrayList<EngineLine> previousLines, String previousEvaluation, String currentEvaluation, boolean whitesMove) {
        switch (currentEvaluation) {
            case PGN.RESULT_WHITE_WON, PGN.RESULT_BLACK_WON, PGN.RESULT_DRAW -> {
                return null;
            }
        }

        double difference = scoreDifference(previousEvaluation, currentEvaluation, whitesMove);

        if (isGreatMove(move, previousLines, whitesMove)) {
            if (whitesMove) whiteGreatMoves++;
            else blackGreatMoves++;
            return ChessAnnotation.GREAT;
        }
        if (!previousEvaluation.contains("M") && !currentEvaluation.contains("M")) if (whitesMove) {
            whiteCPMoves++;
            if (difference > 0) whiteCPLoss += difference * 100;
        } else {
            blackCPMoves++;
            if (difference > 0) blackCPLoss += difference * 100;
        }
        if (difference >= blunderThreshold) {
            if (whitesMove) whiteBlunders++;
            else blackBlunders++;
            return ChessAnnotation.BLUNDER;
        } else if (difference >= mistakeThreshold) {
            if (whitesMove) whiteMistakes++;
            else blackMistakes++;
            return ChessAnnotation.MISTAKE;
        } else if (difference >= inaccuracyThreshold) {
            if (whitesMove) whiteInaccuracies++;
            else blackInaccuracies++;
            return ChessAnnotation.INACCURACY;
        }

        return null;
    }

    private boolean isGreatMove(String move, ArrayList<EngineLine> engineLines, boolean whitesMove) {
        if (engineLines.get(1).getEval() == null) return false;
        return move.equals(engineLines.getFirst().getBestmove()) && scoreDifference(engineLines.getFirst().getEval(), engineLines.get(1).getEval(), whitesMove) > greatMoveThreshold;
    }

    private double scoreDifference(String score1, String score2, boolean whitesMove) {
        double previous = 0.0, current = 0.0, difference;
        int multiplier = whitesMove ? 1 : -1;

        if (score1.contains("M")) previous = score1.startsWith("+") ? 153 : -153;
        if (score2.contains("M")) current = score2.startsWith("+") ? 153 : -153;

        try {
            if (!score1.contains("M")) previous = Double.parseDouble(score1);
            if (!score2.contains("M")) current = Double.parseDouble(score2);

            if (previous == 0.0f && current == 0.0f) return 0;

            difference = multiplier * (previous - current);
            return difference;
        } catch (Exception e) {
            System.err.println("Error while calculating difference!");
            e.printStackTrace(System.err);
            return 0;
        }
    }

    /**
     * @return <code>String</code> - Parsed and analyzed PGN with annotations
     */
    public String getAnalyzedPGN() {
        return pgnString;
    }

    /**
     * Prints and stores the final report of the game
     */
    public void printReport() {
        long whiteACPL = Math.round(whiteCPLoss / whiteCPMoves);
        long blackACPL = Math.round(blackCPLoss / blackCPMoves);

        String whiteName = pgnData.getTag(PGN.TAG_WHITE, "White");
        String blackName = pgnData.getTag(PGN.TAG_BLACK, "Black");

        long whiteAccuracy = computeAverageAccuracy(true), blackAccuracy = computeAverageAccuracy(false);

        System.out.printf("%nTotal moves\tWhite: %d\tBlack: %d%n%n", totalWhiteMoves, totalBlackMoves);

        String decor = "-".repeat(17);
        System.out.printf("%n%sGame Report%s%n", decor, decor);
        System.out.printf("%15s%15s%15s%n", "", "♙ " + whiteName.substring(0, Math.min(whiteName.length(), 10)), "♟ " + blackName.substring(0, Math.min(blackName.length(), 10)));
        System.out.printf("%15s%15s%15s%n", "Great", whiteGreatMoves, blackGreatMoves);
        System.out.printf("%15s%15s%15s%n", "Inaccuracies", whiteInaccuracies, blackInaccuracies);
        System.out.printf("%15s%15s%15s%n", "Mistakes", whiteMistakes, blackMistakes);
        System.out.printf("%15s%15s%15s%n", "Blunders", whiteBlunders, blackBlunders);
        System.out.printf("%15s%15d%15d%n", "Average CPL", whiteACPL, blackACPL);
        System.out.printf("%15s%15s%%%14s%%%n", "Accuracy", whiteAccuracy, blackAccuracy);
    }

}
