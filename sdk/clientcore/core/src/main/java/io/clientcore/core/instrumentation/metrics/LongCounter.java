// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.metrics;

import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;

/**
 * Represents a counter metric that can be used to record {@code long} values.
 *
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 * <p>
 * Counters can be monotonic or non-monotonic. Monotonic counters are cumulative and can only increase over time.
 * Use {@link Meter#createLongCounter(String, String, String)} to create counters that can only go up, such as number of sent messages or created connections.
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
 * <p>
 * Use {@link Meter#createLongUpDownCounter(String, String, String)} to create counters that can go down,
 * such as number of active connections or queue size.
 * <p>
 * See <a href="https://opentelemetry.io/docs/reference/specification/metrics/api/#updowncounter">UpDownCounter definition</a> for more details.
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
 */
public interface LongCounter {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param attributes Collection of attributes representing metric dimensions.
     * @param context The explicit context to associate with this measurement.
     */
    void add(long value, InstrumentationAttributes attributes, InstrumentationContext context);

    /**
     * Flag indicating if metric implementation is detected and functional, use it to minimize performance impact associated with metrics,
     * e.g. measuring latency.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
