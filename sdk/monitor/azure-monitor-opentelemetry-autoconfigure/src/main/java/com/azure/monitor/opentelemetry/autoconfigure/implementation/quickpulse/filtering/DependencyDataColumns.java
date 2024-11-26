// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class DependencyDataColumns implements TelemetryColumns {
    private final CustomDimensions customDims;
    private final Map<String, Object> mapping = new HashMap<>();

    public DependencyDataColumns(RemoteDependencyData rdData) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(rdData.getProperties(), rdData.getMeasurements());
        mapping.put(KnownDependencyColumns.TARGET, rdData.getTarget());
        mapping.put(KnownDependencyColumns.DURATION,
            FormattedDuration.getDurationFromTelemetryItemDurationString(rdData.getDuration()));
        mapping.put(KnownDependencyColumns.SUCCESS, rdData.isSuccess());
        mapping.put(KnownDependencyColumns.NAME, rdData.getName());
        int resultCode;
        try {
            resultCode = Integer.parseInt(rdData.getResultCode());
        } catch (NumberFormatException e) {
            resultCode = -1;
        }
        mapping.put(KnownDependencyColumns.RESULT_CODE, resultCode);
        mapping.put(KnownDependencyColumns.TYPE, rdData.getType());
        mapping.put(KnownDependencyColumns.DATA, rdData.getData());
    }

    // To be used for tests only
    public DependencyDataColumns(String target, long duration, boolean success, String name, int resultCode,
        String type, String data, Map<String, String> dims, Map<String, Double> measurements) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(dims, measurements);
        mapping.put(KnownDependencyColumns.TARGET, target);
        mapping.put(KnownDependencyColumns.DURATION, duration);
        mapping.put(KnownDependencyColumns.SUCCESS, success);
        mapping.put(KnownDependencyColumns.NAME, name);
        mapping.put(KnownDependencyColumns.RESULT_CODE, resultCode);
        mapping.put(KnownDependencyColumns.TYPE, type);
        mapping.put(KnownDependencyColumns.DATA, data);
    }

    public <T> T getFieldValue(String fieldName, Class<T> type) {
        return type.cast(mapping.get(fieldName));
    }

    public Map<String, String> getCustomDimensions() {
        return this.customDims.getCustomDimensions();
    }

    public List<String> getAllFieldValuesAsString() {
        List<String> result = new ArrayList<>();
        for (Object value : mapping.values()) {
            if (value instanceof String) {
                result.add((String) value);
            } else if (value instanceof Integer) {
                result.add(((Integer) value).toString());
            } else if (value instanceof Long) {
                result.add(((Long) value).toString());
            } else { // boolean
                result.add(((Boolean) value).toString());
            }
        }
        return result;
    }

}
