/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

//package com.googlecode.eyesfree.widget;
//package com.android.example.spinner;
package com.todoroo.astrid.demonstration;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.RemoteException;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
// XXX commented out for android-8 build target
//import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import android.util.Log;

import java.io.File;
//import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * @author alanv@google.com (Alan Viverette)
 */
public class AccessibleFrameLayout extends FrameLayout {
    private static final boolean ENABLE_VIBRATE = false;
    private static final String LOG_STRING = "AcessibleFrameLayout";
    private static final String DEMONSTRATION_FILE = "demonstration.ser";

    private final Handler mHandler;
    private final Instrumentation mInstrumentation;
    private final GestureDetector mDetector;
    private final Vibrator mVibrator;
    private final Paint mPaint;
    private final TextToSpeech mTTS;
    private final Rect mSelectedRect;
    private final boolean mCompatibilityMode;

    private View mSelectedView;

    private boolean mExplorationEnabled;
    private boolean mSpeechAvailable;


    Demonstration mDemonstration;
    //ServiceConnection mDemonstrationConnection; // used for connecting to the demonstration service to dispatch events
    IBinder mBinder;

    private File mTouchFile;
    private FileOutputStream mTouchStream;
    private File mExternalDir;
    // private static final long[] mFocusGainedFocusablePattern = new long[] {
    // 0, 100 };
    private static final long[] mFocusLostFocusablePattern = new long[] { 0, 50 };
    // private static final long[] mFocusGainedPattern = new long[] { 0, 50 };
    private static final long[] mFocusLostPattern = new long[] { 0, 15 };

    //public AccessibleFrameLayout(Context context, ServiceConnection demonstrationConnection) {
    public AccessibleFrameLayout(Context context, IBinder binder) {
        super(context);

        mHandler = new Handler();
        mInstrumentation = new Instrumentation();

        if (ENABLE_VIBRATE) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } else {
            mVibrator = null;
        }

        mDetector = new GestureDetector(context, gestureListener);
        mDetector.setIsLongpressEnabled(false);

        mSelectedView = null;
        mSelectedRect = new Rect();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(0xFF8CD2FF);

        mSelectedView = null;
        mSpeechAvailable = false;

        mTTS = new TextToSpeech(context, ttsInit);

        updateExplorationEnabled();

        getViewTreeObserver().addOnGlobalFocusChangeListener(focusChangeListener);

        boolean compatibilityMode = true;

        /* XXX: removed so it works on android-8, which lacks PointerCoords
        try {
            Class.forName("android.view.MotionEvent.PointerCoords");
            compatibilityMode = false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        */

        mCompatibilityMode = compatibilityMode;


        mDemonstration = new Demonstration();

        try {
          mExternalDir = context.getExternalFilesDir(null);
          Log.i(LOG_STRING, "External directory: " + mExternalDir);
        } catch(Exception e) {
          Log.e(LOG_STRING, "Error getting external dir: " + e.toString());
        }
        Log.i(LOG_STRING, "Assigning binder");
        if(binder == null) {
          Log.e(LOG_STRING, "Null binder in constructor");
        }
        mBinder = binder; 

        //unserializeDemonstration();
        //unserializeMotionEvents();
        //setOnClickListener(mCorkyListener);
        //setOnTouchListener(mTouchListener);
        //clearSerialization();
    }

    /*
    private OnClickListener mCorkyListener = new OnClickListener() {
          public void onClick(View v) {
            Log.i(LOG_STRING, "Click! captured from mCorkyListener");
          }
      // do something when the button is clicked
    };

    private OnTouchListener mTouchListener = new OnTouchListener() {
          public boolean onTouch(View v, MotionEvent ev) {
            Log.i(LOG_STRING, "Touch! captured from mTouchListener: " + ev.toString());
            return false;
          }
      // do something when the button is clicked
    };
    */


    public ArrayList<MotionEvent> readTouchFile(File file) {
      ArrayList<MotionEventCache> motionEventCache = new ArrayList<MotionEventCache>();
      ArrayList<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
      try {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        motionEventCache = (ArrayList<MotionEventCache>) ois.readObject(); 
        for(MotionEventCache mec : motionEventCache) {
          MotionEvent ev = mec.obtain();
          motionEvents.add(ev);
        }
        ois.close();
      } catch (Exception e) {
        Log.e(LOG_STRING, "Problem reading touch file: " + e.toString());
      }
      return motionEvents;
    }


    // removes the serialization file, useful for debugging.
    public void clearSerialization() {
      try {
        //Log.i(LOG_STRING, "Opening output directory: " + mExternalDir.toString());
        mTouchFile = new File(mExternalDir, DEMONSTRATION_FILE);
        boolean deleted = mTouchFile.delete();
        Log.i(LOG_STRING, "Touch file deleted: " + deleted);
      } catch (Exception e) {
        Log.e(LOG_STRING, "Error opening touch output: " + e.toString());
      }
      return;
    }

    /** save the demonstration to disk. */
    public void serializeDemonstration() {
      try {
        File demonstrationFile = new File(mExternalDir, DEMONSTRATION_FILE);
        Log.i(LOG_STRING, "Writing " + mDemonstration.toString() + " to " + demonstrationFile.toString());
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(demonstrationFile));
        //mDemonstration.writeObject(out);
        //out.writeObject(mDemonstration);
        out.close();
      } catch (Exception e) {
        Log.e(LOG_STRING, "Error opening demonstration output: " + e.toString());
      }
    }    

    public void unserializeDemonstration() {
      try {
        File file = new File(mExternalDir, DEMONSTRATION_FILE);
        Log.i(LOG_STRING, "Reading from: " + file.toString());
        if(!file.exists()) {
          Log.i(LOG_STRING, "DOES NOT EXIST");
        }
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        //mDemonstration = (Demonstration) in.readObject();
        mDemonstration = new Demonstration();
        //mDemonstration.readObject(in);
        in.close();
      } catch(Exception e) {
        System.err.println("Way to fuck it up: " + e.getMessage());
      }
      Log.i(LOG_STRING, "Read demonstration: " + mDemonstration.toString());
    }



    @Override
    public void getHitRect(Rect rect) {
        getWindowVisibleDisplayFrame(rect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (hasWindowFocus) {
            updateExplorationEnabled();
        }

        if (mExplorationEnabled) {
            if (hasWindowFocus) {
                // Update focus since it may have changed.
                View newFocusedView = findFocus();
                setSelectedView(newFocusedView, false);
            } else {
                setSelectedView(null, false);
            }
        }
    }

    private void updateExplorationEnabled() {
        ContentResolver resolver = getContext().getContentResolver();
        int enabled = Settings.Secure.getInt(resolver, Settings.Secure.ACCESSIBILITY_ENABLED, -1);

        mExplorationEnabled = (enabled == 1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(LOG_STRING, "detach event");

        mTTS.shutdown();
        //serializeMotionEvents();
        //serializeDemonstration();
    }

    /*
    public void onClick(View v) {
      return;
    }
*/

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mSelectedView != null) {
            getGlobalVisibleRect(mSelectedRect);

            int offsetTop = mSelectedRect.top;
            int offsetLeft = mSelectedRect.left;

            mSelectedView.getGlobalVisibleRect(mSelectedRect);

            if (offsetTop > 0 || offsetLeft > 0) {
                int saveCount = canvas.save();
                Matrix matrix = canvas.getMatrix();
                matrix.postTranslate(offsetLeft, offsetTop);
                canvas.setMatrix(matrix);
                canvas.drawRect(mSelectedRect, mPaint);
                canvas.restoreToCount(saveCount);
            } else {
                canvas.drawRect(mSelectedRect, mPaint);
            }
        }
    }

    /**
     * Inserts this frame between a ViewGroup and its children by removing all
     * child views from the parent view, adding them to this frame, and then
     * adding this frame to the parent view.
     *
     * @param parent The parent view into which this frame will be inserted.
     */
    public void inject(ViewGroup parent) {
        int count = parent.getChildCount();
        Log.i(LOG_STRING, "inject: # of children: " + count);

        while (parent.getChildCount() > 0) {
            View child = parent.getChildAt(0);
            parent.removeViewAt(0);
            // XXX to make it recursive, make a new accessibleframelayout for each child ?
            /*
            if(child instanceof ViewGroup) {
              AccessibleFrameLayout childFrame = new AccessibleFrameLayout(child.getContext());
              childFrame.inject((ViewGroup) child); 
            }
            */
            addView(child);
        }

        parent.addView(this);
    }

    /**
     * Strips the last pointer from a {@link MotionEvent} and returns the
     * modified event. Does not modify the original event.
     *
     * @param ev The MotionEvent to modify.
     * @return The modified MotionEvent.
     */
    private MotionEvent stripLastPointer(MotionEvent ev) {
        ev.getPointerCount();

        int removePointer = ev.getPointerCount() - 1;
        int removePointerId = ev.getPointerId(removePointer);

        long downTime = ev.getDownTime();
        long eventTime = ev.getEventTime();
        int action = ev.getAction();
        int pointers = ev.getPointerCount() - 1;
        int[] pointerIds = new int[pointers];
        int metaState = ev.getMetaState();
        float xPrecision = ev.getXPrecision();
        float yPrecision = ev.getYPrecision();
        int deviceId = ev.getDeviceId();
        int edgeFlags = ev.getEdgeFlags();

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                action -= 0x100;
                if (pointers == 1) {
                    action -= 0x5;
                }
                break;
        }

        MotionEvent event = null;

        if (mCompatibilityMode) {
            float x = ev.getX();
            float y = ev.getY();
            float pressure = ev.getPressure();
            float size = ev.getSize();

            event = MotionEvent.obtain(downTime, eventTime, action, pointers, x, y, pressure, size,
                    metaState, xPrecision, yPrecision, deviceId, edgeFlags);
        } 

        // XXX commented out to get this to compile !
        /*
        else {
            PointerCoords[] pointerCoords = new PointerCoords[pointers];
            int source = ev.getSource();
            int flags = ev.getFlags();

            for (int i = 0; i < pointers; i++) {
                pointerIds[i] = ev.getPointerId(i);
                pointerCoords[i] = new PointerCoords();

                ev.getPointerCoords(i, pointerCoords[i]);
            }

            event = MotionEvent.obtain(downTime, eventTime, action, pointers, pointerIds,
                    pointerCoords, metaState, xPrecision, yPrecision, deviceId, edgeFlags, source,
                    flags);
        }
        */

        return event;
    }

    @Override 
    public boolean dispatchKeyEvent(KeyEvent ev) {
      Log.i(LOG_STRING, "key event: " + ev.toString());
      int code = ev.getKeyCode();
      int action = ev.getAction();
      if(code == 80 && action == 1) {
        //Log.i(LOG_STRING, "CAMERA CAMERA CAMERA CAMERA" + ev.toString());
        super.dispatchKeyEvent(ev);
     //       Parcel reply = Parcel.obtain();
     //       boolean record = false;
     //       try {
     //         mBinder.transact(DemonstrationService.GET_TOGGLE_CODE, null, reply, 0);
     //         record = (Boolean) reply.readValue(Boolean.class.getClassLoader());
     //       } catch (RemoteException e) {
     //         Log.e(LOG_STRING, "Problems transacting with demonstration service: " + e.toString());
     //       }
     //       onRecord(record);
        return true;
      }

     // I/AcessibleFrameLayout( 2081): key event: KeyEvent{action=0 code=80 repeat=0 meta=0 scancode=211 mFlags=8}
     // I/AcessibleFrameLayout( 2081): key event: KeyEvent{action=1 code=80 repeat=0 meta=0 scancode=211 mFlags=8}

      
      return super.dispatchKeyEvent(ev);
    }

    //private void onRecord(boolean start) {
    //  if(!start) {
    //      //mTextView.setText("making it happen");
    //      //RecognizerIntent recognizerIntent = new RecognizerIntent();
    //      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);  
    //      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    //              RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    //      intent.putExtra("calling_package","com.todoroo.astrid.activity.FilterListActivity");
    //      //mSpeechRecognizer.startListening(intent);
    //      //startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH);
    //      mSpeechRecognizer.startListening(intent);
    //  } else {
    //    mSpeechRecognizer.stopListening();
    //    try {
    //      Parcel parcel = Parcel.obtain();
    //      parcel.writeString("STOP RECORDING");
    //      mBinder.transact(DemonstrationService.TOGGLE_CODE, parcel, null, IBinder.FLAG_ONEWAY);
    //    } catch(RemoteException e) {
    //      Log.e(LOG_STRING, "Error transacting with demonstration service: " + e.toString());
    //    }
    //    //mTextView.setText("ain't happening no more");
    //  }
    //}

    @Override
    public boolean dispatchTrackballEvent (MotionEvent ev) {
      Log.i(LOG_STRING, "trackball event: " + ev.toString());
      return super.dispatchTrackballEvent(ev);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(LOG_STRING,"touch event: " + ev.toString());
          //+ " eventTime: " + ev.getEventTime() + " downtime: " + ev.getDownTime());
        //  + " xraw: " + ev.getRawX() +" x: "+ev.getX()
        //  + " yraw: " + ev.getRawY() +" y: "+ev.getY());
        //mDemonstration.addMotionEvent(ev);
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(ev, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        // add motionevent to parcel
        try {
          if(mBinder == null) {
            Log.e(LOG_STRING, "Null binder!"); 
          }
          mBinder.transact(DemonstrationService.MOTION_EVENT_CODE, parcel, null, IBinder.FLAG_ONEWAY);
        } catch (RemoteException e) {
          Log.e(LOG_STRING, "Error transacting with demonstration service: " + e.toString());
        }

        if (mExplorationEnabled) {
            int pointers = ev.getPointerCount();

            if (ev.getPointerCount() == 1) {
                ViewGroup target = this;

                if (ev.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    ev.setAction(MotionEvent.ACTION_DOWN);
                    mDetector.onTouchEvent(ev);
                    ev.setAction(MotionEvent.ACTION_UP);
                }

                mDetector.onTouchEvent(ev);

                return true;
            }

            ev = stripLastPointer(ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    /**
     * Emulates a tap event positioned at the center of the selected view. No-op
     * if no view is selected.
     */
    private void tapSelectedView() {
        if (mSelectedView == null) {
            return;
        }

        final float centerX = mSelectedRect.exactCenterX();
        final float centerY = mSelectedRect.exactCenterY();
        final long currTime = SystemClock.uptimeMillis();

        MotionEvent down = MotionEvent.obtain(
                currTime, currTime, MotionEvent.ACTION_DOWN, centerX, centerY, 0);
        MotionEvent up = MotionEvent.obtain(
                currTime, currTime, MotionEvent.ACTION_UP, centerX, centerY, 0);

        super.dispatchTouchEvent(down);
        super.dispatchTouchEvent(up);
    }

    /**
     * Emulates a directional pad event based on the given flick direction.
     *
     * @param direction A flick direction constant.
     */
    private void changeFocus(int direction) {
        int keyCode = KeyEvent.KEYCODE_UNKNOWN;

        switch (direction) {
            case FlickGestureListener.FLICK_UP:
                keyCode = KeyEvent.KEYCODE_DPAD_UP;
                break;
            case FlickGestureListener.FLICK_RIGHT:
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            case FlickGestureListener.FLICK_DOWN:
                keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
                break;
            case FlickGestureListener.FLICK_LEFT:
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            default:
                // Invalid flick constant.
                return;
        }

        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
            final int keyCodeFinal = keyCode;

            // Exit from touch mode as gracefully as possible.
            if (mSelectedView != null) {
                ViewParent parent = mSelectedView.getParent();

                mSelectedView.requestFocusFromTouch();

                // If the selected view belongs to a list, make sure it's
                // selected within the list (since it's not focusable).
                // TODO(alanv): Check whether the prior call was successful?
                if (parent instanceof AbsListView) {
                    AbsListView listParent = (AbsListView) parent;
                    int position = listParent.getPositionForView(mSelectedView);
                    listParent.setSelection(position);
                }
            } else {
                requestFocusFromTouch();
            }

            // We have to send the key event on a separate thread, then return
            // to the main thread to synchronize the selected view with focus.
            new Thread() {
                @Override
                public void run() {
                    mInstrumentation.sendKeyDownUpSync(keyCodeFinal);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            View newFocusedView = findFocus();
                            setSelectedView(newFocusedView, false);
                        }
                    });
                }
            }.start();
        }
    }

    /**
     * Searches for a selectable view under the given frame-relative
     * coordinates.
     *
     * @param x Frame-relative X coordinate.
     * @param y Frame-relative Y coordinate.
     */
    private void setSelectionAtPoint(int x, int y) {
        View selection = SelectionFinder.getSelectionAtPoint(mSelectedView, this, x, y);

        setSelectedView(selection, true);
    }

    /**
     * Sets the selected view and optionally announces it through TalkBack.
     *
     * @param selectedView The {@link View} to set as the current selection.
     * @param announce Set to <code>true</code> to announce selection changes.
     */
    private void setSelectedView(View selectedView, boolean announce) {
        if (mSelectedView == selectedView) {
            return;
        }

        if (mSelectedView != null) {
            announceSelectionLost(mSelectedView);
        }

        if (selectedView != null) {
            if (selectedView instanceof AbsListView) {
                AbsListView absListView = (AbsListView) selectedView;
                View item = absListView.getSelectedView();

                if (item != null) {
                    selectedView = item;
                } else {
                    // We don't want to select list containers, so if there's no
                    // selected element then we'll just select nothing.
                    selectedView = null;
                }
            }

            if (selectedView != null && announce) {
                announceSelectionGained(selectedView);
            }
        }

        mSelectedView = selectedView;

        invalidate();
    }

    /**
     * Updates the selection rectangle and attempts to shift focus away from the
     * provided view. Clears active TTS.
     *
     * @param view
     */
    private void announceSelectionLost(View view) {
        // TODO(alanv): Add an additional TYPE_VIEW_HOVER_OFF event type that
        // fires a KickBack vibration and (probably) clears active TalkBack
        // utterances.

        if (mSpeechAvailable) {
            mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }

        if (mVibrator != null) {
            if (view.isFocusable()) {
                mVibrator.vibrate(mFocusLostFocusablePattern, -1);
            } else {
                mVibrator.vibrate(mFocusLostPattern, -1);
            }
        }

        mSelectedRect.setEmpty();
    }

    /**
     * Updates the selection rectangle and attempts to shift focus to the
     * provided view. If the view is not focusable, fires an AccessibilityEvent
     * so that it is read aloud.
     *
     * @param view The view which has gained selection.
     * @return Returns the view that actually gained selection.
     */
    private View announceSelectionGained(View view) {
        // TODO(alanv): Add an additional TYPE_VIEW_HOVER event type with a
        // different KickBack response. Otherwise everything looks like a
        // button.

        if (mSpeechAvailable) {
            mTTS.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }

        view.getGlobalVisibleRect(mSelectedRect);

        // If the view is focusable, request focus. This will automatically read
        // the the view's content description using TalkBack (if enabled).
        // if (view.requestFocusFromTouch()) {
        // return view.findFocus();
        // }

        // If the view is not focusable, force it to send an AccessibilityEvent.
        // TODO(alanv): This seems to retain the contentDescription from a
        // previous event.
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);

        return view;
    }

    private final FlickGestureListener gestureListener = new FlickGestureListener() {
        @Override
        protected boolean onSingleTap(float x, float y, float rawX, float rawY) {
            setSelectionAtPoint((int) x, (int) y);

            return true;
        }

        @Override
        protected boolean onDoubleTap(float x, float y, float rawX, float rawY) {
            tapSelectedView();

            return true;
        }

        @Override
        protected boolean onFlick(float x, float y, float rawX, float rawY, int direction) {
            changeFocus(direction);

            return true;
        }

        @Override
        protected boolean onMove(float x, float y, float rawX, float rawY) {
            setSelectionAtPoint((int) x, (int) y);

            return true;
        }
    };

    private final TextToSpeech.OnInitListener ttsInit = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                mSpeechAvailable = true;
            }
        }
    };

    private final ViewTreeObserver.OnGlobalFocusChangeListener focusChangeListener =
            new ViewTreeObserver.OnGlobalFocusChangeListener() {
                @Override
                public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                    if (newFocus != null) {
                        setSelectedView(newFocus, false);
                    }
                }
            };
}
