package com.drdedd.chess.game.pieces;

import com.drdedd.chess.game.gameData.Player;
import com.drdedd.chess.game.gameData.Rank;
import com.drdedd.chess.game.interfaces.GameLogicInterface;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;

/**
 * Abstract class for chess piece
 */
public abstract class Piece implements Serializable, Cloneable {
    /**
     * -- GETTER --
     * Returns Player type of the piece
     */
    @Getter
    private final Player player;
    /**
     * Column number of the piece
     */
    @Getter
    private int col;
    /**
     * Row number of the piece
     */
    @Getter
    private int row;
    /**
     * {@link Rank Rank} of the piece
     */
    @Getter
    private final Rank rank;
    /**
     * Piece has moved from its initial position
     */
    @Setter
    protected boolean moved;
    /**
     * Piece is captured or not
     */
    @Setter
    @Getter
    protected boolean captured;
    @Getter
    private final String unicode;

    /**
     * @param player Player type (<code>WHITE|BLACK</code>)
     * @param rank   {@link Rank} of the piece
     * @param row    Row number of the piece
     * @param col    Column number of the piece
     */
    protected Piece(Player player, int row, int col, Rank rank, String unicode) {
        this.player = player;
        this.row = row;
        this.col = col;
        this.rank = rank;
        this.unicode = unicode;
        this.captured = false;
    }

    /**
     * @return <code>true|false</code> - Piece belongs to white
     */
    public boolean isWhite() {
        return player == Player.WHITE;
    }

    /**
     * Converts logical position to standard notation
     *
     * @return Standard algebraic notation of the position
     */
    public String getPosition() {
        return String.format(Locale.ENGLISH, "%s%s%d", getRankChar(), (char) ('a' + col), row + 1);
    }

    public String getSquare() {
        return String.format(Locale.ENGLISH, "%s%s", (char) ('a' + col), row + 1);
    }

    /**
     * Character of rank of the piece
     *
     * @return <code>K|Q|R|B|N|P</code>
     */
    public char getRankChar() {
        return rank.getLetter();
    }

    /**
     * Moves the piece to the given position
     *
     * @param row Row number of new position
     * @param col Column number of new position
     */
    public void moveTo(int row, int col) {
        this.row = row;
        this.col = col;
        this.moved = true;
    }

    /**
     * @param gameLogicInterface GameLogicInterface of the game
     * @param row                Row number
     * @param col                Column number
     * @return <code>true|false</code> - Piece can move to the position on the board
     */
    public abstract boolean canMoveTo(GameLogicInterface gameLogicInterface, int row, int col);

    /**
     * @return <code>true|false</code> - Piece can capture another piece on the board
     */
    public abstract boolean canCapture(GameLogicInterface gameLogicInterface, Piece capturingPiece);

    /**
     * @return <code>true|false</code> - Piece has moved
     */
    public boolean hasNotMoved() {
        return !moved;
    }

    /**
     * Adds a move to possible moves of the piece
     *
     * @param possibleMoves    <code>HashSet</code> of possible moves of the piece
     * @param obstructingPiece <code>Piece</code> obstructing further moves
     * @param row              Row number of the move
     * @param col              Column number of the move
     * @return <code>Boolean</code> - Continue adding moves without obstruction
     */
    protected boolean addMove(HashSet<Integer> possibleMoves, Piece obstructingPiece, int row, int col) {
        if (obstructingPiece == null) {
            possibleMoves.add(row * 8 + col);
            return true;
        } else if (obstructingPiece.getPlayer() != getPlayer()) possibleMoves.add(row * 8 + col);
        return false;
    }

    /**
     * @return <code>true|false</code> - Piece is <code>{@link King King}</code>
     */
    public boolean isKing() {
        return rank == Rank.KING;
    }

    @Override
    public String toString() {
        return unicode;
    }

    /**
     * Finds all possible moves of the piece on the board
     *
     * @param gameLogicInterface GameLogicInterface of the current board
     * @return <code>HashSet</code> of possible positions of the piece
     */
    public abstract HashSet<Integer> getPossibleMoves(GameLogicInterface gameLogicInterface);

    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
