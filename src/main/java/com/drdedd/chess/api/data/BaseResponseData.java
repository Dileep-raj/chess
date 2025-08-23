package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Base response fields
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BaseResponseData {
    /**
     * Success flag
     */
    protected boolean success;
    /**
     * Status of response
     */
    protected HttpStatus status;
    /**
     * Response message
     */
    protected String message;
    /**
     * Error message if request failed
     */
    protected String error;
    /**
     * Time taken to process request in the server
     */
    protected String time;
    /**
     * Response data for the request
     */
    protected Object data;
}
