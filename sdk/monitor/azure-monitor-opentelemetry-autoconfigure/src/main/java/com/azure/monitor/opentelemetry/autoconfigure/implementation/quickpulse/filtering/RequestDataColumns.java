// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class RequestDataColumns implements TelemetryColumns {
    private final Map<String, Object> mapping = new HashMap<>();
    private final CustomDimensions customDims;

    public RequestDataColumns(RequestData requestData) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(requestData.getProperties(), requestData.getMeasurements());
        mapping.put(KnownRequestColumns.URL, requestData.getUrl());
        mapping.put(KnownRequestColumns.SUCCESS, requestData.isSuccess());
        mapping.put(KnownRequestColumns.DURATION,
            FormattedDuration.getDurationFromTelemetryItemDurationString(requestData.getDuration()));
        mapping.put(KnownRequestColumns.NAME, requestData.getName());
        int responseCode;
        try {
            responseCode = Integer.parseInt(requestData.getResponseCode());
        } catch (NumberFormatException e) {
            responseCode = -1;
        }
        mapping.put(KnownRequestColumns.RESPONSE_CODE, responseCode);
    }

    // To be used in tests only
    public RequestDataColumns(String url, long duration, int responseCode, boolean success, String name,
        Map<String, String> dims, Map<String, Double> measurements) {
        customDims = new CustomDimensions();
        customDims.setCustomDimensions(dims, measurements);
        mapping.put(KnownRequestColumns.URL, url);
        mapping.put(KnownRequestColumns.SUCCESS, success);
        mapping.put(KnownRequestColumns.DURATION, duration);
        mapping.put(KnownRequestColumns.NAME, name);
        mapping.put(KnownRequestColumns.RESPONSE_CODE, responseCode);
    }

    public <T> T getFieldValue(String fieldName, Class<T> type) {
        return type.cast(mapping.get(fieldName));
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

    public Map<String, String> getCustomDimensions() {
        return this.customDims.getCustomDimensions();
    }

}
