// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import java.util.Map;

/**
 * Meter is generally associated with Azure Service Client instance and allows creating
 * instruments that represent individual metrics such as number of active connections or
 * HTTP call latency.
 *
 * Choose instrument kind based on OpenTelemetry guidelines: https://opentelemetry.io/docs/reference/specification/metrics/api/#counter-creation
 *
 * This class is intended to be used by Azure client libraries and provides abstraction over different metrics implementations.
 * Application developers should use metrics implementations such as OpenTelemetry or Micrometer directly.
 *
 *  <!-- src_embed com.azure.core.util.metrics.AzureMeter.longHistogram -->
 * <pre>
 *
 * &#47;&#47; Meter and instruments should be created along with service client instance and retained for the client
 * &#47;&#47; lifetime for optimal performance
 * AzureMeter meter = AzureMeterProvider
 *     .getDefaultProvider&#40;&#41;
 *     .createMeter&#40;&quot;azure-core&quot;, &quot;1.0.0&quot;, new MetricsOptions&#40;&#41;&#41;;
 *
 * AzureLongHistogram amqpLinkDuration = meter
 *     .createLongHistogram&#40;&quot;az.core.amqp.link.duration&quot;, &quot;AMQP link response time.&quot;, &quot;ms&quot;,
 *         Collections.singletonMap&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;&#41;;
 *
 * &#47;&#47; when measured operation starts, record the measurement
 * Instant start = Instant.now&#40;&#41;;
 *
 * doThings&#40;&#41;;
 *
 * &#47;&#47; optionally check if meter is operational for the best performance
 * if &#40;meter.isEnabled&#40;&#41;&#41; &#123;
 *     amqpLinkDuration.record&#40;Instant.now&#40;&#41;.toEpochMilli&#40;&#41; - start.toEpochMilli&#40;&#41;, currentContext&#41;;
 * &#125;
 * </pre>
 *  <!-- end com.azure.core.util.metrics.AzureMeter.longHistogram -->
 */
public interface AzureMeter {
    /**
     * Creates histogram instrument allowing to record long values. Histograms should be used for latency or other measurements where
     * distribution of values is important and values are statistically bounded.
     *
     * See https://opentelemetry.io/docs/reference/specification/metrics/api/#histogram for more details.
     *
     * <!-- src_embed com.azure.core.util.metrics.AzureMeter.longHistogram -->
     * <pre>
     *
     * &#47;&#47; Meter and instruments should be created along with service client instance and retained for the client
     * &#47;&#47; lifetime for optimal performance
     * AzureMeter meter = AzureMeterProvider
     *     .getDefaultProvider&#40;&#41;
     *     .createMeter&#40;&quot;azure-core&quot;, &quot;1.0.0&quot;, new MetricsOptions&#40;&#41;&#41;;
     *
     * AzureLongHistogram amqpLinkDuration = meter
     *     .createLongHistogram&#40;&quot;az.core.amqp.link.duration&quot;, &quot;AMQP link response time.&quot;, &quot;ms&quot;,
     *         Collections.singletonMap&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;&#41;;
     *
     * &#47;&#47; when measured operation starts, record the measurement
     * Instant start = Instant.now&#40;&#41;;
     *
     * doThings&#40;&#41;;
     *
     * &#47;&#47; optionally check if meter is operational for the best performance
     * if &#40;meter.isEnabled&#40;&#41;&#41; &#123;
     *     amqpLinkDuration.record&#40;Instant.now&#40;&#41;.toEpochMilli&#40;&#41; - start.toEpochMilli&#40;&#41;, currentContext&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.core.util.metrics.AzureMeter.longHistogram -->
     *
     * @param name short histogram name following https://opentelemetry.io/docs/reference/specification/metrics/api/#instrument-naming-rule
     * @param description free-form text describing the instrument
     * @param unit optional unit of measurement.
     * @param attributes map of metric dimensions that includes low-cardinality values such as endpoint or error code.
     *                   Attributes can only be specified at counter creation time to avoid dynamic, high-cardinality values.
     * @return new instance of {@link AzureLongCounter}
     * @throws NullPointerException if name or description is null.
     */
    AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes);

    /**
     * Creates counter instrument allowing to record long values. Counters should only be used for incrementing values
     * such as number of sent messages or created connections.
     *
     * See https://opentelemetry.io/docs/reference/specification/metrics/api/#counter for more details.
     * <!-- src_embed com.azure.core.util.metrics.AzureMeter.longCounter -->
     * <pre>
     * Map&lt;String, Object&gt; attributes = new HashMap&lt;&gt;&#40;&#41;;
     * attributes.put&#40;&quot;endpoint&quot;, &quot;http:&#47;&#47;service-endpoint.azure.com&quot;&#41;;
     *
     * AzureLongCounter createdHttpConnections = defaultMeter.createLongCounter&#40;&quot;az.core.http.connections&quot;,
     *     &quot;Number of created HTTP connections&quot;, null, attributes&#41;;
     *
     * createdHttpConnections.add&#40;1, currentContext&#41;;
     * </pre>
     * <!-- end com.azure.core.util.metrics.AzureMeter.longCounter -->
     *
     * @param name short counter  name following https://opentelemetry.io/docs/reference/specification/metrics/api/#instrument-naming-rule
     * @param description free-form text describing the counter
     * @param unit optional unit of measurement.
     * @param attributes map of metric dimensions that includes low-cardinality values such as endpoint or error code.
     *                   Attributes can only be specified at counter creation time to avoid dynamic, high-cardinality values.
     * @return new instance of {@link AzureLongCounter}
     * @throws NullPointerException if name or description is null.
     */
    AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes);

    /**
     * Flag indicating if metric implementation is detected and functional, use it to minimize performance impact associated with metrics,
     * e.g. measuring latency.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
