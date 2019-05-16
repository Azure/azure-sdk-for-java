package com.azure.tracing.opencensus;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.util.Context;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

import java.util.Optional;

/**
 * OpenCensus span
 */
public class OpenCensusTracer implements com.azure.core.implementation.tracing.Tracer {
    // Singleton OpenCensus tracer capable of starting and exporting spans.
    private static final Tracer tracer = Tracing.getTracer();

    private final ServiceLogger logger = new ServiceLogger(OpenCensusTracer.class);

    @Override
    public Context start(String methodName, Context context) {
        /*
         * TODO inspect ContextData for existing OpenCensus-specific tracing information. If present, augment. If not
         * present, create new tracing information.
         *
         * TODO Need to determine the key that users will use to insert the tracing information into the Context, or if a utility method will do this for end users.
         */
        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        Span parentSpan = (Span) spanOptional.orElse(tracer.getCurrentSpan());

        SpanBuilder spanBuilder = tracer.spanBuilderWithExplicitParent(
            methodName, // this is a coarse name like "Azure.KeyVault/getSecret"
            parentSpan); // link to the parent span

        Span span = spanBuilder.startSpan();


        return context.addData(Constants.OPENCENSUS_SPAN_KEY, span);
    }

    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        if (spanOptional.isPresent()) {
           Span span = (Span) spanOptional.get();

            if (span.getOptions().contains(Options.RECORD_EVENTS)) {
                span.setStatus(HttpTraceUtil.parseResponseStatus(responseCode, throwable));
            }

            span.end();
        } else {
            logger.asWarning().log("Failed to find span to end it.");
        }
    }

    @Override
    public void setAttribute(String key, String value, Context context) {

        Optional<Object> spanOptional = context.getData(Constants.OPENCENSUS_SPAN_KEY);
        if (spanOptional.isPresent()) {
            Span span = (Span) spanOptional.get();
            span.putAttribute(key, AttributeValue.stringAttributeValue(value));
        } else {
            logger.asWarning().log("Failed to find span to add attribute.");
        }
    }
}
