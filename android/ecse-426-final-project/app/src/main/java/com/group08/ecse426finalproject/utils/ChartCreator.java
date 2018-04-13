package com.group08.ecse426finalproject.utils;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartCreator {
    public void setChartData(LineChart chart, List<Float> timeData, List<Float> measuredData,
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

    public void setChartData(ScatterChart chart, List<Float> timeData, List<Float> measuredData,
                             String label, int color) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < timeData.size(); i += 2) {
            entries.add(new Entry(timeData.get(i), measuredData.get(i)));
        }
        ScatterDataSet dataSet = new ScatterDataSet(entries, label);
        dataSet.setColor(color);

        chart.setData(new ScatterData(dataSet));
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.invalidate();
    }
}
