package com.drdedd.chess.game.gameData;

import lombok.Getter;

/**
 * Piece rank (King, Queen, Rook, Bishop, Knight, Pawn)
 */
@Getter
public enum Rank {
    KING(Integer.MAX_VALUE, 'K'), QUEEN(9, 'Q'), ROOK(5, 'R'), BISHOP(3, 'B'), KNIGHT(3, 'N'), PAWN(1, 'P');
    private final int value;
    private final char letter;

    Rank(int value, char letter) {
        this.value = value;
        this.letter = letter;
    }

}