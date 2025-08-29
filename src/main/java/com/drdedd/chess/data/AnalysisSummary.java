package com.drdedd.chess.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Analysis summary of a player
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AnalysisSummary {
    /**
     * Name of the player
     */
    String name;
    /**
     * Number of great moves
     */
    int great;
    /**
     * Number of inaccuracy moves
     */
    int inaccuracy;
    /**
     * Number of mistakes
     */
    int mistake;
    /**
     * Number of blunders
     */
    int blunder;
    /**
     * Average centipawn loss
     */
    int acpl;
    /**
     * Accuracy of the player in the game
     */
    int accuracy;
}
