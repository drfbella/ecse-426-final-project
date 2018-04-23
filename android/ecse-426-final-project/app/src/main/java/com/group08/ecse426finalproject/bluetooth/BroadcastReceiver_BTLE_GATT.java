package com.group08.ecse426finalproject.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.group08.ecse426finalproject.utils.ByteManipulator;
import com.group08.ecse426finalproject.utils.ToastShower;

import java.util.Arrays;
import java.util.UUID;

public class BroadcastReceiver_BTLE_GATT extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";

    private Activity_BTLE_Services activity;
    private ByteManipulator byteManipulator;

    public BroadcastReceiver_BTLE_GATT(Activity_BTLE_Services activity) {
        this.activity = activity;
        byteManipulator = new ByteManipulator();
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (Service_BTLE_GATT.ACTION_GATT_DISCONNECTED.equals(action)) {
            ToastShower.showToast(activity.getApplicationContext(), "Disconnected From Device");
            activity.finish();
        }
        else if (Service_BTLE_GATT.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            activity.updateServices();
        }
        else if (Service_BTLE_GATT.ACTION_DATA_AVAILABLE.equals(action)) {
            byte[] byteArray = intent.getByteArrayExtra(Service_BTLE_GATT.EXTRA_DATA);
            if (byteArray != null) {
                int[] unsignedByteArray = byteManipulator.toUnsignedArray(byteArray);
                Log.d("ON RECEIVE DEBUG: " , intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID));
                if(intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID).equals(UUID.fromString(Activity_BTLE_Services.AUDIO_CHARACTERISTIC_UUID).toString())){
                    Log.d(TAG, "Received audio data: " + Arrays.toString(unsignedByteArray));
                    activity.updateSpeechData(byteArray);
                } else if(intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID).equals(UUID.fromString(Activity_BTLE_Services.PITCH_CHARACTERISTIC_UUID).toString())){
                    Log.d(TAG, "Received pitch data: " + Arrays.toString(unsignedByteArray));
                    activity.updatePitchData(byteArray);
                } else if(intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID).equals(UUID.fromString(Activity_BTLE_Services.ROLL_CHARACTERISTIC_UUID).toString())){
                    Log.d(TAG, "Received roll data: " + Arrays.toString(unsignedByteArray));
                    activity.updateRollData(byteArray);
                }
                activity.updateCharacteristic();
            }
        }
    }
}
