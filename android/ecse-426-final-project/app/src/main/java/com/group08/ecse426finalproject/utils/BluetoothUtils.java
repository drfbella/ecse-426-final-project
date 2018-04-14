package com.group08.ecse426finalproject.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.IntentFilter;

import com.group08.ecse426finalproject.bluetooth.BluetoothActivity;
import com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT;

public class BluetoothUtils {
    public static boolean checkBluetooth(BluetoothAdapter bluetoothAdapter) {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }
        else {
            return true;
        }
    }

    // request to turn on bt by prompting user to change in settings
    public static void requestUserBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, BluetoothActivity.REQUEST_ENABLE_BT);
    }

    public static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Service_BTLE_GATT.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Service_BTLE_GATT.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Service_BTLE_GATT.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Service_BTLE_GATT.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    public static int hasWriteProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_WRITE;
    }

    public static int hasReadProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_READ;
    }

    public static int hasNotifyProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_NOTIFY;
    }
}
