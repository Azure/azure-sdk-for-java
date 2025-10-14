// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyDataColumns implements TelemetryColumns {
    private final CustomDimensions customDims;
    private final Map<String, Object> mapping = new HashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(DependencyDataColumns.class);

    public DependencyDataColumns(RemoteDependencyData rdData) {
        customDims = new CustomDimensions(rdData.getProperties(), rdData.getMeasurements());
        mapping.put(KnownDependencyColumns.TARGET, rdData.getTarget());

        long durationMicroSec = FormattedDuration.getDurationFromTelemetryItemDurationString(rdData.getDuration());
        if (durationMicroSec == -1) {
            LOGGER.verbose("The provided timestamp {} could not be converted to microseconds", rdData.getDuration());
        }
        mapping.put(KnownDependencyColumns.DURATION, durationMicroSec);

        mapping.put(KnownDependencyColumns.SUCCESS, rdData.isSuccess());
        mapping.put(KnownDependencyColumns.NAME, rdData.getName());
        int resultCode;
        try {
            resultCode = Integer.parseInt(rdData.getResultCode());
        } catch (NumberFormatException e) {
            LOGGER.verbose("The provided result code {} could not be converted to a numeric value",
                rdData.getResultCode());
            resultCode = -1;
        }
        mapping.put(KnownDependencyColumns.RESULT_CODE, resultCode);
        mapping.put(KnownDependencyColumns.TYPE, rdData.getType());
        mapping.put(KnownDependencyColumns.DATA, rdData.getData());
    }

    // To be used for tests only
    public DependencyDataColumns(String target, long duration, boolean success, String name, int resultCode,
        String type, String data, Map<String, String> dims, Map<String, Double> measurements) {
        customDims = new CustomDimensions(dims, measurements);
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
