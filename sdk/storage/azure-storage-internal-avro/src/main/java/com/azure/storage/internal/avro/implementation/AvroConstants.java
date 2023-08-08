// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Constants for Avro.
 */
public class AvroConstants {
    public static final int SYNC_MARKER_SIZE = 16;
    public static final List<Byte> MAGIC_BYTES = Collections.unmodifiableList(
        Arrays.asList((byte) 'O', (byte) 'b', (byte) 'j', (byte) 1));
    public static final String CODEC_KEY = "avro.codec";
    public static final String SCHEMA_KEY = "avro.schema";
    public static final String NULL_CODEC = "null";
    public static final String MAGIC = "magic";
    public static final String META = "meta";
    public static final String SYNC = "sync";
    public static final String RECORD = "$record";

    public static final long BOOLEAN_SIZE = 1;
    public static final long FLOAT_SIZE = 4;
    public static final long DOUBLE_SIZE = 8;

    public static class Types {
        public static final String NULL = "null";
        public static final String BOOLEAN = "boolean";
        public static final String STRING = "string";
        public static final String BYTES = "bytes";
        public static final String INT = "int";
        public static final String LONG = "long";
        public static final String FLOAT = "float";
        public static final String DOUBLE = "double";
        public static final String FIXED = "fixed";
        public static final String ENUM = "enum";
        public static final String RECORD = "record";
        public static final String ARRAY = "array";
        public static final String MAP = "map";
        public static final String UNION = "union";

        public static final Set<String> PRIMITIVE_TYPES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(NULL, BOOLEAN, STRING, BYTES, INT, LONG, FLOAT, DOUBLE)));
    }
}
