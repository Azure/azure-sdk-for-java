// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.metrics;

import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;

/**
 * Represents a histogram metric that can be used to record distribution of {@code double} values.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
public interface DoubleHistogram {
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
     * @param value The amount of the measurement.
     * @param attributes Collection of attributes representing metric dimensions.
     * @param context The explicit context to associate with this measurement.
     */
    void record(double value, InstrumentationAttributes attributes, InstrumentationContext context);

    /**
     * Flag indicating if metric implementation is detected and functional, use it to minimize performance impact associated with metrics,
     * e.g. measuring latency.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
