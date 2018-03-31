package com.group08.ecse426finalproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

@TargetApi(18) //18 needed for BT manager
public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private final static String TAG = MainActivity.class.getSimpleName();

    //permission constants
    public final static int REQUEST_ENABLE_BT = 1;
    public final static int BTLE_SERVICES = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTLEDeviceArrayList;
    private ListAdapter_BTLE_Devices adapter;

    private long scanPeriod = 7500; // scanning period in ms
    private int signalStrength = -75; // signal strength, may need to modify
                                      // if signal strength is low and not recognizable

    BluetoothAdapter mBluetoothAdapter;

    private Button scanButton;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // use this to check BLE support and location access for SDK version >= 23
        checkBTCompatibility();
        requestLocation();

        // create ble device mapping
        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, scanPeriod, signalStrength);
        mBTDevicesHashMap = new HashMap<>();
        mBTLEDeviceArrayList = new ArrayList<>();

        // create listview for bluetooth...
        adapter = new ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTLEDeviceArrayList);

        //ListView to UI
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        scanButton = findViewById(R.id.scanButton);
        ((ScrollView)findViewById(R.id.scrollView)).addView(listView);
        scanButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();

    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            }
            else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on bluetooth.");
            }
        }
        else if (requestCode == BTLE_SERVICES){

        }
    }

    //checks if BLE is supported in the current device
    public void checkBTCompatibility() {
        if
                (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE
        )) {
            Utils.toast(this, "Bluetooth not supported with this device. GG");
            finish();
        }
    }

    /**
     * Called when an item in the ListView is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Used in future BLE tutorials
        Utils.toast(getApplicationContext(), "you clicked on" + mBTLEDeviceArrayList.get(position).getName());

        stopScan();

        /**
         * prepare bundle for new BTLE services Activity
         */
        String name = mBTLEDeviceArrayList.get(position).getName();
        String address = mBTLEDeviceArrayList.get(position).getAddress();

        Intent intent = new Intent(this, Activity_BTLE_Services.class);
        intent.putExtra(Activity_BTLE_Services.EXTRA_NAME, name);
        intent.putExtra(Activity_BTLE_Services.EXTRA_ADDRESS, address);
        startActivityForResult(intent, BTLE_SERVICES);
    }

    /**
     * Called when the scan button is clicked.
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.scanButton:
                Utils.toast(getApplicationContext(), "Scan Button Pressed");

                if(!mBTLeScanner.isScanning()) {
                    startScan();
                } else {
                    stopScan(); // stops scan if is already scanning
                }

                break;
            default:
                break;
        }
    }

    public void addDevice(BluetoothDevice device, int new_rssi) {

        String address = device.getAddress();
        if(!mBTDevicesHashMap.containsKey(address)) {
            BTLE_Device btle_device = new BTLE_Device(device);
            btle_device.setRSSI(new_rssi);

            mBTDevicesHashMap.put(address, btle_device);
            mBTLEDeviceArrayList.add(btle_device);
        }
        else {
            mBTDevicesHashMap.get(address).setRSSI(new_rssi);
        }
        adapter.notifyDataSetChanged(); // update
    }

    public void startScan(){
            scanButton.setText(R.string.scanning);

            mBTLEDeviceArrayList.clear();
            mBTDevicesHashMap.clear();

            adapter.notifyDataSetChanged();

            mBTLeScanner.start();
    }

    public void stopScan() {
        scanButton.setText(R.string.scan_again);

        mBTLeScanner.stop();
    }

    /**
     *     must request location if SDK >= 23
     *     Inspired from stackoverflow
     *     Availability: https://github.com/googlesamples/android-BluetoothLeGatt/issues/38
     *     date accessed: 03-30-2018
     */


    public void requestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }

//    private BluetoothGatt mGatt;
//
//    /**
//     * Inspiration from PART 2 of BLE Tutorial McGill PDF slides
//     */
//
//
//    /**
//     * Connection established, pass address to get device Gatt
//     * @param address of the selected device in the list
//     */
//
//    private void connectDevice(String address){
//        mBluetoothAdapter = mBTLeScanner.getBluetoothAdapter(); //obtain adapter from BTLeScanner
//
//        if(!mBluetoothAdapter.isEnabled()){
//            Utils.toast(getApplicationContext(), "Bluetooth is disabled...");
//            finish();
//        }
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        mGatt = device.connectGatt(getApplicationContext(), false, mCallback);
//        Log.d("BLE", "connectDevice");
//        Utils.toast(getApplicationContext(), "Selected device is " + mGatt.getDevice().getAddress());
//    }
//    private BluetoothGattCallback mCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//            switch(newState){
//                case BluetoothGatt.STATE_CONNECTED:
//                    //when connected, check services
//                    mGatt.discoverServices(); // Checks the services or characteristics on the device
//                    Log.d(TAG, "onConnectionStateChange");
//                    break;
//            }
//        }
//
//        /**
//         * called when services are discovered when connecting to a device
//         * @param gatt
//         * @param status
//         */
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status){
//            super.onServicesDiscovered(gatt, status);
//        }
//    };
}
