package com.group08.ecse426finalproject.speech;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.firebase.FirebaseService;

import static com.group08.ecse426finalproject.utils.Constants.SPEECH_DATA_NAME;

public class SpeechActivity extends AppCompatActivity {
    private TextView textTranscript;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        FirebaseService firebaseService = new FirebaseService();
        SpeechService speechService = new SpeechService(this);

        textTranscript = findViewById(R.id.text_transcript);
        TextView textFirebaseLink = findViewById(R.id.text_firebase_link);
        progressBar = findViewById(R.id.progress_bar);

        byte[] speechData = getIntent().getByteArrayExtra(SPEECH_DATA_NAME);

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
    }
}
