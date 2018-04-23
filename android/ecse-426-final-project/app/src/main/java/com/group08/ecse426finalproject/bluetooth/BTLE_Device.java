package com.group08.ecse426finalproject.bluetooth;

import android.bluetooth.BluetoothDevice;

public class BTLE_Device {

    private BluetoothDevice bluetoothDevice;
    private int rssi;

    BTLE_Device(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    void setRSSI(int rssi) {
        this.rssi = rssi;
    }

    int getRSSI() {
        return rssi;
    }
}
