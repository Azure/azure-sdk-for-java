// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceDataColumns implements TelemetryColumns {
    private final Map<String, Object> mapping = new HashMap<>();

    private final CustomDimensions customDims;

    public TraceDataColumns(MessageData traceData) {
        customDims = new CustomDimensions(traceData.getProperties(), traceData.getMeasurements());
        mapping.put(KnownTraceColumns.MESSAGE, traceData.getMessage());
    }

    // to be used in tests only
    public TraceDataColumns(String message, Map<String, String> dims, Map<String, Double> measurements) {
        customDims = new CustomDimensions(dims, measurements);
        mapping.put(KnownTraceColumns.MESSAGE, message);
    }

    public boolean checkAllCustomDims(FilterInfo filter, TelemetryColumns data) {
        return customDims.matchesAnyFieldInCustomDimensions(filter);
    }

    public boolean checkCustomDimFilter(FilterInfo filter, TelemetryColumns data, String trimmedFieldName) {
        return customDims.matchesCustomDimFilter(filter, trimmedFieldName);
    }

    public <T> T getFieldValue(String fieldName, Class<T> type) {
        return type.cast(mapping.get(fieldName));
    }

    public List<String> getAllFieldValuesAsString() {
        List<String> result = new ArrayList<>();
        result.add((String) mapping.get(KnownTraceColumns.MESSAGE));
        return result;
    }

    public double getCustomDimValueForProjection(String key) {
        return customDims.getCustomDimValueForProjection(key);
    }
}
