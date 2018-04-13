package com.group08.ecse426finalproject.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.group08.ecse426finalproject.utils.BluetoothUtils;

import java.util.UUID;

public class BroadcastReceiver_BTLE_GATT extends BroadcastReceiver {

    private boolean mConnected = false;

    private Activity_BTLE_Services activity;

    public BroadcastReceiver_BTLE_GATT(Activity_BTLE_Services activity) {
        this.activity = activity;
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

        if (Service_BTLE_GATT.ACTION_GATT_CONNECTED.equals(action)) {
            mConnected = true;
        }
        else if (Service_BTLE_GATT.ACTION_GATT_DISCONNECTED.equals(action)) {
            mConnected = false;
            BluetoothUtils.toast(activity.getApplicationContext(), "Disconnected From Device");
            activity.finish();
        }
        else if (Service_BTLE_GATT.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            activity.updateServices();
        }
        else if (Service_BTLE_GATT.ACTION_DATA_AVAILABLE.equals(action)) {

            byte[] byteArray = intent.getByteArrayExtra(Service_BTLE_GATT.EXTRA_DATA);

            Log.d("ON RECEIVE DEBUG: " , intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID));
            if(intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID).equals(UUID.fromString(Activity_BTLE_Services.audioCharacteristicUUID).toString())){
                Log.d("ON RECEIVE DEBUG: " , new String(byteArray));
                activity.updateSpeechData(byteArray); //TODO: store in byte array?
            } else if(intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID).equals(UUID.fromString(Activity_BTLE_Services.accelerometerPitchUUID).toString())){
                activity.updatePitchData(byteArray);
            } else if(intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID).equals(UUID.fromString(Activity_BTLE_Services.accelerometerRollUUID).toString())){
                activity.updateRollData(byteArray);
            }

            activity.updateCharacteristic();
        }
        return;
    }
}
