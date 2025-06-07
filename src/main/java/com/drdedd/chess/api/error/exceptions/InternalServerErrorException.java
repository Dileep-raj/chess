package com.drdedd.chess.api.error.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends RuntimeException {

    @Getter
    private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public InternalServerErrorException(String message) {
        super(message);
    }
}
