package com.spsi.minesweeper.service;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Action {
    private int loc;
    private ActionType actionType;
    public enum ActionType{
        DIG,
        FLAG
    };

    public Action(){};

    public Action(int loc, ActionType actionType) {
        this.loc = loc;
        this.actionType = actionType;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
