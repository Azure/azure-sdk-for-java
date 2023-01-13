// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Signal;

import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;

public class InstrumentationPolicy implements HttpPipelinePolicy {
    static final String HTTP_USER_AGENT = "http.user_agent";
    static final String HTTP_METHOD = "http.method";
    static final String HTTP_URL = "http.url";
    static final String HTTP_STATUS_CODE = "http.status_code";
    static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";
    static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String REACTOR_HTTP_TRACE_CONTEXT_KEY = "instrumentation-context-key";
    private static final String SERVICE_REQUEST_ID_HEADER = "x-ms-request-id";
    private static final String CLIENT_REQUEST_ID_HEADER = "x-ms-client-request-id";
    private static final String LEGACY_OTEL_POLICY_NAME = "io.opentelemetry.javaagent.instrumentation.azurecore.v1_19.shaded.com.azure.core.tracing.opentelemetry.OpenTelemetryHttpPolicy";
    private static final ClientLogger LOGGER = new ClientLogger(InstrumentationPolicy.class);

    private Tracer tracer;
    private ScalarPropagatingMono propagatingMono;
    private static boolean foundLegacyOTelPolicy;

    static {
        try {
            Class.forName(LEGACY_OTEL_POLICY_NAME, true, HttpPipelinePolicy.class.getClassLoader());
            foundLegacyOTelPolicy = true;
        } catch (ClassNotFoundException e) {
            foundLegacyOTelPolicy = false;
        }
    }
    public InstrumentationPolicy() {
    }

    /*public InstrumentationPolicy(Tracer tracer) {
        this.tracer = tracer;
        this.propagatingMono = new ScalarPropagatingMono(tracer);
    }*/

    public void initialize(Tracer tracer) {
        this.tracer = tracer;
        this.propagatingMono = new ScalarPropagatingMono(tracer);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(context)) {
            return next.process();
        }

        // OpenTelemetry reactor instrumentation needs a bit of help
        // to pick up Azure SDK context. While we're working on explicit
        // context propagation, ScalarPropagatingMono.INSTANCE is the workaround
        return propagatingMono
            .flatMap(ignored -> next.process())
            .doOnEach(this::handleResponse)
            .contextWrite(reactor.util.context.Context.of(REACTOR_HTTP_TRACE_CONTEXT_KEY, startSpan(context)));
    }

    @SuppressWarnings("try")
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!isTracingEnabled(context)) {
            return next.processSync();
        }

        Context span = startSpan(context);
        try (AutoCloseable scope = tracer.makeSpanCurrent(span)) {
            HttpResponse response = next.processSync();
            endSpan(response, null, span);

            return response;
        } catch (RuntimeException ex) {
            endSpan(null, ex, span);
            throw ex;
        } catch (Exception ex) {
            endSpan(null, ex, span);
            throw LOGGER.logExceptionAsWarning(new RuntimeException(ex));
        }
    }

    private Context startSpan(HttpPipelineCallContext azContext) {
        HttpRequest request = azContext.getHttpRequest();

        // Build new child span representing this outgoing request.
        String methodName = request.getHttpMethod().toString();
        StartSpanOptions spanOptions = new StartSpanOptions(SpanKind.CLIENT)
            .setAttribute(HTTP_METHOD, methodName)
            .setAttribute(HTTP_URL, request.getUrl().toString());
        Context span = tracer.start("HTTP " + methodName, spanOptions, azContext.getContext());

        addPostSamplingAttributes(span, request);

        tracer.injectContext((k, v) -> request.getHeaders().set(k, v), span);
        return span;
    }

    private void addPostSamplingAttributes(Context span, HttpRequest request) {
        String userAgent = request.getHeaders().getValue("User-Agent");
        if (!CoreUtils.isNullOrEmpty(userAgent)) {
            tracer.setAttribute(HTTP_USER_AGENT, userAgent, span);
        }

        String requestId = request.getHeaders().getValue(CLIENT_REQUEST_ID_HEADER);
        if (!CoreUtils.isNullOrEmpty(requestId)) {
            tracer.setAttribute(CLIENT_REQUEST_ID_ATTRIBUTE, requestId, span);
        }
    }

    /**
     * Handles retrieving the information from the service response and ending the span.
     *
     * @param signal Reactive Stream signal fired by Reactor.
     */
    private void handleResponse(Signal<? extends HttpResponse> signal) {
        // Ignore the on complete and on subscribe events, they don't contain the information needed to end the span.
        if (signal.isOnComplete() || signal.isOnSubscribe()) {
            return;
        }

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        Context span = signal.getContextView().getOrDefault(REACTOR_HTTP_TRACE_CONTEXT_KEY, null);
        endSpan(signal.get(), signal.getThrowable(), span);
    }

    private void endSpan(HttpResponse response, Throwable error, Context span) {
        if (response != null) {
            int statusCode = response.getStatusCode();
            tracer.setAttribute(HTTP_STATUS_CODE, statusCode, span);
            String requestId = response.getHeaderValue(SERVICE_REQUEST_ID_HEADER);
            if (requestId != null) {
                tracer.setAttribute(SERVICE_REQUEST_ID_ATTRIBUTE, requestId, span);
            }

            tracer.end(statusCode, null, span);
        }

        tracer.end(null, error, span);
    }

    private boolean isTracingEnabled(HttpPipelineCallContext context) {
        return tracer != null && tracer.isEnabled() && !foundLegacyOTelPolicy
            && !((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false));
    }

    /**
     * Helper class allowing to run Mono subscription and any hot path
     * in scope of trace context. This enables OpenTelemetry auto-collection
     * to pick it up and correlate lower levels of instrumentation and logs
     * to logical/HTTP spans.
     *
     * OpenTelemetry reactor auto-instrumentation will take care of the cold path.
     */
    static final class ScalarPropagatingMono extends Mono<Object> {
        private final Object value = new Object();
        private final Tracer tracer;

        private ScalarPropagatingMono(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        @SuppressWarnings("try")
        public void subscribe(CoreSubscriber<? super Object> actual) {
            Context traceContext = actual.currentContext().getOrDefault(REACTOR_HTTP_TRACE_CONTEXT_KEY, null);
            if (tracer.isEnabled() && traceContext != null) {
                try (AutoCloseable scope = tracer.makeSpanCurrent(traceContext)) {
                    actual.onSubscribe(Operators.scalarSubscription(actual, value));
                } catch (Exception e) {
                    LOGGER.verbose("Error closing scope", e);
                }
            } else {
                actual.onSubscribe(Operators.scalarSubscription(actual, value));
            }
        }
    }
}
