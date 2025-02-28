// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.implementation.instrumentation.NoopInstrumentationContext;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.OPERATION_NAME_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_ADDRESS_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_PORT_KEY;

/**
 * Provides convenient methods to instrument client call with distributed tracing and metrics.
 *
 * <p><strong>This class is intended to be used by client libraries. Application developers
 * should use OpenTelemetry API directly</strong></p>
 *
 * This class is typically used by the auto-generated code and provides generic instrumentation.
 */
public final class OperationInstrumentation {

    // Histogram boundaries are optimized for common latency ranges (in seconds). They are
    // provided as advice at metric creation time and could be overriden by the user application via
    // OTel configuration.
    // TODO (limolkova): document client core metric conventions along with logical operation histogram boundaries.
    private static final List<Double> DURATION_BOUNDARIES_ADVICE = Collections.unmodifiableList(
        Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d, 0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d));
    private final Tracer tracer;
    private final DoubleHistogram callDuration;
    private final String operationName;
    private final Instrumentation instrumentation;
    private final SpanKind spanKind;

    private final InstrumentationAttributes commonAttributes;

    /**
     * Creates a new instance of {@link OperationInstrumentation}.
     *
     * @param operationInfo the operation information.
     * @param instrumentation the instrumentation instance.
     */
    OperationInstrumentation(InstrumentedOperationDetails operationInfo, Instrumentation instrumentation) {
        Objects.requireNonNull(operationInfo, "'operationInfo' cannot be null");
        Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null");

        this.instrumentation = instrumentation;
        this.tracer = instrumentation.createTracer();
        this.callDuration = createCallDurationMetric(operationInfo, instrumentation);
        this.commonAttributes = createAttributes(operationInfo, instrumentation);
        this.operationName = operationInfo.getOperationName();
        this.spanKind = operationInfo.getSpanKind();
    }

    /**
     * Instruments a client call which includes distributed tracing and duration metric.
     * Created span becomes current and is used to correlate all telemetry reported under it such as other spans, logs, or metrics exemplars.
     * <p>
     * The method updates the {@link RequestOptions} object with the instrumentation context that should be used for the call.
     * <!-- src_embed io.clientcore.core.instrumentation.instrument -->
     * <pre>
     * return downloadContentInstrumentation.instrument&#40;&#40;updatedOptions, instrumentationContext&#41; -&gt;
     *     downloadImpl&#40;updatedOptions&#41;, options&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.instrument -->
     *
     * @param operation the operation to instrument. Note: the operation is executed in the scope of the instrumentation and should use updated request options passed to it.
     * @param requestOptions the initial request options.
     * @param <TResponse> the type of the response.
     * @return the response.
     * @throws RuntimeException if the call throws a runtime exception.
     */
    public <TResponse> TResponse instrument(Function<RequestOptions, TResponse> operation,
                                            RequestOptions requestOptions) {
        Objects.requireNonNull(operation, "'operation' cannot be null");

        if (!shouldInstrument(requestOptions)) {
            return operation.apply(requestOptions);
        }

        if (requestOptions == null || requestOptions == RequestOptions.none()) {
            requestOptions = new RequestOptions();
        }

        OperationInstrumentation.Scope scope = startScope(requestOptions);
        try {
            return operation.apply(requestOptions);
        } catch (RuntimeException t) {
            scope.setError(t);
            throw t;
        } finally {
            scope.close();
        }
    }

    /**
     * Determines whether the client call should be instrumented.
     *
     * @param requestOptions the request options.
     * @return {@code true} if the client call should be instrumented, otherwise {@code false}.
     */
    private boolean shouldInstrument(RequestOptions requestOptions) {
        return instrumentation.shouldInstrument(spanKind,
            requestOptions == null ? null : requestOptions.getInstrumentationContext());
    }

    /**
     * Starts a new scope for the client call which includes starting a new span for the client call if tracing is enabled.
     * Created span becomes current and is used to correlate all telemetry reported under it such as other spans, logs, or metrics exemplars.
     * <p>
     * The method updates the {@link RequestOptions} object with the instrumentation context that should be used for the call.
     * <p>
     * The scope MUST be closed on the same thread that created it.
     * <p>
     * <strong>Closing the returned scope end the underlying span and records duration measurement.</strong>
     * <p>
     *
     * @param requestOptions the request options.
     * @return the scope.
     */
    private Scope startScope(RequestOptions requestOptions) {
        InstrumentationContext parent = requestOptions.getInstrumentationContext();
        Scope scope = new Scope(operationName, spanKind, commonAttributes, parent, tracer, callDuration);

        if (scope.getInstrumentationContext().isValid()) {
            requestOptions.setInstrumentationContext(scope.getInstrumentationContext());
        }

        return scope.makeCurrent();
    }

    /**
     * Represents a scope for the client call that combines span and duration measurement.
     */
    private static class Scope implements AutoCloseable {
        private final Span span;
        private final InstrumentationContext instrumentationContext;
        private final InstrumentationAttributes commonAttributes;
        private final long startTimeNs;
        private final DoubleHistogram callDuration;
        private Throwable error;
        private TracingScope tracingScope;

        Scope(String operationName, SpanKind kind, InstrumentationAttributes commonAttributes,
            InstrumentationContext parent, Tracer tracer, DoubleHistogram callDuration) {
            this.commonAttributes = commonAttributes;
            this.span = tracer.spanBuilder(operationName, kind, parent).setAllAttributes(commonAttributes).startSpan();

            this.instrumentationContext = (parent == null || span.getInstrumentationContext().isValid())
                ? span.getInstrumentationContext()
                : parent;

            this.startTimeNs = callDuration.isEnabled() ? System.nanoTime() : 0;
            this.callDuration = callDuration;
        }

        Scope makeCurrent() {
            if (span != null) {
                this.tracingScope = span.makeCurrent();
            }

            return this;
        }

        /**
         * Sets error on the scope. This should match the exception (or its cause)
         * that will be thrown to the application code.
         * <p>
         * Exceptions handled by the client library should not be passed to this method.
         * <p>
         *
         * <strong>It is important to record any exceptions that are about to be thrown
         * to the user code including unchecked ones.</strong>
         *
         * @param throwable The throwable to set on the scope.
         * @return The updated {@link Scope} object.
         */
        public Scope setError(Throwable throwable) {
            this.error = throwable;
            return this;
        }

        /**
         * Gets the instrumentation context identifying this call.
         * <!-- src_embed io.clientcore.core.instrumentation.enrich -->
         * <pre>
         * operationInstrumentation.instrument&#40;&#40;updatedOptions, instrumentationContext&#41; -&gt; &#123;
         *     Span span = instrumentationContext.getSpan&#40;&#41;;
         *     if &#40;span.isRecording&#40;&#41;&#41; &#123;
         *         span.setAttribute&#40;&quot;sample.content.id&quot;, &quot;&#123;content-id&#125;&quot;&#41;;
         *     &#125;
         *
         *     return clientCall&#40;updatedOptions&#41;;
         * &#125;, null&#41;;
         *
         * </pre>
         * <!-- end io.clientcore.core.instrumentation.enrich -->
         * @return The instrumentation context.
         */
        public InstrumentationContext getInstrumentationContext() {
            return instrumentationContext;
        }

        /**
         * Ends the scope: end the span and record duration measurement.
         */
        @Override
        public void close() {
            if (callDuration != null && callDuration.isEnabled()) {
                InstrumentationAttributes attributes = error == null
                    ? commonAttributes
                    : commonAttributes.put(ERROR_TYPE_KEY, error.getClass().getCanonicalName());
                callDuration.record((System.nanoTime() - startTimeNs) / 1e9, attributes, instrumentationContext);
            }

            if (span != null) {
                span.end(error);
            }

            if (tracingScope != null) {
                tracingScope.close();
                tracingScope = null;
            }
        }
    }

    private static InstrumentationAttributes createAttributes(InstrumentedOperationDetails operationInfo,
        Instrumentation instrumentation) {
        Map<String, Object> attributeMap = new HashMap<>(4);
        attributeMap.put(OPERATION_NAME_KEY, operationInfo.getOperationName());
        if (operationInfo.getEndpoint() != null) {
            attributeMap.put(SERVER_ADDRESS_KEY, operationInfo.getEndpoint().getHost());
            int port = getServerPort(operationInfo.getEndpoint());
            if (port != -1) {
                attributeMap.put(SERVER_PORT_KEY, port);
            }
        }

        return instrumentation.createAttributes(attributeMap);
    }

    /**
     * Does the best effort to capture the server port with minimum perf overhead.
     * If port is not set, we check scheme for "http" and "https" (case-sensitive).
     * If scheme is not one of those, returns -1.
     *
     * @param uri request URI
     */
    private static int getServerPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            switch (uri.getScheme()) {
                case "http":
                    return 80;

                case "https":
                    return 443;

                default:
                    break;
            }
        }
        return port;
    }

    private static DoubleHistogram createCallDurationMetric(InstrumentedOperationDetails operationInfo,
        Instrumentation instrumentation) {
        Meter meter = instrumentation.createMeter();

        // TODO (lmolkova): it'd be great to get typespec namespace (e.g. Azure.Batch)
        // Metric name should be fully qualified, e.g. `azure.batch` or `azure.storage.blob` - if we
        // had it from typespec, we could auto-generate metric name and description.
        String metricDescription = "Duration of client operation";
        return meter.createDoubleHistogram(operationInfo.getDurationMetricName(), metricDescription, "s",
            DURATION_BOUNDARIES_ADVICE);
    }
}
