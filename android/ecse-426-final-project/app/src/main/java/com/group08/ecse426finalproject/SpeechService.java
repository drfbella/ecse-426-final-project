package com.group08.ecse426finalproject;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpeechService {
    private static final String TAG = "SpeechService";
    private static final String SPEECH_URL = "https://speech.googleapis.com/v1/speech:recognize";
    private Context context;

    public SpeechService(Context context) {
        this.context = context;
    }

    public void sendRequest()
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SPEECH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "POST failed.");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("d", readRawResourceString(R.raw.sync_request));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/json");
                params.put("Authorization",
                        "Bearer " + readRawResourceString(R.raw.gcloud_access_token));
                return params;
            }
        };
        String s_r = readRawResourceString(R.raw.sync_request);
        Log.d(TAG, "Sent request: " + stringRequest.toString());
        Log.d(TAG, "d: " + s_r);
        Log.d(TAG, "Last char: " + s_r.substring(s_r.length() - 10));
        Log.d(TAG, "Bearer: " + readRawResourceString(R.raw.gcloud_access_token));
        queue.add(stringRequest);
    }

    private String readRawResourceString(int id) {
        try {
            InputStream in_s = context.getResources().openRawResource(id);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return new String(b);
        } catch (IOException e) {
            Log.d(TAG, "Unable to read raw text resource.");
        }
        return null;
    }
}
