// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RequestDataColumns implements TelemetryColumns {
    private final Map<String, Object> mapping = new HashMap<>();
    private final CustomDimensions customDims;

    private static final ClientLogger LOGGER = new ClientLogger(RequestDataColumns.class);

    public RequestDataColumns(RequestData requestData) {
        customDims = new CustomDimensions(requestData.getProperties(), requestData.getMeasurements());
        mapping.put(KnownRequestColumns.URL, requestData.getUrl());
        mapping.put(KnownRequestColumns.SUCCESS, requestData.isSuccess());

        long durationMicroSec = FormattedDuration.getDurationFromTelemetryItemDurationString(requestData.getDuration());
        if (durationMicroSec == -1) {
            LOGGER.verbose("The provided timestamp {} could not be converted to microseconds",
                requestData.getDuration());
        }

        mapping.put(KnownRequestColumns.DURATION, durationMicroSec);
        mapping.put(KnownRequestColumns.NAME, requestData.getName());
        int responseCode;
        try {
            responseCode = Integer.parseInt(requestData.getResponseCode());
        } catch (NumberFormatException e) {
            responseCode = -1;
            LOGGER.verbose("The provided response code {} could not be converted to a numeric value",
                requestData.getResponseCode());
        }
        mapping.put(KnownRequestColumns.RESPONSE_CODE, responseCode);
    }

    // To be used in tests only
    public RequestDataColumns(String url, long duration, int responseCode, boolean success, String name,
        Map<String, String> dims, Map<String, Double> measurements) {
        customDims = new CustomDimensions(dims, measurements);
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

    public boolean checkAllCustomDims(FilterInfo filter, TelemetryColumns data) {
        return customDims.matchesAnyFieldInCustomDimensions(filter);
    }

    public boolean checkCustomDimFilter(FilterInfo filter, TelemetryColumns data, String trimmedFieldName) {
        return customDims.matchesCustomDimFilter(filter, trimmedFieldName);
    }

    public double getCustomDimValueForProjection(String key) {
        return customDims.getCustomDimValueForProjection(key);
    }

}
