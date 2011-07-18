package com.todoroo.astrid.UI_interaction;

import java.util.ArrayList;
import java.util.List;

import android.app.Instrumentation;
import android.os.Parcel;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ListView;

import com.todoroo.astrid.activity.TaskListActivity;
import com.todoroo.astrid.demonstration.DemonstrationService;
import com.todoroo.astrid.voice.VoiceOutputService;
import com.todoroo.astrid.voice.VoiceOutputService.VoiceOutputAssistant;

public class UI_playback extends ActivityInstrumentationTestCase2<TaskListActivity> {

    private final float xOffset = 0;
    private final float yOffset = 0;

    private TaskListActivity mActivity;
    private DemonstrationService.DemonstrationBinder mBinder;
    private Instrumentation inst;
    private VoiceOutputAssistant voiceOutput;
    private View focusedView;
    private int lastFocusedIndex;
    List<CharSequence> texts;

    public UI_playback() {
        super("com.todoroo.astrid.activity.TaskListActivity", TaskListActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
        voiceOutput = VoiceOutputService.getVoiceOutputInstance();
        voiceOutput.checkIsTTSInstalled();
        mBinder = mActivity.getBinder();
    }

    public void test_UI() throws InterruptedException {

        inst = getInstrumentation();

        // Testing
        //this.readScreen();
        //return;


        while (true){
            Thread.sleep(1000);

            // code for polling the binder parcel
            Parcel reply = Parcel.obtain();
            try {
                mBinder.transact(DemonstrationService.TEST_EVENT_CODE, null, reply, 0);
            }
            catch (Exception e){
                System.out.println("way to screw up");
            }

            List<MotionEvent> evList = new ArrayList<MotionEvent>();
            reply.readList(evList, MotionEvent.class.getClassLoader());

            // used to see the time difference between events
            long lastEventTime = -1;

            boolean eventsDispatched = false;

            int parcel_size = evList.size();
            for (int i = 0; i < parcel_size; i++){
                MotionEvent currentEvent = evList.get(i);
                System.out.println("Playback event x = " + currentEvent.getX() + " y = " + currentEvent.getY());
                currentEvent.offsetLocation(xOffset, yOffset);
                inst.sendPointerSync(currentEvent);
                long waitDuration = currentEvent.getEventTime()-lastEventTime;
                System.out.println("wait duration = " + waitDuration);
                if (lastEventTime != -1 && waitDuration > 0){
                    Thread.sleep(waitDuration);
                }
                lastEventTime = currentEvent.getEventTime();
                eventsDispatched = true;
            }

            if (eventsDispatched){
                this.readScreen();
            }

        }
    }

    protected void onDestroy (){
        voiceOutput.onDestroy();
    }

    protected void readScreen() throws InterruptedException{

        Thread.sleep(500);

        System.out.println("Reading screen.");

        // go up and left as long as focus changes to init screen?

        boolean focusChanged = true;

        // starting action determines what views get left out
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);

        View newFocusedView = mActivity.getCurrentFocus();
        System.out.println(newFocusedView.toString());
        if (!ListView.class.isInstance(newFocusedView)) return;

        // still misses some views on the side
        while (focusChanged){
            this.readFocusedItem();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            focusChanged = this.isFocusChanged();
        }

//        focusChanged = true;
//
//        // go up and left as long as focus changes
//        while (focusChanged){
//            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
//            focusChanged = this.isFocusChanged();
//        }
    }

    protected void readFocusedItem() throws InterruptedException{
        focusedView = mActivity.getCurrentFocus();
        String labelText = "no description";

        AccessibilityEvent event = AccessibilityEvent.obtain();
        focusedView.dispatchPopulateAccessibilityEvent(event);



        if (event != null){
            lastFocusedIndex = event.getCurrentItemIndex();
            texts = event.getText();
            int sizeOfTexts = texts.size();
            for (int i = 0; i < sizeOfTexts; i++){
                labelText = texts.get(i).toString();
                voiceOutput.queueSpeak(labelText);
                Thread.sleep(500);
            }
        }
    }

    protected boolean isFocusChanged(){
        View newFocusedView = mActivity.getCurrentFocus();
        AccessibilityEvent event = AccessibilityEvent.obtain();
        newFocusedView.dispatchPopulateAccessibilityEvent(event);

        int newFocusedIndex = event.getCurrentItemIndex();
        List<CharSequence> newTexts = event.getText();

        return !texts.equals(newTexts);
        //return (lastFocusedIndex != newFocusedIndex || !texts.equals(newTexts));
    }
}
