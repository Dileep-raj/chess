package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Legal moves of a given position
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class LegalMovesData extends BaseResponseData {
    /**
     * FEN of the position
     */
    String fen;
    /**
     * Set of UCI moves
     */
    HashSet<String> uci;
    /**
     * Legal moves from each square
     */
    HashMap<String, HashSet<String>> legalMoves;
}
