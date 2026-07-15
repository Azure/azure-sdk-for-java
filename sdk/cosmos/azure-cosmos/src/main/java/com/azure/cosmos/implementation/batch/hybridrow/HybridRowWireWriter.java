// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class HybridRowWireWriter {
    private final ByteBuf buffer;

    HybridRowWireWriter(ByteBuf buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    void writeByte(int value) {
        buffer.writeByte(value);
    }

    void writeInt32(int value) {
        buffer.writeIntLE(value);
    }

    void writeFloat64(double value) {
        buffer.writeDoubleLE(value);
    }

    void writeVariable(String value) {
        writeVariable(value == null ? null : value.getBytes(StandardCharsets.UTF_8));
    }

    void writeVariable(byte[] value) {
        if (value == null) {
            return;
        }
        writeVarUInt(value.length);
        buffer.writeBytes(value);
    }

    void writeSparseString(HybridRowBatchSchema.OperationField field, String value) {
        if (value == null) {
            return;
        }
        writeByte(HybridRowBatchSchema.SparseType.UTF8.code());
        writeVarUInt(field.pathToken());
        writeVariable(value);
    }

    void writeSparseBoolean(HybridRowBatchSchema.OperationField field, boolean value) {
        if (!value) {
            return;
        }
        writeByte(HybridRowBatchSchema.SparseType.BOOLEAN_TRUE.code());
        writeVarUInt(field.pathToken());
    }

    void writeVarUInt(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be nonnegative");
        }
        int remaining = value;
        do {
            int next = remaining & 0x7F;
            remaining >>>= 7;
            writeByte(remaining == 0 ? next : next | 0x80);
        } while (remaining != 0);
    }
}
