package com.todoroo.astrid.demonstration;

import android.view.View;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.*;
import android.speech.tts.*;
import android.speech.RecognitionListener;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.EditorInfo;
import android.util.Log;
import android.os.Bundle;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;


public class VoiceKeyboard extends InputMethodService {
  private final String LOG_STRING = "VoiceKeyboard";
  private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

  private SpeechRecognizer mSpeechRecognizer = null;
  private SpeechListener mSpeechListener = null;
  private TextToSpeech mTextToSpeech = null;


  public VoiceKeyboard() {
    super();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
    mSpeechListener = new SpeechListener();
    mSpeechRecognizer.setRecognitionListener(mSpeechListener);
    mTextToSpeech = new TextToSpeech(this.getApplicationContext(), null);
  }

  @Override
  public void onStartInputView (EditorInfo info, boolean restarting) {
    Log.i(LOG_STRING, "onStartInputView");  
    //Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    //    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    //            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    //    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
    //    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
    //    //startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    //    startActivity(intent);

    // XXX moved
    mTextToSpeech.speak("Speak!", 0, null);

    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
          intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
          intent.putExtra("calling_package","com.todoroo.astrid.demonstration.VoiceKeyboard");
          //mSpeechRecognizer.startListening(intent);
          //startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH);
          mSpeechRecognizer.startListening(intent);
    
  }

  class SpeechListener implements RecognitionListener {
      public void onBeginningOfSpeech() {
      }
      public void onBufferReceived(byte[] buffer) {}
      public void onEndOfSpeech() {
      }
      public void onError(int error) {
      }
      public void onEvent(int eventType, Bundle params) {
      }
      public void onPartialResults(Bundle partialResults) {}
      public void onReadyForSpeech(Bundle params) {
      }
      public void onResults(Bundle results) { // this is a key one ?
        ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // now display these to a layout element - how?
        //mTextView.setText("" + text.size() + " " + text.get(0));
        String result = text.get(0);
        Log.i(LOG_STRING, "ASR result: " + result);
        mTextToSpeech.speak(result, 0, null);

        InputConnection ic = getCurrentInputConnection();
        ic.deleteSurroundingText(1000,1000);
        ic.commitText(result, 1);
        ic.finishComposingText();
      }
      public void onRmsChanged(float rmsdB) {
      }
    }

    @Override
    public View onCreateInputView (){

      Log.i(LOG_STRING, "on create input view");
      return null;
    }

    public void onWindowShown () {
      Log.i(LOG_STRING, "onWindowShown()");
    //mTextToSpeech.speak("Speak!", 0, null);

    //Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    //      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    //              RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    //      intent.putExtra("calling_package","com.todoroo.astrid.demonstration.VoiceKeyboard");
    //      //mSpeechRecognizer.startListening(intent);
    //      //startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH);
    //      mSpeechRecognizer.startListening(intent);

    //  hideWindow (); // hide after you do it
    }

}
