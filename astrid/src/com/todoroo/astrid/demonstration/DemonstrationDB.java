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


/** Holds a collection of Demonstrations. Provides capability for editing, accessing, and storing. */
public class DemonstrationDB implements Serializable {
  static final long serialVersionUID = -4777430298940145978L;
  
  public static final String LOG_STRING = "DemonstrationDB";

  public Set<Demonstration> mDemonstrations;

  public DemonstrationDB() {
    mDemonstrations = new HashSet<Demonstration>();
  }

  /** Looks up the demonstration for a given voice command. */
  public Demonstration lookupDemonstration(String command) {
    // TODO: loop through all demonstrations, find the one with maximal word overlap.
    return null;
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
