package com.duam.scripty;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 06/06/14.
 */
public class CheckValidationTask extends RoboAsyncTask<Boolean> {
    protected long deviceId;

    protected CheckValidationTask(Context context, long deviceId) {
        super(context);
        this.deviceId = deviceId;
    }

    @Override
    public Boolean call() throws Exception {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SCRIPTY_SERVER_URL).setConverter(new GsonConverter(gson)).build();

        ScriptyService service = restAdapter.create(ScriptyService.class);

        Ln.d("Calling server to ask for device " + deviceId);
        Device device = service.getDevice(deviceId);

        Ln.d("Got this from the server: "+ device.describe());
        return (device != null && device.isEmailChecked());
    }
}
