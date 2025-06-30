package com.drdedd.chess.game.pieces;

import com.drdedd.chess.game.gameData.Player;
import com.drdedd.chess.game.gameData.Rank;
import com.drdedd.chess.game.interfaces.GameLogicInterface;

import java.util.HashSet;

/**
 * {@inheritDoc}
 */
public class Queen extends Piece {

    /**
     * Creates a new <code>Queen</code> piece
     *
     * @param player Player type (<code>WHITE|BLACK</code>)
     * @param row    Row number of the piece
     * @param col    Column number of the piece
     */
    public Queen(Player player, int row, int col, String unicode) {
        super(player, row, col, Rank.QUEEN, unicode);
    }

    @Override
    public boolean canMoveTo(GameLogicInterface gameLogicInterface, int row, int col) {
        return getPossibleMoves(gameLogicInterface).contains(row * 8 + col);
    }

    @Override
    public boolean canCapture(GameLogicInterface gameLogicInterface, Piece capturingPiece) {
        return canMoveTo(gameLogicInterface, capturingPiece.getRow(), capturingPiece.getCol());
    }


    @Override
    public HashSet<Integer> getPossibleMoves(GameLogicInterface gameLogicInterface) {
        HashSet<Integer> possibleMoves = new HashSet<>();
        int i, j;
//        Column top
        for (i = getRow() + 1, j = getCol(); i < 8; i++)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Column bottom
        for (i = getRow() - 1, j = getCol(); i >= 0; i--)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Row right
        for (i = getRow(), j = getCol() + 1; j < 8; j++)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Row left
        for (i = getRow(), j = getCol() - 1; j >= 0; j--)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Top right diagonal
        for (i = getRow() + 1, j = getCol() + 1; i < 8 && j < 8; i++, j++)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Bottom left diagonal
        for (i = getRow() - 1, j = getCol() - 1; i >= 0 && j >= 0; i--, j--)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Bottom right diagonal
        for (i = getRow() - 1, j = getCol() + 1; i >= 0 && j < 8; i--, j++)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;

//        Top left diagonal
        for (i = getRow() + 1, j = getCol() - 1; i < 8 && j >= 0; i++, j--)
            if (!addMove(possibleMoves, gameLogicInterface.pieceAt(i, j), i, j)) break;
        return possibleMoves;
    }
}