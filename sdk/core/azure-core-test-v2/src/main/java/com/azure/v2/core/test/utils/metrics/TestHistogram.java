// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils.metrics;

import com.azure.v2.core.utils.TelemetryAttributes;
import com.azure.v2.core.utils.metrics.DoubleHistogram;
import io.clientcore.core.utils.Context;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Test histogram implementation.
 */
public class TestHistogram implements DoubleHistogram {
    private final ConcurrentLinkedQueue<TestMeasurement<Double>> measurements = new ConcurrentLinkedQueue<>();
    private final boolean isEnabled;

    TestHistogram(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public void record(double value, TelemetryAttributes attributes, Context context) {
        if (isEnabled) {
            measurements.add(new TestMeasurement<>(value, (TestTelemetryAttributes) attributes, context));
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Gets all measurements reported with this histogram.
     *
     * @return list of all measurements
     */
    public List<TestMeasurement<Double>> getMeasurements() {
        return measurements.stream().collect(Collectors.toList());
    }
}
