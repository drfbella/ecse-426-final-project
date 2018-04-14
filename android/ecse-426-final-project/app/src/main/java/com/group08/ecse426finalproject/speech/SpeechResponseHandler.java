package com.group08.ecse426finalproject.speech;

public interface SpeechResponseHandler {
    void handleSpeechResponse(int transcribedNumber);

    void handleSpeechErrorResponse();
}
