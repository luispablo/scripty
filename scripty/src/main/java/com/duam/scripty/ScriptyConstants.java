package com.duam.scripty;

/**
 * Created by luispablo on 11/05/14.
 */
public class ScriptyConstants {
    public static final String SCRIPTY_SERVER_URL = "http://scripty.duamsistemas.com.ar";
    public static final String FIND_USER_URI = "/users/find.json";
    public static final String USER_URI = "/users.json";
    public static final String CREATE_DEVICE_URI = "/users/:id/devices.json";

    public static final String PREF_DEVICE_ID = "deviceId";
    public static final String PREF_DEVICE_KEY = "deviceKey";
    public static final String PREF_DEVICE_CHECKED = "deviceChecked";
    public static final String PREF_USER_ID = "userId";
    public static final String PREF_USER_EMAIL = "userEmail";
    public static final String PREF_LAST_SYNC_DB_MILLIS = "lastSyncDBMillis";
    public static final String PREF_TIMEOUT_SECOND = "timeoutSeconds";

    public static final int DEFAULT_TIMEOUT_SECONDS = 60;
}
