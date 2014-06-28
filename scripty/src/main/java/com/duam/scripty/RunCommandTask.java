package com.duam.scripty;

import android.content.Context;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.InputStream;

import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 24/06/14.
 */
public class RunCommandTask extends RoboAsyncTask<String> {

    private Command command;

    public RunCommandTask(Context context, Command command) {
        super(context);

        this.command = command;
    }

    @Override
    public String call() throws Exception {
        ScriptyHelper helper = new ScriptyHelper(getContext());
        Server server = helper.retrieveServer(command.getServerId());

        String response = null;

        JSch jsch = new JSch();

        Session session = jsch.getSession(server.getUsername(), server.getAddress());
        session.setUserInfo(userInfo(server));
        session.connect();

        Channel channel=session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command.getCommand());

        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] tmp = new byte[1024];

        while(true)
        {
            while(in.available()>0)
            {
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;

                response = new String(tmp, 0, i);
                Ln.d("response: " + response);
            }

            if(channel.isClosed())
            {
                Ln.d("exit-status: "+channel.getExitStatus());
                break;
            }

            Thread.sleep(1000);
        }

        channel.disconnect();
        session.disconnect();

        return response;
    }

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
