package com.drdedd.chess.api;

import com.drdedd.chess.api.data.JSONConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ErrorResponse {

    protected static ResponseEntity<Object> internalErrorResponse(String error) {
        return new ResponseEntity<>(Map.of(JSONConstants.error, error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected static ResponseEntity<Object> badRequest(String message) {
        return new ResponseEntity<>(Map.of(JSONConstants.error, message), HttpStatus.BAD_REQUEST);
    }
}
