package com.spsi.minesweeper.service;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


import java.util.Set;

public class ServerMessage {
    String id;
    String host;
    int y;
    int x;
    Set<String> attenders;
    String message;
    int[] board;

    public ServerMessage(String id, String host, int y, int x, Set<String> attenders, int[] board, String message) {
        this.id = id;
        this.host = host;
        this.y = y;
        this.x = x;
        this.attenders = attenders;
        this.board = board;
        this.message = message;
    }
    
    public ServerMessage() {

    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Set<String> getAttenders() {
        return attenders;
    }

    public void setAttenders(Set<String> attenders) {
        this.attenders = attenders;
    }

    public int[] getBoard() {
        return board;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
