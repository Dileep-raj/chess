package com.drdedd.chess.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler(value = {APIRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(APIRequestException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        APIException apiException = new APIException(e.getMessage(), badRequest);
        return new ResponseEntity<>(apiException, badRequest);
    }
}
