package com.spsi.minesweeper.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;

import com.spsi.minesweeper.domain.Action;
import com.spsi.minesweeper.domain.Action.ActionType;
import com.spsi.minesweeper.service.message.ClientMessage;
import com.spsi.minesweeper.service.message.ServerMessage;

import reactor.core.publisher.Flux;

@SpringBootTest
public class GameRsocketHandlerTest {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder)throws URISyntaxException{
        requester = builder.websocket(URI.create("ws://localhost:9898"));
    }

    @Test
    void testInitGame() {
        Action action = new Action(12,ActionType.DIG);
        List<Action> actions  = new ArrayList<>();
        actions.add(action);
        Flux<ClientMessage> input = Flux.just(new ClientMessage(null, "123", actions ));
        Flux<ServerMessage> result = requester.route("game.minesweeper").data(input).retrieveFlux(ServerMessage.class);
        result.subscribe(System.out::println);
    }
}
