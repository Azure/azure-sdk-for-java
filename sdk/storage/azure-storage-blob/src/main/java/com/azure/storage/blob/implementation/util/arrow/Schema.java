// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;

/**
 * Accessor for the Arrow IPC {@code Schema} table.
 */
public final class Schema extends Table {
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
    public Schema __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the byte order of the schema's buffers (see {@link Endianness}).
     *
     * @return the endianness, or {@code 0} ({@link Endianness#LITTLE}) when absent.
     */
    public short endianness() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    /**
     * Gets the field at the given index.
     *
     * @param j the field index.
     * @return the field accessor.
     */
    public Field fields(int j) {
        return fields(new Field(), j);
    }

    /**
     * Gets the field at the given index into the supplied accessor.
     *
     * @param obj the accessor to assign.
     * @param j the field index.
     * @return the assigned accessor, or {@code null} when absent.
     */
    public Field fields(Field obj, int j) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    /**
     * Gets the number of top-level fields in the schema.
     *
     * @return the field count.
     */
    public int fieldsLength() {
        int o = __offset(6);
        return o != 0 ? __vector_len(o) : 0;
    }

    /**
     * Gets the custom metadata entry at the given index.
     *
     * @param j the entry index.
     * @return the key/value accessor.
     */
    public KeyValue customMetadata(int j) {
        return customMetadata(new KeyValue(), j);
    }

    /**
     * Gets the custom metadata entry at the given index into the supplied accessor.
     *
     * @param obj the accessor to assign.
     * @param j the entry index.
     * @return the assigned accessor, or {@code null} when absent.
     */
    public KeyValue customMetadata(KeyValue obj, int j) {
        int o = __offset(8);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    /**
     * Gets the number of custom metadata entries.
     *
     * @return the entry count.
     */
    public int customMetadataLength() {
        int o = __offset(8);
        return o != 0 ? __vector_len(o) : 0;
    }
}
