package com.group08.ecse426finalproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ResourceAccess resourceAccess = new ResourceAccess(this);
        new FirebaseService().uploadBytes(
                resourceAccess.readRawResourceBytes(R.raw.audio),
                "audio/demo.raw");
        new SpeechService(this, new BluetoothTransmitter(), resourceAccess).sendDemoRequestBytes();
    }
}
