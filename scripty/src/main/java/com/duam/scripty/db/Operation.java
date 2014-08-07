package com.duam.scripty.db;

import java.util.Date;

/**
 * Created by luispablo on 28/07/14.
 */
public class Operation {

    public static enum Code { INSERT, UPDATE, DELETE }

    private long _id = -1;
    private long localId = -1;
    private long remoteId = -1;
    private String modelClass;
    private Code code;
    private Date createdAt;

    public boolean isClass(Class clazz) {
        return modelClass.equals(clazz.getName());
    }

    public boolean isInsert() {
        return Code.INSERT.equals(code);
    }
    public boolean isUpdate() {
        return Code.UPDATE.equals(code);
    }
    public boolean isDelete() {
        return Code.DELETE.equals(code);
    }

    @Override
    public String toString() {
        return code.name()+": "+modelClass+" (id "+localId+")";
    }

    public Operation() {

    }

    public static Operation register(RemoteModel model, Code operation) {
        Operation op = new Operation();

        if (operation != Code.INSERT) {
            op.setRemoteId(model.getId());
        }
        op.setLocalId(model.get_id());
        op.setCode(operation);
        op.setModelClass(model.getClass().getName());
        op.setCreatedAt(new Date());

        return op;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(long remoteId) {
        this.remoteId = remoteId;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getModelClass() {
        return modelClass;
    }

    public void setModelClass(String modelClass) {
        this.modelClass = modelClass;
    }
}
