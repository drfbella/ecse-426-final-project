package com.group08.ecse426finalproject.speech;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.group08.ecse426finalproject.R;

import java.util.Arrays;

import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

public class SpeechActivity extends AppCompatActivity {
    private static final String TAG = "SpeechActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        byte[] speechData = getIntent().getByteArrayExtra(SPEECH_DATA_NAME);
        Log.d(TAG, "Received speech data: " + Arrays.toString(speechData));
    }
}
