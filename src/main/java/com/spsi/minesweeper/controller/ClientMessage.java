package com.spsi.minesweeper.controller;

import com.spsi.minesweeper.service.Action;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class ClientMessage {
    private String id;
    private String host;
    private List<Action> actions;

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

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public ClientMessage(String id, String host, List<Action> actions) {
        this.id = id;
        this.host = host;
        this.actions = actions;
    }
    public ClientMessage(){};
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
