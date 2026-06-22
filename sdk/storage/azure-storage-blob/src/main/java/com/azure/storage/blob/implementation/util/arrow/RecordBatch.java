// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code RecordBatch} table.
 */
public final class RecordBatch extends Table {
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
    public RecordBatch __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the number of rows in the record batch.
     *
     * @return the row count, or {@code 0} when absent.
     */
    public long length() {
        int o = __offset(4);
        return o != 0 ? bb.getLong(o + bb_pos) : 0L;
    }

    /**
     * Gets the field node at the given index.
     *
     * @param j the node index.
     * @return the field node accessor.
     */
    public FieldNode nodes(int j) {
        return nodes(new FieldNode(), j);
    }

    /**
     * Gets the field node at the given index into the supplied accessor.
     *
     * @param obj the accessor to assign.
     * @param j the node index.
     * @return the assigned accessor, or {@code null} when absent.
     */
    public FieldNode nodes(FieldNode obj, int j) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__vector(o) + j * 16, bb) : null;
    }

    /**
     * Gets the number of field nodes.
     *
     * @return the field node count.
     */
    public int nodesLength() {
        int o = __offset(6);
        return o != 0 ? __vector_len(o) : 0;
    }

    /**
     * Gets the buffer region at the given index.
     *
     * @param j the buffer index.
     * @return the buffer accessor.
     */
    public Buffer buffers(int j) {
        return buffers(new Buffer(), j);
    }

    /**
     * Gets the buffer region at the given index into the supplied accessor.
     *
     * @param obj the accessor to assign.
     * @param j the buffer index.
     * @return the assigned accessor, or {@code null} when absent.
     */
    public Buffer buffers(Buffer obj, int j) {
        int o = __offset(8);
        return o != 0 ? obj.__assign(__vector(o) + j * 16, bb) : null;
    }

    /**
     * Gets the number of buffers.
     *
     * @return the buffer count.
     */
    public int buffersLength() {
        int o = __offset(8);
        return o != 0 ? __vector_len(o) : 0;
    }

    /**
     * Gets the optional body compression descriptor.
     *
     * @return the body compression accessor, or {@code null} when the batch is uncompressed.
     */
    public BodyCompression compression() {
        return compression(new BodyCompression());
    }

    /**
     * Gets the optional body compression descriptor into the supplied accessor.
     *
     * @param obj the accessor to assign.
     * @return the assigned accessor, or {@code null} when the batch is uncompressed.
     */
    public BodyCompression compression(BodyCompression obj) {
        int o = __offset(10);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }
}

