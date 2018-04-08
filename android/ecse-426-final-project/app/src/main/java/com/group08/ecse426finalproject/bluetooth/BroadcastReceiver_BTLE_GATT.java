package com.group08.ecse426finalproject.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.group08.ecse426finalproject.utils.BluetoothUtils;

public class BroadcastReceiver_BTLE_GATT extends BroadcastReceiver {

    private boolean mConnected = false;

    private Activity_BTLE_Services activity;

    public BroadcastReceiver_BTLE_GATT(Activity_BTLE_Services activity) {
        this.activity = activity;
    }

    public String data;
    public String uuid;
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

             uuid = intent.getStringExtra(Service_BTLE_GATT.EXTRA_UUID); // TODO: this stores the values to the appropriate characterstics...
             data = intent.getStringExtra(Service_BTLE_GATT.EXTRA_DATA);
            Log.d("DEBUG CHECK ONRECEIVE", data);
//            speechData = intent.getByteArrayExtra(Service_BTLE_GATT.EXTRA_DATA); //TODO: store in byte array?

            activity.updateCharacteristic();
        }

        return;
    }
}
