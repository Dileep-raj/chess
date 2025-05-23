package com.drdedd.chess.game.pgn;

import com.drdedd.chess.game.gameData.ChessAnnotation;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PGNMove {
    private final int n;
    private final String san, uci;
    private String e, cl;
    private ChessAnnotation a;
    private ArrayList<String> al, cs;

    /**
     * @param moveNumber     Half move number/ply number
     * @param san            Move in SAN notation
     * @param uci            Move in UCI notation
     * @param eval           Eval of the position
     * @param chessAnnotation     Annotation of the move
     * @param alternateMoves Alternate move sequences
     * @param comments       Comments on the move
     */
    public PGNMove(int moveNumber, String san, String uci, String eval, ChessAnnotation chessAnnotation, ArrayList<String> alternateMoves, ArrayList<String> comments) {
        n = moveNumber;
        this.san = san;
        this.uci = uci;
        e = eval;
        a = chessAnnotation;
        al = alternateMoves;
        cs = comments;
    }

    public void setAnnotation(ChessAnnotation a) {
        this.a = a;
    }

    public void setEval(String eval) {
        e = eval;
    }

    public void setClock(String clock) {
        cl = clock;
    }

    public void setAlternateMoves(ArrayList<String> a) {
        al = a;
    }

    public void addAlternateMoves(String a) {
        al.add(a);
    }

    public void setComments(ArrayList<String> c) {
        cs = c;
    }

    public void addComment(String c) {
        cs.add(c);
    }

    public String getSAN() {
        return san;
    }

    public String getUCI() {
        return uci;
    }

    @Override
    public String toString() {
        return String.format("%s %s%s%s%s ", n / 2 + 1 + n % 2 == 0 ? "." : "...", san, a == null ? "" : a.getAnnotation(), al(), c());
    }

    private String al() {
        if (al == null || al.isEmpty()) return "";
        return al.stream().map(a -> String.format(" (%s)", a)).collect(Collectors.joining());
    }

    private String c() {
        if (cs == null || cs.isEmpty()) return "";
        return cs.stream().map(c -> String.format(" {%s}", c)).collect(Collectors.joining());
    }
}