package com.group08.ecse426finalproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new SpeechService(this, new BluetoothTransmitter()).sendDemoRequestBytes();
    }
}
