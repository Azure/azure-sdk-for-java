// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import java.util.List;
import java.util.Map;

public interface TelemetryColumns {
    <T> T getFieldValue(String fieldName, Class<T> type);

    List<String> getAllFieldValuesAsString();

    Map<String, String> getCustomDimensions();

}
