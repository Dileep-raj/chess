package com.drdedd.chess.game.pgn;

public class InvalidPGNException extends RuntimeException {

    public InvalidPGNException(String pgn, String message) {
        super("%s%nPGN:'''%n%s%n'''}".formatted(message, pgn));
    }
}
