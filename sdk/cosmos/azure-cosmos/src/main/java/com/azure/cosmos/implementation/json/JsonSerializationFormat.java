// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

/**
 * Defines JSON different serialization Formats.
 */
enum JsonSerializationFormat {

    /**
     * Plain text.
     */
    TEXT((byte) 0),

    /**
     * Binary Encoding.
     */
    BINARY((byte) 128),

    /**
     * HybridRow Binary Encoding.
     */
    HYBRIDROW((byte) 129);

    // All other format values need to be > 127,
    // otherwise a valid JSON starting character (0-9, f[alse], t[rue], n[ull],{,[,") might be interpreted as a serialization format.

    JsonSerializationFormat(byte byteValue) {
        this.byteValue = byteValue;
    }

    private final byte byteValue;
}
