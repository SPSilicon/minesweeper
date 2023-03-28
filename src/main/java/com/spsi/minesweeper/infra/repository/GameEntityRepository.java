package com.spsi.minesweeper.infra.repository;

import com.redis.om.spring.repository.RedisDocumentRepository;
import com.spsi.minesweeper.domain.Game;


import com.spsi.minesweeper.domain.GameRepository;
//import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GameEntityRepository extends RedisDocumentRepository<Game, String>, GameRepository  {
    @Override
    Game save(Game entity);
    @Override
    Optional<Game> findById(String id);
    @Override
    boolean existsById(String id);
    @Override
    void delete(Game entity);
}
