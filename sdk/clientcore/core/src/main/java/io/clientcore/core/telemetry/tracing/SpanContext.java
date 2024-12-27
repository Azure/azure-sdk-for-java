// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

/**
 * Uniquely identifies span in a trace. It is propagated over the wire and
 * is used to correlated spans across different services.
 * <p>
 *
 * <strong>Classes in this package are not intended to be used by client libraries. Application developers
 * should use OpenTelemetry API directly</strong>
 */
public interface SpanContext {

    /**
     * Gets the trace id.
     * @return The trace id.
     */
    String getTraceId();

    /**
     * Gets the span id.
     * @return The span id.
     */
    String getSpanId();

    /**
     * Gets the string (hex-encoded) representation of trace flags.
     *
     * @return The trace flags.
     */
    String getTraceFlags();
}
