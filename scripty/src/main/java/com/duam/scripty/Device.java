package com.duam.scripty;

/**
 * Created by luispablo on 18/05/14.
 */
public class Device {
    private long id;
    private String key;
    private long userId;

    public Device() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
