package com.drdedd.chess.api.error.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Unprocessable content <br>
 * Used for invalid schema or requests with bad data body
 */
public class UnprocessableContentException extends RuntimeException {
    @Getter
    private static final HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

    public UnprocessableContentException(String message) {
        super(message);
    }
}
