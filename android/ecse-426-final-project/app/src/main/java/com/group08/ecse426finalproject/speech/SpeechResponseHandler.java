package com.group08.ecse426finalproject.speech;

public interface SpeechResponseHandler {
    void handleSpeechResponse(String transcript);

    void handleSpeechErrorResponse();
}
