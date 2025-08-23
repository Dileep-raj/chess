package com.drdedd.chess.api.data;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameData {
    List<String> uci, san;
    PlayersData players;
    Map<String, String> tags;
    ClocksData clocks;
    AnalysisData analysis;
}
