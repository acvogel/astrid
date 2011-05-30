package com.todoroo.astrid.UI_interaction;

import android.speech.tts.TextToSpeech;
import android.test.ActivityInstrumentationTestCase2;

import com.todoroo.astrid.activity.TaskListActivity;
import com.todoroo.astrid.demonstration.DemonstrationService;

public class UI_playback extends ActivityInstrumentationTestCase2<TaskListActivity> {

    private TaskListActivity mActivity;
    private TextToSpeech mTts;
    private DemonstrationService.DemonstrationBinder mBinder;

    public UI_playback() {
        super("com.todoroo.astrid.activity.TaskListActivity", TaskListActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
        mBinder = mActivity.getBinder();

    }

    public void test_UI() throws InterruptedException {

        System.out.println(mBinder.toString());

        while (true){
            Thread.sleep(500);
            // code for polling the binder parcel
            Parcel reply = Parcel.obtain();
            mBinder.transact(DemonstrationService.TEST_EVENT_CODE, null, reply, 0);
            List<MotionEvent> evList; 
            reply.readList(evList, MotionEvent.class.getClassLoader());
        }
    }

}
