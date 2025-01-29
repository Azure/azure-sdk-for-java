package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import java.util.ArrayList;
import java.util.HashMap;

public class OTelMetric {

    private String name;
    private ArrayList<OTelDataPoint> dataPoints;

    public OTelMetric(String name) {
        this.name = name;
        this.dataPoints = new ArrayList<>();
    }

    public void addDataPoint(double value, HashMap<String, String> dimensions) {
        OTelDataPoint dataPoint = new OTelDataPoint(value, dimensions);
        this.dataPoints.add(dataPoint);
    }

    public String getName() {
        return name;
    }

    public ArrayList<OTelDataPoint> getDataPoints() {
        return dataPoints;
    }

    public ArrayList<Double> getDataValues() {
        ArrayList<Double> values = new ArrayList<>();
        for (OTelDataPoint dataPoint : dataPoints) {
            values.add(dataPoint.getValue());
        }
        return values;
    }

}
