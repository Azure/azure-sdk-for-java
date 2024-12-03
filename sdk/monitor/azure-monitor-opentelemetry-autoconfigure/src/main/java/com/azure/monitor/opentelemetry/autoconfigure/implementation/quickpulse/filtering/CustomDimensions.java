// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;

import java.util.HashMap;
import java.util.Map;

public class CustomDimensions {
    private Map<String, String> customDimensions;

    public CustomDimensions(Map<String, String> customDimensions, Map<String, Double> customMeasurements) {
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

    public boolean checkAllCustomDims(FilterInfo filter, TelemetryColumns data) {
        for (String value : customDimensions.values()) {
            if (Filter.stringCompare(value, filter.getComparand(), filter.getPredicate())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkCustomDimFilter(FilterInfo filter, TelemetryColumns data, String trimmedFieldName) {
        if (customDimensions.containsKey(trimmedFieldName)) {
            String value = customDimensions.get(trimmedFieldName);
            return Filter.stringCompare(value, filter.getComparand(), filter.getPredicate());
        } else {
            return false; // the asked for field is not present in the custom dimensions
        }
    }

}
