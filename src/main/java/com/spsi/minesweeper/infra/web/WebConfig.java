package com.spsi.minesweeper.infra.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spsi.minesweeper.controller.GameWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;


import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer{
    @Autowired
    private GameWebSocketHandler gameWebSocketHandler;


    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
          .allowedOrigins("/**")
          .allowedMethods("GET")
          .maxAge(3600);
    }
    
    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/minesweeper", gameWebSocketHandler);
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }


    
    @Bean
    public ObjectMapper ObjectMapper() {
        return new ObjectMapper();
    }
}
