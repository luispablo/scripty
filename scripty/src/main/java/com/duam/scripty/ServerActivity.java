package com.duam.scripty;

import static com.duam.scripty.ScriptyConstants.PREF_USER_ID;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.duam.scripty.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class ServerActivity extends Activity {
    @InjectView(R.id.editDescription) EditText editDescription;
    @InjectView(R.id.editAddress) EditText editAddress;
    @InjectView(R.id.editPort) EditText editPort;
    @InjectView(R.id.editUsername) EditText editUsername;
    @InjectView(R.id.editPassword) EditText editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btnServerOK)
    public void ok() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ScriptyHelper helper = new ScriptyHelper(this);
        long userId = prefs.getLong(PREF_USER_ID, -1);

        Server server = new Server();
        server.setUsername(editUsername.getText().toString());
        server.setPort(Integer.valueOf(editPort.getText().toString()));
        server.setPassword(editPassword.getText().toString());
        server.setDescription(editDescription.getText().toString());
        server.setAddress(editAddress.getText().toString());
        server.setUserId(userId);

        helper.insertServer(server);

        Toast.makeText(this, getString(R.string.server_saved), Toast.LENGTH_LONG).show();

        onServerSaved(server);

        finish();
    }

    @OnClick(R.id.btnServerCancel)
    public void cancel() {
        finish();
    }

    protected void onServerSaved(Server server) {
        // To be implemented by clients.
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
