package com.drdedd.chess.api;

import com.drdedd.chess.data.Game;
import com.drdedd.chess.db.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/game", produces = MediaType.APPLICATION_JSON_VALUE)
public class GameController {

    @Autowired
    private GamesService gamesService;

    @PostMapping
    public ResponseEntity<Game> saveGame(@RequestBody Game game) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gamesService.saveGame(game));
    }

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gamesService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable String id) {
        return ResponseEntity.ok(gamesService.getGameById(id));
    }

    @PostMapping("/new")
    public ResponseEntity<String> newGame() {
        Game game = new Game();
        gamesService.saveGame(game);
        return ResponseEntity.ok(game.getGameId());
    }
}
