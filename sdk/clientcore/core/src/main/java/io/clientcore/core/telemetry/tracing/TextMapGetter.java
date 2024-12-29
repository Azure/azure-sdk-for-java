// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

/**
 * A {@code TextMapGetter} retrieves context fields from a carrier, such as {@link io.clientcore.core.http.models.HttpRequest}.
 *
 * @param <C> the type of the carrier.
 */
public interface TextMapGetter<C> {
    /**
     * Returns all the keys in the given carrier.
     *
     * @param carrier carrier of propagation fields, such as an http request.
     *
     * @return all the keys in the given carrier.
     */
    Iterable<String> keys(C carrier);

    /**
     * Returns the first value of the given propagation {@code key} or returns {@code null}.
     *
     * @param carrier carrier of propagation fields, such as an http request.
     * @param key the key of the field.
     * @return the first value of the given propagation {@code key} or returns {@code null}.
     */
    String get(C carrier, String key);
}
