package com.drdedd.chess.api.exceptions;

import org.springframework.http.HttpStatus;

public class APIException {
    private final String message;
    private final HttpStatus status;

    public APIException(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }


    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
