package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Base response fields
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public abstract class ResponseData {
    protected boolean success;
    protected String status, message, error;
}
