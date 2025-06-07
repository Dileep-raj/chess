package com.drdedd.chess.api.error.handlers;

import com.drdedd.chess.api.error.exceptions.BadRequestException;
import com.drdedd.chess.api.error.exceptions.InternalServerErrorException;
import com.drdedd.chess.api.error.exceptions.ResourceNotFoundException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class APIErrorController implements ErrorController {

    @GetMapping
    public void handleError(HttpServletRequest request) {
        Object attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = HttpStatus.valueOf(Integer.parseInt(attribute.toString()));
        switch (status) {
            case BAD_REQUEST -> throw new BadRequestException("Bad request");
            case NOT_FOUND -> throw new ResourceNotFoundException("Requested resource was not found");
            case INTERNAL_SERVER_ERROR -> throw new InternalServerErrorException("Unexpected internal server error");
        }
    }
}
