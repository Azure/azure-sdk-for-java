package com.azure.tracing.opencensus;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.spi.BeforeRetryPolicyProvider;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.util.Optional;
import reactor.core.publisher.Mono;

public class OpenCensusTelemetryPolicy implements BeforeRetryPolicyProvider, HttpPipelinePolicy {

    private static final Tracer tracer = Tracing.getTracer();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context,
        HttpPipelineNextPolicy next) {
        // Incoming request has a Context which MIGHT have a current span
        Span parentSpan = null;

        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        if (spanOptional.isPresent()) {
            parentSpan = (Span) spanOptional.get();
        }

        // here we start a new Span
        SpanBuilder spanBuilder = tracer.spanBuilderWithExplicitParent(
            getSpanName(context), // this is a coarse name like "Microsoft.Storage.Blob/downloadBlob" or "Microsoft.KeyVault/getSecret".
            parentSpan); // link to the parent span

        Span span = spanBuilder.startSpan();

        context.setData(Constants.OPENCENSUS_SPAN_KEY, span);

        // run the next policy and handle success and error
        return next.process()
            .map(httpResponse -> {
                spanEnd(span, httpResponse, null);
                return httpResponse;
            })
            .doOnError(throwable -> spanEnd(span, null, throwable));
    }

    /**
     *  Returns FQN of logical operation: Azure.Blob/downloadBlob
     *  that is the same across all languages
     */
    final String getSpanName(HttpPipelineCallContext context) {


        // this is not efficient. May be store sdk name somewhere else (static var) or embed in OperationName
        Optional<Object> sdkNameOptional = context.getData(Constants.SDK_NAME_KEY);
        Optional<Object> operationNameOptional = context.getData(Constants.OPERATION_NAME_KEY);
        if (sdkNameOptional.isPresent() && operationNameOptional.isPresent())
        {
            return (String)sdkNameOptional.get() + "/" + (String)operationNameOptional.get();
        }

        return "";
    }

    void spanEnd(Span span, HttpResponse response, Throwable error) {
        //no need to populate status code on sampled-out spans
        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            span.setStatus(HttpTraceUtil.parseResponseStatus(response.statusCode(), error));
        }

        span.end();
    }

    @Override
    public HttpPipelinePolicy create() {
        return this;
    }
}
