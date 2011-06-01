package com.todoroo.astrid.demonstration;

import android.widget.EditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.view.View;
import android.widget.EditText;

import com.todoroo.astrid.demonstration.*;
import android.content.ServiceConnection;
import android.app.IntentService;
import android.content.Intent;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.speech.*;
import android.speech.tts.*;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.util.AttributeSet;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;

public class SpyEditText extends EditText {
  private static final String LOG_STRING = "SpyEditText";

  //private boolean mTouched = false; // whether this field has been touched yet
  //private boolean mEdited = false; // whether this field has been edited
  private IBinder mBinder = null; // how we communicate to automation
  private boolean mEditInProgress = false;

  public SpyEditText(Context context) {
    super(context); 
  }

  public SpyEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SpyEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setBinder(IBinder binder) {
    mBinder = binder;
  }

  // what to override - get focus? whatever it is that fires the keyboard...

  public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    //Log.i(LOG_STRING, "onCreateInputConnection: "  + outAttrs.toString() 
    //      + " actionID: " + outAttrs.actionId
    //      + " label: " + outAttrs.actionLabel);
    return super.onCreateInputConnection(outAttrs);
  }

  public boolean dispatchTouchEvent(MotionEvent event) {
    //Log.i(LOG_STRING, " Touch event: " + event.toString());
    if(didTouchFocusSelect()) {
      Log.i(LOG_STRING, " FOCUS SELECT");
      mEditInProgress = true;
      try {
        Parcel parcel = Parcel.obtain();
        parcel.writeString("EDIT TEXT");
        mBinder.transact(DemonstrationService.EDIT_TEXT_CODE, parcel, null, IBinder.FLAG_ONEWAY);
      } catch(RemoteException e) {
        Log.e(LOG_STRING, "Error transacting with demonstration service: " + e.toString());
      }
      // this is the money!
    }
    return super.dispatchTouchEvent(event);
  }

  public void onEditorAction(int actionCode) {
    Log.i(LOG_STRING, " Edit event: " + actionCode);
  }

  protected void onTextChanged (CharSequence text, int start, int before, int after) {
    if(mEditInProgress) {
      mEditInProgress = false;
      try {
        Parcel parcel = Parcel.obtain();
        parcel.writeString("EDIT DONE");
        mBinder.transact(DemonstrationService.EDIT_TEXT_CODE, parcel, null, IBinder.FLAG_ONEWAY);
      } catch(RemoteException e) {
        Log.e(LOG_STRING, "Error transacting with demonstration service: " + e.toString());
      }
      Log.i(LOG_STRING, "DONE EDITING HOPEFULLY VOICE CAPTURED");
      // TODO send a message to press the back button.
    }
    super.onTextChanged(text, start, before, after);
  }


}

