package com.drdedd.chess.game.gameData;

import lombok.Getter;

/**
 * Player type (White/Black)
 */
@Getter
public enum Player {
    WHITE("White", false), BLACK("Black", false);
    /**
     * -- GETTER --
     *
     */
    boolean inCheck;
    /**
     * -- GETTER --
     *
     */
    String name;

    Player(String name, boolean inCheck) {
        this.name = name;
        this.inCheck = inCheck;
    }

    /**
     * Set Player's name
     * @param name Name of the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set check flag <code>(true|false)</code>
     */
    public void setInCheck(boolean inCheck) {
        this.inCheck = inCheck;
    }
}

