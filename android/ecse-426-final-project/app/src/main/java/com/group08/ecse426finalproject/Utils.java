package com.group08.ecse426finalproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.widget.Toast;

public class Utils {

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

    public static void toast(Context context, String string) {

        Toast toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        toast.show();
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

    /**
     *  Used to transfer byte[] to String if want to display the values in the app.
      * @param data in byte,
     * @return String value of the byte
     */
    public static String hexToString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length);

        for(byte byteChar : data) {
            sb.append(String.format("%02X ", byteChar));
        }

        return sb.toString();
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
