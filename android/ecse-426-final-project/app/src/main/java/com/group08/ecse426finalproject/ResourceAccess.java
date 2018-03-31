package com.group08.ecse426finalproject;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

class ResourceAccess {
    private static final String TAG = "ResourceAccess";
    private Context context;

    ResourceAccess(Context context) {
        this.context = context;
    }

    String readRawResourceString(int id) {
        return new String(readRawResourceBytes(id));
    }

    byte[] readRawResourceBytes(int id) {
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
