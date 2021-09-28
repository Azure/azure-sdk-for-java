// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.spring.tracing.sleuth.implementation.HttpTraceUtil;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.spring.tracing.sleuth.implementation.TraceContextUtil.isValid;

/**
 * Pipeline policy that creates a Sleuth span which traces the service request.
 */
public class SleuthHttpPolicy implements HttpPipelinePolicy, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    // Singleton Sleuth tracer capable of starting and exporting spans.
    private final Tracer tracer;
    private final Propagator propagator;

    // standard attributes with http call information
    private static final String HTTP_USER_AGENT = "http.user_agent";
    private static final String HTTP_METHOD = "http.method";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String REQUEST_ID = "x-ms-request-id";
    private static final String AZ_NAMESPACE_KEY = "az.namespace";

    public SleuthHttpPolicy(Tracer tracer, Propagator propagator) {
        Assert.notNull(tracer, "tracer must not be null!");
        Assert.notNull(propagator, "propagator must not be null!");
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false)) {
            return next.process();
        }
        //        tracer.getBaggage()
        Span parentSpan = (Span) context.getData(PARENT_SPAN_KEY).orElse(tracer.currentSpan());
        HttpRequest request = context.getHttpRequest();

        // Build new child span representing this outgoing request.
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());

        Span.Builder spanBuilder = tracer.spanBuilder().name(urlBuilder.getPath())
                                         .setParent(parentSpan.context());

        // A span's kind can be SERVER (incoming request) or CLIENT (outgoing request);
        spanBuilder.kind(Span.Kind.CLIENT);

        // Starting the span makes the sampling decision (nothing is logged at this time)
        Span span = spanBuilder.start();

        // If span is sampled in, add additional TRACING attributes
        if (!span.isNoop()) {
            addSpanRequestAttributes(span, request, context); // Adds HTTP method, URL, & user-agent
        }

        // For no-op tracer, SpanContext is INVALID; inject valid span headers onto outgoing request
        TraceContext traceContext = span.context();
        if (isValid(traceContext)) {
            propagator.inject(traceContext, request, contextSetter);
        }

        // run the next policy and handle success and error
        return next.process()
                   .doOnEach(SleuthHttpPolicy::handleResponse)
                   .contextWrite(Context.of("TRACING_SPAN", span, "REQUEST", request));
    }

    private static void addSpanRequestAttributes(Span span, HttpRequest request,
                                                 HttpPipelineCallContext context) {
        putTagIfNotEmptyOrNull(span, HTTP_USER_AGENT,
            request.getHeaders().getValue("User-Agent"));
        putTagIfNotEmptyOrNull(span, HTTP_METHOD, request.getHttpMethod().toString());
        putTagIfNotEmptyOrNull(span, HTTP_URL, request.getUrl().toString());
        Optional<Object> tracingNamespace = context.getData(AZ_TRACING_NAMESPACE_KEY);
        tracingNamespace.ifPresent(o -> putTagIfNotEmptyOrNull(span, AZ_NAMESPACE_KEY,
            o.toString()));
    }

    private static void putTagIfNotEmptyOrNull(Span span, String key, String value) {
        // AttributeValue will throw an error if the value is null.
        if (!CoreUtils.isNullOrEmpty(value)) {
            span.tag(key, value);
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
     *
     * @param span Span to end.
     * @param response Response from the service.
     * @param error Potential error returned from the service.
     */
    private static void spanEnd(Span span, HttpResponse response, Throwable error) {
        if (!span.isNoop()) {
            int statusCode = 0;
            String requestId = null;
            if (response != null) {
                statusCode = response.getStatusCode();
                requestId = response.getHeaderValue(REQUEST_ID);
            }

            putTagIfNotEmptyOrNull(span, REQUEST_ID, requestId);
            span.tag(HTTP_STATUS_CODE, String.valueOf(statusCode));
            span = HttpTraceUtil.setSpanStatus(span, statusCode, error);
        }

        // Ending the span schedules it for export if sampled in or just ignores it if sampled out.
        span.end();
    }

    // lambda that actually injects arbitrary header into the request
    private final Propagator.Setter<HttpRequest> contextSetter =
        (request, key, value) -> request.getHeaders().set(key, value);
}
