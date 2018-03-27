package com.group08.ecse426finalproject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

@TargetApi(18) //18 needed for BT manager
public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBTCompatibility();
        //start fragment...
    }

    //checks if BLE is supported in the current device
    public void checkBTCompatibility() {
        if
                (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE
        )) {
            Toast.makeText(this, R.string.ble_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
