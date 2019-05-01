// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.implementation.tracing;

import com.azure.common.http.ContextData;

import java.util.ServiceLoader;

/**
 * This class provides a means for all client libraries to augment the context information they have received from an
 * end user with additional distributed tracing information, that may then be passed on to a backend for analysis.
 */
public class Tracer {
    private static ServiceLoader<? extends TraceBuilder> traceBuilders;
    static {
        traceBuilders = ServiceLoader.load(TraceBuilder.class);
    }

    private Tracer() {
        // no-op
    }

    // TODO determine what tracing information must be passed in here, and update the TraceBuilder interface accordingly
    public static void trace(String methodName, ContextData context) {
        traceBuilders.forEach(traceBuilder -> traceBuilder.trace(methodName, context));
    }
}
