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

import com.azure.storage.blob.implementation.util.flatbuffers.Struct;

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
