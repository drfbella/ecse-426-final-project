package com.group08.ecse426finalproject.speech;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.ScatterChart;
import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.firebase.FirebaseService;
import com.group08.ecse426finalproject.utils.ByteUtils;
import com.group08.ecse426finalproject.utils.ChartCreator;

import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

public class SpeechActivity extends AppCompatActivity {
    private static final String TAG = "SpeechActivity";
    private FirebaseService firebaseService;
    private ByteUtils byteUtils;
    private ChartCreator chartCreator;
    private SpeechService speechService;

    private TextView textTranscript;
    private TextView textFirebaseLink;
    private ProgressBar progressBar;
    private ScatterChart audioChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        firebaseService = new FirebaseService();
        byteUtils = new ByteUtils();
        chartCreator = new ChartCreator();
        speechService = new SpeechService(this);

        textTranscript = findViewById(R.id.text_transcript);
        textFirebaseLink = findViewById(R.id.text_firebase_link);
        progressBar = findViewById(R.id.progress_bar);

        byte[] speechData = getIntent().getByteArrayExtra(SPEECH_DATA_NAME);
//        byte[] speechData = resourceAccessor.readRawResourceBytes(R.raw.audio);

        firebaseService.uploadBytesUnique(speechData, "audio/","raw", textFirebaseLink);

        progressBar.setVisibility(View.VISIBLE);

        speechService.sendGoogleSpeechTranscriptionRequest(speechData, new SpeechResponseHandler() {
            @Override
            public void handleSpeechResponse(int transcript) {
                textTranscript.setText(transcript);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void handleSpeechErrorResponse() {
                textTranscript.setText(R.string.transcription_error);
                progressBar.setVisibility(View.GONE);
            }
        });
//        sendDemoRequestBytes();
    }
}
