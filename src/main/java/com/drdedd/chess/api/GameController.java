package com.drdedd.chess.api;

import com.drdedd.chess.api.data.GameData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private String newGame(GameData data) {
        String id = "";
        return id;
    }

    @GetMapping("/{gameId}/export")
    public ResponseEntity<Object> exportGame(@PathVariable String gameId, @RequestParam("clocks") boolean clocks) {
        GameData data = new GameData();
        // TODO get game from MongoDB
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}
