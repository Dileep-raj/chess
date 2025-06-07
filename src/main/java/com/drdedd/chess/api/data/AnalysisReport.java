package com.drdedd.chess.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AnalysisReport {
    String name;
    int great, inaccuracy, mistake, blunder, acpl, accuracy;
}
