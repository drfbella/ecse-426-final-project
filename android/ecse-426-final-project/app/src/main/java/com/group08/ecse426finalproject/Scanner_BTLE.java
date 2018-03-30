package com.group08.ecse426finalproject;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

@TargetApi(21) // which one?

public class Scanner_BTLE {

    private MainActivity mMainActivity;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    BluetoothLeScanner mLeScanner;
    private ScanCallback mScanCallback;



    private long scanPeriod;
    private int signalStrength;

    public Scanner_BTLE(MainActivity mainActivity, long scanPeriod, int signalStrength) {
        mMainActivity = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        // fetches bluetooth adapter with the bluetooth manager

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScanning(){
        return mScanning;
    }
    public void start(){
        if (!Utils.checkBluetooth(mBluetoothAdapter)) {
            Utils.requestUserBluetooth(mMainActivity);
            mMainActivity.stopScan();
        }
        else {
            scanLeDevice(true);
        }
    }

    public void stop(){
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable){

        if(enable && !mScanning) {
            Utils.toast(mMainActivity.getApplicationContext(), "Started BLE scanning...");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.toast(mMainActivity.getApplicationContext(), "Stopping BLE Scanning...");

                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    mMainActivity.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);

            //api 18-20
//            if (Build.VERSION.SDK_INT < 21) {
//                mBluetoothAdapter.startLeScan(mLeScanCallback);
//            } else {
//                //api 21
//                // request BluetoothLeScanner if it hasn't been initialized yet
//                if (mLeScanner == null) mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//                // start scan in low latency mode
//                mLeScanner.startScan(new ArrayList<ScanFilter>(),
//                        new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
//            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    // adding devices to list...
    // scan call back, result

    private  BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            final int new_rssi = rssi; // rssi == signal strength
            if(rssi > signalStrength){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMainActivity.addDevice(device, new_rssi);
                    }
                });
            }
        }
    };
    public BluetoothAdapter getBluetoothAdapter(){
        return this.mBluetoothAdapter;
    }
}
