package com.spsi.minesweeper.domain;

import com.spsi.minesweeper.service.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;



@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GameTest {

    @Mock
    GameRepository gameRepository;

    @InjectMocks
    GameService gameService;


    @Test
    public void GameCreateTest() {
        /*
        String gameId = gameService.init("dlsrb");
        //Game game = new Game("21343",gameId,null,null,);
        Mockito.when(gameRepository.save(any())).thenReturn(game);

        Game gamet = gameService.getGame(gameId);

        //assertEquals(1L,game.getId());
        assertEquals("dlsrb",game.getHost());
        assertEquals(null,game.getP2Id());
        //assertEquals(null,game.getRevealed());
        //assertEquals(null,game.getBoard());
        assertEquals(GameStatus.INIT,game.getGameStatus());
        */
    }


}