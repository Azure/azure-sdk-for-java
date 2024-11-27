// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class ExceptionDataColumns implements TelemetryColumns {

    private final CustomDimensions customDims;

    private final Map<String, Object> mapping = new HashMap<>();

    public ExceptionDataColumns(TelemetryExceptionData exceptionData) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(exceptionData.getProperties(), exceptionData.getMeasurements());
        List<TelemetryExceptionDetails> details = exceptionData.getExceptions();
        mapping.put(KnownExceptionColumns.MESSAGE,
            details != null && !details.isEmpty() ? details.get(0).getMessage() : "");
        mapping.put(KnownExceptionColumns.STACK,
            details != null && !details.isEmpty() ? details.get(0).getStack() : "");
    }

    // To be used in tests only
    public ExceptionDataColumns(String message, String stackTrace, Map<String, String> dims,
        Map<String, Double> measurements) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(dims, measurements);
        mapping.put(KnownExceptionColumns.MESSAGE, message);
        mapping.put(KnownExceptionColumns.STACK, stackTrace);
    }

    public Map<String, String> getCustomDimensions() {
        return this.customDims.getCustomDimensions();
    }

    public <T> T getFieldValue(String fieldName, Class<T> type) {
        return type.cast(mapping.get(fieldName));
    }

    public List<String> getAllFieldValuesAsString() {
        List<String> result = new ArrayList<>();
        for (Object value : mapping.values()) {
            result.add((String) value);
        }
        return result;
    }
}
