// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opencensus;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.spi.AfterRetryPolicyProvider;
import com.azure.core.implementation.util.ImplUtils;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

import java.util.Optional;

public class OpenCensusHttpPolicy implements AfterRetryPolicyProvider, HttpPipelinePolicy {

    /**
     * @return a OpenCensus HTTP policy.
     */
    public HttpPipelinePolicy create() {
        return this;
    }

    // Singleton OpenCensus tracer capable of starting and exporting spans.
    private static final Tracer TRACER = Tracing.getTracer();

    // standard attributes with http call information
    private static final String HTTP_USER_AGENT = "http.user_agent";
    private static final String HTTP_METHOD = "http.method";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String REQUEST_ID = "x-ms-request-id";

    // This helper class implements W3C distributed tracing protocol and injects SpanContext into the outgoing http request
    private final TextFormat traceContextFormat = Tracing.getPropagationComponent().getTraceContextFormat();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        Span parentSpan = (Span) context.getData(Constants.OPENCENSUS_SPAN_KEY).orElse(TRACER.getCurrentSpan());
        HttpRequest request = context.httpRequest();

        // Build new child span representing this outgoing request.
        SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(getSpanName(request), parentSpan);

        // A span's kind can be SERVER (incoming request) or CLIENT (outgoing request); useful for Gantt chart
        spanBuilder.setSpanKind(Kind.CLIENT);

        // Starting the span makes the sampling decision (nothing is logged at this time)
        Span span = spanBuilder.startSpan();

        // If span is sampled in, add additional TRACING attributes
        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            addSpanRequestAttributes(span, request); // Adds HTTP method, URL, & user-agent
        }

        // For no-op tracer, SpanContext is INVALID; inject valid span headers onto outgoing request
        SpanContext spanContext = span.getContext();
        if (!spanContext.equals(SpanContext.INVALID)) {
            traceContextFormat.inject(spanContext, request, contextSetter);
        }

        // run the next policy and handle success and error
        return next.process()
            .doOnEach(OpenCensusHttpPolicy::handleResponse)
            .subscriberContext(Context.of("TRACING_SPAN", span, "REQUEST", request));
    }

    private static void addSpanRequestAttributes(Span span, HttpRequest request) {
        putAttributeIfNotEmptyOrNull(span, HTTP_USER_AGENT, request.headers().value("User-Agent"));
        putAttributeIfNotEmptyOrNull(span, HTTP_METHOD, request.httpMethod().toString());
        putAttributeIfNotEmptyOrNull(span, HTTP_URL, request.url().toString());
    }

    private static void putAttributeIfNotEmptyOrNull(Span span, String key, String value) {
        // AttributeValue will throw an error if the value is null.
        if (!ImplUtils.isNullOrEmpty(value)) {
            span.putAttribute(key, AttributeValue.stringAttributeValue(value));
        }
    }

    private static void handleResponse(Signal<HttpResponse> signal) {
        // Ignore the on complete and on subscribe events, they don't contain the information needed to end the span.
        if (signal.isOnComplete() || signal.isOnSubscribe()) {
            return;
        }

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        Context context = signal.getContext();
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
            if (error instanceof HttpRequestException) {
                HttpRequestException exception = (HttpRequestException) error;
                httpResponse = exception.response();
            }
        }

        spanEnd(span, httpResponse, error);
    }

    // Sets status on the span and ends it
    private static void spanEnd(Span span, HttpResponse response, Throwable error) {
        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            int statusCode = 0;
            String requestId = null;
            if (response != null) {
                statusCode = response.statusCode();
                requestId = response.headerValue(REQUEST_ID);
            }

            putAttributeIfNotEmptyOrNull(span, REQUEST_ID, requestId);
            span.putAttribute(HTTP_STATUS_CODE, AttributeValue.longAttributeValue(statusCode));
            span.setStatus(HttpTraceUtil.parseResponseStatus(statusCode, error));
        }

        // Ending the span schedules it for export if sampled in or just ignores it if sampled out.
        span.end();
    }

    private String getSpanName(HttpRequest request) {
        // you can probably optimize it away and remove all checks for null and preceding '/' if path is guaranteed to be valid and not empty
        String path = request.url().getPath();
        if (path == null) {
            path = "/";
        }
        return path;
    }

    // lambda that actually injects arbitrary header into the request
    private final TextFormat.Setter<HttpRequest> contextSetter = new TextFormat.Setter<HttpRequest>() {
        @Override
        public void put(HttpRequest request, String key, String value) {
            request.withHeader(key, value);
        }
    };
}
