// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils.metrics;

import com.typespec.core.util.Context;

import java.util.Map;

/**
 * Test implementation of metric measurements allowing to verify what measurements were reported.
 */
public class TestMeasurement<T> {
    private final T value;
    private final TestTelemetryAttributes attributes;
    private final Context context;

    TestMeasurement(T value, TestTelemetryAttributes attributes, Context context) {
        this.value = value;
        this.attributes = attributes;
        this.context = context;
    }

    /**
     * Returns value this measurement was reported with.
     *
     * @return reported value.
     */
    public T getValue() {
        return value;
    }

    /**
     * Returns attributes this measurement was reported with.
     *
     * @return attribute map.
     */
    public Map<String, Object> getAttributes() {
        return attributes.getAttributes();
    }

    /**
     * Returns context this measurement was reported with.
     *
     * @return context.
     */
    public Context getContext() {
        return context;
    }
}
