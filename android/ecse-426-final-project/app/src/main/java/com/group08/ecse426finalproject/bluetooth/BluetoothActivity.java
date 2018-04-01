package com.group08.ecse426finalproject.bluetooth;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.group08.ecse426finalproject.R;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;


public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    private byte[] pitchData = new byte[]{}; // TODO: Update accelerometer/speech data
    private byte[] rollData = new byte[]{};
    private byte[] speechData = new byte[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra(PITCH_DATA_NAME, pitchData);
        i.putExtra(ROLL_DATA_NAME, rollData);
        i.putExtra(SPEECH_DATA_NAME, speechData);
        setResult(RESULT_OK, i);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
