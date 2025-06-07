package com.drdedd.chess.engine;

import com.drdedd.chess.api.data.EvaluationData;
import com.drdedd.chess.engine.stockfish.EngineLine;
import com.drdedd.chess.engine.stockfish.Stockfish;
import com.drdedd.chess.engine.stockfish.StockfishOption;
import com.drdedd.chess.game.data.Regexes;

import java.util.ArrayList;
import java.util.List;

public class FENEvaluator {
    public static final int NO_LIMIT = -1, MAX_DEPTH = 30, MIN_DEPTH = 15, MAX_VARIATIONS = 5;
    private final int evaluationDepth, evaluationVariations;

    public FENEvaluator(int depth, int variations) {
        evaluationDepth = depth < 1 ? MIN_DEPTH : Math.min(depth, MAX_DEPTH);
        evaluationVariations = variations < 1 ? 1 : Math.min(variations, MAX_VARIATIONS);
    }

    public EvaluationData evaluate(String FEN) {
        EvaluationData data = new EvaluationData();
        data.setSuccess(false);
        data.setFen(FEN);

        if (!FEN.matches(Regexes.FENRegex)) {
            data.setError("Invalid FEN!");
            return data;
        }

        try {
            // Initialize engine
            HardwareInfo hardwareInfo = new HardwareInfo();
            Stockfish stockfish = new Stockfish(String.valueOf(hardwareInfo.maximumSafeThreads()));
            stockfish.setOption(StockfishOption.optionMultiPV, String.valueOf(evaluationVariations));
            data.setEngine(stockfish.getStockfishVersion());

            data.setEngine(stockfish.getStockfishVersion());

            ArrayList<EngineLine> engineLines = stockfish.getEngineLines(FEN, "", NO_LIMIT, evaluationDepth, NO_LIMIT);
            stockfish.quitEngine();
            List<List<String>> variations = new ArrayList<>();

            for (EngineLine line : engineLines) variations.add(line.moves);

            data.setSuccess(true);
            data.setMessage("Evaluation successful");
            data.setEval(engineLines.getFirst().getEval());
            data.setBestmove(engineLines.getFirst().getBestmove());
            data.setEngineLine(engineLines.getFirst().getLine());
            data.setVariations(variations);
        } catch (Exception e) {
            System.err.println("Error while analyzing PGN!");
            e.printStackTrace(System.err);
        }
        return data;
    }
}
