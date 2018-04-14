package com.group08.ecse426finalproject.speech;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.utils.ResourceAccessor;

import org.json.JSONException;
import org.json.JSONObject;

public class SpeechService {
    private static final String TAG = "SpeechService";
    private static final String SPEECH_URL =
            "https://speech.googleapis.com/v1/speech:recognize?key=";
    private ResourceAccessor resourceAccessor;
    private Context context;

    public SpeechService(Context context) {
        this.context = context;
        this.resourceAccessor = new ResourceAccessor(context);
    }

    public void sendGoogleSpeechTranscriptionRequest(byte[] audio_bytes, SpeechResponseHandler speechResponseHandler) {
        String audio_base64 = Base64.encodeToString(audio_bytes, Base64.NO_WRAP);
        sendGoogleSpeechTranscriptionRequest(audio_base64, speechResponseHandler);
    }

    public void sendGoogleSpeechTranscriptionRequest(String audio_base64,
                                                     final SpeechResponseHandler speechResponseHandler) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest = new JSONObject(resourceAccessor.readRawResourceString(R.raw.sync_request));
            jsonRequest.getJSONObject("audio").put("content", audio_base64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String api_key = resourceAccessor.readRawResourceString(R.raw.gcloud_api_key);
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
                    speechResponseHandler.handleSpeechResponse(transcript);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                speechResponseHandler.handleSpeechErrorResponse();
                Log.d(TAG, "POST failed: " + error.getMessage() + ", " + error.toString());
            }
        });
        queue.add(jsonObjectRequest);
    }
}
