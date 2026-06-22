// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code Timestamp} type table.
 */
public final class Timestamp extends Table {
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
    public Timestamp __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the timestamp resolution (see {@link TimeUnit}).
     *
     * @return the time unit, or {@code 0} ({@link TimeUnit#SECOND}) when absent.
     */
    public short unit() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }
}

