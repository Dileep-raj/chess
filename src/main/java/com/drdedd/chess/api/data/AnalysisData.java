package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

/**
 * Chess game analysis data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class AnalysisData extends BaseResponseData {
    /**
     * Depth of analysis
     */
    int depth;
    /**
     * Engine used for analysis
     */
    String engine;
    /**
     * PGN string content
     */
    String pgn;
    /**
     * Analysis report of white player
     */
    AnalysisSummary whiteAnalysis;
    /**
     * Analysis report of black player
     */
    AnalysisSummary blackAnalysis;
    /**
     * List of FENs of board position
     */
    ArrayList<String> fens;
    /**
     * Evaluations of each position
     */
    ArrayList<String> evaluations;
    /**
     * Annotations of each move
     */
    ArrayList<String> annotations;
}
