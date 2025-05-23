package com.drdedd.chess.api.data;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationData {
    String status;
    String FEN;
    String eval;
    String bestmove;
    List<String> moves;
    String engineLine;
}
