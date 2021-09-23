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
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

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
     * Creates new OpenTelemetry {@linkHttpPipelinePolicy} with default 
     * {@link GlobalOpenTelemetry#getTracer(String) global tracer}
     */
    public OpenTelemetryHttpPolicy() {
        this(GlobalOpenTelemetry.getTracer("Azure-OpenTelemetry"));
    }

    /**
     * Creates new {@code OpenTelemetryHttpPolicy} that uses custom tracer.
     * Use it for tests.
     *
     * @param tracer {@code io.opentelemetry.api.trace.Tracer} instance.
     */
    OpenTelemetryHttpPolicy(Tracer tracer) {
        this.tracer = tracer;
    }

    // standard attributes with http call information
    private static final String HTTP_USER_AGENT = "http.user_agent";
    private static final String HTTP_METHOD = "http.method";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String REQUEST_ID = "x-ms-request-id";

    // This helper class implements W3C distributed tracing protocol and injects SpanContext into the outgoing http
    // request
    private final TextMapPropagator traceContextFormat = W3CTraceContextPropagator.getInstance();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false)) {
            return next.process();
        }

        io.opentelemetry.context.Context currentContext = io.opentelemetry.context.Context.current();
        Span parentSpan = (Span) context.getData(PARENT_SPAN_KEY).orElse(Span.current());
        HttpRequest request = context.getHttpRequest();

        // Build new child span representing this outgoing request.
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());

        SpanBuilder spanBuilder = tracer.spanBuilder(urlBuilder.getPath())
            .setParent(currentContext.with(parentSpan));

        // A span's kind can be SERVER (incoming request) or CLIENT (outgoing request);
        spanBuilder.setSpanKind(SpanKind.CLIENT);

        // Starting the span makes the sampling decision (nothing is logged at this time)
        Span span = spanBuilder.startSpan();

        // If span is sampled in, add additional TRACING attributes
        if (span.isRecording()) {
            addSpanRequestAttributes(span, request, context); // Adds HTTP method, URL, & user-agent
        }

        // For no-op tracer, SpanContext is INVALID; inject valid span headers onto outgoing request
        SpanContext spanContext = span.getSpanContext();
        if (spanContext.isValid()) {
            traceContextFormat.inject(currentContext.with(span), request, contextSetter);
        }

        // run the next policy and handle success and error
        return next.process()
            .doOnEach(OpenTelemetryHttpPolicy::handleResponse)
            .contextWrite(Context.of("TRACING_SPAN", span, "REQUEST", request));
    }

    private static void addSpanRequestAttributes(Span span, HttpRequest request,
        HttpPipelineCallContext context) {
        putAttributeIfNotEmptyOrNull(span, HTTP_USER_AGENT,
            request.getHeaders().getValue("User-Agent"));
        putAttributeIfNotEmptyOrNull(span, HTTP_METHOD, request.getHttpMethod().toString());
        putAttributeIfNotEmptyOrNull(span, HTTP_URL, request.getUrl().toString());
        Optional<Object> tracingNamespace = context.getData(AZ_TRACING_NAMESPACE_KEY);
        tracingNamespace.ifPresent(o -> putAttributeIfNotEmptyOrNull(span, OpenTelemetryTracer.AZ_NAMESPACE_KEY,
            o.toString()));
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
        Optional<Span> tracingSpan = context.getOrEmpty("TRACING_SPAN");

        if (!tracingSpan.isPresent()) {
            return;
        }

        Span span = tracingSpan.get();
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
                requestId = response.getHeaderValue(REQUEST_ID);
            }

            putAttributeIfNotEmptyOrNull(span, REQUEST_ID, requestId);
            span.setAttribute(HTTP_STATUS_CODE, statusCode);
            span = HttpTraceUtil.setSpanStatus(span, statusCode, error);
        }

        // Ending the span schedules it for export if sampled in or just ignores it if sampled out.
        span.end();
    }

    // lambda that actually injects arbitrary header into the request
    private final TextMapSetter<HttpRequest> contextSetter =
        (request, key, value) -> request.getHeaders().set(key, value);
}
