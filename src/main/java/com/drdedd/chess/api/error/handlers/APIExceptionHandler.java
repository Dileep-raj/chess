package com.drdedd.chess.api.error.handlers;

import com.drdedd.chess.api.data.BaseResponseData;
import com.drdedd.chess.api.error.exceptions.BadRequestException;
import com.drdedd.chess.api.error.exceptions.InternalServerErrorException;
import com.drdedd.chess.api.error.exceptions.ResourceNotFoundException;
import com.drdedd.chess.api.error.exceptions.UnprocessableContentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException e) {
        BaseResponseData data = new BaseResponseData();
        data.setError(e.getMessage());
        data.setStatus(BadRequestException.getStatus());
        return new ResponseEntity<>(data, data.getStatus());
    }

    @ExceptionHandler(UnprocessableContentException.class)
    public ResponseEntity<Object> handleUnprocessableContentException(UnprocessableContentException e) {
        BaseResponseData data = new BaseResponseData();
        data.setError(e.getMessage());
        data.setStatus(BadRequestException.getStatus());
        return new ResponseEntity<>(data, data.getStatus());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> handleInternalError(InternalServerErrorException e) {
        BaseResponseData data = new BaseResponseData();
        data.setError(e.getMessage());
        data.setStatus(BadRequestException.getStatus());
        return new ResponseEntity<>(data, data.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(ResourceNotFoundException e) {
        BaseResponseData data = new BaseResponseData();
        data.setError(e.getMessage());
        data.setStatus(BadRequestException.getStatus());
        return new ResponseEntity<>(data, data.getStatus());
    }
}
