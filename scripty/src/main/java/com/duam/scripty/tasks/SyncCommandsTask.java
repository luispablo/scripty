package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.db.Command;
import com.duam.scripty.db.RemoteModel;
import com.duam.scripty.db.ScriptyHelper;

import java.util.List;

import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 08/08/14.
 */
public class SyncCommandsTask extends RoboAsyncTask<Void> {
    private List<Command> remoteCommands;

    protected SyncCommandsTask(Context context, List<Command> remoteCommands) {
        super(context);
        this.remoteCommands = remoteCommands;
    }

    @Override
    public Void call() throws Exception {
        Ln.d("Syncing commands...");
        ScriptyHelper helper = new ScriptyHelper(getContext());

        helper.sync(remoteCommands, Command.class);

        return null;
    }
}
