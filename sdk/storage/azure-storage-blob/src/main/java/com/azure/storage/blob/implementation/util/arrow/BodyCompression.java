// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code BodyCompression} table.
 * <p>
 * The ListBlobs reader only needs to detect the presence of this table to reject compressed record batches, so no
 * fields are exposed.
 */
public final class BodyCompression extends Table {
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
    public BodyCompression __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }
}

