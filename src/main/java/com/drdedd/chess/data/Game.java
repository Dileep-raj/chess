package com.drdedd.chess.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Document(collection = "games")
public class Game {
    @Id
    String gameId;
    @Nullable
    String startingPosition;
    List<String> uciMoves;
    PlayersData players;
    Map<String, String> tags;
    ClocksData clocks;
    AnalysisData analysis;
    @JsonFormat(pattern = "yyyy.MM.dd")
    Date date;
    boolean ongoing;
}
