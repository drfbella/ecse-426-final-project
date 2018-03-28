# Cloud
Here is an example script calling the Google Cloud Speech API. The `audio.wav` file is speech of someone saying "1 2 3 4 5", and running `example.sh` will call the API, returning the desired text.

To aid the speech recognition, phrases of numbers from 0 to 99 are passed as hints, since two-digit numbers are the only phrases that will be likely supported.
