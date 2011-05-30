// records demonstrations in the Astrid application.
// re-use the demonstration interface of the spinner app. hah.
package com.todoroo.astrid.demonstration;

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

  private Demonstration mDemonstration;
  private DemonstrationBinder mBinder;

  // service has 2 modes, training or testing.
  public boolean capture = true; 

  private List<Demonstration> mDemonstrationList; // stored demonstrations. needs to be serialized.

  //public Demonstration mCommandToRun; // holds the current command
  //public boolean mRunCommand = false; // set to true when mCommandToRun is ready to go... ?



  /** 
   * A constructor is required, and must call the super IntentService(String)
   * constructor with a name for the worker thread.
   */
  public DemonstrationService() {
      super("DemonstrationService");

      mDemonstration = new Demonstration();
      mBinder = new DemonstrationBinder();
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
      long endTime = System.currentTimeMillis() + 5*1000;
      while (System.currentTimeMillis() < endTime) {
          synchronized (this) {
              try {
                  //wait(endTime - System.currentTimeMillis());
              } catch (Exception e) {
              }
          }
      }
  }

  @Override
  public IBinder onBind (Intent intent) {
    Log.i(LOG_STRING, "onBind(). intent: " + intent.toString());
    Toast.makeText(getApplicationContext(), "hello demonstration!", Toast.LENGTH_SHORT).show();
    return mBinder;
  }

  public class DemonstrationBinder extends Binder {
    public DemonstrationService getService() {
      return DemonstrationService.this;
    }
    
    @Override
    protected boolean onTransact (int code, Parcel data, Parcel reply, int flags) {
      Log.e("DEMONSTRATION_BINDER", "Got a transaction!");
      switch (code) {
        case MOTION_EVENT_CODE:
          MotionEvent ev = data.readParcelable(MotionEvent.class.getClassLoader());
          Log.i(LOG_STRING, "Read motion event from parcel2: " + ev.toString());
          Log.i(LOG_STRING, "deviceid:" + ev.getDeviceId() + " edge: " + ev.getEdgeFlags() + " event time: " + ev.getEventTime() +  " action: " + ev.getAction());
          mDemonstration.addMotionEvent(ev);
          break;
        case KEY_EVENT_CODE:
          KeyEvent kev = data.readParcelable(KeyEvent.class.getClassLoader());
          Log.i(LOG_STRING, "Read key event from parcel: " + kev.toString());
          mDemonstration.addKeyEvent(kev);
          break;
        case TRACKBALL_EVENT_CODE: //currently unhandeled
          break;

        case TEST_EVENT_CODE:
          // build a parcel.
          MotionEvent ev1 = MotionEvent.obtain((long)1, (long)2, 0, (float)440, (float)780, (float)0.157, (float)0.066, 0, (float)1.0, (float)1.0, 0, 0);
          MotionEvent ev2 = MotionEvent.obtain((long)1, (long)2, 1, (float)440, (float)780, (float)0.157, (float)0.066, 0, (float)1.0, (float)1.0, 0, 0);
          List<MotionEvent> evList = new ArrayList<MotionEvent>();
          evList.add(ev1);
          evList.add(ev2);

          //Parcel parcel = Parcel.obtain();
          reply.writeList(evList);

          break;

        default:
          Log.e(LOG_STRING, "Unknown transaction code: " + code);

        
      }
      return true;
    }
  }

}
