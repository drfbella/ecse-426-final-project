package com.group08.ecse426finalproject;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

class SpeechService {
    private static final String TAG = "SpeechService";
    private static final String SPEECH_URL =
            "https://speech.googleapis.com/v1/speech:recognize?key=";
    private Context context;

    SpeechService(Context context) {
        this.context = context;
    }

    void sendRequest() {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest = new JSONObject(readRawResourceString(R.raw.sync_request));
            jsonRequest.getJSONObject("audio").put("content", readRawResourceString(R.raw.audio_64));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String api_key = readRawResourceString(R.raw.gcloud_api_key);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                SPEECH_URL + api_key, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "POST failed: " + error.getMessage() + ", " + error.toString());
            }
        });
        queue.add(jsonObjectRequest);
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
