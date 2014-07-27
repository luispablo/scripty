package com.duam.scripty.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.duam.scripty.db.Device;
import com.duam.scripty.ScriptyService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;

import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 11/05/14.
 */
public class SendValidationTask extends RoboAsyncTask<Device> {

    protected ProgressDialog dialog;

    protected String email;

    public SendValidationTask(Context context, String email) {
        super(context);
        this.dialog = new ProgressDialog(context);
        this.email = email;
    }

    @Override
    public Device call() throws Exception {
        String userId = findUserId(email);
        Device device = null;

        if (userId == null || userId.trim().length() == 0) userId = createUser(email);

        if (userId == null || userId.trim().length() == 0) {
            throw new RuntimeException("Something's wrong... Cannot find nor create the user.");
        } else {
            device = createDevice(userId);
            Ln.d("Created device: "+ device.describe());
        }

        return device;
    }

    private Device createDevice(String userId) throws IOException {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SCRIPTY_SERVER_URL).setConverter(new GsonConverter(gson)).build();

        ScriptyService service = restAdapter.create(ScriptyService.class);

        return service.createDevice(userId, userId);
    }

    private String createUser(String email) throws IOException, JSONException {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        LinkedTreeMap response = service.createUser(email);

        if (response != null) {
            return String.valueOf(((Double) response.get("id")).intValue());
        } else {
            return null;
        }
    }

    private String findUserId(String email) throws IOException, JSONException {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        LinkedTreeMap response = service.findUserByEmail(email);

        if (response != null) {
            return String.valueOf(((Double) response.get("id")).intValue());
        } else {
            return null;
        }
    }
}
