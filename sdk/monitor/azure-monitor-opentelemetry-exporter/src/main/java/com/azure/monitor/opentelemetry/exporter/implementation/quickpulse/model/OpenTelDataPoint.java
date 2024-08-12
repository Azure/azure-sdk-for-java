package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class OpenTelDataPoint {
    private double value;
    //private HashMap<String, String> dimensions;

    public OpenTelDataPoint(double value) {
        this.value = value;
        //this.dimensions = new HashMap<>();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
