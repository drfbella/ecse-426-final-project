package com.group08.ecse426finalproject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.group08.ecse426finalproject.LeDevice.BLEDevice;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
@TargetApi(21)
public class ScanFragment extends Fragment {

    // bluetooth properties

    public final static int REQUEST_ENABLE_BT = 1; // Request BT constant (> 0)
    private ScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    BluetoothLeScanner mLeScanner;

    private BluetoothAdapter mBluetoothAdapter; // bluetooth adapter
    private Handler mHandler;
    private boolean mScanning;


    // RecyclerView
    RecyclerView mRecyclerView;
    MyDeviceRecyclerViewAdapter mRecyclerViewAdapter;

    // Stops scanning after 10 seconds
    private static final long SCAN_PERIOD = 10000;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScanFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ScanFragment newInstance(int columnCount) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
            // Api 21+
        if (Build.VERSION.SDK_INT >= 21) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.d("addDevice", "add!");
                    mRecyclerViewAdapter.addDevice(result.getDevice().getAddress(), result.getDevice().getName());
                }
            };
        } else {
            // Api 18-20
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    mRecyclerViewAdapter.addDevice(bluetoothDevice.getAddress(), bluetoothDevice.getName());
                }
            };
        }

        // initialize bluetooth adapter
        mBluetoothAdapter = getBluetoothAdapter();
        // check if user has enabled bluetooth
        enableBluetoothAdapter(mBluetoothAdapter);
    }

    //work with recyclerView

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mRecyclerViewAdapter = new MyDeviceRecyclerViewAdapter(LeDevice.ITEMS, mListener);
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BLEDevice item);
    }


    //helper methods

    // Initializes bluetooth adapter
    public BluetoothAdapter getBluetoothAdapter(){
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager.getAdapter() == null) {
            return null;
        } else
            return bluetoothManager.getAdapter();
    }

    /**
     * Check if BT is enabled and prompt user to enable in settings
     * @param mBluetoothAdapter
     */
    public void enableBluetoothAdapter(BluetoothAdapter mBluetoothAdapter){
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    // startActivity results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        // bluetooth results
        if (requestCode == ScanFragment.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Bluetooth Disabled",
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }


    private void startScan() {
        Log.d("startScan", "scan!");
        if (mRecyclerViewAdapter.getItemCount() == 0)
//            mListener.onShowProgress();        TODO: what is this for?

            //api 18-20
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            //api 21
            // request BluetoothLeScanner if it hasn't been initialized yet
            if (mLeScanner == null) mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            // start scan in low latency mode
            mLeScanner.startScan(new ArrayList<ScanFilter>(),
                    new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
        }
    }

    private void stopScan(){
        // 18-20
        if(Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        // 21+
        else {
            mLeScanner.stopScan(mScanCallback);
        }
    }



    // method for scanning followed android developer guide

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

}
