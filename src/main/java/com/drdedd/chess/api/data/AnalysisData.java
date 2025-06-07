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
public class AnalysisData extends ResponseData {
    int depth;
    String engine, pgn;
    AnalysisReport whiteAnalysis, blackAnalysis;
    ArrayList<String> fens, evaluations, annotations;
}
