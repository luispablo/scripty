package com.duam.scripty;

import com.duam.scripty.db.Command;
import com.duam.scripty.db.Device;
import com.duam.scripty.db.Server;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
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

    @FormUrlEncoded
    @POST("/servers.json")
    Server createServer(@Field("server[user_id]") long userId,
                        @Field("server[description]") String description,
                        @Field("server[address]") String address,
                        @Field("server[port]") int port,
                        @Field("server[username]") String username,
                        @Field("server[password]") String password);

    @FormUrlEncoded
    @POST("/commands.json")
    Command createCommand(@Field("command[server_id]") long serverId,
                          @Field("command[description]") String description,
                          @Field("command[command]") String command);

    @FormUrlEncoded
    @PUT("/servers/{id}.json")
    Server updateServer(@Path("id") long serverId,
                        @Field("server[user_id]") long userId,
                        @Field("server[description]") String description,
                        @Field("server[address]") String address,
                        @Field("server[port]") int port,
                        @Field("server[username]") String username,
                        @Field("server[password]") String password);

    @FormUrlEncoded
    @PUT("/commands/{id}.json")
    Command updateCommand(@Path("id") long commandId,
                          @Field("command[server_id]") long serverId,
                          @Field("command[description]") String description,
                          @Field("command[command]") String command);

    @GET("/devices/{id}.json")
    Device getDevice(@Path("id") long deviceId);

    @GET("/users/{id}/servers.json")
    List<Server> getServers(@Path("id") long userId);

    @GET("/servers/{id}/commands.json")
    List<Command> getCommands(@Path("id") long serverId);

    @DELETE("/servers/{id}.json")
    String deleteServer(@Path("id") long serverId);

    @DELETE("/commands/{id}.json")
    String deleteCommand(@Path("id") long commandId);
}
