package com.duam.scripty;

import android.content.Context;

import retrofit.RestAdapter;
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
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);

        Device device = service.getDevice(deviceId);

        return (device != null && device.isEmailChecked());
    }
}
