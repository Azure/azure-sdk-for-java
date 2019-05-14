package com.azure.tracing.opencensus;

import com.azure.core.http.ContextData;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

import java.util.Optional;

public class OpenCensusTracer implements com.azure.core.implementation.tracing.Tracer {

    // Singleton OpenCensus tracer capable of starting and exporting spans.
    private static final Tracer tracer = Tracing.getTracer();

    public ContextData start(String methodName, ContextData context) {
        /*
         * TODO inspect ContextData for existing OpenCensus-specific tracing information. If present, augment. If not
         * present, create new tracing information.
         *
         * TODO Need to determine the key that users will use to insert the tracing information into the Context, or if a utility method will do this for end users.
         */

        Span parentSpan = null;

        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        parentSpan = (Span) spanOptional.orElse(tracer.getCurrentSpan());

        SpanBuilder spanBuilder = tracer.spanBuilderWithExplicitParent(
            methodName, // this is a coarse name like "Azure.KeyVault/getSecret"
            parentSpan); // link to the parent span

        Span span = spanBuilder.startSpan();


        return context.addData(Constants.OPENCENSUS_SPAN_KEY, span);
    }

    public void end(int responseCode, Throwable throwable, ContextData context) {
        // TODO Optional<Integer>?

        Span span = null;

        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        if (spanOptional.isPresent()) {
            span = (Span) spanOptional.get();
        }
        else {
            // todo: log error!
        }

        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            span.setStatus(HttpTraceUtil.parseResponseStatus(responseCode, throwable));
        }

        span.end();
    }

    @Override
    public void setAttribute(String key, String value, ContextData context) {

        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        if (spanOptional.isPresent()) {
            Span span = (Span) spanOptional.get();
            span.putAttribute(key, AttributeValue.stringAttributeValue(value));
        }
        else {
            // todo: log error!
        }
    }
}
