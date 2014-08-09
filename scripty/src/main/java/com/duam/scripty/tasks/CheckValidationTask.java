package com.duam.scripty.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.duam.scripty.R;
import com.duam.scripty.Utils;
import com.duam.scripty.db.Device;
import com.duam.scripty.ScriptyService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.inject.InjectResource;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 06/06/14.
 */
public class CheckValidationTask extends RoboAsyncTask<Boolean> {

    @InjectResource(R.string.checking_validation) String checkingValidation;
    @InjectResource(R.string.please_wait) String pleaseWait;

    protected ProgressDialog dialog;
    protected long deviceId;

    protected CheckValidationTask(Context context, long deviceId) {
        super(context);

        this.deviceId = deviceId;

        dialog = new ProgressDialog(context);
        dialog.setTitle(checkingValidation);
        dialog.setMessage(pleaseWait);
    }

    @Override
    public Boolean call() throws Exception {
        Ln.d("Calling server to ask for device " + deviceId);
        Device device = Utils.scriptyService().getDevice(deviceId);

        Ln.d("Got this from the server: "+ device.describe());
        return (device != null && device.isEmailChecked());
    }
}
