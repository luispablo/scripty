package com.duam.scripty.db;

import com.duam.scripty.Utils;

/**
 * Created by lgallo on 19/05/14.
 */
public class Server extends RemoteModel{

    private long userId;
    private String description;
    private String address;
    private int port;
    private String username;
    private String password;

    @Override
    public String toString() {
        return "Server{" +
                "_id=" + get_id() +
                ", id="+ getId() +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public Server() {

    }

    public Server(long userId, String description, String address, int port, String username, String password) {
        this.userId = userId;
        this.description = description;
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public boolean hasChanged(RemoteModel other) {
        Server otherServer = (Server) other;

        return this.userId != otherServer.getUserId()
                || !Utils.nullSafeEquals(this.description, otherServer.getDescription())
                || !Utils.nullSafeEquals(this.address, otherServer.getAddress())
                || this.port != otherServer.getPort()
                || !Utils.nullSafeEquals(this.username, otherServer.getUsername())
                || !Utils.nullSafeEquals(this.password, otherServer.getPassword());
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
