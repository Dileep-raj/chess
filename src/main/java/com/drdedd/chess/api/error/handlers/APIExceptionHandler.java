package com.drdedd.chess.api.error.handlers;

import com.drdedd.chess.api.error.exceptions.BadRequestException;
import com.drdedd.chess.api.error.exceptions.ExceptionRecord;
import com.drdedd.chess.api.error.exceptions.InternalServerErrorException;
import com.drdedd.chess.api.error.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleInvalidAPIRequestException(BadRequestException e) {
        ExceptionRecord record = new ExceptionRecord(e.getMessage(), BadRequestException.getStatus());
        return new ResponseEntity<>(record, BadRequestException.getStatus());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> handleInternalError(InternalServerErrorException e) {
        ExceptionRecord record = new ExceptionRecord(e.getMessage(), InternalServerErrorException.getStatus());
        return new ResponseEntity<>(record, InternalServerErrorException.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(ResourceNotFoundException e) {
        ExceptionRecord record = new ExceptionRecord(e.getMessage(), ResourceNotFoundException.getStatus());
        return new ResponseEntity<>(record, ResourceNotFoundException.getStatus());
    }
}
