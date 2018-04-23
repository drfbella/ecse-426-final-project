package com.group08.ecse426finalproject.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import com.group08.ecse426finalproject.utils.BluetoothUtils;
import com.group08.ecse426finalproject.utils.ToastShower;

@TargetApi(21)
class Scanner_BTLE {

    private BluetoothActivity mBluetoothActivity;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private long scanPeriod;
    private int signalStrength;

    Scanner_BTLE(BluetoothActivity bluetoothActivity, long scanPeriod, int signalStrength) {
        mBluetoothActivity = bluetoothActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        // fetches bluetooth adapter with the bluetooth manager

        final BluetoothManager bluetoothManager =
                (BluetoothManager) bluetoothActivity.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    boolean isScanning(){
        return mScanning;
    }
    void start(){
        if (!BluetoothUtils.checkBluetooth(mBluetoothAdapter)) {
            BluetoothUtils.requestUserBluetooth(mBluetoothActivity);
            mBluetoothActivity.stopScan();
        }
        else {
            scanLeDevice(true);
        }
    }

    void stop(){
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable){

        if(enable && !mScanning) {
            ToastShower.showToast(mBluetoothActivity.getApplicationContext(), "Started BLE scanning...");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ToastShower.showToast(mBluetoothActivity.getApplicationContext(), "Stopping BLE Scanning...");

                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    mBluetoothActivity.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private  BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            final int new_rssi = rssi; // rssi == signal strength
            if(rssi > signalStrength){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothActivity.addDevice(device, new_rssi);
                    }
                });
            }
        }
    };
}
