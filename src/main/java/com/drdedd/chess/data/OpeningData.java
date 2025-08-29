package com.drdedd.chess.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Openings response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OpeningData{
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
