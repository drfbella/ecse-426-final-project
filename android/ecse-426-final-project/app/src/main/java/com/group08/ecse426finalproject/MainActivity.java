package com.group08.ecse426finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.group08.ecse426finalproject.accelerometer.AccelerometerActivity;
import com.group08.ecse426finalproject.bluetooth.Activity_BTLE_Services;
import com.group08.ecse426finalproject.bluetooth.BluetoothActivity;
import com.group08.ecse426finalproject.speech.SpeechActivity;
import com.group08.ecse426finalproject.utils.BluetoothUtils;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int BLUETOOTH_ACTIVITY_REQUEST_CODE = 2;
    private static byte[] pitchData = new byte[]{};
    private static byte[] rollData = new byte[]{};
    private static byte[] speechData = new byte[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonBluetooth = findViewById(R.id.button_bluetooth);
        Button buttonSpeech = findViewById(R.id.button_speech);
        Button buttonAccelerometer = findViewById(R.id.button_accelerometer);

        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivityForResult(i, BLUETOOTH_ACTIVITY_REQUEST_CODE);
            }
        });

        buttonSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SpeechActivity.class);
                i.putExtra(SPEECH_DATA_NAME, speechData);
                startActivity(i);
            }
        });

        buttonAccelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AccelerometerActivity.class);
                i.putExtra(PITCH_DATA_NAME, pitchData);
                i.putExtra(ROLL_DATA_NAME, rollData);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                pitchData = data.getByteArrayExtra(PITCH_DATA_NAME);
                rollData = data.getByteArrayExtra(ROLL_DATA_NAME);
                speechData = data.getByteArrayExtra(SPEECH_DATA_NAME);
                Log.d(TAG, "speechData value is: " + new String(speechData));
//                Log.d(TAG, "onActivityResult in MainActivity");
            }
        }
    }
}
