// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils.metrics;

import com.typespec.core.util.TelemetryAttributes;
import com.typespec.core.util.metrics.DoubleHistogram;
import com.typespec.core.util.metrics.LongCounter;
import com.typespec.core.util.metrics.LongGauge;
import com.typespec.core.util.metrics.Meter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test meter implementation.
 */
public class TestMeter implements Meter {
    private final Map<String, TestHistogram> histograms = new ConcurrentHashMap<>();
    private final Map<String, TestCounter> counters = new ConcurrentHashMap<>();
    private final Map<String, TestGauge> gauges = new ConcurrentHashMap<>();
    private final Map<String, TestCounter> upDownCounters = new ConcurrentHashMap<>();

    private final boolean isEnabled;

    /**
     * Creates test meter.
     */
    public TestMeter() {
        this(true);
    }

    /**
     * Creates test meter
     *
     * @param isEnabled flag indicating if meter should be enabled.
     */
    public TestMeter(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public DoubleHistogram createDoubleHistogram(String name, String description, String unit) {
        return histograms.computeIfAbsent(name, n -> new TestHistogram(isEnabled));
    }

    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        return counters.computeIfAbsent(name, n -> new TestCounter(isEnabled));
    }

    @Override
    public LongCounter createLongUpDownCounter(String name, String description, String unit) {
        return upDownCounters.computeIfAbsent(name, n -> new TestCounter(isEnabled));
    }

    @Override
    public LongGauge createLongGauge(String name, String description, String unit) {
        return gauges.computeIfAbsent(name, n -> new TestGauge(isEnabled));
    }

    @Override
    public TelemetryAttributes createAttributes(Map<String, Object> attributeMap) {
        return new TestTelemetryAttributes(attributeMap);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void close() {
    }

    /**
     * Gets histograms created with this meter.
     *
     * @return map of histograms (by histogram name)
     */
    public Map<String, TestHistogram> getHistograms() {
        return histograms;
    }

    /**
     * Gets counters created with this meter.
     *
     * @return map of counters (by counter name)
     */
    public Map<String, TestCounter> getCounters() {
        return counters;
    }

    /**
     * Gets up-down counters created with this meter.
     *
     * @return map of counters (by counter name)
     */
    public Map<String, TestCounter> getUpDownCounters() {
        return upDownCounters;
    }

    /**
     * Gets gauges created with this meter.
     *
     * @return map of counters (by gauge name)
     */
    public Map<String, TestGauge> getGauges() {
        return gauges;
    }

}

