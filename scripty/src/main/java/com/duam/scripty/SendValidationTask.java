package com.duam.scripty;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;
import static com.duam.scripty.ScriptyConstants.FIND_USER_URI;
import static com.duam.scripty.ScriptyConstants.USER_URI;
import static com.duam.scripty.ScriptyConstants.CREATE_DEVICE_URI;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

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
        String url = (SCRIPTY_SERVER_URL + CREATE_DEVICE_URI).replace(":id", userId);
        HttpPost post = new HttpPost(url);
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("device[user_id]", userId));
        post.setEntity(new UrlEncodedFormEntity(pairs));
        HttpResponse response = new DefaultHttpClient().execute(post);
    }

    private String createUser(String email) throws IOException, JSONException {
        HttpPost post = new HttpPost(SCRIPTY_SERVER_URL + USER_URI);
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("user[email]", email));
        post.setEntity(new UrlEncodedFormEntity(pairs));

        HttpResponse response = new DefaultHttpClient().execute(post);
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

        return json.getString("id");
    }

    private String findUserId(String email) throws IOException, JSONException {
        HttpPost post = new HttpPost(SCRIPTY_SERVER_URL + FIND_USER_URI);
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("email", email));
        post.setEntity(new UrlEncodedFormEntity(pairs));

        HttpResponse response = new DefaultHttpClient().execute(post);
        String responseText = EntityUtils.toString(response.getEntity());

        if (responseText != null && !responseText.equals("null")) {
            JSONObject json = new JSONObject(responseText);
            return json.getString("id");
        } else {
            return null;
        }
    }
}
