package com.drdedd.chess.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

/**
 * Chess game analysis data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AnalysisData {
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
     * Evaluations of each position
     */
    ArrayList<String> evaluations;
    /**
     * Annotations of each move
     */
    ArrayList<String> annotations;
}
