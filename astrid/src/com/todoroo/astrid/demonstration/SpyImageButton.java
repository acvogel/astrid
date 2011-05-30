// attempt to make an imagebutton which can capture touch events
package com.todoroo.astrid.demonstration;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;



// hmmm, connect to service. but do i need to be an activity for that ??? hmmm.
public class SpyImageButton extends ImageButton {
  public String LOG_STRING = "SpyImageButton";

  // keep around a list of motionevents, then serialize them all 
  public List<MotionEvent> mMotionEventList = new ArrayList<MotionEvent>();

  public SpyImageButton(Context context) { 
    super(context);
  }

  public SpyImageButton(Context context, AttributeSet attrs) {
    super(context, attrs); 
  }

  public SpyImageButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    Log.i(LOG_STRING, "Motion event: " + event.toString());
    MotionEvent me = MotionEvent.obtain(event);
    mMotionEventList.add(me);
    return super.dispatchTouchEvent(event);
  }

}
