package com.drdedd.chess.game.interfaces;

public interface GameUI {
    void updateViews();

    void terminateGame(String termination);

    boolean saveProgress();
}