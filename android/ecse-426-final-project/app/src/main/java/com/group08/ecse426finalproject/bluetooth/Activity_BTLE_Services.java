package com.group08.ecse426finalproject.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.accelerometer.AccelerometerActivity;
import com.group08.ecse426finalproject.firebase.FirebaseService;
import com.group08.ecse426finalproject.speech.SpeechResponseHandler;
import com.group08.ecse426finalproject.speech.SpeechService;
import com.group08.ecse426finalproject.utils.BluetoothUtils;
import com.group08.ecse426finalproject.utils.ToastShower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
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
    public static final String SERVICE_UUID              = "03366e80-cf3a-11e1-9ab4-2002a5d5c51c";
    public static final String AUDIO_CHARACTERISTIC_UUID = "e43e78a0-cf4a-11e1-8ffc-2002a5d5c51c";
    public static final String PITCH_CHARACTERISTIC_UUID = "e73e78a0-cf4a-11e1-8ffc-2002a5d5c51c";
    public static final String ROLL_CHARACTERISTIC_UUID  = "e63e78a0-cf4a-11e1-8ffc-2002a5d5c51c";
    public static final String WRITE_CHARACTERISTIC_UUID = "e53f78a0-cf4a-11e1-8ffc-2002a5d5c51c";

    private List<byte[]> pitchData = new ArrayList<>();
    private List<byte[]> rollData = new ArrayList<>();
    private List<byte[]> speechData = new ArrayList<>();


    // listView for services
    private ListAdapter_BTLE_Services expandableListAdapter;

    // mapping all available services
    private ArrayList<BluetoothGattService> services_ArrayList;
    private HashMap<String, ArrayList<BluetoothGattCharacteristic>> characteristics_HashMapList;

    private Intent mBTLE_Service_Intent;
    private Service_BTLE_GATT mBTLE_Service;
    private BroadcastReceiver_BTLE_GATT mGattUpdateReceiver;
    private SpeechService speechService;
    private FirebaseService firebaseService;

    private String address;

    private Button buttonStoreValues;

    private TextView textSpeechTranscript;
    private TextView textSpeechFirebaseLink;

    // service connection
    private ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Service_BTLE_GATT.BTLeServiceBinder binder = (Service_BTLE_GATT.BTLeServiceBinder) service;
            mBTLE_Service = binder.getService();

            if (!mBTLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBTLE_Service.connect(address);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btle_services);

        Log.d(TAG, "onCreate");

        // retrieve name and address of device
        Intent intent = getIntent();
        String name = intent.getStringExtra(Activity_BTLE_Services.EXTRA_NAME);
        address = intent.getStringExtra(Activity_BTLE_Services.EXTRA_ADDRESS);

        // map services and characteristics
        // characteristics will be displayed
        services_ArrayList = new ArrayList<>();
        characteristics_HashMapList = new HashMap<>();

        speechService = new SpeechService(this);
        firebaseService = new FirebaseService();

        textSpeechTranscript = findViewById(R.id.text_transcript_ble);
        textSpeechFirebaseLink = findViewById(R.id.text_firebase_link);

        textSpeechTranscript.setVisibility(View.GONE);
        textSpeechFirebaseLink.setVisibility(View.GONE);

        buttonStoreValues = findViewById(R.id.btn_storeValues);
        buttonStoreValues.setVisibility(View.INVISIBLE);
        buttonStoreValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readDataFromCallBack(SERVICE_UUID, PITCH_CHARACTERISTIC_UUID);
            }
        });

        Button buttonSpeech = findViewById(R.id.button_speech);
        buttonSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSpeechData();
            }
        });

        Button buttonPitchRoll = findViewById(R.id.button_pitch_roll);
        buttonPitchRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPitchRollData();
            }
        });

        Button buttonClearSpeech = findViewById(R.id.button_clear_speech);
        buttonClearSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechData.clear();
                textSpeechTranscript.setVisibility(View.GONE);
                textSpeechFirebaseLink.setVisibility(View.GONE);
                ToastShower.showToast(Activity_BTLE_Services.this, "Speech data cleared.");
            }
        });

        Button buttonClearPitchRoll = findViewById(R.id.button_clear_pitch_roll);
        buttonClearPitchRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pitchData.clear();
                rollData.clear();
                ToastShower.showToast(Activity_BTLE_Services.this, "Pitch and roll data cleared.");
            }
        });

        // list view for services
        expandableListAdapter = new ListAdapter_BTLE_Services(
                this, services_ArrayList, characteristics_HashMapList);

        ExpandableListView expandableListView = findViewById(R.id.lv_expandable);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener(this);

        ((TextView) findViewById(R.id.tv_name)).setText(String.format("%s %s", name, "Services"));
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
        Sets up activity for chosen characteristics
     */
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        BluetoothGattCharacteristic characteristic = characteristics_HashMapList.get(
                services_ArrayList.get(groupPosition).getUuid().toString())
                .get(childPosition);

        // write-able property
        if (BluetoothUtils.hasWriteProperty(characteristic.getProperties()) != 0) {
            String uuid = characteristic.getUuid().toString();
            Log.d(TAG, "Clicked on a characteristic " + uuid);
            if (mBTLE_Service != null) {
                characteristic.setValue(5, FORMAT_UINT8, 0); // Testing purposes
                mBTLE_Service.writeCharacteristic(characteristic); // write something to the characteristic
                updateCharacteristic();
                Log.d(TAG, "Wrote to " + characteristic.getUuid().toString());
            }
        }
        else {
            Log.d(TAG, characteristic.getUuid() + " Doesn't have write property");

        }

        return false;
    }

    /**
     *  return to BluetoothActivity with result
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();

        byte[] bytePitchData = concatenateByteArrays(pitchData);
        byte[] byteRollData = concatenateByteArrays(rollData);
        byte[] byteSpeechData = concatenateByteArrays(speechData);

        Log.d(TAG, "Number of speech bytes: " + byteSpeechData.length);
        Log.d(TAG, "Speech data in BTLE: " + Arrays.toString(byteSpeechData));

        Log.d(TAG, "Number of pitch bytes: " + bytePitchData.length);
        Log.d(TAG, "pitch data in BTLE: " + Arrays.toString(bytePitchData));

        Log.d(TAG, "Number of speech bytes: " + byteRollData.length);
        Log.d(TAG, "Speech data in BTLE: " + Arrays.toString(byteRollData));

        intent.putExtra(PITCH_DATA_NAME, bytePitchData);
        intent.putExtra(ROLL_DATA_NAME, byteRollData);
        intent.putExtra(SPEECH_DATA_NAME, byteSpeechData);
        setResult(BluetoothActivity.BTLE_SERVICES, intent);

        mBTLE_Service.disconnect();

        finish();
        super.onBackPressed();
    }

    public void updateSpeechData(byte[] byteArray) {
        this.speechData.add(byteArray);
        if (speechData.size() == 1600) { // 1600 packets of 20 bytes = 32 000 bytes
            processSpeechData();
            speechData.clear();
        }
    }

    public void updatePitchData(byte[] byteArray) {
        this.pitchData.add(byteArray);
        if (pitchData.size() == 100) {// 100 packets
            processPitchRollData();
        }
    }

    public void updateRollData(byte[] byteArray) {
        this.rollData.add(byteArray);
    }

    public void updateServices() {

        if (mBTLE_Service != null) {

            services_ArrayList.clear();
            characteristics_HashMapList.clear();

            List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                services_ArrayList.add(service);

                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                newCharacteristicsList.addAll(characteristicsList);

                characteristics_HashMapList.put(service.getUuid().toString(), newCharacteristicsList);
            }

            if (servicesList.size() > 0) {
                expandableListAdapter.notifyDataSetChanged();
            }
        }
        ToastShower.showToast(getApplicationContext(), "Can now click on store values");
        buttonStoreValues.setVisibility(View.VISIBLE);
    }

    public void updateCharacteristic() {
        expandableListAdapter.notifyDataSetChanged();
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
        if(BluetoothUtils.hasReadProperty(mCharacteristic.getProperties()) != 0){    // check write property
            if(mGatt.readCharacteristic(mCharacteristic)) {   //read data
                Log.d(TAG, "Successfully read " + mCharacteristic.getUuid().toString());
            }
        } else {
            ToastShower.showToast(this," The characteristic doesn't have read property!");
        }
    }

    private byte[] concatenateByteArrays(List<byte[]> source) {
        byte[] concatenatedBytes = new byte[source.size() * 20];
        int i = 0;
        for (byte[] data : source) {
            System.arraycopy(data, 0, concatenatedBytes, i * 20, data.length);
            i++;
        }
        return concatenatedBytes;
    }

    private void processSpeechData() {
        if (!speechData.isEmpty()) {
            byte[] byteSpeechData = concatenateByteArrays(speechData);
            firebaseService.uploadBytesUnique(byteSpeechData, "audio/", "raw", textSpeechFirebaseLink);
            speechService.sendGoogleSpeechTranscriptionRequest(byteSpeechData,
                    new SpeechResponseHandler() {
                        @Override
                        public void handleSpeechResponse(int transcribedNumber) {
                            textSpeechTranscript.setVisibility(View.VISIBLE);
                            textSpeechTranscript.setText(String.valueOf(transcribedNumber));
                            ToastShower.showToast(Activity_BTLE_Services.this, "Transcribed audio as " + transcribedNumber);
                            BluetoothGatt mGatt = mBTLE_Service.getGatt();
                            BluetoothGattService mService = mGatt.getService(UUID.fromString(SERVICE_UUID));
                            if(mService == null) {
                                Log.d(TAG, "couldn't find service");
                                return;
                            }
                            BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID.fromString(WRITE_CHARACTERISTIC_UUID));
                            if(characteristic == null) {
                                Log.d(TAG, "Unable to read given characteristic.");
                                return;
                            }

                            // write-able property
                            if (BluetoothUtils.hasWriteProperty(characteristic.getProperties()) != 0) {
                                String uuid = characteristic.getUuid().toString();
                                Log.d(TAG, "Clicked on a characteristic " + uuid);
                                if (mBTLE_Service != null) {

                                    characteristic.setValue(transcribedNumber, FORMAT_UINT8, 0);
                                    mBTLE_Service.writeCharacteristic(characteristic); // write something to the characteristic
                                    updateCharacteristic();
                                    Log.d(TAG, "Wrote to " + characteristic.getUuid().toString());
                                }
                            }
                            else {
                                Log.d(TAG, characteristic.getUuid() + " Doesn't have write property");

                            }
                        }

                        @Override
                        public void handleSpeechErrorResponse() {
                            textSpeechTranscript.setVisibility(View.VISIBLE);
                            textSpeechTranscript.setText(R.string.transcription_failed);
                            ToastShower.showToast(Activity_BTLE_Services.this, "Unable to transcribe audio as number.");
                        }
                    });
        }
    }

    private void processPitchRollData() {
        Intent i = new Intent(Activity_BTLE_Services.this, AccelerometerActivity.class);
        byte[] bytePitchData = concatenateByteArrays(pitchData);
        byte[] byteRollData = concatenateByteArrays(rollData);
        i.putExtra(PITCH_DATA_NAME, bytePitchData);
        i.putExtra(ROLL_DATA_NAME, byteRollData);
        pitchData.clear();
        rollData.clear();
        startActivity(i);
    }
}
