package com.spsi.minesweeper.controller;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.spsi.minesweeper.service.GameService;
import com.spsi.minesweeper.service.ServerMessage;

import reactor.core.publisher.Flux;

@Controller
public class GameRsocketHandler {
    

    private final Logger logger = LogManager.getLogger(this);
    private final GameService gameService;
    
    public GameRsocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("game.minesweeper")
    public Flux<ServerMessage> initGame(Flux<ClientMessage> message) {
        AtomicReference<String> sessionGameID = new AtomicReference<>("init");
        AtomicReference<String> sessionUserID = new AtomicReference<>("init");

        message.doOnNext(clientMessage->{
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
        });
        

        Flux<ServerMessage> output = Flux.interval(Duration.ofMillis(150))
            .map(tick -> sessionGameID.get())
            .map(sMessage ->{
                if(sMessage.equals("init")) return new ServerMessage("","",1,1,new HashSet<>(),new int[1],"init");

                return gameService.getServerMessage(sMessage);
            });

        return output;
    }
}
