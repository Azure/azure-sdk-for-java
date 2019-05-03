package com.azure.tracing.opencensus;

import com.azure.common.http.ContextData;
import com.azure.common.implementation.tracing.TraceBuilder;
import io.opencensus.trace.Span;

import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.util.Optional;

public class OpenCensusTraceBuilder implements TraceBuilder {

    // Singleton OpenCensus tracer capable of starting and exporting spans.
    private static final Tracer tracer = Tracing.getTracer();

    public void trace(String methodName, ContextData context) {
        /*
         * TODO inspect ContextData for existing OpenCensus-specific tracing information. If present, augment. If not
         * present, create new tracing information.
         *
         * TODO Need to determine the key that users will use to insert the tracing information into the Context, or if a utility method will do this for end users.
         */
        Optional<Object> spanOptional = context.getData("OPEN_CENSUS_SPAN");
        if (!spanOptional.isPresent()) {
            // If no parentSpan, then span MIGHT exist via byte-code instrumentation, TLS, etc
            // let's put it there
            Span span = tracer.getCurrentSpan();
            context.addData("OPEN_CENSUS_SPAN", span);
        }

        context.addData("OPERATION_NAME", methodName);
    }
}
