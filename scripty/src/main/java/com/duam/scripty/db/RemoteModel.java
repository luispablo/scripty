package com.duam.scripty.db;

import java.io.Serializable;
import java.util.List;

/**
 * Created by luispablo on 28/07/14.
 */
public abstract class RemoteModel implements Serializable{

    private long _id;
    private long id;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public abstract boolean hasChanged(RemoteModel other);

    public static <T extends RemoteModel> T getByRemoteId(List<T> models, long id) {
        T result = null;

        for (T model : models) {
            if (model.getId() == id) {
                result = model;
            }
        }

        return result;
    }
}
