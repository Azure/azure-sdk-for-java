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
 * <pre>
 *
 * &#47;&#47; Meter and instruments should be created along with service client instance and retained for the client
 * &#47;&#47; lifetime for optimal performance
 * Meter meter = meterProvider
 *     .createMeter&#40;&quot;azure-core&quot;, &quot;1.0.0&quot;, new MetricsOptions&#40;&#41;&#41;;
 *
 * DoubleHistogram amqpLinkDuration = meter
 *     .createDoubleHistogram&#40;&quot;az.core.amqp.link.duration&quot;, &quot;AMQP link response time.&quot;, &quot;ms&quot;&#41;;
 *
 * TelemetryAttributes attributes = defaultMeter.createAttributes&#40;
 *     Collections.singletonMap&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;&#41;;
 *
 * &#47;&#47; when measured operation starts, record the measurement
 * Instant start = Instant.now&#40;&#41;;
 *
 * doThings&#40;&#41;;
 *
 * &#47;&#47; optionally check if meter is operational for the best performance
 * if &#40;amqpLinkDuration.isEnabled&#40;&#41;&#41; &#123;
 *     amqpLinkDuration.record&#40;Instant.now&#40;&#41;.toEpochMilli&#40;&#41; - start.toEpochMilli&#40;&#41;, attributes, currentContext&#41;;
 * &#125;
 * </pre>
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
     * <pre>
     *
     * &#47;&#47; Meter and instruments should be created along with service client instance and retained for the client
     * &#47;&#47; lifetime for optimal performance
     * Meter meter = meterProvider
     *     .createMeter&#40;&quot;azure-core&quot;, &quot;1.0.0&quot;, new MetricsOptions&#40;&#41;&#41;;
     *
     * DoubleHistogram amqpLinkDuration = meter
     *     .createDoubleHistogram&#40;&quot;az.core.amqp.link.duration&quot;, &quot;AMQP link response time.&quot;, &quot;ms&quot;&#41;;
     *
     * TelemetryAttributes attributes = defaultMeter.createAttributes&#40;
     *     Collections.singletonMap&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;&#41;;
     *
     * &#47;&#47; when measured operation starts, record the measurement
     * Instant start = Instant.now&#40;&#41;;
     *
     * doThings&#40;&#41;;
     *
     * &#47;&#47; optionally check if meter is operational for the best performance
     * if &#40;amqpLinkDuration.isEnabled&#40;&#41;&#41; &#123;
     *     amqpLinkDuration.record&#40;Instant.now&#40;&#41;.toEpochMilli&#40;&#41; - start.toEpochMilli&#40;&#41;, attributes, currentContext&#41;;
     * &#125;
     * </pre>
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
     * <pre>
     * TelemetryAttributes attributes = defaultMeter.createAttributes&#40;new HashMap&lt;String, Object&gt;&#40;&#41; &#123;&#123;
     *         put&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;;
     *         put&#40;&quot;status&quot;, &quot;ok&quot;&#41;;
     *     &#125;&#125;&#41;;
     *
     * LongCounter createdHttpConnections = defaultMeter.createLongCounter&#40;&quot;az.core.http.connections&quot;,
     *     &quot;Number of created HTTP connections&quot;, null&#41;;
     *
     * createdHttpConnections.add&#40;1, attributes, currentContext&#41;;
     * </pre>
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
     * <pre>
     * TelemetryAttributes attributes = defaultMeter.createAttributes&#40;new HashMap&lt;String, Object&gt;&#40;&#41; &#123;&#123;
     *         put&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;;
     *         put&#40;&quot;status&quot;, &quot;ok&quot;&#41;;
     *     &#125;&#125;&#41;;
     *
     * LongCounter activeHttpConnections = defaultMeter.createLongUpDownCounter&#40;&quot;az.core.http.active.connections&quot;,
     *     &quot;Number of active HTTP connections&quot;, null&#41;;
     *
     * &#47;&#47; on connection initialized:
     * activeHttpConnections.add&#40;1, attributes, currentContext&#41;;
     *
     * &#47;&#47; on connection closed:
     * activeHttpConnections.add&#40;-1, attributes, currentContext&#41;;
     * </pre>
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
     * <pre>
     * TelemetryAttributes attributes = defaultMeter.createAttributes&#40;new HashMap&lt;String, Object&gt;&#40;&#41; &#123;&#123;
     *         put&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;;
     *         put&#40;&quot;container&quot;, &quot;my-container&quot;&#41;;
     *     &#125;&#125;&#41;;
     *
     * LongGauge latestSequenceNumber = defaultMeter.createLongGauge&#40;&quot;az.eventhubs.consumer.sequence_number&quot;,
     *     &quot;Sequence number of the latest event received from the broker.&quot;, null&#41;;
     *
     * AutoCloseable subscription = latestSequenceNumber.registerCallback&#40;sequenceNumber::get, attributes&#41;;
     *
     * &#47;&#47; update value when event is received
     * sequenceNumber.set&#40;getSequenceNumber&#40;&#41;&#41;;
     *
     * try &#123;
     *     subscription.close&#40;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     e.printStackTrace&#40;&#41;;
     * &#125;
     * </pre>
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
     * <pre>
     *
     * &#47;&#47; Create attributes for possible error codes. Can be done lazily once specific error code is received.
     * TelemetryAttributes successAttributes = defaultMeter.createAttributes&#40;new HashMap&lt;String, Object&gt;&#40;&#41; &#123;&#123;
     *         put&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;;
     *         put&#40;&quot;error&quot;, true&#41;;
     *     &#125;&#125;&#41;;
     *
     * TelemetryAttributes errorAttributes =  defaultMeter.createAttributes&#40;new HashMap&lt;String, Object&gt;&#40;&#41; &#123;&#123;
     *         put&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;;
     *         put&#40;&quot;error&quot;, false&#41;;
     *     &#125;&#125;&#41;;
     *
     * LongCounter httpConnections = defaultMeter.createLongCounter&#40;&quot;az.core.http.connections&quot;,
     *     &quot;Number of created HTTP connections&quot;, null&#41;;
     *
     * boolean success = false;
     * try &#123;
     *     success = doThings&#40;&#41;;
     * &#125; finally &#123;
     *     httpConnections.add&#40;1, success ? successAttributes : errorAttributes, currentContext&#41;;
     * &#125;
     *
     * </pre>
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
