package com.duam.scripty;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;
import static com.duam.scripty.ScriptyConstants.FIND_USER_URI;
import static com.duam.scripty.ScriptyConstants.USER_URI;
import static com.duam.scripty.ScriptyConstants.CREATE_DEVICE_URI;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.internal.LinkedTreeMap;
import com.google.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 11/05/14.
 */
public class SendValidationTask extends RoboAsyncTask<Void> {

    protected ProgressDialog dialog;

    protected String email;

    public SendValidationTask(Context context, String email) {
        super(context);
        this.dialog = new ProgressDialog(context);
        this.email = email;
    }

    @Override
    public Void call() throws Exception {
        String userId = findUserId(email);
        Ln.d("Found user_id: ["+ userId +"]");

        if (userId == null || userId.trim().length() == 0) userId = createUser(email);

        if (userId == null || userId.trim().length() == 0) {
            throw new RuntimeException("Something's wrong... Cannot find nor create the user.");
        } else {
            createDevice(userId);
        }

        return null;
    }

    private void createDevice(String userId) throws IOException {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);
        service.createDevice(userId);
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
