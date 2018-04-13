package com.group08.ecse426finalproject.bluetooth;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.utils.BluetoothUtils;

import java.util.List;
import java.util.UUID;
/*
    inspired from https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
 */
@TargetApi(18)
public class Service_BTLE_GATT extends Service {
    /*
     * Service for managing connection and data communication with a GATT server hosted on a
     * given Bluetooth LE device.
     */
    private final static String TAG = Service_BTLE_GATT.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_UUID = "com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT.EXTRA_UUID";
    public final static String EXTRA_DATA = "com.group08.ecse426finalproject.bluetooth.Service_BTLE_GATT.EXTRA_DATA";

    public static final String uuid_accelerometer_roll = "TODO";
//    public static final UUID UUID_ACCELEROMETER_MEASUREMENT_ROLL = UUID.fromString(uuid_accelerometer_roll);


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;

                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                // Discover services
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;

                mConnectionState = STATE_DISCONNECTED;

                Log.i(TAG, "Disconnected from GATT server.");

                broadcastUpdate(intentAction);
            }
        }

        /**
         *  called when a service is discovered
         * @param gatt
         * @param status
         */

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // update to broadcast
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                // TODO: can implement automatic data polling on services discovered
                Log.d(TAG, "services discovered, can now press on store values");
            }
            else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }

            setNotificationForCharacteristic(gatt, Activity_BTLE_Services.serviceUUID,
                    Activity_BTLE_Services.audioCharacteristicUUID);

            setNotificationForCharacteristic(gatt, Activity_BTLE_Services.serviceUUID,
                    Activity_BTLE_Services.accelerometerPitchUUID);

            setNotificationForCharacteristic(gatt, Activity_BTLE_Services.serviceUUID,
                    Activity_BTLE_Services.accelerometerRollUUID);
        }

        /**
         * Called when characteristic is read
         * Retrieve characteristic data
         * @param gatt
         * @param characteristic
         * @param status
         */

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "GATT SUCCESS...");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }


        /**
         *  Called on characteristic changed, update data
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Chacteristic changed: " + characteristic.getUuid().toString());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        /**
         * Called when type gatt.writeCharacteristic(characteristic);
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // Reads data...

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {

        final Intent intent = new Intent(action);

        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());

        // poll data from audio

        if(UUID.fromString(Activity_BTLE_Services.audioCharacteristicUUID).equals(characteristic.getUuid())){
            intent.putExtra(EXTRA_DATA, characteristic.getValue());

            Log.d(TAG, "Added new data which is : " + new String(characteristic.getValue()));
        }

        // poll data from pitch

        if(UUID.fromString(Activity_BTLE_Services.accelerometerPitchUUID).equals(characteristic.getUuid())){
            intent.putExtra(EXTRA_DATA, characteristic.getValue());

            Log.d(TAG, "Got new data from : " + characteristic.getUuid().toString());
        }

        // poll data from roll

        if(UUID.fromString(Activity_BTLE_Services.accelerometerRollUUID).equals(characteristic.getUuid())){
            intent.putExtra(EXTRA_DATA, characteristic.getValue());

            Log.d(TAG, "Got new data from : " + characteristic.getUuid().toString());
        }

        sendBroadcast(intent);

    }

    public BluetoothGatt getGatt() {
        return mBluetoothGatt;
    }

    public class BTLeServiceBinder extends Binder {

        Service_BTLE_GATT getService() {
            return Service_BTLE_GATT.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new BTLeServiceBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback); // mGattCallback
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {

        if (mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(getString(R.string.CLIENT_CHARACTERISTIC_CONFIG))); //TODO: read about this

        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {

        if (mBluetoothGatt == null) {
            return null;
        }

        return mBluetoothGatt.getServices();
    }


    private void setNotificationForCharacteristic(BluetoothGatt gatt, String serviceUUID, String characteristicUUID) {
        BluetoothGattService mService = gatt.getService(UUID.fromString(serviceUUID));
        if(mService == null) {
            Log.d(TAG, "couldn't find service: " + serviceUUID);
            return;
        }
        BluetoothGattCharacteristic mCharacteristic = mService.getCharacteristic(UUID.fromString(characteristicUUID));
        if(mCharacteristic == null) {
            Log.d(TAG, "Unable to read given characteristic: " + characteristicUUID);
            return;
        }

        if (BluetoothUtils.hasNotifyProperty(mCharacteristic.getProperties()) != 0) {
            if (gatt.setCharacteristicNotification(mCharacteristic, true)) {
                BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptors().get(0);
                if (0 != (mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
                    // It's an indicate characteristic
                    Log.d("onServicesDiscovered", "Characteristic (" + mCharacteristic.getUuid() + ") is INDICATE");
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                } else {
                    // It's a notify characteristic
                    Log.d("onServicesDiscovered", "Characteristic (" + mCharacteristic.getUuid() + ") is NOTIFY");
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
        }

//            if(BluetoothUtils.hasReadProperty(mCharacteristic.getProperties()) != 0){    // check write property
//                if(gatt.readCharacteristic(mCharacteristic)) {   //read data
//                    Log.d(TAG, "Successfully read " + mCharacteristic.getUuid().toString());
//                }
//            }
    }
}