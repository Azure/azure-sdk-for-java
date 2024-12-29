// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

public interface TextMapSetter<C> {
    /**
     * Sets the context property on the carrier.
     *
     * @param carrier The carrier to set the context property on.
     * @param key The key of the context property.
     * @param value The value of the context property.
     */
    void set(C carrier, String key, String value);
}
