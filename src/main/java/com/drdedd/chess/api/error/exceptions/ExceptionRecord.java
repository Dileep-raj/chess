package com.drdedd.chess.api.error.exceptions;

import org.springframework.http.HttpStatus;

public record ExceptionRecord(String error, HttpStatus status) {
}
