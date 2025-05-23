package com.drdedd.chess.game.gameData;

/**
 * State of the game ({@link ChessState#CHECKMATE Checkmate}, {@link ChessState#DRAW Draw}, {@link ChessState#ONGOING Ongoing}, {@link ChessState#RESIGN Resigned},
 * {@link ChessState#STALEMATE Stalemate}, {@link ChessState#TIMEOUT Timeout})
 */
public enum ChessState {
    /**
     * com.drdedd.Chess.Game over by checkmate
     */
    CHECKMATE,
    /**
     * com.drdedd.Chess.Game over by draw
     */
    DRAW,
    /**
     * com.drdedd.Chess.Game is ongoing
     */
    ONGOING,
    /**
     * com.drdedd.Chess.Game over by Resignation
     */
    RESIGN,
    /**
     * com.drdedd.Chess.Game over by stalemate
     */
    STALEMATE,
    /**
     * com.drdedd.Chess.Game over by timeout
     */
    TIMEOUT
}