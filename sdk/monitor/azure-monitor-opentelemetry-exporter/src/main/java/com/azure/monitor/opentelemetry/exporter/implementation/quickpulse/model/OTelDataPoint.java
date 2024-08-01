package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

public class OTelDataPoint {
    private double value;

    public OTelDataPoint(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
