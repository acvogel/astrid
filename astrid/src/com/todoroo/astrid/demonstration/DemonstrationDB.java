package com.todoroo.astrid.demonstration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.io.Serializable;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import android.view.KeyEvent;
import android.view.MotionEvent;

/*
back button:
I/AcessibleFrameLayout( 7774): key event: KeyEvent{action=0 code=4 repeat=0 meta=0 scancode=158 mFlags=72}
I/AcessibleFrameLayout( 7774): key event: KeyEvent{action=1 code=4 repeat=0 meta=0 scancode=158 mFlags=72}

microphone press:

*/


/** Holds a collection of Demonstrations. Provides capability for editing, accessing, and storing. */
public class DemonstrationDB implements Serializable {
  static final long serialVersionUID = -4777430298940145978L;
  
  public static final String LOG_STRING = "DemonstrationDB";

  public Set<Demonstration> mDemonstrations;

  //public Demonstration mVoicePress; // hardcoded sequence to press the voice button

  //public Demonstration mBackPress; // hardcoded sequence to hit the back button ?

  public DemonstrationDB() {
    mDemonstrations = new HashSet<Demonstration>();
  }

  /** Looks up the demonstration for a given voice command.
    * Loops through all demonstrations, returning the one with maximal overlap to command.
    */
  public Demonstration parseCommand(String command) {
    Demonstration argMax = null;
    int maxScore = -100000; 
    for(Demonstration demonstration : mDemonstrations) {
      int score = demonstration.overlapScore(command);
      if(score > maxScore) {
        maxScore = score;
        argMax = demonstration;
      }
    }
    return argMax;
  }

  /** Adds a new demonstration to the DB. */
  public void addDemonstration(Demonstration demonstration) {
    mDemonstrations.add(demonstration);
    return;
  }


  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    Integer length = mDemonstrations.size();
    out.writeObject(length);
    Log.i(LOG_STRING, "Write length: " + length);
    for(Demonstration demonstration: mDemonstrations) {
      Log.i(LOG_STRING, demonstration.toString());
      out.writeObject(demonstration);
    }
    return;
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Integer length = (Integer) in.readObject();
    Log.i(LOG_STRING, "Read length: " + length);
    mDemonstrations = new HashSet<Demonstration>();
    for(int i = 0; i < length; i++) {
      Demonstration demonstration = (Demonstration) in.readObject();
      mDemonstrations.add(demonstration);
    }
    return;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("DemonstrationDB has " + mDemonstrations.size() + " entries.\n");
    for(Demonstration demonstration : mDemonstrations) {
      sb.append(demonstration.toString() + "\n");
    }
    return sb.toString();
  }
}
