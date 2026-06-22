// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code FieldNode} struct (per-column metadata within a record batch).
 */
public final class FieldNode extends Struct {
    /**
     * Positions this accessor at the given struct offset.
     *
     * @param i the struct offset.
     * @param bb the backing buffer.
     */
    public void __init(int i, ByteBuffer bb) {
        __reset(i, bb);
    }

    /**
     * Assigns this accessor to the given struct offset.
     *
     * @param i the struct offset.
     * @param bb the backing buffer.
     * @return this accessor.
     */
    public FieldNode __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the number of value slots in the column.
     *
     * @return the value count.
     */
    public long length() {
        return bb.getLong(bb_pos);
    }

    /**
     * Gets the number of null value slots in the column.
     *
     * @return the null count.
     */
    public long nullCount() {
        return bb.getLong(bb_pos + 8);
    }
}

