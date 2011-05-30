package com.todoroo.astrid.UI_interaction;

import java.util.ArrayList;
import java.util.List;

import android.app.Instrumentation;
import android.os.Parcel;
import android.speech.tts.TextToSpeech;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;

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

            try {
                mBinder.transact(DemonstrationService.TEST_EVENT_CODE, null, reply, 0);
            }
            catch (Exception e){
                System.out.println("way to fuck up");
            }

            List<MotionEvent> evList = new ArrayList<MotionEvent>();
            reply.readList(evList, MotionEvent.class.getClassLoader());

            Instrumentation inst = getInstrumentation();

            int parcel_size = evList.size();
            for (int i = 0; i < parcel_size; i++){
                inst.sendPointerSync(evList.get(i));
                Thread.sleep(1000);
            }
        }
    }

}
