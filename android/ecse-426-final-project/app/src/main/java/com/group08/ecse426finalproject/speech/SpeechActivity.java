package com.group08.ecse426finalproject.speech;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.bluetooth.BluetoothTransmitter;
import com.group08.ecse426finalproject.utils.ResourceAccessor;

import org.json.JSONException;
import org.json.JSONObject;

import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

public class SpeechActivity extends AppCompatActivity {
    private static final String TAG = "SpeechActivity";
    private static final String SPEECH_URL =
            "https://speech.googleapis.com/v1/speech:recognize?key=";
    private BluetoothTransmitter bluetoothTransmitter;
    private ResourceAccessor resourceAccessor;
    private TextView textTranscript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        bluetoothTransmitter = new BluetoothTransmitter();
        resourceAccessor = new ResourceAccessor(this);

        textTranscript = findViewById(R.id.text_transcript);

        byte[] speechData = getIntent().getByteArrayExtra(SPEECH_DATA_NAME);
        //sendGoogleSpeechTranscriptionRequest(speechData);
        sendDemoRequestBytes();
    }

    public void sendDemoRequestString() {
        sendGoogleSpeechTranscriptionRequest(resourceAccessor.readRawResourceString(R.raw.audio_64));
    }

    public void sendDemoRequestBytes() {
        sendGoogleSpeechTranscriptionRequest(resourceAccessor.readRawResourceBytes(R.raw.audio));
    }

    public void sendGoogleSpeechTranscriptionRequest(byte[] audio_bytes) {
        String audio_base64 = Base64.encodeToString(audio_bytes, Base64.NO_WRAP);
        sendGoogleSpeechTranscriptionRequest(audio_base64);
    }

    public void sendGoogleSpeechTranscriptionRequest(String audio_base64) {
        RequestQueue queue = Volley.newRequestQueue(this);
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
                    textTranscript.setText(transcript);
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
