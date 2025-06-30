package com.drdedd.chess.engine.stockfish;

import lombok.Getter;

import java.util.*;

/**
 * A data class to represent each output line of Stockfish
 */
public class EngineLine {

    public static final String INFO_DEPTH = "depth", INFO_VARIATION = "multipv", INFO_SELDEPTH = "seldepth";
    public static final String INFO_SCORE = "score", INFO_NODES = "nodes", INFO_NODES_PER_SECOND = "nps";
    public static final String INFO_HASHFULL = "hashfull", INFO_TABLE_HITS = "tbhits", INFO_TIME = "time";
    public static final String INFO_PRIMARY_VARIATION = "pv", INFO_BEST_MOVE = "bestmove", INFO_PONDER = "ponder", INFO_EVAL = "eval";
    public static final String WHITE_WON = "1-0", BLACK_WON = "0-1", DRAW = "1/2-1/2";
    @Getter
    private static final HashSet<String> infoParameters = new HashSet<>(Set.of(INFO_DEPTH, INFO_VARIATION, INFO_SCORE, INFO_TIME, INFO_NODES, INFO_NODES_PER_SECOND, INFO_PONDER, INFO_PRIMARY_VARIATION, INFO_SELDEPTH, INFO_TABLE_HITS, INFO_HASHFULL, INFO_BEST_MOVE, INFO_EVAL));

    @Getter
    private String line, pv, depth, variation, seldepth, score, nodes, nps, hashfull, tbhits, time, bestmove, ponder, eval;
    public ArrayList<String> moves;

    EngineLine(String line, boolean whiteToPlay, boolean gameOver) {
        moves = new ArrayList<>();

        if (line == null) return;

        this.line = line;

        Scanner reader = new Scanner(line);
        reader.next(); //Skip the first word
        String parameter = reader.next(), word, value;
        StringBuilder valueBuilder = new StringBuilder(reader.next());
        while (reader.hasNext()) {
            word = reader.next();
            if (infoParameters.contains(word)) {
                value = valueBuilder.toString().trim();
                switch (parameter) {
                    case INFO_DEPTH -> depth = value;
                    case INFO_VARIATION -> variation = value;
                    case INFO_SELDEPTH -> seldepth = value;
                    case INFO_SCORE -> score = value;
                    case INFO_NODES -> nodes = value;
                    case INFO_NODES_PER_SECOND -> nps = value;
                    case INFO_HASHFULL -> hashfull = value;
                    case INFO_TABLE_HITS -> tbhits = value;
                    case INFO_TIME -> time = value;
                    case INFO_PRIMARY_VARIATION -> pv = value;
                }
                parameter = word;
                valueBuilder = new StringBuilder();
            } else valueBuilder.append(word).append(' ');
        }

        value = valueBuilder.toString().trim();
        switch (parameter) {
            case INFO_DEPTH -> depth = value;
            case INFO_VARIATION -> variation = value;
            case INFO_SELDEPTH -> seldepth = value;
            case INFO_SCORE -> score = value;
            case INFO_NODES -> nodes = value;
            case INFO_NODES_PER_SECOND -> nps = value;
            case INFO_HASHFULL -> hashfull = value;
            case INFO_TABLE_HITS -> tbhits = value;
            case INFO_TIME -> time = value;
            case INFO_PRIMARY_VARIATION -> pv = value;
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
            if (split[0].equals("cp")) return DRAW;
            else if (split[0].equals("mate")) return whiteToPlay ? BLACK_WON : WHITE_WON;
        }
        int no = Integer.parseInt(split[1]);
        if (!whiteToPlay) no = -no;
        String prefix = no == 0 ? "" : no > 0 ? "+" : "-";
        if (split[0].equals("mate")) return prefix + "M" + Math.abs(no);
        return prefix + Math.abs((float) no / 100);
    }

    private void build(StringBuilder s, String name, String value) {
        s.append(name).append(" = ").append(value);
    }

    private String print() {
        if (depth == null) return "-";
        if (variation == null) return eval;
        StringBuilder s = new StringBuilder();
        build(s, "Variation", variation + ", ");
        build(s, INFO_DEPTH, depth + ", ");
        build(s, INFO_EVAL, eval + '\n');
        build(s, INFO_BEST_MOVE, bestmove + ", ");
        build(s, INFO_PONDER, ponder);
        build(s, "\nMoves", moves.toString());
        return s.toString();
    }

    @Override
    public String toString() {
        return print();
    }
}
