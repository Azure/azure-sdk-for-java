package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import java.util.ArrayList;

public class OTelMetric {

    private String name;
    private ArrayList<OTelDataPoint> dataPoints;

    public OTelMetric(String name) {
        this.name = name;
        this.dataPoints = new ArrayList<>();
    }

    public void addDataPoint(double value) {
        OTelDataPoint dataPoint = new OTelDataPoint(value);
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
