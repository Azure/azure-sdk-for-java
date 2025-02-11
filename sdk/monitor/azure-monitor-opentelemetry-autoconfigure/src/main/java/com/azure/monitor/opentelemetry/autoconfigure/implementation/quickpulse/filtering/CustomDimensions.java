// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;

import java.util.HashMap;
import java.util.Map;

public class CustomDimensions {
    private final Map<String, String> customDimensions;
    private static final ClientLogger LOGGER = new ClientLogger(CustomDimensions.class);

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

    public boolean matchesAnyFieldInCustomDimensions(FilterInfo filter) {
        for (String value : customDimensions.values()) {
            if (Filter.stringCompare(value, filter.getComparand(), filter.getPredicate())) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesCustomDimFilter(FilterInfo filter, String trimmedFieldName) {
        if (customDimensions.containsKey(trimmedFieldName)) {
            String value = customDimensions.get(trimmedFieldName);
            return Filter.stringCompare(value, filter.getComparand(), filter.getPredicate());
        } else {
            return false; // the asked for field is not present in the custom dimensions
        }
    }

    public double getCustomDimValueForProjection(String key) {
        if (customDimensions.containsKey(key)) {
            String value = customDimensions.get(key);
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                LOGGER.verbose(
                    "The value \"{}\" for the custom dimension \"{}\" could not be converted to a numeric value for a derived metric projection",
                    value, key);
            }
            return Double.NaN;
        }
        LOGGER.verbose(
            "The custom dimension could not be found in this telemetry item when calculating a derived metric.");
        return Double.NaN;
    }

}
