package com.group08.ecse426finalproject.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.utils.ToastShower;

import java.util.ArrayList;
import java.util.HashMap;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

@TargetApi(18) //18 needed for BT manager
public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final static String TAG = BluetoothActivity.class.getSimpleName();

    //permission constants
    public final static int REQUEST_ENABLE_BT = 1;
    public final static int BTLE_SERVICES = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    private final static long SCAN_PERIOD = 7500; // scanning period in ms
    private final static int SIGNAL_STRENGTH = -75; // signal strength, may need to modify
    // if signal strength is low and not recognizable

    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTLEDeviceArrayList;
    private ListAdapter_BTLE_Devices adapter;

    private byte[] pitchData = new byte[]{};
    private byte[] rollData = new byte[]{};
    private byte[] speechData = new byte[]{};
    private Button scanButton;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // use this to check BLE support and location access for SDK version >= 23
        checkBTCompatibility();
        requestLocation();

        // create ble device mapping
        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, SCAN_PERIOD, SIGNAL_STRENGTH);
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

    public void checkBTCompatibility() {
        if
                (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE
        )) {
            ToastShower.showToast(this, "Bluetooth not supported with this device. GG");
            finish();
        }
    }

    /**
     * Called when an item in the ListView is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Used in future BLE tutorials
        ToastShower.showToast(getApplicationContext(), "you clicked on" + mBTLEDeviceArrayList.get(position).getName());

        stopScan();

        String name = mBTLEDeviceArrayList.get(position).getName();
        String address = mBTLEDeviceArrayList.get(position).getAddress();

        Intent intent = new Intent(this, Activity_BTLE_Services.class);
        intent.putExtra(Activity_BTLE_Services.EXTRA_NAME, name);
        intent.putExtra(Activity_BTLE_Services.EXTRA_ADDRESS, address);
        Log.d(TAG, "clicked on a device");
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
                ToastShower.showToast(getApplicationContext(), "Scan Button Pressed");

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
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra(PITCH_DATA_NAME, pitchData);
        i.putExtra(ROLL_DATA_NAME, rollData);
        i.putExtra(SPEECH_DATA_NAME, speechData);
        setResult(RESULT_OK, i);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //checks if BLE is supported in the current device

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ToastShower.showToast(getApplicationContext(), "Thank you for turning on Bluetooth");
            }
            else if (resultCode == RESULT_CANCELED) {
                ToastShower.showToast(getApplicationContext(), "Please turn on bluetooth.");
            }
        }
        else if (requestCode == BTLE_SERVICES){
            if (data != null) {
                pitchData = data.getByteArrayExtra(PITCH_DATA_NAME);
                rollData = data.getByteArrayExtra(ROLL_DATA_NAME);
                speechData = data.getByteArrayExtra(SPEECH_DATA_NAME);
            }
            Log.d(TAG, "onActivityResult for BluetoothActivity");
        }
    }
}
