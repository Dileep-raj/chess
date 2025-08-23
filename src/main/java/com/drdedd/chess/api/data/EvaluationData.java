package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Chess position evaluation data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class EvaluationData extends BaseResponseData {
    /**
     * FEN of the position
     */
    String fen;
    /**
     * Evaluation of the position
     */
    String eval;
    /**
     * Best move in the given position
     */
    String bestmove;
    /**
     * Raw engine line
     */
    String engineLine;
    /**
     * Engine used for evaluation
     */
    String engine;
    /**
     * Variations of engine move lines
     */
    List<List<String>> variations;
}
