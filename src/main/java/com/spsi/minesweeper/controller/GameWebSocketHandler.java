package com.spsi.minesweeper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spsi.minesweeper.domain.Game;
import com.spsi.minesweeper.domain.GameStatus;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.spsi.minesweeper.service.GameService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class GameWebSocketHandler implements WebSocketHandler {

    private final Logger logger = LogManager.getLogger(this);
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        AtomicReference<String> sessionGameID = new AtomicReference<>("init");
        AtomicReference<String> sessionUserID = new AtomicReference<>("init");

        Mono<Void> input = session.receive()
            .concatMap(message -> Mono.just(message.getPayloadAsText()))
            .doOnNext(message -> {
                try {
                    ClientMessage clientMessage = objectMapper.readValue(message, ClientMessage.class);
                    String roomId = clientMessage.getId();
                    String userId = clientMessage.getHost();
                    Game game;
                    if(roomId.equals("")) {
                        roomId = null;
                    }

                    if(roomId!=null) {
                        game = gameService.findGameById(clientMessage.getId())
                                        .orElse(null);
                        if(game ==null) {
                            game = gameService.createGame(userId,roomId,20,30,100);
                        }

                        if(game.getGameStatus()==GameStatus.ENDED){
                            game = gameService.restartGame(game,clientMessage.getHost());
                        } else {
                            game = gameService.updateGame(game, userId, clientMessage.getActions());
                        }
                    } else {
                        game = gameService.createGame(clientMessage.getHost(), roomId,20,30,100);
                    }

                    sessionUserID.set(userId);
                    sessionGameID.set(game.getId());
                    //game = gameService.findGameById(clientMessage.getId()).orElse(null);
                    logger.log(Level.INFO, clientMessage.getHost()+"send from gameID"+clientMessage.getId()+"received : ");
                } catch (Exception e) {
                    logger.log(Level.ERROR,e.toString());
                    throw new RuntimeException(e);
                }
            })
            .doOnComplete(()->{ // close시 호출됨
                gameService.leave(sessionGameID.get(), sessionUserID.get());
                logger.log(Level.INFO, session.getHandshakeInfo().toString()+"closed!");
            })
        .then();

        Mono<Void> output = session.send(
            Flux.interval(Duration.ofMillis(150))
                .map(tick -> sessionGameID.get())
                .map(message ->{
                    if(message.equals("init")) return "init";
                    Game game = gameService.findGameById(message).orElse(null);
                    if(game == null) {
                        return "performance error";
                    }
                    int[] board = game.getBoard();
                    boolean[] revealed = game.getRevealed();
                    boolean[] flag = game.getFlag();
                    String GameMessage= "";
                    for(int i=0;i<board.length;++i) {
                        if(!revealed[i]) {
                            if(flag[i])
                                board[i]=10;
                            else
                                board[i]=9;
                        }
                    }

                    if(game.getGameStatus() == GameStatus.PLAYING) {
                        GameMessage = Integer.toString(game.getMines().size());
                    } else if(game.getGameStatus() == GameStatus.ENDED) {
                        for(int i : game.getMines()){
                            board[i] = -1;
                        }
                        GameMessage = "GAME OVER";
                    } else if(game.getGameStatus()== GameStatus.WIN) {
                        GameMessage = "WIN";
                    }

                    ServerMessage serverMessage = new ServerMessage(game.getId(),game.getHost(),game.getHeight(),game.getWidth(), game.getAttenders(),board,GameMessage);
                    return serverMessage.toString();
                })
        .concatMap(sm -> Mono.just(session.textMessage(sm))));





        return Mono.zip(input,output).then();
    }
}
