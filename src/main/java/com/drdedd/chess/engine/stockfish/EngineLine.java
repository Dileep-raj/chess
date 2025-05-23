package com.drdedd.chess.engine.stockfish;

import lombok.Getter;

import java.util.*;

/**
 * A data class to represent each output line of Stockfish
 */
public class EngineLine {

    public static final String infoDepth = "depth", infoVariation = "multipv", infoSeldepth = "seldepth";
    public static final String infoScore = "score", infoNodes = "nodes", infoNodesPerSecond = "nps";
    public static final String infoHashfull = "hashfull", infoTableHits = "tbhits", infoTime = "time";
    public static final String infoPrimaryVariation = "pv", infoBestMove = "bestmove", infoPonder = "ponder", infoEval = "eval";
    public static final String whiteWon = "1-0", blackWon = "0-1", draw = "1/2-1/2";
    @Getter
    private static final HashSet<String> infoParameters = new HashSet<>(Set.of(infoDepth, infoVariation, infoScore, infoTime, infoNodes, infoNodesPerSecond, infoPonder, infoPrimaryVariation, infoSeldepth, infoTableHits, infoHashfull, infoBestMove, infoEval));

    private String pv;
    @Getter
    private String depth;
    @Getter
    private String variation;
    private String seldepth;
    @Getter
    private String score;
    private String nodes;
    private String nps;
    private String hashfull;
    private String tbhits;
    private String time;
    private String bestmove;
    @Getter
    private String ponder;
    @Getter
    private String eval;
    public ArrayList<String> moves;

    EngineLine(String line, boolean whiteToPlay, boolean gameOver) {
        moves = new ArrayList<>();

        if (line == null) return;

        Scanner reader = new Scanner(line);
        reader.next(); //Skip the first word
        String parameter = reader.next(), word, value;
        StringBuilder valueBuilder = new StringBuilder(reader.next());
        while (reader.hasNext()) {
            word = reader.next();
            if (infoParameters.contains(word)) {
                value = valueBuilder.toString().trim();
                switch (parameter) {
                    case infoDepth -> depth = value;
                    case infoVariation -> variation = value;
                    case infoSeldepth -> seldepth = value;
                    case infoScore -> score = value;
                    case infoNodes -> nodes = value;
                    case infoNodesPerSecond -> nps = value;
                    case infoHashfull -> hashfull = value;
                    case infoTableHits -> tbhits = value;
                    case infoTime -> time = value;
                    case infoPrimaryVariation -> pv = value;
                }
                parameter = word;
                valueBuilder = new StringBuilder();
            } else valueBuilder.append(word).append(' ');
        }

        value = valueBuilder.toString().trim();
        switch (parameter) {
            case infoDepth -> depth = value;
            case infoVariation -> variation = value;
            case infoSeldepth -> seldepth = value;
            case infoScore -> score = value;
            case infoNodes -> nodes = value;
            case infoNodesPerSecond -> nps = value;
            case infoHashfull -> hashfull = value;
            case infoTableHits -> tbhits = value;
            case infoTime -> time = value;
            case infoPrimaryVariation -> pv = value;
        }

        if (pv != null && !pv.isEmpty()) {
            moves.addAll(List.of(pv.split(" ")));
            bestmove = moves.get(0);
            if (moves.size() > 1) ponder = moves.get(1);
        }

        eval = parseScore(score, whiteToPlay, gameOver);
        if (nps == null) nps = "0";
    }

    /**
     * Parses com.drdedd.Chess.engine score to evaluation score
     *
     * @param score       Score string
     * @param gameOver    If there are no legal moves
     * @param whiteToPlay Whether next move is white
     * @return Evaluation of the position {@code (+|-|<empty>)<evaluation>|Draw|1-0|0-1}
     */
    private static String parseScore(String score, boolean whiteToPlay, boolean gameOver) {
        String[] split = score.split(" ");
        if (gameOver) {
            if (split[0].equals("cp")) return draw;
            else if (split[0].equals("mate")) return whiteToPlay ? blackWon : whiteWon;
        }
        int no = Integer.parseInt(split[1]);
        if (!whiteToPlay) no = -no;
        String prefix = no == 0 ? "" : no > 0 ? "+" : "-";
        if (split[0].equals("mate")) return prefix + "M" + Math.abs(no);
        return prefix + Math.abs((float) no / 100);
    }

    public String getNodesPerSecond() {
        return nps;
    }

    public String getBestMove() {
        return bestmove;
    }

    private void build(StringBuilder s, String name, String value) {
        s.append(name).append(" = ").append(value);
    }

    private String print() {
        if (depth == null) return "-";
        if (variation == null) return eval;
        StringBuilder s = new StringBuilder();
        build(s, "Variation", variation + ", ");
        build(s, infoDepth, depth + ", ");
        build(s, infoEval, eval + '\n');
        build(s, infoBestMove, bestmove + ", ");
        build(s, infoPonder, ponder);
        build(s, "\nMoves", moves.toString());
        return s.toString();
    }

    @Override
    public String toString() {
        if (depth == null) return "-";
        if (variation == null) return eval;
        StringBuilder stringBuilder = new StringBuilder();
        build(stringBuilder, "Variation", variation + ", ");
        build(stringBuilder, infoDepth, depth + ", ");
        build(stringBuilder, infoEval, eval + '\n');
        build(stringBuilder, infoBestMove, bestmove + ", ");
        build(stringBuilder, infoPonder, ponder);
        return stringBuilder.toString();
    }
}