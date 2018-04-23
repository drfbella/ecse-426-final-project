package com.group08.ecse426finalproject.utils;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class ResourceAccessor {
    private static final String TAG = "ResourceAccessor";
    private Context context;

    public ResourceAccessor(Context context) {
        this.context = context;
    }

    public String readRawResourceString(int id) {
        byte[] bytes = readRawResourceBytes(id);
        if (bytes == null) {
            return "";
        } else {
            return new String(bytes);
        }
    }

    private byte[] readRawResourceBytes(int id) {
        try {
            InputStream in_s = context.getResources().openRawResource(id);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return b;
        } catch (IOException e) {
            Log.d(TAG, "Unable to read raw text resource.");
        }
        return null;
    }
}
