package com.drdedd.chess.db.repository;

import com.drdedd.chess.data.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamesRepository extends MongoRepository<Game, String> {

    @Query("{ gameId: '?0' }")
    Game getGameById(String id);

    @Query("{ $or: [ { players: { white: { name: '?0' } } }, { players: { black: { name: '?0' } } } ] }")
    List<Game> getGamesByPlayer(String name);
}
