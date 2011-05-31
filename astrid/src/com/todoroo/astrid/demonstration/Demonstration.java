//package com.android.example.spinner;
package com.todoroo.astrid.demonstration;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;


import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Holds key presses and voice actions taken during a demonstration. */
public class Demonstration implements Serializable {
  static final long serialVersionUID = 7526472295622776147L;
  // serialization UID command. run from astrid/astrid/
  //serialver -classpath bin/classes/:/home/av/android/android-sdk-linux_x86/platforms/android-8/android.jar com.todoroo.astrid.demonstration.Demonstration 

  public final String LOG_STRING = "DEMONSTRATION";

  public final String MOTION_FILE = "MotionEvents.txt";

  // should instead do these with something else... a list of InputEvents ?

  public transient List<MotionEvent> mMotionEvents;
  public transient List<KeyEvent> mKeyEvents;

  public String mCommand; // the voice command

  //public transient int mLocation; // where we are in the demonstration. used for playback.

  //public List<Object> mInputEvents;

  /** Returns the next MotionEvent for playback and increments mLocation. 
      Returns null if we have reached the end.
    */
  /*
  public MotionEvent getNextEvent() {
    if(mLocation < mMotionEvents.size()) {
      MotionEvent ev = mMotionEvents.get(mLocation);
      mLocation++;
      return ev;
    } else {
      return null;
    }
  }
  */



  //File mExternalDir;

  public Demonstration () {
    mMotionEvents = new ArrayList<MotionEvent>();
    mKeyEvents = new ArrayList<KeyEvent>();
    //mLocation = 0;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    //sb.append("External directory: " + mExternalDir.toString() + "\n");
    sb.append(mMotionEvents.toString());
    sb.append(mKeyEvents.toString());
    return sb.toString();
  }


  // serialization
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    ArrayList<MotionEventCache> cache = new ArrayList<MotionEventCache>();
    for(MotionEvent ev : mMotionEvents) {
      MotionEventCache evc = new MotionEventCache(ev);
      cache.add(evc);
    }
    Integer length = cache.size();
    out.writeObject(length);
    for(int i = 0; i < length; i ++) {
      out.writeObject(cache.get(i));
    }
    //out.writeObject(cache);
    return;
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Integer length = (Integer) in.readObject();
    List<MotionEventCache> cache = new ArrayList<MotionEventCache>(); 
      //= (ArrayList<MotionEventCache>) in.readObject(); 
    for(int i = 0; i < length; i++) {
      MotionEventCache evc = (MotionEventCache) in.readObject();
      cache.add(evc);
    }

    mMotionEvents = new ArrayList<MotionEvent>();
    mKeyEvents = new ArrayList<KeyEvent>();
    for(MotionEventCache mec : cache) {
      MotionEvent ev = mec.obtain();
      mMotionEvents.add(ev);
    }
  }

  public void addMotionEvent(MotionEvent ev) {
    MotionEvent newMotionEvent = MotionEvent.obtain(ev);
    mMotionEvents.add(newMotionEvent);
  }

  public void addKeyEvent(KeyEvent ev) {
    KeyEvent newKeyEvent = new KeyEvent(ev);
    mKeyEvents.add(newKeyEvent);
  }

  public void setCommand(String command) {
    mCommand = command;
    return;
  }


  // XXX DEPRECATED CODE -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

  // XXX deprecated Parcel version XXX
  public ArrayList<MotionEvent> ParcelreadTouchFile(File file) {
    // make a byte stream reader, then unmarshal or whatever.
    Parcel parcel = Parcel.obtain();
    byte [] bytes;
    ArrayList<MotionEvent> motionEvents = null;
    try {
      FileInputStream fis = new FileInputStream(file);
      int nbits = (int) file.length();
      bytes = new byte[nbits];
      fis.read(bytes, 0 , bytes.length);
      //String str = new String(bytes);
      //Log.i(LOG_STRING, "Read in: " + str);

      Log.i(LOG_STRING, "size of bytes: " + bytes.length);
      parcel.unmarshall(bytes, 0, bytes.length);
      parcel.setDataPosition(0);
      Log.i(LOG_STRING, "size of data in parcel: " + parcel.dataSize());

      // XXX this is the problem.
      MotionEvent ev = parcel.readParcelable(MotionEvent.class.getClassLoader());
      Log.i(LOG_STRING, "read in MotionEvent: " + ev.toString());
      MotionEvent [] motionEventsArray = new MotionEvent[1];
      motionEventsArray[0] = ev;


      // XXX uncomment this for the full array reading
      ////Object [] objectArray = parcel.readParcelableArray(MotionEvent.class.getClassLoader());
      //Parcelable [] objectArray = parcel.readParcelableArray(MotionEvent.class.getClassLoader());
      ////Object [] objectArray = parcel.readParcelableArray(null);
      //Log.i(LOG_STRING, "size of objectArray: " + objectArray.length);
      //MotionEvent [] motionEventsArray = new MotionEvent[objectArray.length];
      //for(int i = 0; i < objectArray.length; i++) {
      //  motionEventsArray[i] = (MotionEvent) objectArray[i];
      //}
      motionEvents = new ArrayList<MotionEvent>(motionEventsArray.length);
      for(int i = 0; i < motionEventsArray.length; i++) {
        motionEvents.add(motionEventsArray[i]);
      }
      fis.close();
    } catch (Exception e) {
      Log.e(LOG_STRING, "Error reading byte stream: " + e.toString());
    }
    return motionEvents;
  }

    public Parcel buildParcel(ArrayList<MotionEvent> motionEvents) {
      Parcel parcel = Parcel.obtain();
      MotionEvent[] motionEventsArray =  new MotionEvent[motionEvents.size()];
      for(int i = 0; i < motionEvents.size(); i++) {
        motionEventsArray[i] = motionEvents.get(i);
      }

      // XXX changed to just write the first one. !!!
      //parcel.writeParcelableArray(motionEventsArray, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
      parcel.writeParcelable(motionEventsArray[0], 0);
      //motionEventsArray[0].writeToParcel(parcel, 0);
      return parcel;
    }

    public void writeParcel(Parcel parcel, FileOutputStream fos) {
      byte[] bytes = parcel.marshall();
      try {
        fos.write(bytes);
      } catch(Exception e) {
        Log.e(LOG_STRING, "Error writing byte stream: " + e.toString());
      }
      return; 
    }
    /*
    public void serializeMotionEvents() {
      if(mMotionEvents == null || mMotionEvents.size() == 0) return;
      try {
        Log.i(LOG_STRING, "Opening output directory: " + mExternalDir.toString());
        mTouchFile = new File(mExternalDir, DEMONSTRATION_FILE);
        mTouchStream = new FileOutputStream(mTouchFile);
        ObjectOutputStream oos = new ObjectOutputStream(mTouchStream);
        ArrayList<MotionEventCache> cache = new ArrayList<MotionEventCache>();
        for(MotionEvent ev : mMotionEvents) {
          MotionEventCache evc = new MotionEventCache(ev);
          Log.i(LOG_STRING, "Serializing motion event: " + ev.toString());
          cache.add(evc); 
        }
        oos.writeObject(cache);
        oos.close();
      } catch (Exception e) {
        Log.e(LOG_STRING, "Error opening touch output: " + e.toString());
      }
      Log.i(LOG_STRING, "serializing motion events of size: " + mMotionEvents.size());
    }
    */
/*
    public void unserializeMotionEvents() {
      mMotionEvents = new ArrayList<MotionEvent>();
      try {
        Log.i(LOG_STRING, "Opening input directory: " + mExternalDir.toString());
        mTouchFile = new File(mExternalDir, DEMONSTRATION_FILE);
        if(mTouchFile != null && mTouchFile.isFile()) {
          mMotionEvents = readTouchFile(mTouchFile);
          Log.i(LOG_STRING, "Yes serialized motions to read: " + mMotionEvents.size());
          for(MotionEvent me : mMotionEvents) {
            Log.i(LOG_STRING, "Read motion event: " + me.toString());
          }
        } else {
          Log.i(LOG_STRING, "No serialized motions to read.");
          mMotionEvents = new ArrayList<MotionEvent>();
        }
      } catch (Exception e) {
         Log.e(LOG_STRING, "Error opening touch input: " + e.toString());
      }

    }
*/
}
