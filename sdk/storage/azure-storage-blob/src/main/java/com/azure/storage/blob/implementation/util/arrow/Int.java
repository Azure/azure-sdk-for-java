// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code Int} type table.
 */
public final class Int extends Table {
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
    public Int __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the bit width of the integer (8, 16, 32, or 64).
     *
     * @return the bit width, or {@code 0} when absent.
     */
    public int bitWidth() {
        int o = __offset(4);
        return o != 0 ? bb.getInt(o + bb_pos) : 0;
    }

    /**
     * Gets whether the integer is signed.
     *
     * @return {@code true} if signed, otherwise {@code false}.
     */
    public boolean isSigned() {
        int o = __offset(6);
        return o != 0 && 0 != bb.get(o + bb_pos);
    }
}

