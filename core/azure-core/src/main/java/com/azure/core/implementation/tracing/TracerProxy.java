// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.tracing;

import com.azure.core.http.ContextData;
import java.util.ServiceLoader;

/**
 * This class provides a means for all client libraries to augment the context information they have received from an
 * end user with additional distributed tracing information, that may then be passed on to a backend for analysis.
 */
public final class TracerProxy {

    private static ServiceLoader<? extends Tracer> tracers;

    static {
        tracers = ServiceLoader.load(Tracer.class);
    }

    private TracerProxy() {
        // no-op
    }

    public static ContextData start(String methodName, ContextData context) {
        ContextData local = context;
        for (Tracer tracer : tracers) {
            local = tracer.start(methodName, local);
        }

        return local;
    }

    public static void setAttribute(String key, String value, ContextData context) {
        tracers.forEach(tracer -> tracer.setAttribute(key, value, context));
    }


    public static void end(int responseCode, Throwable error, ContextData context) {
        tracers.forEach(tracer -> tracer.end(responseCode, error, context));
    }
}

