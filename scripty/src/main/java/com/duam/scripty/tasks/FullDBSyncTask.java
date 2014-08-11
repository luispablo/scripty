package com.duam.scripty.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.duam.scripty.FullDBSyncCommand;
import com.duam.scripty.R;

import roboguice.inject.InjectResource;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created by luispablo on 09/08/14.
 */
public class FullDBSyncTask extends RoboAsyncTask<Void> {
    private ProgressDialog pd;
    private long userId;

    @InjectResource(R.string.sync_db_progress_title) String syncDBProgressTitle;
    @InjectResource(R.string.sync_db_progress_message) String syncDBProgressMessage;

    public FullDBSyncTask(Context context, long userId) {
        super(context);

        this.userId = userId;

        pd  = new ProgressDialog(context);
        pd.setTitle(syncDBProgressTitle);
        pd.setMessage(syncDBProgressMessage);
    }

    @Override
    protected void onPreExecute() throws Exception {
        pd.show();
    }

    @Override
    public Void call() throws Exception {
        new FullDBSyncCommand(getContext(), userId).execute();
        return null;
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
        Ln.e(e);
    }

    @Override
    protected void onFinally() throws RuntimeException {
        pd.dismiss();
    }
}
