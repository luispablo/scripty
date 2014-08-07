package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.db.Server;

import java.util.List;

import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 06/08/14.
 */
public class SyncServersTask extends RoboAsyncTask<Void> {
    private List<Server> remoteServers;

    protected SyncServersTask(Context context, List<Server> servers) {
        super(context);
        remoteServers = servers;
    }

    @Override
    public Void call() throws Exception {
        // get local servers
        // for each remote server
            // exists local?
                // remove local from collection
                // different?
                    // UPDATE
                // else - not changed
                    // void
            // else - not exits local
                // INSERT
        // end for

        // DELETE all locals remaining in collection

        return null;
    }
}
