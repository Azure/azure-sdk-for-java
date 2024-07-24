package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import java.util.ArrayList;
import java.time.LocalDateTime;

public class OpenTelMetric {

    private String name;
    private ArrayList<OpenTelDataPoint> dataPoints;
    //private LocalDateTime lastTimestamp;

    public OpenTelMetric(String name) {
        this.name = name;
        this.dataPoints = new ArrayList<>();
        //this.lastTimestamp = LocalDateTime.now();
    }

    public void addDataPoint(double value) {
        OpenTelDataPoint dataPoint = new OpenTelDataPoint(value);
        this.dataPoints.add(dataPoint);
        //this.updateLastTimestamp();
    }

    public String getName() {
        return name;
    }

    public ArrayList<OpenTelDataPoint> getDataPoints() {
        return dataPoints;
    }

    public ArrayList<Double> getDataValues() {
        ArrayList<Double> values = new ArrayList<>();
        for (OpenTelDataPoint dataPoint : dataPoints) {
            values.add(dataPoint.getValue());
        }
        return values;
    }

     /*
    public LocalDateTime getLastTimestamp() {
        return lastTimestamp;
    }


    public void updateLastTimestamp() {
        this.lastTimestamp = LocalDateTime.now();
    }
    */

    public void clearDataPoints() {
        this.dataPoints.clear();
    }

}
