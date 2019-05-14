package com.azure.tracing.opencensus;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.spi.AfterRetryPolicyProvider;
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

    public HttpPipelinePolicy create() {
        return this;
    }

    // Singleton OpenCensus tracer capable of starting and exporting spans.
    private static final Tracer tracer = Tracing.getTracer();

    // standard attributes with http call information
    public static final String HTTP_USER_AGENT = "http.user_agent";
    public static final String HTTP_METHOD = "http.method";
    public static final String HTTP_URL = "http.url";
    public static final String HTTP_STATUS_CODE = "http.status_code";

    // This helper class implements W3C distributed tracing protocol and injects SpanContext into the outgoing http request
    private final TextFormat traceContextFormat = Tracing.getPropagationComponent().getTraceContextFormat();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Incoming request has a Context which MUST have a current span
        Span parentSpan = null;

        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        parentSpan = (Span) spanOptional.orElse(tracer.getCurrentSpan());

        HttpRequest request = context.httpRequest();
        // Build new child span representing this outgoing request.
        SpanBuilder spanBuilder = tracer.spanBuilderWithExplicitParent(
            getSpanName(request), // Name is request's URL's path
            parentSpan); // reference to the parent

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
            .subscriberContext(Context.of("TRACING_SPAN", span));
    }

    private void addSpanRequestAttributes(Span span, HttpRequest request) {
        putAttributeIfNotEmptyOrNull(span, HTTP_USER_AGENT, request.headers().value("User-Agent"));
        putAttributeIfNotEmptyOrNull(span, HTTP_METHOD, request.httpMethod().toString());
        putAttributeIfNotEmptyOrNull(span, HTTP_URL, request.url().toString());
    }

    private void putAttributeIfNotEmptyOrNull(Span span, String key, String value) {
        if (value != null && !value.isEmpty()) {
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
        if (signal.isOnNext()) {
            HttpResponse httpResponse = signal.get();
            if (span.getOptions().contains(Options.RECORD_EVENTS)) {
                // Successful response, add x-ms-request-id header attribute to span before logging.
                String serverRequestId = httpResponse.headers().value("x-ms-request-id");
                if (serverRequestId != null) {
                    span.putAttribute("x-ms-request-id", AttributeValue.stringAttributeValue(serverRequestId));
                }
            }

            spanEnd(span, httpResponse, null);
        } else {
            spanEnd(span, null, signal.getThrowable());
        }
    }

    // Sets status on the span and ends it
    private static void spanEnd(Span span, HttpResponse response, Throwable error) {
        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            // If sampled in, add status code and set overall status
            int statusCode = response == null ? 0 : response.statusCode();
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
