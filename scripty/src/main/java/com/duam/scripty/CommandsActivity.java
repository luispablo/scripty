package com.duam.scripty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import roboguice.activity.RoboListActivity;

/**
 * Created by luispablo on 06/06/14.
 */
public class CommandsActivity extends RoboListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScriptyHelper helper = new ScriptyHelper(CommandsActivity.this);

        // Si no hay servers ofrecer la descarga.
        if (!helper.existsAnyServer()) {
            offerServerDownload();
        }
        else {

        }
    }

    private void offerServerDownload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CommandsActivity.this);
        builder.setMessage(getString(R.string.ask_download_servers));
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO: Do nothing for now...
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO: Fire server download
                new DownloadServersTask(CommandsActivity.this, userId)
            }
        });
        builder.create().show();
    }

}
