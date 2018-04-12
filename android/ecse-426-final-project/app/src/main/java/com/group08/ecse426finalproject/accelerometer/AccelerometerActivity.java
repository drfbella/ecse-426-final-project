package com.group08.ecse426finalproject.accelerometer;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.group08.ecse426finalproject.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;


public class AccelerometerActivity extends AppCompatActivity {
    private static final String TAG = "AccelerometerActivity";
    private static final float PITCH_ROLL_RESOLUTION = 65_536f; // 16 bits of resolution
    private static final int MAX_PITCH_ROLL_VALUE = 360;
    private static final int NUM_SAMPLES = 1000; // 10 seconds at 100 Hz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        byte[] pitchData = getIntent().getByteArrayExtra(PITCH_DATA_NAME);
        byte[] rollData = getIntent().getByteArrayExtra(ROLL_DATA_NAME);
        Log.d(TAG, "Received pitch data: " + Arrays.toString(pitchData));
        Log.d(TAG, "Received roll data: " + Arrays.toString(rollData));

        LineChart pitchChart = findViewById(R.id.pitch_chart);
        LineChart rollChart = findViewById(R.id.roll_chart);

        setChartData(pitchChart, randomData(NUM_SAMPLES), "pitch", Color.RED);
        setChartData(rollChart, randomData(NUM_SAMPLES), "roll", Color.BLUE);

    }

    private void setChartData(LineChart chart, byte[] data, String label, int color) {
        List<Entry> entries = new ArrayList<>();
        float t = 0;
        for (int i = 0; i < data.length; i += 2) {
            entries.add(new Entry(t, twoBytesToPitchRollData(data[i], data[i + 1])));
            t += 0.01; // Accelerometer data sampled at 100 Hz
        }
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);

        chart.setData(new LineData(dataSet));
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.invalidate();
    }

    private byte[] randomData(int length) {
        byte[] data = new byte[length * 2]; // 2 bytes per sample
        new Random().nextBytes(data);
        return data;
    }

    private int twoBytesToUnsignedInt(byte b1, byte b2) {
        return shortToUnsignedInt((short)((b1 << 8) | (b2 & 0xFF)));
    }

    private int shortToUnsignedInt(short s) {
        return s & 0xFFFF;
    }

    private float twoBytesToPitchRollData(byte b1, byte b2) {
        return (twoBytesToUnsignedInt(b1, b2) / PITCH_ROLL_RESOLUTION) * MAX_PITCH_ROLL_VALUE;
    }
}
