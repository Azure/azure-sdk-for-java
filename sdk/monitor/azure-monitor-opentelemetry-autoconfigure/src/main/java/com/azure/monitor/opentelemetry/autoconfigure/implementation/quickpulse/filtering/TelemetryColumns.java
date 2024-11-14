package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import java.util.Map;

public class TelemetryColumns {
    private Map<String, String> customDimensions;

    public TelemetryColumns() {

    }

    public void setCustomDimensions(Map<String, String> customDimensions, Map<String, Double> customMeasurements) {
        for ()
        this.customDimensions = customDimensions;
    }

    public Map<String, String> getCustomDimensions() {
        return this.customDimensions;
    }

}
