package com.group08.ecse426finalproject.accelerometer;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.group08.ecse426finalproject.R;

import java.util.Arrays;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;

public class AccelerometerActivity extends AppCompatActivity {
    private static final String TAG = "AccelerometerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        byte[] pitchData = getIntent().getByteArrayExtra(PITCH_DATA_NAME);
        byte[] rollData = getIntent().getByteArrayExtra(ROLL_DATA_NAME);
        Log.d(TAG, "Received pitc data: " + Arrays.toString(pitchData));
        Log.d(TAG, "Received roll data: " + Arrays.toString(rollData));
    }
}
