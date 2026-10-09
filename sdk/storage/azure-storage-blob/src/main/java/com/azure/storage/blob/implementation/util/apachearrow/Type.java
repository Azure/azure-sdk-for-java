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

/**
 * Discriminator values for the Arrow IPC {@code Type} union, identifying a field's logical type.
 */
public final class Type {
    private Type() {
    }

    /** No type. */
    public static final byte NONE = 0;
    /** Null type. */
    public static final byte NULL = 1;
    /** Integer type (see {@link Int}). */
    public static final byte INT = 2;
    /** Floating-point type. */
    public static final byte FLOATING_POINT = 3;
    /** Variable-length binary type. */
    public static final byte BINARY = 4;
    /** Variable-length UTF-8 string type. */
    public static final byte UTF8 = 5;
    /** Boolean type. */
    public static final byte BOOL = 6;
    /** Decimal type. */
    public static final byte DECIMAL = 7;
    /** Date type. */
    public static final byte DATE = 8;
    /** Time-of-day type. */
    public static final byte TIME = 9;
    /** Timestamp type (see {@link Timestamp}). */
    public static final byte TIMESTAMP = 10;
    /** Interval type. */
    public static final byte INTERVAL = 11;
    /** List type. */
    public static final byte LIST = 12;
    /** Struct type. */
    public static final byte STRUCT = 13;
    /** Union type. */
    public static final byte UNION = 14;
    /** Fixed-size binary type. */
    public static final byte FIXED_SIZE_BINARY = 15;
    /** Fixed-size list type. */
    public static final byte FIXED_SIZE_LIST = 16;
    /** Map type. */
    public static final byte MAP = 17;
    /** Duration type. */
    public static final byte DURATION = 18;
    /** Large variable-length binary type. */
    public static final byte LARGE_BINARY = 19;
    /** Large variable-length UTF-8 string type. */
    public static final byte LARGE_UTF8 = 20;
    /** Large list type. */
    public static final byte LARGE_LIST = 21;
    /** Run-end encoded type. */
    public static final byte RUN_END_ENCODED = 22;
    /** Binary view type. */
    public static final byte BINARY_VIEW = 23;
    /** UTF-8 string view type. */
    public static final byte UTF8_VIEW = 24;
    /** List view type. */
    public static final byte LIST_VIEW = 25;
    /** Large list view type. */
    public static final byte LARGE_LIST_VIEW = 26;

    private static final String[] NAMES = {
        "NONE",
        "Null",
        "Int",
        "FloatingPoint",
        "Binary",
        "Utf8",
        "Bool",
        "Decimal",
        "Date",
        "Time",
        "Timestamp",
        "Interval",
        "List",
        "Struct_",
        "Union",
        "FixedSizeBinary",
        "FixedSizeList",
        "Map",
        "Duration",
        "LargeBinary",
        "LargeUtf8",
        "LargeList",
        "RunEndEncoded",
        "BinaryView",
        "Utf8View",
        "ListView",
        "LargeListView" };

    /**
     * Gets the canonical Arrow name for a {@code Type} union discriminator, for diagnostic messages.
     *
     * @param e the discriminator value.
     * @return the canonical Arrow type name.
     */
    public static String name(int e) {
        return NAMES[e];
    }
}
