package com.group08.ecse426finalproject.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group08.ecse426finalproject.R;

import java.util.ArrayList;
import java.util.Locale;

public class ListAdapter_BTLE_Devices extends ArrayAdapter<BTLE_Device> {

    private Activity activity;
    private int layoutResourceID;
    private ArrayList<BTLE_Device> devices;

    ListAdapter_BTLE_Devices(Activity activity, int resource, ArrayList<BTLE_Device> objects) {
        super(activity.getApplicationContext(), resource, objects);

        this.activity = activity;
        layoutResourceID = resource;
        devices = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                convertView = inflater.inflate(layoutResourceID, parent, false);
            }
        }

        BTLE_Device device = devices.get(position); // retrieve devices in array list
        String name = device.getName();
        String address = device.getAddress();
        int rssi = device.getRSSI();

        TextView tv_name = convertView.findViewById(R.id.tv_name);
        if(name != null && name.length() > 0) {
            tv_name.setText(device.getName());
        } else {
            tv_name.setText(R.string.no_name);
        }

        TextView tv_rssi = convertView.findViewById(R.id.tv_rssi);
        tv_rssi.setText(String.format(Locale.CANADA, "RSSI: %d", rssi)); // will not be null...

        TextView tv_maccaddress = convertView.findViewById(R.id.tv_macaddr);
        if(address != null && address.length() > 0){ //null check
            tv_maccaddress.setText(device.getAddress());
        } else {
            tv_maccaddress.setText(R.string.no_address);
        }

        return convertView;
    }
}
