/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.storage.blob.implementation.util.apachearrow;

import com.azure.storage.blob.implementation.util.flatbuffers.Table;

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
