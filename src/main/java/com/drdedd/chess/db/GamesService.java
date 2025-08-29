package com.drdedd.chess.db;

import com.drdedd.chess.data.Game;
import com.drdedd.chess.db.repository.GamesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamesService {

    @Autowired
    private GamesRepository gamesRepository;

    public List<Game> getAllGames() {
        return gamesRepository.findAll();
    }

    public Game getGameById(String id) {
        return gamesRepository.findById(id).orElse(null);
    }

    public Game saveGame(Game game) {
        return gamesRepository.save(game);
    }

    public void deleteGame(String id) {
        gamesRepository.deleteById(id);
    }
}
