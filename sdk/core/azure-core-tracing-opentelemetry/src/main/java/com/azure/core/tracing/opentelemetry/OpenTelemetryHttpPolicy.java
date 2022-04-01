// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AfterRetryPolicyProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.tracing.opentelemetry.implementation.HttpTraceUtil;
import com.azure.core.tracing.opentelemetry.implementation.OpenTelemetrySpanSuppressionHelper;
import com.azure.core.util.CoreUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * Pipeline policy that creates an OpenTelemetry span which traces the service request.
 */
public class OpenTelemetryHttpPolicy implements AfterRetryPolicyProvider, HttpPipelinePolicy {
    /**
     * @return a OpenTelemetry HTTP policy.
     */
    @Override
    public HttpPipelinePolicy create() {
        return this;
    }

    // OpenTelemetry tracer capable of starting and exporting spans.
    private final Tracer tracer;

    /**
     * Creates new OpenTelemetry {@link HttpPipelinePolicy} with default
     * {@link GlobalOpenTelemetry#getTracer(String) global tracer}
     */
    public OpenTelemetryHttpPolicy() {
        this(GlobalOpenTelemetry.getTracer("Azure-OpenTelemetry"));
    }

    /**
     * Creates new {@link OpenTelemetryHttpPolicy} that uses custom tracer.
     * Use it for tests.
     *
     * @param tracer {@link io.opentelemetry.api.trace.Tracer} instance.
     */
    OpenTelemetryHttpPolicy(Tracer tracer) {
        this.tracer = tracer;
    }

    // standard attributes with http call information
    private static final String HTTP_USER_AGENT = "http.user_agent";
    private static final String HTTP_METHOD = "http.method";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String SERVICE_REQUEST_ID_HEADER = "x-ms-request-id";
    private static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";

    private static final String CLIENT_REQUEST_ID_HEADER = "x-ms-client-request-id";
    private static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String REACTOR_PARENT_TRACE_CONTEXT_KEY = "otel-context-key";

    // This helper class implements W3C distributed tracing protocol and injects SpanContext into the outgoing http
    // request
    private final TextMapPropagator traceContextFormat = W3CTraceContextPropagator.getInstance();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false)) {
            return next.process();
        }

        // OpenTelemetry reactor instrumentation needs a bit of help
        // to pick up Azure SDK context. While we're working on explicit
        // context propagation, ScalarPropagatingMono.INSTANCE is the workaround
        return ScalarPropagatingMono.INSTANCE
                .flatMap(ignored -> next.process())
                .doOnEach(OpenTelemetryHttpPolicy::handleResponse)
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpan(context)));
    }

    private Context startSpan(HttpPipelineCallContext azContext) {
        Context parentContext = getTraceContextOrCurrent(azContext);

        HttpRequest request = azContext.getHttpRequest();

        // Build new child span representing this outgoing request.
        String methodName = request.getHttpMethod().toString();
        Span span = tracer.spanBuilder("HTTP " + methodName)
            .setAttribute(HTTP_METHOD, methodName)
            .setAttribute(HTTP_URL, request.getUrl().toString())
            .setParent(parentContext)
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();

        if (span.isRecording()) {
            addPostSamplingAttributes(span, request, azContext);
        }

        Context traceContext = parentContext.with(span);
        traceContextFormat.inject(traceContext, request, contextSetter);
        return traceContext;
    }

    private static void addPostSamplingAttributes(Span span, HttpRequest request,
        HttpPipelineCallContext context) {
        putAttributeIfNotEmptyOrNull(span, HTTP_USER_AGENT,
            request.getHeaders().getValue("User-Agent"));
        Optional<Object> tracingNamespace = context.getData(AZ_TRACING_NAMESPACE_KEY);
        tracingNamespace.ifPresent(o -> putAttributeIfNotEmptyOrNull(span, OpenTelemetryTracer.AZ_NAMESPACE_KEY,
            o.toString()));

        String requestId = request.getHeaders().getValue(CLIENT_REQUEST_ID_HEADER);
        putAttributeIfNotEmptyOrNull(span, CLIENT_REQUEST_ID_ATTRIBUTE, requestId);
    }

    private static void putAttributeIfNotEmptyOrNull(Span span, String key, String value) {
        // AttributeValue will throw an error if the value is null.
        if (!CoreUtils.isNullOrEmpty(value)) {
            span.setAttribute(AttributeKey.stringKey(key), value);
        }
    }

    /**
     * Handles retrieving the information from the service response and ending the span.
     *
     * @param signal Reactive Stream signal fired by Reactor.
     */
    private static void handleResponse(Signal<? extends HttpResponse> signal) {
        // Ignore the on complete and on subscribe events, they don't contain the information needed to end the span.
        if (signal.isOnComplete() || signal.isOnSubscribe()) {
            return;
        }

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        ContextView context = signal.getContextView();
        Optional<io.opentelemetry.context.Context> traceContext = context.getOrEmpty(REACTOR_PARENT_TRACE_CONTEXT_KEY);
        if (!traceContext.isPresent()) {
            return;
        }

        Span span = Span.fromContext(traceContext.get());
        HttpResponse httpResponse = null;
        Throwable error = null;
        if (signal.isOnNext()) {
            httpResponse = signal.get();
        } else {
            error = signal.getThrowable();
            if (error instanceof HttpResponseException) {
                HttpResponseException exception = (HttpResponseException) error;
                httpResponse = exception.getResponse();
            }
        }

        spanEnd(span, httpResponse, error);
    }

    /**
     * Sets status information and ends the span.
     * @param span Span to end.
     * @param response Response from the service.
     * @param error Potential error returned from the service.
     */
    private static void spanEnd(Span span, HttpResponse response, Throwable error) {
        if (span.isRecording()) {
            int statusCode = 0;
            String requestId = null;
            if (response != null) {
                statusCode = response.getStatusCode();
                requestId = response.getHeaderValue(SERVICE_REQUEST_ID_HEADER);
            }

            putAttributeIfNotEmptyOrNull(span, SERVICE_REQUEST_ID_ATTRIBUTE, requestId);
            span.setAttribute(HTTP_STATUS_CODE, statusCode);
            span = HttpTraceUtil.setSpanStatus(span, statusCode, error);
        }

        // Ending the span schedules it for export if sampled in or just ignores it if sampled out.
        span.end();
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY
     * or {@link io.opentelemetry.context.Context#current()}
     */
    private static io.opentelemetry.context.Context getTraceContextOrCurrent(HttpPipelineCallContext azContext) {
        final Optional<Object> traceContextOpt = azContext.getData(PARENT_TRACE_CONTEXT_KEY);
        if (traceContextOpt.isPresent() && traceContextOpt.get() instanceof Context) {
            return (io.opentelemetry.context.Context) traceContextOpt.get();
        }

        // no need for back-compat with PARENT_SPAN_KEY - OpenTelemetryTracer will always set
        // PARENT_TRACE_CONTEXT_KEY

        return io.opentelemetry.context.Context.current();
    }

    // lambda that actually injects arbitrary header into the request
    private final TextMapSetter<HttpRequest> contextSetter =
        (request, key, value) -> request.getHeaders().set(key, value);

    /**
     * Helper class allowing to run Mono subscription and any hot path
     * in scope of trace context. This enables OpenTelemetry auto-collection
     * to pick it up and correlate lower levels of instrumentation and logs
     * to logical/HTTP spans.
     *
     * OpenTelemetry reactor auto-instrumentation will take care of the cold path.
     */
    static final class ScalarPropagatingMono extends Mono<Object> {
        public static final Mono<Object> INSTANCE = new ScalarPropagatingMono();

        private final Object value = new Object();

        private ScalarPropagatingMono() {
        }

        @Override
        public void subscribe(CoreSubscriber<? super Object> actual) {
            Context traceContext = actual.currentContext().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, null);
            if (traceContext != null) {
                Object agentContext = OpenTelemetrySpanSuppressionHelper.registerClientSpan(traceContext);
                AutoCloseable closeable = OpenTelemetrySpanSuppressionHelper.makeCurrent(agentContext, traceContext);
                actual.onSubscribe(Operators.scalarSubscription(actual, value));
                try {
                    closeable.close();
                } catch (Exception ignored) {
                }
            } else {
                actual.onSubscribe(Operators.scalarSubscription(actual, value));
            }
        }
    }
}
