package com.drdedd.chess.game.pgn;

import com.drdedd.chess.game.data.ChessAnnotation;
import com.drdedd.chess.game.data.Rank;
import com.drdedd.chess.misc.MiscMethods;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Single move in a PGN
 */
public class PGNMove {
    private final int moveNumber;
    @Getter
    private final String san, uci;
    @Getter
    private final int fromRow, fromCol, toRow, toCol;
    @Getter
    private final Rank promotionRank;
    @Getter
    private final boolean check, checkmate;
    @Getter
    @Setter
    private String eval, clock;
    @Setter
    private ChessAnnotation annotation;
    private final ArrayList<String> alternateMoves;
    private final ArrayList<String> comments;

    /**
     * @param moveNumber    Half move number/ply number
     * @param san           Move in SAN notation
     * @param fromRow       Initial row number of the piece
     * @param fromCol       Initial column number of the piece
     * @param toRow         Destination row number of the piece
     * @param toCol         Destination column number of the piece
     * @param promotionRank Rank of promotion if pawn promotion
     * @param check         Check flag
     * @param checkmate     Checkmate flag
     */
    public PGNMove(int moveNumber, String san, int fromRow, int fromCol, int toRow, int toCol, Rank promotionRank, boolean check, boolean checkmate) {
        this.moveNumber = moveNumber;
        this.san = san;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        uci = MiscMethods.getUCIMove(fromRow, fromCol, toRow, toCol, promotionRank);
        this.promotionRank = promotionRank;
        this.check = check;
        this.checkmate = checkmate;
        alternateMoves = new ArrayList<>();
        comments = new ArrayList<>();
    }

    /**
     * @param moveNumber      Half move number/ply number
     * @param san             Move in SAN notation
     * @param uci             Move in UCI notation
     * @param eval            Eval of the position
     * @param chessAnnotation Annotation of the move
     * @param alternateMoves  Alternate move sequences
     * @param comments        Comments on the move
     */
    public PGNMove(int moveNumber, String san, String uci, boolean check, boolean checkmate, String eval, ChessAnnotation chessAnnotation, ArrayList<String> alternateMoves, ArrayList<String> comments) {
        this.moveNumber = moveNumber;
        this.san = san;
        this.uci = uci;
        String from = uci.substring(0, 2), to = uci.substring(2, 4);
        fromRow = MiscMethods.toRow(from);
        fromCol = MiscMethods.toCol(from);
        toRow = MiscMethods.toRow(to);
        toCol = MiscMethods.toCol(to);
        if (uci.length() == 5) {
            char ch = uci.charAt(4);
            promotionRank = switch (ch) {
                case 'r' -> Rank.ROOK;
                case 'n' -> Rank.KNIGHT;
                case 'b' -> Rank.BISHOP;
                default -> Rank.QUEEN;
            };
        } else promotionRank = null;
        this.check = check;
        this.checkmate = checkmate;
        this.eval = eval;
        annotation = chessAnnotation;
        this.alternateMoves = alternateMoves;
        this.comments = comments;
    }

    /**
     * Adds alternate moves for the move
     *
     * @param a Alternate move sequence
     */
    public void addAlternateMoves(String a) {
        alternateMoves.add(a);
    }

    /**
     * Adds comment to the move
     *
     * @param c Comment for the move
     */
    public void addComment(String c) {
        comments.add(c);
    }

    @Override
    public String toString() {
        return String.format("%s %s%s%s%s%s ", moveNumber / 2 + 1 + moveNumber % 2 == 0 ? "." : "...", san, annotation == null ? "" : annotation.getAnnotation(), al(), c(), misc());
    }

    /**
     * @return {@link String} - Alternate move sequences for the move
     */
    private String al() {
        if (alternateMoves == null || alternateMoves.isEmpty()) return "";
        return alternateMoves.stream().map(a -> String.format(" %s", a)).collect(Collectors.joining(" ", "(", ")"));
    }

    /**
     * @return {@link String} - Comments for the move
     */
    private String c() {
        if (comments == null || comments.isEmpty()) return "";
        return comments.stream().map(c -> String.format(" %s", c)).collect(Collectors.joining("", "{", "}"));
    }

    /**
     * @return {@link String} - Comment with eval and clocks
     */
    private String misc() {
        return " {%s%s}".formatted(eval == null || eval.isEmpty() ? "" : "[%eval " + eval + "]", clock == null || clock.isEmpty() ? "" : " [%clk " + clock + "]");
    }
}
