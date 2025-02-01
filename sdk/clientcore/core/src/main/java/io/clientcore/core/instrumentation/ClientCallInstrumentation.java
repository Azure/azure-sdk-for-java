// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.OPERATION_NAME_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_ADDRESS_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_PORT_KEY;

/**
 * Provides convenient methods to instrument client call with distributed tracing and metrics.
 * <p><strong>This method is intended to be used by client libraries. Application developers
 * should use OpenTelemetry API directly</strong></p>
 */
public class ClientCallInstrumentation {

    private static final List<Double> DURATION_BOUNDARIES_ADVICE
        = Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d, 0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d);
    private final Tracer tracer;
    private final DoubleHistogram callDuration;
    private final String operationName;
    private final Instrumentation instrumentation;

    private final InstrumentationAttributes successAttributes;

    /**
     * Creates a new instance of {@link ClientCallInstrumentation}.
     *
     * @param clientName the name of the client. The name is used as a prefix for the metric name following {@code {client-name.}client.operation.duration} format.
     * It should be short, unique, and descriptive.
     * See <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/general/naming.md#metrics">OpenTelemetry naming conventions</a> for more information.
     *
     * @param operationName the name of the operation. The name is used as a value for the {@code operation.name} attribute.
     * @param serviceEndpoint the service endpoint URI. The host and port are used as values for the {@code server.address} and {@code server.port} attributes.
     * @param instrumentation the instrumentation instance.
     */
    public ClientCallInstrumentation(String clientName, String operationName, URI serviceEndpoint,
        Instrumentation instrumentation) {
        Objects.requireNonNull(operationName, "'operationName' cannot be null");
        Objects.requireNonNull(serviceEndpoint, "'serviceEndpoint' cannot be null");
        Objects.requireNonNull(clientName, "'clientName' cannot be null");
        Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null");

        this.instrumentation = instrumentation;
        this.tracer = instrumentation.createTracer();
        Meter meter = instrumentation.createMeter();
        String metricName = clientName.toLowerCase(Locale.ROOT) + "." + "client.operation.duration";
        String metricDescription = "Duration of " + clientName + " client service method call";
        this.callDuration = meter.createDoubleHistogram(metricName, metricDescription, "s", DURATION_BOUNDARIES_ADVICE);
        this.successAttributes = createAttributes(operationName, serviceEndpoint, instrumentation);
        this.operationName = operationName;
    }

    /**
     * Determines whether the client call should be instrumented.
     * @param requestOptions the request options.
     * @return {@code true} if the client call should be instrumented, otherwise {@code false}.
     */
    public boolean shouldInstrument(RequestOptions requestOptions) {
        return instrumentation.shouldInstrument(SpanKind.CLIENT,
            requestOptions == null ? null : requestOptions.getInstrumentationContext());
    }

    /**
     * Starts a new scope for the client call which includes starting a new span for the client call if tracing is enabled.
     * Created span becomes current and is used to correlate all telemetry reported under it such as other spans, logs, or metrics exemplars.
     *
     * <p>
     * The scope MUST be closed on the same thread that created it.
     * <p>
     * <strong>Closing the returned scope end the underlying span and records duration measurement.</strong>
     * @param requestOptions the request options.
     *
     * @return the scope.
     */
    public Scope startScope(RequestOptions requestOptions) {
        Objects.requireNonNull(requestOptions, "'requestOptions' cannot be null");
        if (!shouldInstrument(requestOptions)) {
            return Scope.NOOP;
        }

        InstrumentationContext parent = requestOptions.getInstrumentationContext();
        Span span = tracer.spanBuilder(operationName, SpanKind.CLIENT, parent)
            .setAllAttributes(successAttributes)
            .startSpan();
        Scope scope = new Scope(successAttributes, requestOptions.getInstrumentationContext(), span, callDuration);

        if (scope.instrumentationContext.isValid()) {
            requestOptions.setInstrumentationContext(scope.instrumentationContext);
        }

        return scope.makeCurrent();
    }

    /**
     * Represents a scope for the client call that combines span and duration measurement.
     */
    public static class Scope implements AutoCloseable {
        static final Scope NOOP = new Scope();
        private final Span span;
        private final InstrumentationContext instrumentationContext;
        private final InstrumentationAttributes startAttributes;
        private final long startTimeNs;
        private final DoubleHistogram callDuration;
        private Throwable error;
        private TracingScope tracingScope;

        private Scope() {
            this.span = null;
            this.instrumentationContext = null;
            this.startAttributes = null;
            this.startTimeNs = 0;
            this.callDuration = null;
        }

        Scope(InstrumentationAttributes startAttributes, InstrumentationContext parent, Span span,
            DoubleHistogram callDuration) {
            this.startAttributes = startAttributes;
            this.span = span;
            this.instrumentationContext
                = span.getInstrumentationContext().isValid() ? span.getInstrumentationContext() : parent;
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
         * @param throwable The throwable to set on the scope.
         * @return The updated {@link Scope} object.
         */
        public Scope setError(Throwable throwable) {
            this.error = throwable;
            return this;
        }

        /**
         * Gets the instrumentation context identifying this call.
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
                    ? startAttributes
                    : startAttributes.put(ERROR_TYPE_KEY, error.getClass().getCanonicalName());
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

    private static InstrumentationAttributes createAttributes(String operationName, URI endpoint,
        Instrumentation instrumentation) {
        Map<String, Object> attributeMap = new HashMap<>(4);
        attributeMap.put(OPERATION_NAME_KEY, operationName);
        attributeMap.put(SERVER_ADDRESS_KEY, endpoint.getHost());
        int port = getServerPort(endpoint);
        if (port != -1) {
            attributeMap.put(SERVER_PORT_KEY, port);
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
}
