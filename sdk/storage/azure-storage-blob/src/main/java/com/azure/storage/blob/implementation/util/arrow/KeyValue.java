// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code KeyValue} table (a single custom metadata entry).
 */
public final class KeyValue extends Table {
    /**
     * Positions this accessor at the given table offset.
     *
     * @param i the table offset.
     * @param bb the backing buffer.
     */
    public void __init(int i, ByteBuffer bb) {
        __reset(i, bb);
    }

    /**
     * Assigns this accessor to the given table offset.
     *
     * @param i the table offset.
     * @param bb the backing buffer.
     * @return this accessor.
     */
    public KeyValue __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the metadata key.
     *
     * @return the key, or {@code null} when absent.
     */
    public String key() {
        int o = __offset(4);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    /**
     * Gets the metadata value.
     *
     * @return the value, or {@code null} when absent.
     */
    public String value() {
        int o = __offset(6);
        return o != 0 ? __string(o + bb_pos) : null;
    }
}
