package com.drdedd.chess.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Legal moves of a given position
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class LegalMovesData {
    /**
     * FEN of the position
     */
    String fen;
    /**
     * Set of UCI moves
     */
    HashSet<String> moves;
    /**
     * Legal moves from each square
     */
    HashMap<String, HashSet<String>> legalMoves;
}
