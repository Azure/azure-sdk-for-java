// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;

import java.util.List;

public interface TelemetryColumns {
    <T> T getFieldValue(String fieldName, Class<T> type);

    List<String> getAllFieldValuesAsString();

    boolean checkAllCustomDims(FilterInfo filter, TelemetryColumns data);

    boolean checkCustomDimFilter(FilterInfo filter, TelemetryColumns data, String trimmedFieldName);

    double getCustomDimValueForProjection(String key);

}
