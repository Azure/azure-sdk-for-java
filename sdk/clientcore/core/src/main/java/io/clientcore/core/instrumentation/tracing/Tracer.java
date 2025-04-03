// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

import io.clientcore.core.instrumentation.InstrumentationContext;

/**
 * Represents a tracer - a component that creates spans.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
public interface Tracer {
    /**
     * Creates a new span builder.
     *
     * <p><strong>Make sure to follow <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry semantic conventions</a>
     * </strong></p>
     *
     * <p><strong>Basic tracing instrumentation for a service method:</strong></p>
     * <!-- src_embed io.clientcore.core.instrumentation.tracecall -->
     * <pre>
     *
     * if &#40;!tracer.isEnabled&#40;&#41;&#41; &#123;
     *     &#47;&#47; tracing is disabled, so we don't need to create a span
     *     clientCall&#40;context&#41;.close&#40;&#41;;
     *     return;
     * &#125;
     *
     * InstrumentationContext instrumentationContext = context.getInstrumentationContext&#40;&#41;;
     * Span span = tracer.spanBuilder&#40;&quot;&#123;operationName&#125;&quot;, SpanKind.CLIENT, instrumentationContext&#41;
     *     .startSpan&#40;&#41;;
     *
     * RequestContext childContext = context.toBuilder&#40;&#41;
     *     .setInstrumentationContext&#40;span.getInstrumentationContext&#40;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; we'll propagate context implicitly using span.makeCurrent&#40;&#41; as shown later.
     * &#47;&#47; Libraries that write async code should propagate context explicitly in addition to implicit propagation.
     * try &#40;TracingScope scope = span.makeCurrent&#40;&#41;&#41; &#123;
     *     clientCall&#40;childContext&#41;.close&#40;&#41;;
     * &#125; catch &#40;Throwable t&#41; &#123;
     *     &#47;&#47; make sure to report any exceptions including unchecked ones.
     *     span.end&#40;getCause&#40;t&#41;&#41;;
     *     throw t;
     * &#125; finally &#123;
     *     &#47;&#47; NOTE: closing the scope does not end the span, span should be ended explicitly.
     *     span.end&#40;&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.tracecall -->
     *
     * <p><strong>Adding attributes to spans:</strong></p>
     * <!-- src_embed io.clientcore.core.instrumentation.tracewithattributes -->
     * <pre>
     *
     * Span sendSpan = tracer.spanBuilder&#40;&quot;send &#123;queue-name&#125;&quot;, SpanKind.PRODUCER, context.getInstrumentationContext&#40;&#41;&#41;
     *     &#47;&#47; Some of the attributes should be provided at the start time &#40;as documented in semantic conventions&#41; -
     *     &#47;&#47; they can be used by client apps to sample spans.
     *     .setAttribute&#40;&quot;messaging.system&quot;, &quot;servicebus&quot;&#41;
     *     .setAttribute&#40;&quot;messaging.destination.name&quot;, &quot;&#123;queue-name&#125;&quot;&#41;
     *     .setAttribute&#40;&quot;messaging.operations.name&quot;, &quot;send&quot;&#41;
     *     .startSpan&#40;&#41;;
     *
     * RequestContext childContext = context.toBuilder&#40;&#41;
     *     .setInstrumentationContext&#40;sendSpan.getInstrumentationContext&#40;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * try &#40;TracingScope scope = sendSpan.makeCurrent&#40;&#41;&#41; &#123;
     *     if &#40;sendSpan.isRecording&#40;&#41;&#41; &#123;
     *         sendSpan.setAttribute&#40;&quot;messaging.message.id&quot;, &quot;&#123;message-id&#125;&quot;&#41;;
     *     &#125;
     *
     *     Response&lt;?&gt; response = clientCall&#40;childContext&#41;;
     *     response.close&#40;&#41;;
     * &#125; catch &#40;Throwable t&#41; &#123;
     *     sendSpan.end&#40;t&#41;;
     *     throw t;
     * &#125; finally &#123;
     *     sendSpan.end&#40;&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.tracewithattributes -->
     *
     * @param spanName The name of the span.
     * @param spanKind The kind of the span.
     * @param instrumentationContext The parent context.
     * @return The span builder.
     */
    SpanBuilder spanBuilder(String spanName, SpanKind spanKind, InstrumentationContext instrumentationContext);

    /**
     * Checks if the tracer is enabled.
     *
     * @return true if the tracer is enabled, false otherwise.
     */
    default boolean isEnabled() {
        return false;
    }
}
