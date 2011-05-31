// records demonstrations in the Astrid application.
// re-use the demonstration interface of the spinner app. hah.
package com.todoroo.astrid.demonstration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import android.view.KeyEvent;
import android.view.MotionEvent;




public class DemonstrationService extends IntentService {
  private String LOG_STRING = "DEMONSTRATION_SERVICE";
  static final int MSG_SAY_HELLO = 1;
  // codes used in transact
  public static final int MOTION_EVENT_CODE = 1;
  public static final int KEY_EVENT_CODE = 2;
  public static final int TRACKBALL_EVENT_CODE = 3;
  public static final int TEST_EVENT_CODE = 4;
  /** User hit the settings button to toggle demonstration. Parcel will possibly contain a String. */
  public static final int TOGGLE_CODE = 5; 
  public static final int GET_TOGGLE_CODE = 6; 
  public static final int SET_DIRECTORY_CODE = 7;

  public static final String DEMONSTRATION_FILE = "demonstration.ser";

  private Demonstration mDemonstration;
  private DemonstrationBinder mBinder;

  // service has 2 modes, training or testing.
  public boolean mRecord = false; 
  public boolean mPlaybackReady = false; // ready to playback a demonstration.

  private List<Demonstration> mDemonstrationList; // stored demonstrations. needs to be serialized.

  //public Demonstration mCommandToRun; // holds the current command
  //public boolean mRunCommand = false; // set to true when mCommandToRun is ready to go... ?

  public String mExternalDir = null;
  //public String mExternalDir = "/mnt/sdcard/Android/data/com.timsu.astrid/files/";



  /** 
   * A constructor is required, and must call the super IntentService(String)
   * constructor with a name for the worker thread.
   */
  public DemonstrationService() {
    super("DemonstrationService");

    mDemonstration = new Demonstration();
    mBinder = new DemonstrationBinder();
    
    // do reading and writing here.
    unserializeDemonstration();
  }

  @Override
  public void onDestroy () {
    // do serialization
    serializeDemonstration();
    return;
  }

  // open the file, write it out
  public void serializeDemonstration() {
    Log.i(LOG_STRING, "Serializing demonstration");
    try {
      File demonstrationFile = new File(mExternalDir, DEMONSTRATION_FILE);
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(demonstrationFile));
      //mDemonstration.writeObject(out);
      out.writeObject(mDemonstration);
      out.close();
    } catch (Exception e) {
      Log.e(LOG_STRING, "Messed up serialization: " + e.toString());
    }    
  }

  public void unserializeDemonstration() {
    Log.i(LOG_STRING, "Unserializing demonstration");
    try {
      File demonstrationFile = new File(mExternalDir, DEMONSTRATION_FILE);
      if(demonstrationFile.exists()) {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(demonstrationFile));
        //mDemonstration.writeObject(out);
        mDemonstration = (Demonstration) in.readObject();
        in.close();
      }
    } catch (Exception e) {
      Log.e(LOG_STRING, "Messed up unserialization: " + e.toString() + " " + e.getMessage());
      e.printStackTrace();
    }    
    Log.i(LOG_STRING, "Demonstration we read: " + mDemonstration.toString());
    mPlaybackReady = true; // ready to playback after we read in the demonstration.
  }

  /**
   * The IntentService calls this method from the default worker thread with
   * the intent that started the service. When this method returns, IntentService
   * stops the service, as appropriate.
   */
  @Override
  protected void onHandleIntent(Intent intent) {
      Log.i(LOG_STRING, "Handling intent: " + intent.toString());
      // Normally we would do some work here, like download a file.
      // For our sample, we just sleep for 5 seconds.
      /*
      long endTime = System.currentTimeMillis() + 5*1000;
      while (System.currentTimeMillis() < endTime) {
          synchronized (this) {
              try {
                  //wait(endTime - System.currentTimeMillis());
              } catch (Exception e) {
              }
          }
      }
      */
  }

  @Override
  public IBinder onBind (Intent intent) {
    return mBinder;
  }

  public class DemonstrationBinder extends Binder {
    public DemonstrationService getService() {
      return DemonstrationService.this;
    }
    
    @Override
    protected boolean onTransact (int code, Parcel data, Parcel reply, int flags) {
      switch (code) {
        case MOTION_EVENT_CODE:
          if(mRecord) {
            MotionEvent ev = data.readParcelable(MotionEvent.class.getClassLoader());
            Log.i(LOG_STRING, "Read motion event from parcel: " + ev.toString());
            mDemonstration.addMotionEvent(ev);
          }
          break;
        case KEY_EVENT_CODE:
          if(mRecord) {
            KeyEvent kev = data.readParcelable(KeyEvent.class.getClassLoader());
            Log.i(LOG_STRING, "Read key event from parcel: " + kev.toString());
            mDemonstration.addKeyEvent(kev);
          }
          break;
        case TRACKBALL_EVENT_CODE: //currently unhandeled
            Log.i(LOG_STRING, "Read trackball event from parcel.");
          break;

        case TEST_EVENT_CODE:
          if(mPlaybackReady) {
            mRecord = false; // make sure we are in playback mode.
            // build a parcel.
            //MotionEvent ev1 = MotionEvent.obtain((long)1, (long)2, 0, (float)440, (float)780, (float)0.157, (float)0.066, 0, (float)1.0, (float)1.0, 0, 0);
            //MotionEvent ev2 = MotionEvent.obtain((long)1, (long)2, 1, (float)440, (float)780, (float)0.157, (float)0.066, 0, (float)1.0, (float)1.0, 0, 0);
            List<MotionEvent> evList = mDemonstration.mMotionEvents;
            Log.i(LOG_STRING, "Sending for playback: ");
            for(MotionEvent ev: evList) {
              Log.i(LOG_STRING, ev.toString());
            }
            //evList.add(ev1);
            //evList.add(ev2);
            //Parcel parcel = Parcel.obtain();
            reply.writeList(evList);
            mPlaybackReady = false;
          }
          break;
        case TOGGLE_CODE:
          if(mRecord == false) {
            String command = data.readString();
            mDemonstration = new Demonstration();
            mDemonstration.setCommand(command);
            Log.i(LOG_STRING, "Starting to record demonstration with command: " + command);
            mRecord = true;
          } else {
            Log.i(LOG_STRING, "Done recording demonstration.");
            mRecord = false;
            // should do some sort of saving here. make a DemonstrationDatabase class.
          }
          break;
        case GET_TOGGLE_CODE: // used to get whether we are currently recording a demonstration.
          reply.writeValue(mRecord);
          break; 

        case SET_DIRECTORY_CODE:
          // set the directory to read/write files to.
          if(mExternalDir == null) {
            mExternalDir = data.readString();
            unserializeDemonstration();
          }
          
          break;
        default:
          Log.e(LOG_STRING, "Unknown transaction code: " + code);
      }
      return true;
    }
  }

}
