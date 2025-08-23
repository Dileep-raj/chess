package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Openings response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class OpeningData extends BaseResponseData {
    /**
     * ECO code of the opening
     */
    String eco;
    /**
     * Name of the opening
     */
    String name;
    /**
     * Last opening move
     */
    int lastMove;
    /**
     * Complete list of opening moves in SAN notation
     */
    List<String> moves;
    /**
     * Complete list of opening moves in UCI notation
     */
    List<String> uci;
}
