package com.duam.scripty.tasks;

import android.content.Context;
import android.preference.PreferenceManager;

import com.duam.scripty.R;
import com.duam.scripty.db.Command;
import com.duam.scripty.db.Server;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.InputStream;

import roboguice.inject.InjectResource;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.DEFAULT_TIMEOUT_SECONDS;
import static com.duam.scripty.ScriptyConstants.PREF_TIMEOUT_SECOND;

/**
 * Created by luispablo on 24/06/14.
 */
public abstract class RunCommandTask extends RoboAsyncTask<String> {

    private static final int BUFFER_SIZE = 80;

    @InjectResource(R.string.last_command_line) String lastCommandLine;
    @InjectResource(R.string.server_without_credentials) String serverWithoutCredentials;

    private Command command;
    private Server server;

    public RunCommandTask(Context context, Command command, Server server) {
        super(context);

        this.command = command;
        this.server = server;
    }

    @Override
    public String call() throws Exception {
        if (server.hasAuthentication()) {
            JSch jsch = new JSch();

            int timeout = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(PREF_TIMEOUT_SECOND, String.valueOf(DEFAULT_TIMEOUT_SECONDS)));

            Session session = jsch.getSession(server.getUsername(), server.getAddress());
            session.setUserInfo(userInfo(server));
            session.setTimeout(timeout * 1000);
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command.getCommand());

            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            InputStream errIn = ((ChannelExec) channel).getErrStream();
            channel.connect();

            byte[] tmp = new byte[BUFFER_SIZE];

            String buffer = "";

            for (int i = in.read(tmp, 0, BUFFER_SIZE); i > 0; i = in.read(tmp, 0, BUFFER_SIZE)) {
                String line = new String(tmp, 0, i);
                buffer = appendTemporalResponse(buffer, line);
                Thread.sleep(100);
            }
            publishProgress(buffer);

            buffer = "";

            for (int i = errIn.read(tmp, 0, BUFFER_SIZE); i > 0; i = errIn.read(tmp, 0, BUFFER_SIZE)) {
                String line = new String(tmp, 0, i);
                buffer = appendTemporalError(buffer, line);
                Thread.sleep(100);
            }
            publishError(buffer);

            channel.disconnect();
            session.disconnect();

            return lastCommandLine;
        } else {
            publishError(serverWithoutCredentials);
            return null;
        }
    }

    protected String appendTemporalResponse(String buffer, String currentLine) {
        String aux = buffer + currentLine;
        String[] lines = aux.split("\\n");

        for (int i = 0; i < lines.length - 1; i++) {
            publishProgress(lines[i]);
        }

        return lines[lines.length - 1];
    }

    protected abstract void publishProgress(String line);

    protected String appendTemporalError(String buffer, String currentLine) {
        String aux = buffer + currentLine;
        String[] lines = aux.split("\\n");

        for (int i = 0; i < lines.length - 1; i++) {
            publishError(lines[i]);
        }

        return lines[lines.length - 1];
    }

    protected abstract void publishError(String line);

    private UserInfo userInfo(final Server server) {
        return new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return server.getPassword();
            }

            @Override
            public boolean promptPassword(String message) {
                return true;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return true;
            }

            @Override
            public boolean promptYesNo(String message) {
                return true;
            }

            @Override
            public void showMessage(String message) {

            }
        };
    }
}
