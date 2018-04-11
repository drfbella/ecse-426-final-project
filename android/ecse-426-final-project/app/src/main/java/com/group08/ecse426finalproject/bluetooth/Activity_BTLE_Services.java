package com.group08.ecse426finalproject.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

@TargetApi(18)
public class Activity_BTLE_Services extends AppCompatActivity implements ExpandableListView.OnChildClickListener {
    private final static String TAG = Activity_BTLE_Services.class.getSimpleName();

    public static final String EXTRA_NAME = "com.group08.ecse426finalproject.bluetooth.Activity_BTLE_Services.NAME";
    public static final String EXTRA_ADDRESS = "com.group08.ecse426finalproject.bluetooth.Activity_BTLE_Services.ADDRESS";

    /*
        ECSE-426-PROJECT SPECIFIC UUIDs
     */
    public static final  String audioCharacteristicUUID = "E893D43D-C166-4E77-9ECF-6F6F81D76006"; // TODO: configure audio characteristic UUID
    public static final  String audioServiceUUID = "7E12324C-4323-403F-AD58-85ED7D218CAC"; // TODO: configure audio service UUID
    public static final  String accelerometerPitchUUID = "hi"; //TODO: configure accelerometer characteristic for pitch UUID
    public static final  String accelerometerRollUUID = "yo"; //TODO: configure accelerometer characteristic for roll UUID
    public static final  String accelerometerServiceUUID = "hii"; //TODO: configure accelerometer service UUID

    private byte[] pitchData = new byte[]{}; // TODO: Update accelerometer/speech data
    private byte[] rollData = new byte[]{};
    private byte[] speechData = new byte[]{};


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

    Button buttonStoreValues;

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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
            mBTLE_Service_Bound = false;
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

        buttonStoreValues = findViewById(R.id.btn_storeValues);
        buttonStoreValues.setVisibility(View.INVISIBLE);
        buttonStoreValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: reconfigure this and requires testing

                readDataFromCallBack(audioServiceUUID, audioCharacteristicUUID);
//                speechData = readData(audioServiceUUID, audioCharacteristicUUID);
//                rollData = readData(accelerometerServiceUUID, accelerometerRollUUID);
//                pitchData = readData(accelerometerServiceUUID, accelerometerPitchUUID);
            }
        });


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

            Log.d(TAG, "Clicked on a characteristic " + uuid);
            if (mBTLE_Service != null) {
                characteristic.setValue("Hello my friend");
                mBTLE_Service.writeCharacteristic(characteristic); // write something to the characteristic
                Log.d(TAG, "Wrote Hello my friend to " + characteristic.getUuid().toString());
            }

//            Dialog_BTLE_Characteristic dialog_btle_characteristic = new Dialog_BTLE_Characteristic();
//
//            dialog_btle_characteristic.setTitle(uuid);
//            dialog_btle_characteristic.setService(mBTLE_Service);
//            dialog_btle_characteristic.setCharacteristic(characteristic);
//
//            dialog_btle_characteristic.show(getFragmentManager(), "Dialog_BTLE_Characteristic");
//            speechData = characteristic.getValue();
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
        BluetoothUtils.toast(getApplicationContext(), "Can now click on store values");
        buttonStoreValues.setVisibility(View.VISIBLE);
    }

    public void updateCharacteristic() {
        expandableListAdapter.notifyDataSetChanged();
    }

    /**
     *  More generic way of accessing data from gatt
     * @param serviceStringUUID String of UUID of the service
     * @param characteristicStringUUID String of UUID of the characteristic
     * @return expected byte[] from the board
     */
    public byte[] readData(String serviceStringUUID, String characteristicStringUUID){
        BluetoothGatt mGatt = mBTLE_Service.getGatt();
        BluetoothGattService mService = mGatt.getService(UUID.fromString(serviceStringUUID));
        if(mService == null) {
            Log.d(TAG, "couldn't find service");
            return null;
        }
        BluetoothGattCharacteristic mCharacteristic = mService.getCharacteristic(UUID.fromString(characteristicStringUUID));
        if(mCharacteristic == null) {
            Log.d(TAG, "Unable to read given characteristic.");
            return null;
        }
        Log.d(TAG, "characteristic found was : " + mCharacteristic.getUuid().toString());
        return mCharacteristic.getValue();
    }

    public void readDataFromCallBack(String serviceStringUUID, String characteristicStringUUID){

        BluetoothGatt mGatt = mBTLE_Service.getGatt();
        BluetoothGattService mService = mGatt.getService(UUID.fromString(serviceStringUUID));
        if(mService == null) {
            Log.d(TAG, "couldn't find service");
            return;
        }
        BluetoothGattCharacteristic mCharacteristic = mService.getCharacteristic(UUID.fromString(characteristicStringUUID));
        if(mCharacteristic == null) {
            Log.d(TAG, "Unable to read given characteristic.");
            return;
        }

        if(mGatt.readCharacteristic(mCharacteristic)) {   //read data
            Log.d(TAG, "Successfully read " + mCharacteristic.getUuid().toString());
        }
    }


    public byte[] readAudio(){
        BluetoothGatt mGatt = mBTLE_Service.getGatt();
        BluetoothGattService mAudioService = mGatt.getService(UUID.fromString(audioServiceUUID));
        if(mAudioService == null) {
            Log.d(TAG, "coudln't find audio service");
            return null;
        }
        BluetoothGattCharacteristic mAudioCharacteristic = mAudioService.getCharacteristic(UUID.fromString(audioCharacteristicUUID));
        if(mAudioCharacteristic == null) {
            Log.d(TAG, "Unable to read audio.");
            return null;
        }
        Log.d(TAG, "audio characteristic found was : " + mAudioCharacteristic.getUuid().toString());
        return mAudioCharacteristic.getValue();
    }

    public byte[] readAccelerometerPitch() {
        BluetoothGatt mGatt = mBTLE_Service.getGatt();
        BluetoothGattService mAccelerometerService = mGatt.getService(UUID.fromString(accelerometerServiceUUID));
        if(mAccelerometerService == null) {
            Log.d(TAG, "coudln't find" +
                    " accelerometer service");
            return null;
        }
        BluetoothGattCharacteristic mAccelerometerCharacteristic = mAccelerometerService.getCharacteristic(UUID.fromString(accelerometerPitchUUID));
        if(mAccelerometerCharacteristic == null) {
            Log.d(TAG, "Unable to read audio.");
            return null;
        }
        Log.d(TAG, "audio characteristic found was : " + mAccelerometerCharacteristic.getUuid().toString());
        return mAccelerometerCharacteristic.getValue();
    }
    public byte[] readAccelerometerRoll() {
        BluetoothGatt mGatt = mBTLE_Service.getGatt();
        BluetoothGattService mAccelerometerService = mGatt.getService(UUID.fromString(accelerometerRollUUID));
        if(mAccelerometerService == null) {
            Log.d(TAG, "coudln't find" +
                    " accelerometer service");
            return null;
        }
        BluetoothGattCharacteristic mAccelerometerCharacteristic = mAccelerometerService.getCharacteristic(UUID.fromString(accelerometerRollUUID));
        if(mAccelerometerCharacteristic == null) {
            Log.d(TAG, "Unable to read audio.");
            return null;
        }
        Log.d(TAG, "audio characteristic found was : " + mAccelerometerCharacteristic.getUuid().toString());
        return mAccelerometerCharacteristic.getValue();
    }

    public void writeAccelerometer(byte[] data){
        BluetoothGatt mGatt = mBTLE_Service.getGatt();
        BluetoothGattService mAccelerometerService = mGatt.getService(UUID.fromString(audioServiceUUID));
        if(mAccelerometerService == null) {
            Log.d(TAG, "couldn't find accelerometer service");
            return;
        }
        BluetoothGattCharacteristic mAccelerometerCharacteristic = mAccelerometerService.getCharacteristic(UUID.fromString(audioCharacteristicUUID));
        if(mAccelerometerCharacteristic == null) {
            Log.d(TAG, "Unable to read audio.");
            return;
        }
        mAccelerometerCharacteristic.setValue(data);
        mBTLE_Service.writeCharacteristic(mAccelerometerCharacteristic);
        // TODO: need to send the data too?
    }

    /**
     *  return to BluetoothActivity with result
     */
    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra(PITCH_DATA_NAME, pitchData);
        i.putExtra(ROLL_DATA_NAME, rollData);
        i.putExtra(SPEECH_DATA_NAME, speechData);
        setResult(BluetoothActivity.BTLE_SERVICES, i);

        Log.d(TAG, "the extra data is " + new String(speechData));

        finish();
        super.onBackPressed();
    }

    public void updateSpeechData(byte[] byteArrayExtra) {
        this.speechData = byteArrayExtra;
//        String test = new String(byteArrayExtra);
//
//        Log.d(TAG, test);
    }
    public void updateSpeechData(String stringData){
//        this.speechData = Byte.parseByte(stringData);
    }
}
