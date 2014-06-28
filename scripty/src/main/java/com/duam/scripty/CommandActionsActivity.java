package com.duam.scripty;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.duam.scripty.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.duam.scripty.ScriptyHelper.COMMAND_ID;

public class CommandActionsActivity extends RoboActivity {

    private Command command;
    private Server server;

    @InjectView(R.id.txtCommand) TextView txtCommand;
    @InjectView(R.id.btnRunCommand) Button btnRunCommand;

    @InjectResource(R.string.run_command) String runCommand;
    @InjectResource(R.string.running) String running;
    @InjectResource(R.string.successfully) String successfully;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_actions);

        long id = getIntent().getLongExtra(COMMAND_ID, -1);
        ScriptyHelper helper = new ScriptyHelper(this);
        command = helper.retrieveCommand(id);
        server = helper.retrieveServer(command.getServerId());

        setTitle(command.getDescription());
        txtCommand.setText(String.format(runCommand, command.getCommand(), server.getDescription()));

        btnRunCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runCommand(command);
            }
        });
    }

    private void runCommand(Command command) {
        final ProgressDialog pd = new ProgressDialog(this);

        new RunCommandTask(this, command) {
            protected void onPreExecute() throws Exception {
                super.onPreExecute();
                pd.setMessage(running);
                pd.show();
            }
            @Override
            protected void onSuccess(String s) throws Exception {
                super.onSuccess(s);

                Toast.makeText(CommandActionsActivity.this, successfully, Toast.LENGTH_LONG).show();
            }
            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Toast.makeText(CommandActionsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            @Override
            protected void onFinally() throws RuntimeException {
                super.onFinally();

                pd.dismiss();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.command_actions, menu);
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
