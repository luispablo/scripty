package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.ScriptyService;
import com.duam.scripty.db.Command;

import java.util.List;

import retrofit.RestAdapter;
import roboguice.util.RoboAsyncTask;

import static com.duam.scripty.ScriptyConstants.SCRIPTY_SERVER_URL;

/**
 * Created by luispablo on 06/08/14.
 */
public class DownloadUserCommandsTask extends RoboAsyncTask<List<Command>> {
    private long userId;

    protected DownloadUserCommandsTask(Context context, long userId) {
        super(context);
        this.userId = userId;
    }

    @Override
    public List<Command> call() throws Exception {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SCRIPTY_SERVER_URL).build();
        ScriptyService service = restAdapter.create(ScriptyService.class);

        return service.getUserCommands(userId);
    }
}
