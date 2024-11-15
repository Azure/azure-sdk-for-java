package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import java.util.HashMap;
import java.util.Map;

public class TelemetryColumns {
    private Map<String, String> CustomDimensions;

    public TelemetryColumns() {

    }

    public void setCustomDimensions(Map<String, String> customDimensions, Map<String, Double> customMeasurements) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.putAll(customDimensions);
        for (Map.Entry<String, Double> cmEntry: customMeasurements.entrySet()) {
            resultMap.put(cmEntry.getKey(), cmEntry.getValue().toString());
        }
        this.CustomDimensions = resultMap;
    }

    public Map<String, String> getCustomDimensions() {
        return this.CustomDimensions;
    }

}
