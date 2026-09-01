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
 * Accessor for the Arrow IPC {@code Field} table describing a single column.
 */
public final class Field extends Table {
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
    public Field __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the field name.
     *
     * @return the field name, or {@code null} when absent.
     */
    public String name() {
        int o = __offset(4);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    /**
     * Gets the discriminator identifying the field's {@code type} union (see {@link Type}).
     *
     * @return the type union discriminator, or {@code 0} when absent.
     */
    public byte typeType() {
        int o = __offset(8);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    /**
     * Resolves the {@code type} union value into the supplied accessor.
     *
     * @param obj the accessor to assign to the union value.
     * @return the assigned accessor, or {@code null} when absent.
     */
    public Table type(Table obj) {
        int o = __offset(10);
        return o != 0 ? __union(obj, o + bb_pos) : null;
    }

    /**
     * Gets the child field at the given index.
     *
     * @param j the child index.
     * @return the child field accessor.
     */
    public Field children(int j) {
        return children(new Field(), j);
    }

    /**
     * Gets the child field at the given index into the supplied accessor.
     *
     * @param obj the accessor to assign.
     * @param j the child index.
     * @return the assigned accessor, or {@code null} when absent.
     */
    public Field children(Field obj, int j) {
        int o = __offset(14);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    /**
     * Gets the number of child fields.
     *
     * @return the child field count.
     */
    public int childrenLength() {
        int o = __offset(14);
        return o != 0 ? __vector_len(o) : 0;
    }
}
