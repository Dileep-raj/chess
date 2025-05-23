package com.drdedd.chess.api.data;

import lombok.Data;

import java.util.HashMap;

@Data
public class AnalysisData {
    boolean success;
    int depth;
    String engine;
    String pgn;
    HashMap<String, Object> whiteAnalysis;
    HashMap<String, Object> blackAnalysis;
}
