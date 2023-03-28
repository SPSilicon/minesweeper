package com.spsi.minesweeper.domain;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository {

    Game save(Game entity);
    Optional<Game> findById(String id);
    boolean existsById(String id);
    void delete(Game entity);
}
