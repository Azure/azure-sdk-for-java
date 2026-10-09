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
