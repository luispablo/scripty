package com.duam.scripty;

import android.app.ActivityManager;
import android.content.Context;
import android.widget.EditText;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 19/06/14.
 */
public class Utils {

    /**
     * Compares two strings. The are equal if both are null, or if both aren't null and fullfill
     * equals.
     *
     * @param s1
     * @param s2
     * @return
     */
    public static boolean nullSafeEquals(String s1, String s2) {
        return (s1 == null && s2 == null) || (s1 != null && s2 != null && s1.equals(s2));
    }

    public static boolean isEmpty(EditText edit) {
        return edit.getText().length() == 0;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static ScriptyService scriptyService() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SCRIPTY_SERVER_URL).setConverter(new GsonConverter(gson)).build();

        return restAdapter.create(ScriptyService.class);
    }
}
