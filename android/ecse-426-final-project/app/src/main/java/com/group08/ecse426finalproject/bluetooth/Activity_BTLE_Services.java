package com.group08.ecse426finalproject.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@TargetApi(18)
public class Activity_BTLE_Services extends AppCompatActivity implements ExpandableListView.OnChildClickListener {
    private final static String TAG = Activity_BTLE_Services.class.getSimpleName();

    public static final String EXTRA_NAME = "com.group08.ecse426finalproject.bluetooth.Activity_BTLE_Services.NAME";
    public static final String EXTRA_ADDRESS = "com.group08.ecse426finalproject.bluetooth.Activity_BTLE_Services.ADDRESS";

    /*
        ECSE-426-PROJECT SPECIFIC UUIDs
     */
    private UUID accelerometerUuid = null; // TODO: configure audio characteristic UUID
    private UUID audioUuid = null; // TODO: configure audio characteristic UUID



    // listView for services
    private ListAdapter_BTLE_Services expandableListAdapter;
    private ExpandableListView expandableListView;

    // mapping all available services
    private ArrayList<BluetoothGattService> services_ArrayList;
    private HashMap<String, BluetoothGattCharacteristic> characteristics_HashMap;
    private HashMap<String, ArrayList<BluetoothGattCharacteristic>> characteristics_HashMapList;

    private Intent mBTLE_Service_Intent;
    private Service_BTLE_GATT mBTLE_Service;
    private boolean mBTLE_Service_Bound;
    private BroadcastReceiver_BTLE_GATT mGattUpdateReceiver;

    private String name;
    private String address;

    // service connection
    private ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Service_BTLE_GATT.BTLeServiceBinder binder = (Service_BTLE_GATT.BTLeServiceBinder) service;
            mBTLE_Service = binder.getService();
            mBTLE_Service_Bound = true;

            if (!mBTLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mBTLE_Service.connect(address);

            // Automatically connects to the device upon successful start-up initialization. //TODO: review
//            mBTLeService.connect(mBTLeDeviceAddress);

//            mBluetoothGatt = mBTLeService.getmBluetoothGatt();
//            mGattUpdateReceiver.setBluetoothGatt(mBluetoothGatt);
//            mGattUpdateReceiver.setBTLeService(mBTLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
            mBTLE_Service_Bound = false;

//            mBluetoothGatt = null;
//            mGattUpdateReceiver.setBluetoothGatt(null);
//            mGattUpdateReceiver.setBTLeService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btle_services);

        Log.d(TAG, "onCreate");

        // retrieve name and address of device
        Intent intent = getIntent();
        name = intent.getStringExtra(Activity_BTLE_Services.EXTRA_NAME);
        address = intent.getStringExtra(Activity_BTLE_Services.EXTRA_ADDRESS);

        // map services and characteristics
        // characteristics will be displayed
        services_ArrayList = new ArrayList<>();
        characteristics_HashMap = new HashMap<>();
        characteristics_HashMapList = new HashMap<>();

        // list view for services
        expandableListAdapter = new ListAdapter_BTLE_Services(
                this, services_ArrayList, characteristics_HashMapList);

        expandableListView = findViewById(R.id.lv_expandable);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener(this);

        ((TextView) findViewById(R.id.tv_name)).setText(name + " Services");
        ((TextView) findViewById(R.id.tv_address)).setText(address);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // event handler
        mGattUpdateReceiver = new BroadcastReceiver_BTLE_GATT(this);
        registerReceiver(mGattUpdateReceiver, BluetoothUtils.makeGattUpdateIntentFilter());

        // BTLE_GATT service
        mBTLE_Service_Intent = new Intent(this, Service_BTLE_GATT.class);
        bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mBTLE_Service_Intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mBTLE_ServiceConnection);
        mBTLE_Service_Intent = null;
    }

    /*
        Sets up activity for chosen characteristics //TODO: review
     */
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        BluetoothGattCharacteristic characteristic = characteristics_HashMapList.get(
                services_ArrayList.get(groupPosition).getUuid().toString())
                .get(childPosition);

        // TODO: implement fire base connection here, for read and write

        // write-able property
        if (BluetoothUtils.hasWriteProperty(characteristic.getProperties()) != 0) {
            String uuid = characteristic.getUuid().toString();

            Dialog_BTLE_Characteristic dialog_btle_characteristic = new Dialog_BTLE_Characteristic();

            dialog_btle_characteristic.setTitle(uuid);
            dialog_btle_characteristic.setService(mBTLE_Service);
            dialog_btle_characteristic.setCharacteristic(characteristic);

            dialog_btle_characteristic.show(getFragmentManager(), "Dialog_BTLE_Characteristic");
        } else if (BluetoothUtils.hasReadProperty(characteristic.getProperties()) != 0) {
            if (mBTLE_Service != null) {
                mBTLE_Service.readCharacteristic(characteristic);
            }
        } else if (BluetoothUtils.hasNotifyProperty(characteristic.getProperties()) != 0) {
            if (mBTLE_Service != null) {
                mBTLE_Service.setCharacteristicNotification(characteristic, true);
            }
        }

        return false;
    }

    public void updateServices() {

        if (mBTLE_Service != null) {

            services_ArrayList.clear();
            characteristics_HashMap.clear();
            characteristics_HashMapList.clear();

            List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                services_ArrayList.add(service);

                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                for (BluetoothGattCharacteristic characteristic: characteristicsList) {
                    characteristics_HashMap.put(characteristic.getUuid().toString(), characteristic);
                    newCharacteristicsList.add(characteristic);
                }

                characteristics_HashMapList.put(service.getUuid().toString(), newCharacteristicsList);
            }

            if (servicesList != null && servicesList.size() > 0) {
                expandableListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void updateCharacteristic() {
        expandableListAdapter.notifyDataSetChanged();
    }


    /**
     *  Used to obtain a specific characteristic based on String input...
     * @param uuid  of the specific characteristic to be read/written
     * @return BluetoothGattCharacteristic used to read or manipulate
     */
    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();
        for(BluetoothGattService service : servicesList) {
            List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
            for(BluetoothGattCharacteristic c : characteristicsList){
                if(c.getUuid() == uuid){
                    return c;
                }
            }
        }
        Log.d(TAG, "No UUID found for " + uuid.toString());
        return null;
    }

    public byte[] readAudio(){
        BluetoothGattCharacteristic audioCharacteristic = getCharacteristic(audioUuid);
        if(audioCharacteristic == null) {
            Log.d(TAG, "Unable to read audio.");
            return null;
        }
        return audioCharacteristic.getValue();
    }

    public byte[] readAccelerometer() {
        BluetoothGattCharacteristic accelerometerCharacteristic = getCharacteristic(accelerometerUuid);
        if(accelerometerCharacteristic == null) {
            Log.d(TAG, "Unable to read accelerometer value.");
            return null;
        }
        return accelerometerCharacteristic.getValue();
    }

    public void writeAccelerometer(byte[] data){
        BluetoothGattCharacteristic accelerometerCharacteristic = getCharacteristic(accelerometerUuid);
        if(accelerometerCharacteristic == null){
            Log.d(TAG, "Unable to read accelerometer value.");
            return;
        }
        accelerometerCharacteristic.setValue(data);
        mBTLE_Service.writeCharacteristic(accelerometerCharacteristic);
        // TODO: need to send the data too?
    }
}
