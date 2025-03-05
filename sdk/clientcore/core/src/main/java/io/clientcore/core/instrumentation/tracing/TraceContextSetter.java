// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

/**
 * A {@code TextMapSetter} sets context fields on a carrier, such as {@link io.clientcore.core.http.models.HttpRequest}.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 *
 * @param <C> the type of the carrier.
 */
public interface TraceContextSetter<C> {
    /**
     * Sets the context property on the carrier.
     *
     * @param carrier The carrier to set the context property on.
     * @param key The key of the context property.
     * @param value The value of the context property.
     */
    void set(C carrier, String key, String value);
}
