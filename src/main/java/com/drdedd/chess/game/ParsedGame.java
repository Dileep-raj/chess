package com.drdedd.chess.game;

import com.drdedd.chess.game.pgn.PGN;

import java.io.Serializable;
import java.util.Stack;

public record ParsedGame(Stack<BoardModel> boardModelStack, Stack<String> FENs, PGN pgn, String eco, String opening,
                         int lastBookMove) implements Serializable {

}
