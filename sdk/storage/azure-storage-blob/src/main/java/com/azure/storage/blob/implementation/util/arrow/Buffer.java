// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code Buffer} struct (offset/length of a buffer within a record batch body).
 */
public final class Buffer extends Struct {
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
    public Buffer __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the byte offset of the buffer relative to the start of the record batch body.
     *
     * @return the buffer offset.
     */
    public long offset() {
        return bb.getLong(bb_pos);
    }

    /**
     * Gets the length, in bytes, of the buffer.
     *
     * @return the buffer length.
     */
    public long length() {
        return bb.getLong(bb_pos + 8);
    }
}

