package com.azure.tracing.opencensus;

import com.azure.common.http.ContextData;
import com.azure.common.implementation.tracing.TraceBuilder;
import io.opencensus.trace.Span;

import java.util.Optional;

public class OpenCensusTraceBuilder implements TraceBuilder {

    public void trace(String methodName, ContextData context) {
        /*
         * TODO inspect ContextData for existing OpenCensus-specific tracing information. If present, augment. If not
         * present, create new tracing information.
         *
         * TODO Need to determine the key that users will use to insert the tracing information into the Context, or if a utility method will do this for end users.
         */
        Optional<Object> spanOptional = context.getData("OPEN_CENSUS_SPAN");
        if (spanOptional.isPresent()) {
            Span span = (Span) spanOptional.get();
            // augment existing span
        } else {
            // no tracing information found, create a new one and insert it back into the Context
        }
    }
}
