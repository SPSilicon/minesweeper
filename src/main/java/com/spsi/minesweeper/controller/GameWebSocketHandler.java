package com.spsi.minesweeper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.spsi.minesweeper.service.GameService;
import com.spsi.minesweeper.service.ServerMessage;

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
                    if(roomId.equals("")) {
                        roomId = null;
                    }

                    if(roomId!=null) {
                        if(gameService.existsGameById(roomId)) {
                            if(gameService.isEnded(roomId)){
                                roomId = gameService.restartGame(roomId,clientMessage.getHost());
                            } else {
                                roomId = gameService.updateGame(roomId, userId, clientMessage.getActions());
                            }
                        } else {
                            roomId = gameService.createGame(userId,20,30,100);
                        }
 
                    } else {
                        roomId = gameService.createGame(clientMessage.getHost(),20,30,100);
                    }

                    sessionUserID.set(userId);
                    sessionGameID.set(roomId);
                    //game = gameService.findGameById(clientMessage.getId()).orElse(null);
                    logger.log(Level.INFO, clientMessage.getHost()+"send from gameID"+clientMessage.getId()+"received : ");
                } catch (Exception e) {
                    logger.log(Level.ERROR,e.toString());
                    throw new RuntimeException(e);
                }
            })
            .doOnError(e->{
                //에러 처리
            })
            .doOnComplete(()->{ // close시 호출됨
                gameService.leave(sessionGameID.get(), sessionUserID.get());
                logger.log(Level.INFO, session.getHandshakeInfo().toString()+"closed!");
            })
        .then();

        Mono<Void> output = session.send(
            Flux.interval(Duration.ofMillis(150))
                .map(tick -> sessionGameID.get())
                .map(roomId ->{
                    if(roomId.equals("init")) return "init";

                    return gameService.getServerMessage(roomId).toString();
                })
                .doOnError(e->{
                    //에러 처리
                })
        .concatMap(sm -> Mono.just(session.textMessage(sm))));





        return Mono.zip(input,output).then();
    }

}
