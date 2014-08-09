package com.duam.scripty.tasks;

import android.content.Context;

import com.duam.scripty.ScriptyService;
import com.duam.scripty.Utils;
import com.duam.scripty.db.Command;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.util.Ln;
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
        Ln.d("Downloading user "+ userId +" commands");

        return Utils.scriptyService().getUserCommands(userId);
    }
}
