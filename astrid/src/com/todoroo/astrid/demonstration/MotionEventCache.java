//package com.android.example.spinner;
package com.todoroo.astrid.demonstration;

import java.io.Serializable;
import android.view.MotionEvent;

// want to save and read in an array of these values. one set per line, make a save struct that overrides serializeable (eventcache or something)
// see what it is for keypress and see if you can replicate it. then you can serialize a container in a straightforward manner? check this first.
//public static MotionEvent obtain (long downTime, long eventTime, int action, int pointers, float x, float y, float pressure, float size, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags)
public class MotionEventCache implements Serializable {

  public long downTime;
  public long eventTime;
  public int action;
  public float x;
  public float y;
  public float pressure;
  public float size;
  public int metaState;
  public float xPrecision;
  public float yPrecision;
  public int deviceId;
  public int edgeFlags;    

  public MotionEventCache() { }

  public MotionEventCache(MotionEvent ev) {
    downTime = ev.getDownTime();
    eventTime = ev.getEventTime();
    action = ev.getAction();
    x = ev.getX();
    y = ev.getY();
    pressure = ev.getPressure();
    size = ev.getSize();
    metaState = ev.getMetaState();
    xPrecision = ev.getXPrecision();
    yPrecision = ev.getYPrecision();
    deviceId = ev.getDeviceId();
    edgeFlags = ev.getEdgeFlags();
  }
  
  // returns a MotionEvent version of this object
  public MotionEvent obtain() {
    MotionEvent ev = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags);
    return ev;
  }
}
