package com.duam.scripty.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.duam.scripty.R;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;
import com.google.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;
import static com.duam.scripty.Utils.isEmpty;
import static com.duam.scripty.db.ScriptyHelper.SERVER_ID;


public class ServerActivity extends RoboActivity {
    public static final int EDIT_SERVER_CODE = 50;
    public static final int DELETE_SERVER_CODE = 60;

    public static final int SERVER_SAVED = 1;
    public static final int ACTION_CANCELED = 2;

    @InjectView(R.id.editDescription) EditText editDescription;
    @InjectView(R.id.editAddress) EditText editAddress;
    @InjectView(R.id.editPort) EditText editPort;
    @InjectView(R.id.editUsername) EditText editUsername;
    @InjectView(R.id.editPassword) EditText editPassword;

    @InjectView(R.id.btnServerOK) Button btnServerOK;
    @InjectView(R.id.btnServerCancel) Button btnServerCancel;

    @InjectResource(R.string.server_saved) String serverSaved;
    @InjectResource(R.string.field_required) String fieldRequired;
    @InjectResource(R.string.fix_errors) String fixErrors;

    @Inject SharedPreferences prefs;

    private long serverId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        serverId = getIntent().getLongExtra(SERVER_ID, -1);
        initializeValues();

        btnServerOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ok();
            }
        });
        btnServerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    private void initializeValues() {
        if (serverId > 0) {
            ScriptyHelper helper = new ScriptyHelper(this);
            Server server = helper.retrieveServer(serverId);

            editAddress.setText(server.getAddress());
            editDescription.setText(server.getDescription());
            editPort.setText(String.valueOf(server.getPort()));
            editUsername.setText(server.getUsername());
        }
    }

    public void ok() {
        if (validate()) {
            Ln.d("About to save server");
            ScriptyHelper helper = new ScriptyHelper(this);
            long userId = prefs.getLong(PREF_USER_ID, -1);

            Server server = new Server();
            server.set_id(serverId);
            server.setPort(Integer.valueOf(editPort.getText().toString()));
            server.setDescription(editDescription.getText().toString());
            server.setAddress(editAddress.getText().toString());
            server.setUserId(userId);

            if (!isEmpty(editUsername)) server.setUsername(editUsername.getText().toString());
            if (!isEmpty(editPassword)) server.setPassword(editPassword.getText().toString());

            if (serverId > 0) {
                helper.updateServer(server);
            } else {
                helper.insertServer(server);
            }
            Ln.d("Server saved");

            Toast.makeText(this, serverSaved, Toast.LENGTH_LONG).show();

            setResult(SERVER_SAVED);
            finish();
        } else {
            Toast.makeText(this, fixErrors, Toast.LENGTH_LONG).show();
        }
    }



    public boolean validate() {
        boolean isValid = true;

        if (isEmpty(editDescription)) {
            editDescription.setError(fieldRequired);
            isValid = false;
        }
        if (isEmpty(editAddress)) {
            editAddress.setError(fieldRequired);
            isValid = false;
        }
        if (isEmpty(editPort)) {
            editPort.setError(fieldRequired);
            isValid = false;
        }

        return isValid;
    }

    public void cancel() {
        setResult(ACTION_CANCELED);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
