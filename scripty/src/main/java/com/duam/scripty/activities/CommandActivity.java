package com.duam.scripty.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duam.scripty.db.Command;
import com.duam.scripty.R;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;
import com.duam.scripty.tasks.RunCommandTask;

import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.duam.scripty.db.ScriptyHelper.COMMAND_ID;
import static com.duam.scripty.db.ScriptyHelper.COMMAND;
import static com.duam.scripty.db.ScriptyHelper.SERVER_ID;

public class CommandActivity extends RoboActivity {
    public static final int EDIT_COMMAND_CODE = 10;

    public static final int COMMAND_EDITED_RESULT = 100;
    public static final int COMMAND_DELETED_RESULT = 200;

    private ArrayAdapter<String> adapter;
    private List<String> lines;
    private List<Integer> errors;
    private Command command;
    private Server server;

    @InjectView(R.id.lwTerminal) ListView lwTerminal;
    @InjectView(R.id.btnRunCommand) Button btnRunCommand;

    @InjectResource(R.string.running) String running;
    @InjectResource(R.string.successfully) String successfully;
    @InjectResource(R.string.title_activity_command) String titleActivityCommand;
    @InjectResource(R.string.command_exec_error) String commandExecError;

    @InjectResource(R.color.terminal_error) ColorStateList terminarError;
    @InjectResource(R.color.terminal_font) ColorStateList terminalFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        long id = getIntent().getLongExtra(COMMAND_ID, -1);
        ScriptyHelper helper = new ScriptyHelper(this);
        command = helper.retrieveCommand(id);
        server = helper.retrieveServer(command.getServerId());

        setTitle(String.format(titleActivityCommand, command.getDescription()));

        errors = new ArrayList<>();

        lines = new ArrayList<>();
        lines.add(buildCommandLine());
        adapter = new ArrayAdapter<String>(this, R.layout.terminal_list_item, lines) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.terminal_list_item, parent, false);
                }
                TextView txtLine = (TextView) convertView.findViewById(R.id.txtLine);
                txtLine.setText(getItem(position));
                txtLine.setTextColor(errors.contains(position) ? terminarError : terminalFont);

                return convertView;
            }
        };

        lwTerminal.setAdapter(adapter);
        lwTerminal.setItemsCanFocus(false);

        btnRunCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runCommand(command);
            }
        });
    }

    private String buildCommandLine() {
        String username = server.getUsername() != null ? server.getUsername() : "";
        return username + "@" + server.getAddress() + ":~$ " + command.getCommand();
    }

    private void delete(Command command) {
        ScriptyHelper helper = new ScriptyHelper(this);
        helper.deleteCommand(command.get_id());
        setResult(COMMAND_DELETED_RESULT);
        finish();
    }

    private void editCommand(Command command) {
        Intent intent = new Intent(this, NewCommandActivity.class);
        Ln.d("Putting new_command with id "+ command.get_id());
        intent.putExtra(COMMAND, command);
        intent.putExtra(SERVER_ID, server.get_id());
        startActivityForResult(intent, EDIT_COMMAND_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case EDIT_COMMAND_CODE:
                setResult(COMMAND_EDITED_RESULT);
                finish();
                break;
        }
    }

    private void runCommand(Command command) {
        final ProgressDialog pd = new ProgressDialog(this);

        new RunCommandTask(this, command) {
            protected void onPreExecute() throws Exception {
                super.onPreExecute();
                pd.setMessage(running);
                pd.show();

                errors.clear();
            }
            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Ln.e(e);
                errors.add(lines.size());
                addToTerminal(e.getMessage());
                errors.add(lines.size());
                addToTerminal(commandExecError);
            }
            @Override
            protected void onFinally() throws RuntimeException {
                super.onFinally();

                if (pd.isShowing()) pd.dismiss();
            }
            @Override
            protected void publishProgress(String line) {
                if (pd.isShowing()) pd.dismiss();
                addToTerminal(line);
            }

            @Override
            protected void publishError(String line) {
                if (pd.isShowing()) pd.dismiss();
                if (line != null && !line.isEmpty()) errors.add(lines.size());
                addToTerminal(line);
            }

            @Override
            protected void onSuccess(String s) throws Exception {
                addToTerminal(s);
            }
        }.execute();
    }

    private void addToTerminal(final String line) {
        if (line != null && !line.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lines.add(line);
                    adapter.notifyDataSetChanged();
                    lwTerminal.setSelection(adapter.getCount() - 1);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_command:
                editCommand(command);
                return true;
            case R.id.action_delete_command:
                delete(command);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
