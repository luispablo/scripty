package com.duam.scripty.db;

import java.io.Serializable;

/**
 * Created by lgallo on 19/05/14.
 */
public class Command extends RemoteModel implements Serializable{

    private long serverId;
    private String description;
    private String command;

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
