package com.duam.scripty;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 06/06/14.
 */
public class CheckValidationTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = CheckValidationTask.class.getName();

    protected long deviceId;

    protected CheckValidationTask(long deviceId) {
        super();
        this.deviceId = deviceId;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SCRIPTY_SERVER_URL).setConverter(new GsonConverter(gson)).build();

        ScriptyService service = restAdapter.create(ScriptyService.class);

        Log.d(TAG, "Calling server to ask for device " + deviceId);
        Device device = service.getDevice(deviceId);

        Log.d(TAG, "Got this from the server: " + device.describe());
        return (device != null && device.isEmailChecked());
    }
}
