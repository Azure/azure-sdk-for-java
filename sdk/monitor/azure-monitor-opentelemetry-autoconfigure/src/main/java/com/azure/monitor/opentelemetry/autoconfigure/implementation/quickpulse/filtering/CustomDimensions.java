// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import java.util.HashMap;
import java.util.Map;

public class CustomDimensions {
    private Map<String, String> customDimensions;

    public CustomDimensions() {
        this.customDimensions = new HashMap<String, String>();
    }

    public void setCustomDimensions(Map<String, String> customDimensions, Map<String, Double> customMeasurements) {
        Map<String, String> resultMap = new HashMap<>();
        if (customDimensions != null) {
            resultMap.putAll(customDimensions);
        }
        if (customMeasurements != null) {
            for (Map.Entry<String, Double> cmEntry : customMeasurements.entrySet()) {
                resultMap.put(cmEntry.getKey(), cmEntry.getValue().toString());
            }
        }
        this.customDimensions = resultMap;
    }

    public Map<String, String> getCustomDimensions() {
        return this.customDimensions;
    }
}
