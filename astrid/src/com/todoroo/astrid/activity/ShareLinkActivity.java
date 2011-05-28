/**
 * TODO: make this lightweight, don't extend the entire TaskListActivity
 */
package com.todoroo.astrid.activity;

import android.content.Intent;
import android.os.Bundle;

import com.todoroo.astrid.data.Task;

import com.todoroo.astrid.asr.ASRService;
import com.todoroo.astrid.demonstration.*;
import android.content.ServiceConnection;
import android.app.IntentService;
import android.content.Intent;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;

/**
 * @author joshuagross
 *
 * Create a new task based on incoming links from the "share" menu
 */
public final class ShareLinkActivity extends TaskListActivity {
    public ShareLinkActivity () {
        super();
    }

    private DemonstrationService mService;
    private DemonstrationService.DemonstrationBinder mBinder;
    private boolean mBound = false; // whether we are bound to a demonstration service
    private String LOG_STRING = "ShareLinkActivity";

    /** Defines callbacks for demonstration service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.i(LOG_STRING, "onServiceConnect()from ServiceConnection.");
            mBinder = (DemonstrationService.DemonstrationBinder) service;
            mService = mBinder.getService();
            attachShim(); // now that we have the binder, fire up the shim.
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    protected void attachShim() {
      AccessibilityShim.attachToActivity(this, mBinder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent callerIntent = getIntent();

        String subject = callerIntent.getStringExtra(Intent.EXTRA_SUBJECT);
        if(subject == null)
            subject = "";
        Task task = quickAddTask(subject, false);
        task.setValue(Task.NOTES, callerIntent.getStringExtra(Intent.EXTRA_TEXT));
        taskService.save(task);
        Intent intent = new Intent(this, TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.TOKEN_ID, task.getId());
        startActivityForResult(intent, ACTIVITY_EDIT_TASK);
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      if (mBound) {
            unbindService(mConnection);
            mBound = false;
      }
    }
}
