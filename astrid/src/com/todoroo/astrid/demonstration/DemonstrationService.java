// records demonstrations in the Astrid application.
// re-use the demonstration interface of the spinner app. hah.
package com.todoroo.astrid.demonstration;

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

  private Demonstration mDemonstration;
  private DemonstrationBinder mBinder;

  ///**
  //  * Target we publish for clients to send messages to IncomingHandler.
  //  */
  //final Messenger mMessenger = new Messenger(new IncomingHandler());



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
          Log.i(LOG_STRING, "Read motion event from parcel: " + ev.toString());
          mDemonstration.addMotionEvent(ev);
          break;
        case KEY_EVENT_CODE:
          KeyEvent kev = data.readParcelable(KeyEvent.class.getClassLoader());
          Log.i(LOG_STRING, "Read key event from parcel: " + kev.toString());
          mDemonstration.addKeyEvent(kev);
          break;
        case TRACKBALL_EVENT_CODE: //currently unhandeled
          break;
        default:
          Log.e(LOG_STRING, "Unknown transaction code: " + code);
      }
      return true;
    }
  }

  ///**
  // * Handler of incoming messages from clients.
  // */
  //class IncomingHandler extends Handler {
  //    @Override
  //    public void handleMessage(Message msg) {
  //        switch (msg.what) {
  //            case MSG_SAY_HELLO:
  //                Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
  //                break;
  //            default:
  //                super.handleMessage(msg);
  //        }
  //    }
  //}
}
