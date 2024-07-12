// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.metrics;

import com.azure.core.v2.util.TelemetryAttributes;

import java.util.Map;

/**
 * Meter is generally associated with Azure Service Client instance and allows creating
 * instruments that represent individual metrics such as number of active connections or
 * HTTP call latency.
 *
 * Choose instrument kind based on OpenTelemetry guidelines:
 * https://opentelemetry.io/docs/reference/specification/metrics/api/#counter-creation
 *
 * This class is intended to be used by Azure client libraries and provides abstraction over different metrics
 * implementations.
 * Application developers should use metrics implementations such as OpenTelemetry or Micrometer directly.
 *
 * <!-- src_embed com.azure.core.util.metrics.Meter.doubleHistogram -->
 * <!-- end com.azure.core.util.metrics.Meter.doubleHistogram -->
 */
public interface Meter extends AutoCloseable {
    /**
     * Creates histogram instrument allowing to record long values. Histograms should be used for latency or other measurements where
     * distribution of values is important and values are statistically bounded.
     *
     * See https://opentelemetry.io/docs/reference/specification/metrics/api/#histogram for more details.
     *
     * <!-- src_embed com.azure.core.util.metrics.Meter.doubleHistogram -->
     * <!-- end com.azure.core.util.metrics.Meter.doubleHistogram -->
     *
     * @param name short histogram name following https://opentelemetry.io/docs/reference/specification/metrics/api/#instrument-naming-rule
     * @param description free-form text describing the instrument
     * @param unit optional unit of measurement.
     * @return new instance of {@link DoubleHistogram}
     * @throws NullPointerException if name or description is null.
     */
    DoubleHistogram createDoubleHistogram(String name, String description, String unit);

    /**
     * Creates Counter instrument that is used to record incrementing values, such as number of sent messages or created
     * connections.
     *
     * Use {@link Meter#createLongUpDownCounter(String, String, String)} for counters that can go down,
     * such as number of active connections or queue size.
     *
     * See https://opentelemetry.io/docs/reference/specification/metrics/api/#counter for more details.
     *
     * <!-- src_embed com.azure.core.util.metrics.Meter.longCounter -->
     * <!-- end com.azure.core.util.metrics.Meter.longCounter -->
     *
     * @param name short counter  name following https://opentelemetry.io/docs/reference/specification/metrics/api/#instrument-naming-rule
     * @param description free-form text describing the counter
     * @param unit optional unit of measurement.
     * @return new instance of {@link LongCounter}
     * @throws NullPointerException if name or description is null.
     */
    LongCounter createLongCounter(String name, String description, String unit);

    /**
     * Creates UpDownCounter instrument that is used to record values that can go up or down, such as number of active
     * connections or queue size.
     *
     * See https://opentelemetry.io/docs/reference/specification/metrics/api/#updowncounter for more details.
     *
     * <!-- src_embed com.azure.core.util.metrics.Meter.upDownCounter -->
     * <!-- end com.azure.core.util.metrics.Meter.upDownCounter -->
     *
     * @param name short counter name following https://opentelemetry.io/docs/reference/specification/metrics/api/#instrument-naming-rule
     * @param description free-form text describing the counter
     * @param unit optional unit of measurement.
     * @return new instance of {@link LongCounter}
     * @throws NullPointerException if name or description is null.
     */
    LongCounter createLongUpDownCounter(String name, String description, String unit);

    /**
     * Creates {@link LongGauge} instrument that is used to asynchronously record current value of metric.
     *
     * See https://opentelemetry.io/docs/reference/specification/metrics/api/#asynchronous-gauge for more details.
     *
     * <!-- src_embed com.azure.core.util.metrics.Meter.longGauge -->
     * <!-- end com.azure.core.util.metrics.Meter.longGauge -->
     *
     * @param name short counter  name following https://opentelemetry.io/docs/reference/specification/metrics/api/#instrument-naming-rule
     * @param description free-form text describing the counter
     * @param unit optional unit of measurement.
     * @return new instance of {@link LongGauge}
     * @throws NullPointerException if name or description is null.
     */
    default LongGauge createLongGauge(String name, String description, String unit) {
        return DefaultMeterProvider.NOOP_GAUGE;
    }

    /**
     * Creates and returns attribute collection implementation specific to the meter implementation.
     * Attribute collections differ in how they support different types of attributes and internal
     * data structures they use.
     *
     * For the best performance, client libraries should create and cache attribute collections
     * for the client lifetime and pass cached instance when recoding new measurements.
     *
     * <!-- src_embed com.azure.core.util.metrics.Meter.longCounter#errorFlag -->
     * <!-- end com.azure.core.util.metrics.Meter.longCounter#errorFlag -->
     * @param attributeMap map of key value pairs to cache.
     * @return an instance of {@code AttributesBuilder}
     */
    TelemetryAttributes createAttributes(Map<String, Object> attributeMap);

    /**
     * Checks if Meter implementation was found, and it's enabled.
     *
     * @return true if Meter is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * {@inheritDoc}
     */
    @Override
    void close();
}
