// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.metrics;

import java.util.List;

/**
 * Represents a meter - a component that creates instruments.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
public interface Meter {
    /**
     * Creates histogram instrument allowing to record distribution of a double value values.
     * Histograms should be used for latency or other measurements where distribution of values is important and values are
     * statistically bounded.
     * <p>
     * See <a href="https://opentelemetry.io/docs/reference/specification/metrics/api/#histogram">Histogram definition</a>
     * and <a href="https://opentelemetry.io/docs/specs/semconv/general/metrics">conventions</a>  for more details.
     * <p>
     * Avoid creating new histograms for each request. Histogram lifetime should usually match the client lifetime.
     * <!-- src_embed io.clientcore.core.instrumentation.histogram -->
     * <pre>
     *
     * List&lt;Double&gt; bucketBoundariesAdvice = Collections.unmodifiableList&#40;Arrays.asList&#40;0.005d, 0.01d, 0.025d, 0.05d, 0.075d,
     *     0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d&#41;&#41;;
     * DoubleHistogram histogram = meter.createDoubleHistogram&#40;&quot;contoso.sample.client.operation.duration&quot;,
     *     &quot;s&quot;,
     *     &quot;Contoso sample client operation duration&quot;, bucketBoundariesAdvice&#41;;
     * InstrumentationAttributes successAttributes  = instrumentation.createAttributes&#40;
     *     Collections.singletonMap&#40;&quot;operation.name&quot;, &quot;&#123;operationName&#125;&quot;&#41;&#41;;
     *
     * long startTime = System.nanoTime&#40;&#41;;
     * String errorType = null;
     *
     * try &#123;
     *     performOperation&#40;&#41;;
     * &#125; catch &#40;Throwable t&#41; &#123;
     *     &#47;&#47; make sure to report any exceptions including unchecked ones.
     *     errorType = getCause&#40;t&#41;.getClass&#40;&#41;.getCanonicalName&#40;&#41;;
     *     throw t;
     * &#125; finally &#123;
     *     InstrumentationAttributes attributes = errorType == null
     *         ? successAttributes
     *         : successAttributes.put&#40;&quot;error.type&quot;, errorType&#41;;
     *
     *     histogram.record&#40;&#40;System.nanoTime&#40;&#41; - startTime&#41; &#47; 1e9, attributes, null&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.histogram -->
     *
     * @param name short histogram name following <a href="https://opentelemetry.io/docs/specs/semconv/general/naming/">naming conventions</a>. Required
     * @param description free-form text describing the instrument. Required
     * @param unit optional unit of measurement following <a href="https://opentelemetry.io/docs/specs/semconv/general/metrics/#units">units conventions</a>. Required
     * @param bucketBoundaries list of bucket boundaries for the histogram. Optional
     * @return new instance of {@link DoubleHistogram}
     * @throws NullPointerException if name or description is null.
     */
    DoubleHistogram createDoubleHistogram(String name, String description, String unit, List<Double> bucketBoundaries);

    /**
     * Creates Counter instrument that is used to record incrementing values, such as number of sent messages or created
     * connections.
     * <p>
     * Use {@link Meter#createLongUpDownCounter(String, String, String)} for counters that can go down,
     * such as number of active connections or queue size.
     * <p>
     * See <a href="https://opentelemetry.io/docs/reference/specification/metrics/api/#counter">Counter definition</a> for more details.
     * <!-- src_embed io.clientcore.core.instrumentation.counter -->
     * <pre>
     * LongCounter counter = meter.createLongCounter&#40;&quot;sample.client.sent.messages&quot;,
     *     &quot;Number of messages sent by the client library&quot;,
     *     &quot;&#123;message&#125;&quot;&#41;;
     * InstrumentationAttributes successAttributes  = instrumentation.createAttributes&#40;
     *     Collections.singletonMap&#40;&quot;operation.name&quot;, &quot;sendBatch&quot;&#41;&#41;;
     * String errorType = null;
     * try &#123;
     *     sendBatch&#40;batch&#41;;
     * &#125; catch &#40;Throwable t&#41; &#123;
     *     &#47;&#47; make sure to report any exceptions including unchecked ones.
     *     errorType = getCause&#40;t&#41;.getClass&#40;&#41;.getCanonicalName&#40;&#41;;
     *     throw t;
     * &#125; finally &#123;
     *     InstrumentationAttributes attributes = errorType == null
     *         ? successAttributes
     *         : successAttributes.put&#40;&quot;error.type&quot;, errorType&#41;;
     *
     *     counter.add&#40;batch.size&#40;&#41;, attributes, null&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.counter -->
     *
     * @param name short counter  name following <a href="https://opentelemetry.io/docs/specs/semconv/general/naming/">naming conventions</a>
     * @param description free-form text describing the counter
     * @param unit optional unit of measurement following <a href="https://opentelemetry.io/docs/specs/semconv/general/metrics/#units">units conventions</a>
     * @return new instance of {@link LongCounter}
     * @throws NullPointerException if name or description is null.
     */
    LongCounter createLongCounter(String name, String description, String unit);

    /**
     * Creates UpDownCounter instrument that is used to record values that can go up or down, such as number of active
     * connections or queue size.
     * <p>
     * Use {@link Meter#createLongCounter(String, String, String)} for counters that can only go up,
     * such as number of sent messages or created connections.
     * See <a href="https://opentelemetry.io/docs/reference/specification/metrics/api/#updowncounter">UpDownCounter definition</a> for more details.
     * <!-- src_embed io.clientcore.core.instrumentation.updowncounter -->
     * <pre>
     * LongCounter upDownCounter = meter.createLongUpDownCounter&#40;&quot;sample.client.operation.active&quot;,
     *     &quot;Number of operations in progress&quot;,
     *     &quot;&#123;operation&#125;&quot;&#41;;
     * InstrumentationAttributes successAttributes  = instrumentation.createAttributes&#40;
     *     Collections.singletonMap&#40;&quot;operation.name&quot;, &quot;sendBatch&quot;&#41;&#41;;
     * try &#123;
     *     upDownCounter.add&#40;1, successAttributes, null&#41;;
     *     performOperation&#40;&#41;;
     * &#125; finally &#123;
     *     upDownCounter.add&#40;-1, successAttributes, null&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.updowncounter -->
     *
     * @param name short counter name following <a href="https://opentelemetry.io/docs/specs/semconv/general/naming/">naming conventions</a>
     * @param description free-form text describing the counter
     * @param unit optional unit of measurement following <a href="https://opentelemetry.io/docs/specs/semconv/general/metrics/#units">units conventions</a>
     * @return new instance of {@link LongCounter}
     * @throws NullPointerException if name or description is null.
     */
    LongCounter createLongUpDownCounter(String name, String description, String unit);

    /**
     * Checks if Meter implementation was found, and it's enabled.
     *
     * @return true if Meter is enabled, false otherwise.
     */
    boolean isEnabled();
}
