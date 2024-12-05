// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceDataColumns implements TelemetryColumns {
    private final Map<String, Object> mapping = new HashMap<>();

    private final CustomDimensions customDims;

    public TraceDataColumns(MessageData traceData) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(traceData.getProperties(), traceData.getMeasurements());
        mapping.put(KnownTraceColumns.MESSAGE, traceData.getMessage());
    }

    // to be used in tests only
    public TraceDataColumns(String message, Map<String, String> dims, Map<String, Double> measurements) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(dims, measurements);
        mapping.put(KnownTraceColumns.MESSAGE, message);
    }

    public Map<String, String> getCustomDimensions() {
        return this.customDims.getCustomDimensions();
    }

    public <T> T getFieldValue(String fieldName, Class<T> type) {
        return type.cast(mapping.get(fieldName));
    }

    public List<String> getAllFieldValuesAsString() {
        List<String> result = new ArrayList<>();
        result.add((String) mapping.get(KnownTraceColumns.MESSAGE));
        return result;
    }
}
