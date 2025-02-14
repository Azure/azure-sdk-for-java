// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

/**
 * Represents a collection of attributes that can be used to augment telemetry.
 */
public interface InstrumentationAttributes {
    /**
     * Creates a new instance of attributes containing original and new attribute .
     * @param key The key.
     * @param value The value.
     * @return A new instance of attributes containing original and new attribute.
     */
    InstrumentationAttributes put(String key, Object value);
}
