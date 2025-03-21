package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import java.util.HashMap;

public class OTelDataPoint {
    private double value;
    private HashMap<String, String> dimensions;

    public OTelDataPoint(double value, HashMap<String, String> dimensions) {
        this.value = value;
        this.dimensions = dimensions;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public HashMap<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(HashMap<String, String> dimensions) {
        this.dimensions = dimensions;
    }

}
