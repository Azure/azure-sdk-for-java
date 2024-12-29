// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

public interface TextMapGetter<C> {
    /**
     * Returns all the keys in the given carrier.
     *
     * @param carrier carrier of propagation fields, such as an http request.
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
