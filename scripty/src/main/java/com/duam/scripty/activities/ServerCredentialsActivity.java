package com.duam.scripty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.duam.scripty.R;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class ServerCredentialsActivity extends RoboActivity {

    public static final String SERVER_ID = "serverId";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final int OK = 0;
    public static final int CANCEL = 1;

    @InjectView(R.id.editUsername) EditText editUsername;
    @InjectView(R.id.editPassword) EditText editPassword;
    @InjectView(R.id.buttonCancel) Button buttonCancel;
    @InjectView(R.id.buttonOK) Button buttonOK;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_credentials);

        ScriptyHelper helper = ScriptyHelper.getInstance(this);
        server = helper.retrieveServer(getIntent().getLongExtra(SERVER_ID, -1));

        editUsername.setText(server.getUsername());
        editPassword.setText(server.getPassword());

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(CANCEL);
                finish();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(USERNAME, editUsername.getText().toString());
                intent.putExtra(PASSWORD, editPassword.getText().toString());

                setResult(OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server_credentials, menu);
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
