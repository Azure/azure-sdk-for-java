// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.json;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Encodes JSON-compatible values plus {@link CosmosBinary} into Cosmos Binary JSON. */
final class CosmosBinaryWriter {
    private static final int FORMAT_BINARY = 0x80;
    private static final int STRING_LENGTH_BASE = 0x80;
    private static final int STRING_LENGTH_1 = 0xC0;
    private static final int STRING_LENGTH_2 = 0xC1;
    private static final int STRING_LENGTH_4 = 0xC2;
    private static final int NUMBER_UINT64 = 0xC7;
    private static final int NUMBER_UINT8 = 0xC8;
    private static final int NUMBER_INT16 = 0xC9;
    private static final int NUMBER_INT32 = 0xCA;
    private static final int NUMBER_INT64 = 0xCB;
    private static final int NUMBER_DOUBLE = 0xCC;
    private static final int NULL = 0xD0;
    private static final int FALSE = 0xD1;
    private static final int TRUE = 0xD2;
    private static final int BINARY_LENGTH_1 = 0xDD;
    private static final int BINARY_LENGTH_2 = 0xDE;
    private static final int BINARY_LENGTH_4 = 0xDF;
    private static final int ARRAY_EMPTY = 0xE0;
    private static final int ARRAY_SINGLE = 0xE1;
    private static final int ARRAY_LENGTH_1 = 0xE2;
    private static final int ARRAY_LENGTH_2 = 0xE3;
    private static final int ARRAY_LENGTH_4 = 0xE4;
    private static final int OBJECT_EMPTY = 0xE8;
    private static final int OBJECT_SINGLE = 0xE9;
    private static final int OBJECT_LENGTH_1 = 0xEA;
    private static final int OBJECT_LENGTH_2 = 0xEB;
    private static final int OBJECT_LENGTH_4 = 0xEC;
    private static final int MAX_SCOPE_PREFIX = 5;
    private static final int MAX_NESTING_DEPTH = 128;

    private static final Map<String, Integer> SYSTEM_STRING_IDS = systemStringIds();

    private final Buffer buffer;

    private CosmosBinaryWriter(int initialCapacity) {
        this.buffer = new Buffer(initialCapacity);
        this.buffer.writeByte(FORMAT_BINARY);
    }

    static byte[] encode(Object value) {
        CosmosBinaryWriter writer = new CosmosBinaryWriter(256);
        writer.writeValue(value, 0);
        return writer.buffer.toByteArray();
    }

    private void writeValue(Object value, int depth) {
        if (depth > MAX_NESTING_DEPTH) {
            throw new IllegalArgumentException("Maximum nesting depth exceeded");
        }
        if (value == null) {
            buffer.writeByte(NULL);
        } else if (value instanceof String) {
            writeString((String) value, false);
        } else if (value instanceof Boolean) {
            buffer.writeByte((Boolean) value ? TRUE : FALSE);
        } else if (value instanceof Byte || value instanceof Short
            || value instanceof Integer || value instanceof Long) {
            writeInteger(((Number) value).longValue());
        } else if (value instanceof BigInteger) {
            writeBigInteger((BigInteger) value);
        } else if (value instanceof Float || value instanceof Double) {
            buffer.writeByte(NUMBER_DOUBLE);
            buffer.writeLittleEndian(Double.doubleToRawLongBits(((Number) value).doubleValue()), 8);
        } else if (value instanceof CosmosBinary) {
            writeBinary((CosmosBinary) value);
        } else if (value instanceof byte[]) {
            throw new IllegalArgumentException(
                "byte[] is ambiguous; wrap native bytes with CosmosBinary.fromBytes(...)");
        } else if (value instanceof Map<?, ?>) {
            writeObject((Map<?, ?>) value, depth + 1);
        } else if (value instanceof List<?>) {
            writeArray((List<?>) value, depth + 1);
        } else {
            throw new IllegalArgumentException("Unsupported Cosmos value: " + value.getClass().getName());
        }
    }

    private void writeObject(Map<?, ?> fields, int depth) {
        if (fields.isEmpty()) {
            buffer.writeByte(OBJECT_EMPTY);
            return;
        }
        if (fields.size() == 1) {
            buffer.writeByte(OBJECT_SINGLE);
            writeFields(fields, depth);
            return;
        }
        int scopeStart = buffer.reserve(MAX_SCOPE_PREFIX);
        int payloadStart = buffer.position();
        writeFields(fields, depth);
        finishScope(scopeStart, payloadStart, OBJECT_LENGTH_1, OBJECT_LENGTH_2, OBJECT_LENGTH_4);
    }

    private void writeFields(Map<?, ?> fields, int depth) {
        for (Map.Entry<?, ?> field : fields.entrySet()) {
            if (!(field.getKey() instanceof String)) {
                throw new IllegalArgumentException("Cosmos object keys must be strings");
            }
            writeString((String) field.getKey(), true);
            writeValue(field.getValue(), depth);
        }
    }

    private void writeArray(List<?> values, int depth) {
        if (values.isEmpty()) {
            buffer.writeByte(ARRAY_EMPTY);
            return;
        }
        if (values.size() == 1) {
            buffer.writeByte(ARRAY_SINGLE);
            writeValue(values.get(0), depth);
            return;
        }
        int scopeStart = buffer.reserve(MAX_SCOPE_PREFIX);
        int payloadStart = buffer.position();
        for (Object value : values) {
            writeValue(value, depth);
        }
        finishScope(scopeStart, payloadStart, ARRAY_LENGTH_1, ARRAY_LENGTH_2, ARRAY_LENGTH_4);
    }

    private void finishScope(int scopeStart, int payloadStart, int marker1, int marker2, int marker4) {
        int payloadLength = buffer.position() - payloadStart;
        int prefixLength;
        int marker;
        if (payloadLength <= 0xFF) {
            prefixLength = 2;
            marker = marker1;
        } else if (payloadLength <= 0xFFFF) {
            prefixLength = 3;
            marker = marker2;
        } else {
            prefixLength = 5;
            marker = marker4;
        }
        buffer.compact(payloadStart, payloadLength, scopeStart + prefixLength);
        buffer.setPosition(scopeStart);
        buffer.writeByte(marker);
        buffer.writeLittleEndian(payloadLength, prefixLength - 1);
        buffer.setPosition(scopeStart + prefixLength + payloadLength);
    }

    private void writeString(String value, boolean fieldName) {
        if (fieldName) {
            Integer systemId = SYSTEM_STRING_IDS.get(value);
            if (systemId != null) {
                buffer.writeByte(0x20 + systemId);
                return;
            }
        }
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        if (utf8.length <= 63) {
            buffer.writeByte(STRING_LENGTH_BASE + utf8.length);
        } else if (utf8.length <= 0xFF) {
            buffer.writeByte(STRING_LENGTH_1);
            buffer.writeLittleEndian(utf8.length, 1);
        } else if (utf8.length <= 0xFFFF) {
            buffer.writeByte(STRING_LENGTH_2);
            buffer.writeLittleEndian(utf8.length, 2);
        } else {
            buffer.writeByte(STRING_LENGTH_4);
            buffer.writeLittleEndian(utf8.length, 4);
        }
        buffer.writeBytes(utf8);
    }

    private void writeInteger(long value) {
        if (value >= 0 && value < 32) {
            buffer.writeByte((int) value);
        } else if (value >= 0 && value <= 0xFF) {
            buffer.writeByte(NUMBER_UINT8);
            buffer.writeByte((int) value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            buffer.writeByte(NUMBER_INT16);
            buffer.writeLittleEndian(value, 2);
        } else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            buffer.writeByte(NUMBER_INT32);
            buffer.writeLittleEndian(value, 4);
        } else {
            buffer.writeByte(NUMBER_INT64);
            buffer.writeLittleEndian(value, 8);
        }
    }

    private void writeBigInteger(BigInteger value) {
        if (value.signum() < 0 || value.bitLength() > 64) {
            throw new IllegalArgumentException("Integer is outside unsigned 64-bit range: " + value);
        }
        if (value.bitLength() < 64) {
            writeInteger(value.longValue());
            return;
        }
        buffer.writeByte(NUMBER_UINT64);
        byte[] bigEndian = value.toByteArray();
        for (int index = 0; index < 8; index++) {
            buffer.writeByte(bigEndian[bigEndian.length - 1 - index]);
        }
    }

    private void writeBinary(CosmosBinary value) {
        byte[] bytes = value.unsafeValue();
        writeLengthPrefix(bytes.length, BINARY_LENGTH_1, BINARY_LENGTH_2, BINARY_LENGTH_4);
        buffer.writeBytes(bytes);
    }

    private void writeLengthPrefix(int length, int marker1, int marker2, int marker4) {
        if (length <= 0xFF) {
            buffer.writeByte(marker1);
            buffer.writeLittleEndian(length, 1);
        } else if (length <= 0xFFFF) {
            buffer.writeByte(marker2);
            buffer.writeLittleEndian(length, 2);
        } else {
            buffer.writeByte(marker4);
            buffer.writeLittleEndian(length, 4);
        }
    }

    private static Map<String, Integer> systemStringIds() {
        String[] strings = CosmosBinaryReader.systemStrings();
        Map<String, Integer> ids = new HashMap<>();
        for (int index = 0; index < strings.length; index++) {
            ids.put(strings[index], index);
        }
        return ids;
    }

    private static final class Buffer {
        private byte[] bytes;
        private int position;

        private Buffer(int capacity) {
            this.bytes = new byte[capacity];
        }

        private int position() {
            return position;
        }

        private void setPosition(int position) {
            this.position = position;
        }

        private int reserve(int length) {
            ensureCapacity(length);
            int start = position;
            position += length;
            return start;
        }

        private void writeByte(int value) {
            ensureCapacity(1);
            bytes[position++] = (byte) value;
        }

        private void writeBytes(byte[] value) {
            ensureCapacity(value.length);
            System.arraycopy(value, 0, bytes, position, value.length);
            position += value.length;
        }

        private void writeLittleEndian(long value, int width) {
            ensureCapacity(width);
            for (int index = 0; index < width; index++) {
                bytes[position++] = (byte) (value >>> (index * 8));
            }
        }

        private void compact(int source, int length, int destination) {
            System.arraycopy(bytes, source, bytes, destination, length);
        }

        private byte[] toByteArray() {
            return java.util.Arrays.copyOf(bytes, position);
        }

        private void ensureCapacity(int additionalLength) {
            int required = position + additionalLength;
            if (required <= bytes.length) {
                return;
            }
            int capacity = bytes.length;
            while (capacity < required) {
                capacity = Math.max(capacity << 1, required);
            }
            bytes = java.util.Arrays.copyOf(bytes, capacity);
        }
    }
}
