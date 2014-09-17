package com.duam.scripty;

import android.content.Context;

import com.duam.scripty.db.Command;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;

import java.util.List;

import retrofit.RetrofitError;
import roboguice.util.Ln;

/**
 * Created by luispablo on 09/08/14.
 */
public class FullDBSyncCommand {
    private Context context;
    private long userId;

    public FullDBSyncCommand(Context context, long userId) {
        this.context = context;
        this.userId = userId;
    }

    public void execute() throws IllegalAccessException, ScriptyException, InstantiationException {
        List<Server> remoteServers = Utils.scriptyService().getServers(userId);
        Ln.d("Got " + remoteServers.size() + " servers for user " + userId + ".");
        List<Command> remoteCommands = Utils.scriptyService().getUserCommands(userId);
        Ln.d("Got " + remoteCommands.size() + " commands for user " + userId + ".");

        ScriptyHelper helper = ScriptyHelper.getInstance(context);

        helper.sync(remoteServers, Server.class);
        helper.sync(remoteCommands, Command.class);
    }

}
