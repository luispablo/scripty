package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.db.RemoteModel;
import com.duam.scripty.db.ScriptyHelper;
import com.duam.scripty.db.Server;

import java.util.List;

import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 06/08/14.
 */
public class SyncServersTask extends RoboAsyncTask<Void> {
    private List<Server> remoteServers;

    public SyncServersTask(Context context, List<Server> servers) {
        super(context);
        remoteServers = servers;
    }

    @Override
    public Void call() throws Exception {
        Ln.d("Syncing servers...");
        ScriptyHelper helper = new ScriptyHelper(getContext());

        helper.sync(remoteServers, Server.class);

        return null;
    }
}
