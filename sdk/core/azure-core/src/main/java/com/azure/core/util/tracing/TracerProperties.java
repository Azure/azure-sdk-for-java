// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

/**
 * A generic interface for setting tracing properties on outgoing HTTP requests.
 */
public interface TracerProperties {

    /**
     * Gets the attributes to be set on the tracer spans for the request.
     *
     * @return the attributes to be set on the tracer span.
     */
    TracerSpanAttributes getTracerSpanAttributes();
}
