package com.duam.scripty.db;

/**
 * Created by lgallo on 19/05/14.
 */
public class Server {
    private long _id;
    private long userId;
    private String description;
    private String address;
    private int port;
    private String username;
    private String password;

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

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
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
