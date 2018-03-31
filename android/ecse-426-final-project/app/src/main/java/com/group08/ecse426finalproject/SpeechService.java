package com.group08.ecse426finalproject;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

class SpeechService {
    private static final String TAG = "SpeechService";
    private static final String SPEECH_URL =
            "https://speech.googleapis.com/v1/speech:recognize?key=";
    private Context context;
    private final BluetoothTransmitter bluetoothTransmitter;
    private final ResourceAccess resourceAccess;

    SpeechService(Context context, BluetoothTransmitter bluetoothTransmitter, ResourceAccess resourceAccess) {
        this.context = context;
        this.bluetoothTransmitter = bluetoothTransmitter;
        this.resourceAccess = resourceAccess;
    }

    void sendDemoRequestString() {
        sendRequest(resourceAccess.readRawResourceString(R.raw.audio_64));
    }

    void sendDemoRequestBytes() {
        sendRequest(resourceAccess.readRawResourceBytes(R.raw.audio));
    }

    void sendRequest(byte[] audio_bytes) {
        String audio_base64 = Base64.encodeToString(audio_bytes, Base64.NO_WRAP);
        sendRequest(audio_base64);
    }

    void sendRequest(String audio_base64) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest = new JSONObject(resourceAccess.readRawResourceString(R.raw.sync_request));
            jsonRequest.getJSONObject("audio").put("content", audio_base64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String api_key = resourceAccess.readRawResourceString(R.raw.gcloud_api_key);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                SPEECH_URL + api_key, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response);
                try {
                    String transcript = response.getJSONArray("results")
                            .getJSONObject(0).getJSONArray("alternatives")
                            .getJSONObject(0).getString("transcript");
                    Log.d(TAG, "Transcript: " + transcript);
                    bluetoothTransmitter.transmitString(transcript);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "POST failed: " + error.getMessage() + ", " + error.toString());
            }
        });
        queue.add(jsonObjectRequest);
    }
}
