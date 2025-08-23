package com.drdedd.chess.game.data;

import lombok.Getter;

@Getter
public enum Color {
    WHITE, BLACK;
    private boolean inCheck;

    Color() {
        inCheck = false;
    }

    public void setInCheck(boolean inCheck) {
        this.inCheck = inCheck;
    }
}
