package com.spsi.minesweeper.controller;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.spsi.minesweeper.domain.Game;
import com.spsi.minesweeper.domain.GameStatus;
import com.spsi.minesweeper.service.GameService;

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

        message.doOnNext(cMessage->{
            String roomId = cMessage.getId();
            String userId = cMessage.getHost();
            Game game;
            if(roomId.equals("")) {
                roomId = null;
            }

            if(roomId!=null) {
                game = gameService.findGameById(cMessage.getId())
                                .orElse(null);
                if(game ==null) {
                    game = gameService.createGame(userId,roomId,30,50,60);
                }

                if(game.getGameStatus()!=GameStatus.ENDED){
                    game = gameService.updateGame(game,cMessage.getHost(),cMessage.getActions());
                } else {
                    game = gameService.createGame(cMessage.getHost(), roomId,30,50,60);
                }
            } else {
                game = gameService.createGame(cMessage.getHost(), roomId,30,50,60);
            }

            sessionUserID.set(userId);
            sessionGameID.set(game.getId());
            //game = gameService.findGameById(clientMessage.getId()).orElse(null);
            logger.log(Level.INFO, cMessage.getHost()+"send from gameID"+cMessage.getId()+"received : ");
        });
        

        Flux<ServerMessage> output = Flux.interval(Duration.ofMillis(150))
            .map(tick -> sessionGameID.get())
            .map(sMessage ->{
                if(sMessage.equals("init")) return new ServerMessage("","",1,1,new HashSet<>(),new int[1],"init");
                Game game = gameService.findGameById(sMessage).orElse(null);
                if(game == null) {
                    return new ServerMessage("","",1,1,new HashSet<>(),new int[1],"init");
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

                return new ServerMessage(game.getId(),game.getHost(),game.getHeight(),game.getWidth(), game.getAttenders(),board,GameMessage);
            });

        return output;
    }
}
