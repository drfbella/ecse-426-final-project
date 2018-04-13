package com.group08.ecse426finalproject.accelerometer;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.group08.ecse426finalproject.R;
import com.group08.ecse426finalproject.utils.ByteUtils;
import com.group08.ecse426finalproject.utils.ResourceAccessor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.group08.ecse426finalproject.utils.Constants.PITCH_DATA_NAME;
import static com.group08.ecse426finalproject.utils.Constants.ROLL_DATA_NAME;

public class AccelerometerActivity extends AppCompatActivity {
    private static final String TAG = "AccelerometerActivity";
    private static final int NUM_SAMPLES = 1000 + 1; // 10 seconds at 100 Hz
    private static final String PLOTLY_GRIDS_URL = "https://api.plot.ly/v2/grids";
    private static final String PLOTLY_PLOTS_URL = "https://api.plot.ly/v2/plots";
    private ResourceAccessor resourceAccessor;
    private TextView textPlotlyPitchLink;
    private TextView textPlotlyRollLink;
    private ProgressBar pitchProgressBar;
    private ProgressBar rollProgressBar;
    private ByteUtils byteUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        resourceAccessor = new ResourceAccessor(this);
        byteUtils = new ByteUtils();

        byte[] pitchData = getIntent().getByteArrayExtra(PITCH_DATA_NAME);
        byte[] rollData = getIntent().getByteArrayExtra(ROLL_DATA_NAME);

        Log.d(TAG, "Received pitch data: " + Arrays.toString(pitchData));
        Log.d(TAG, "Received roll data: " + Arrays.toString(rollData));

//        pitchData = randomData(NUM_SAMPLES);
//        rollData = randomData(NUM_SAMPLES);

        textPlotlyPitchLink = findViewById(R.id.text_pitch_plotly_link);
        textPlotlyRollLink = findViewById(R.id.text_roll_plotly_link);

        pitchProgressBar = findViewById(R.id.pitch_progress_bar);
        rollProgressBar = findViewById(R.id.roll_progress_bar);

        LineChart pitchChart = findViewById(R.id.pitch_chart);
        LineChart rollChart = findViewById(R.id.roll_chart);

        List<List<Float>> cols = decodePitchRollData(pitchData, rollData);

        List<Float> timeData = cols.get(0);
        List<Float> convertedPitchData = cols.get(1);
        List<Float> convertedRollData = cols.get(2);

        Log.d(TAG, "Converted pitch data: " + convertedPitchData);
        Log.d(TAG, "Converted roll data: " + convertedRollData);

        setChartData(pitchChart, timeData, convertedPitchData, "pitch", Color.RED);
        setChartData(rollChart, timeData, convertedRollData, "roll", Color.BLUE);

        sendPlotlyGridAndPlotRequest(timeData, convertedPitchData, convertedRollData);
    }

    private void setChartData(LineChart chart, List<Float> timeData, List<Float> measuredData,
                              String label, int color) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < timeData.size(); i += 2) {
            entries.add(new Entry(timeData.get(i), measuredData.get(i)));
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

    private List<List<Float>> decodePitchRollData(byte[] pitchData, byte[] rollData) {
        List<Float> timeData = new ArrayList<>();
        List<Float> convertedPitchData = new ArrayList<>();
        List<Float> convertedRollData = new ArrayList<>();

        float t = 0;
        for (int i = 0; i < pitchData.length; i += 2) {
            timeData.add(t);
            convertedPitchData.add(byteUtils.twoBytesToPitchRollData(pitchData[i], pitchData[i + 1]));
            convertedRollData.add(byteUtils.twoBytesToPitchRollData(rollData[i], rollData[i + 1]));
            t += 0.01;
        }

        List<List<Float>> columns = new ArrayList<>();
        columns.add(timeData);
        columns.add(convertedPitchData);
        columns.add(convertedRollData);
        return columns;
    }

    public void sendPlotlyGridAndPlotRequest(final List<Float> timeData, final List<Float> pitchData,
                                             final List<Float> rollData) {
        final RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonGridData = null;
        try {
            jsonGridData = new JSONObject()
                    .put("data", new JSONObject()
                            .put("cols", new JSONObject()
                                    .put("time", new JSONObject()
                                            .put("data", new JSONArray(timeData))
                                            .put("order", 0))
                                    .put("pitch", new JSONObject()
                                            .put("data", new JSONArray(pitchData))
                                            .put("order", 1))
                                    .put("roll", new JSONObject()
                                            .put("data", new JSONArray(rollData))
                                            .put("order", 2))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequestGrids = new JsonObjectRequest(Request.Method.POST, PLOTLY_GRIDS_URL,
                jsonGridData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "plotly grid API call success" + response.toString());
                sendPitchRollPlotRequest(queue, response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textPlotlyPitchLink.setText("Plotly grid request failed.");
                        textPlotlyRollLink.setText("Plotly grid request failed.");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>(super.getHeaders());
                headers.put("Plotly-Client-Platform", "curl");
                String credentials = "seanstappas:" + resourceAccessor.readRawResourceString(R.raw.plotly_api_key);
                headers.put("Authorization", "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP));
                return headers;
            }
        };
        queue.add(stringRequestGrids);
        textPlotlyPitchLink.setText(R.string.grid_request_sent);
        textPlotlyRollLink.setText(R.string.grid_request_sent);
        pitchProgressBar.setProgress(1);
        rollProgressBar.setProgress(1);
    }

    private void sendPitchRollPlotRequest(RequestQueue queue, JSONObject gridResponse) {
        String gridFID = null, timeColUID = null, pitchColUID = null, rollColUID = null;
        try {
            JSONObject file = gridResponse.getJSONObject("file");
            JSONArray colsArray = file.getJSONArray("cols");

            gridFID = file.getString("fid");
            timeColUID = colsArray.getJSONObject(0)
                    .getString("uid");
            pitchColUID = colsArray.getJSONObject(1)
                    .getString("uid");
            rollColUID = colsArray.getJSONObject(2)
                    .getString("uid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendPlotRequest(queue, gridFID, timeColUID, pitchColUID, "rgb(255, 0, 0)",
                "pitch", textPlotlyPitchLink, pitchProgressBar);
        sendPlotRequest(queue, gridFID, timeColUID, rollColUID, "rgb(0, 0, 255)",
                "roll", textPlotlyRollLink, rollProgressBar);
    }

    private void sendPlotRequest(RequestQueue queue, String gridFID, String timeColUID,
                                 String dataColUID, String color, String dataName,
                                 final TextView textLink, final ProgressBar progressBar) {
        JSONObject jsonGridData = null;
        try {
            jsonGridData = new JSONObject()
                    .put("figure", new JSONObject()
                            .put("filename", dataName)
                            .put("data", new JSONArray()
                                    .put(new JSONObject()
                                            .put("xsrc", gridFID + ':' + timeColUID)
                                            .put("ysrc",gridFID + ':' + dataColUID)
                                            .put("mode", "lines")
                                            .put("marker", new JSONObject()
                                                    .put("line", new JSONObject()
                                                            .put("color", color)
                                                            .put("width", 0.5))
                                                    .put("symbol", "dot")
                                                    .put("size", 10))
                                            .put("type", "scatter")
                                            .put("name", dataName)
                                            .put("xaxis", "time")
                                            .put("yaxis", dataName)
                                    )))
                    .put("world_readable", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequestPlot = new JsonObjectRequest(Request.Method.POST,
                PLOTLY_PLOTS_URL, jsonGridData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "plotly plot API call success" + response.toString());
                try {
                    String plotlyURL = response.getJSONObject("file").getString("web_url");
                    textLink.setText(plotlyURL);
                    progressBar.setProgress(3);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textLink.setText("Plotly plot request failed.");
                        progressBar.setVisibility(View.GONE);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>(super.getHeaders());
                headers.put("Plotly-Client-Platform", "curl");
                String credentials = "seanstappas:" + resourceAccessor.readRawResourceString(R.raw.plotly_api_key);
                headers.put("Authorization", "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP));
                return headers;
            }
        };
        queue.add(stringRequestPlot);
        textLink.setText(R.string.plot_request_sent);
        progressBar.setProgress(2);
    }
}