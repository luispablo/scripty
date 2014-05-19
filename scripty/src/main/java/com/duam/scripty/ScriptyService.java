package com.duam.scripty;

import com.google.gson.internal.LinkedTreeMap;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
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
}
