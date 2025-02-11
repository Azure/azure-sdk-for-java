// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

/**
 * A {@code TextMapGetter} retrieves context fields from a carrier, such as {@link io.clientcore.core.http.models.HttpRequest}.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 *
 * @param <C> the type of the carrier.
 */
public interface TraceContextGetter<C> {
    /**
     * Returns all the keys in the given carrier.
     *
     * @param carrier carrier of propagation fields, such as http request.
     *
     * @return all the keys in the given carrier.
     */
    Iterable<String> keys(C carrier);

    /**
     * Returns the first value of the given propagation {@code key} or returns {@code null}.
     *
     * @param carrier carrier of propagation fields, such as http request.
     * @param key the key of the field.
     * @return the first value of the given propagation {@code key} or returns {@code null}.
     */
    String get(C carrier, String key);
}
