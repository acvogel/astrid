package com.todoroo.astrid.UI_interaction;

import android.app.Instrumentation;
import android.speech.tts.TextToSpeech;
import android.test.ActivityInstrumentationTestCase2;

import com.todoroo.astrid.activity.TaskListActivity;

public class UI_playback extends ActivityInstrumentationTestCase2<TaskListActivity> {

    private TaskListActivity mActivity;
    private TextToSpeech mTts;

    public UI_playback() {
        super("com.todoroo.astrid.activity.TaskListActivity", TaskListActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();

    }

    public void test_UI() throws InterruptedException {
        Thread.sleep(1000);

        //Test for change in a text box and carry out the command written in it

        // Open keyboard and accept voice input
        Instrumentation inst = getInstrumentation();

    }

}
