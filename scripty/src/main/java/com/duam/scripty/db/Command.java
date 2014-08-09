package com.duam.scripty.db;

import com.duam.scripty.Utils;

import java.io.Serializable;

/**
 * Created by lgallo on 19/05/14.
 */
public class Command extends RemoteModel implements Serializable{

    private long serverId;
    private String description;
    private String command;

    @Override
    public String toString() {
        return "Command{" +
                "_id="+ get_id() +
                ", remote_id="+ getId() +
                ", serverId=" + serverId +
                ", description='" + description + '\'' +
                ", command='" + command + '\'' +
                '}';
    }

    public boolean hasChanged(RemoteModel other) {
        Command otherCmd = (Command) other;

        return this.serverId != otherCmd.getServerId()
                || !Utils.nullSafeEquals(this.description, otherCmd.getDescription())
                || !Utils.nullSafeEquals(this.command, otherCmd.getCommand());
    }

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
