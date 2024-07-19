package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

public class OpenTelDataPoint {
    private double value;

    public OpenTelDataPoint(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
