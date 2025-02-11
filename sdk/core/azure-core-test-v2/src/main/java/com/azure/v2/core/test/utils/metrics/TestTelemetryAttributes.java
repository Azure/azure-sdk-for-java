// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils.metrics;

import com.azure.v2.core.util.TelemetryAttributes;

import java.util.Collections;
import java.util.Map;

/**
 * Test implementation of {@link TelemetryAttributes}
 */
public class TestTelemetryAttributes implements TelemetryAttributes {
    private final Map<String, Object> map;

    TestTelemetryAttributes(Map<String, Object> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    Map<String, Object> getAttributes() {
        return map;
    }
}
