package com.duam.scripty;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by luispablo on 17/05/14.
 */
public interface ScriptyService {
    @FormUrlEncoded
    @POST("/users/find.json")
    LinkedTreeMap findUserByEmail(@Field("email") String email);

    @FormUrlEncoded
    @POST("/users.json")
    LinkedTreeMap createUser(@Field("user[email]") String email);

    @FormUrlEncoded
    @POST("/users/{id}/devices.json")
    Device createDevice(@Path("id") String id, @Field("device[user_id]") String userId);

    @GET("/users/{id}/servers.json")
    List<Server> getServers(@Path("id") long userId);

    @GET("/servers/{id}/commands.json")
    List<Command> getCommands(@Path("id") long serverId);
}
