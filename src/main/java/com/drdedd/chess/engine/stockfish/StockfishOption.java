package com.drdedd.chess.engine.stockfish;

import lombok.Getter;
import lombok.Setter;

/**
 * Options of Stockfish chess engine
 */
@Getter
public class StockfishOption {
    public static final String optionThreads = "Threads";
    public static final String optionHash = "Hash";
    public static final String optionClearHash = "Clear Hash";
    public static final String optionPonder = "Ponder";
    public static final String optionMultiPV = "MultiPV";
    public static final String optionSkillLevel = "Skill Level";
    public static final String optionMoveOverhead = "Move Overhead";
    public static final String optionNodesTime = "nodestime";
    public static final String optionUCI_Chess960 = "UCI_Chess960";
    public static final String optionUCI_LimitStrength = "UCI_LimitStrength";
    public static final String optionUCI_Elo = "UCI_Elo";
    public static final String optionUCI_ShowWDL = "UCI_ShowWDL";
    public static final String optionSyzygyPath = "SyzygyPath";
    public static final String optionSyzygyProbeDepth = "SyzygyProbeDepth";
    public static final String optionSyzygy50MoveRule = "Syzygy50MoveRule";
    public static final String optionSyzygyProbeLimit = "SyzygyProbeLimit";
    public static final String optionEvalFile = "EvalFile";
    public static final String optionEvalFileSmall = "EvalFileSmall";

    public static final String typeString = "string";
    public static final String typeCheck = "check";
    public static final String typeSpin = "spin";
    public static final String typeButton = "button";

    public static final String attributeName = "name";
    public static final String attributeType = "type";
    public static final String attributeDefault = "default";
    public static final String attributeMin = "min";
    public static final String attributeMax = "max";
    public static final String attributeValue = "value";

    private final String name, type, defaultValue, min, max;
    @Setter
    private String value;

    StockfishOption(String name, String type, String defaultValue, String min, String max) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
    }

    public boolean isValidValue(String value) {
        switch (type) {
            case StockfishOption.typeSpin -> {
                int val = Integer.parseInt(value), min = Integer.parseInt(this.min), max = Integer.parseInt(this.max);
                return val >= min && val <= max;
            }
            case StockfishOption.typeCheck -> {
                return value.equals("true") || value.equals("false");
            }
            case StockfishOption.typeString, StockfishOption.typeButton -> {
                return true;
            }
        }
        return false;
    }
}
