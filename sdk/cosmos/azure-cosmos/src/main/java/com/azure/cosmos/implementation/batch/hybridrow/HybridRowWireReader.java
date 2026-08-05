// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class HybridRowWireReader {
    private final ByteBuf buffer;

    HybridRowWireReader(ByteBuf buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    int readableBytes() {
        return buffer.readableBytes();
    }

    int readUnsignedByte() {
        require(Byte.BYTES);
        return buffer.readUnsignedByte();
    }

    int readInt32() {
        require(Integer.BYTES);
        return buffer.readIntLE();
    }

    int readNonNegativeInt32(String field) {
        int value = readInt32();
        if (value < 0) {
            throw corrupt(field + " is negative");
        }
        return value;
    }

    long readUnsignedInt32() {
        require(Integer.BYTES);
        return buffer.readUnsignedIntLE();
    }

    double readFloat64() {
        require(Double.BYTES);
        return buffer.readDoubleLE();
    }

    String readVariableString() {
        byte[] value = readVariableBytes();
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(value))
                .toString();
        } catch (CharacterCodingException error) {
            throw new CorruptedFrameException("Invalid UTF-8 in HybridRow string", error);
        }
    }

    byte[] readVariableBytes() {
        int length = readVarUInt();
        require(length);
        byte[] value = new byte[length];
        buffer.readBytes(value);
        return value;
    }

    ByteBuf readRetainedSlice(int length) {
        require(length);
        return buffer.readRetainedSlice(length);
    }

    void expectByte(int expected, String field) {
        int actual = readUnsignedByte();
        if (actual != expected) {
            throw corrupt("Invalid " + field + ": " + actual);
        }
    }

    void expectInt32(int expected, String field) {
        int actual = readInt32();
        if (actual != expected) {
            throw corrupt("Invalid " + field + ": " + actual);
        }
    }

    private int readVarUInt() {
        int value = 0;
        for (int index = 0; index < 5; index++) {
            int next = readUnsignedByte();
            if (index == 4 && (next & 0xF0) != 0) {
                throw corrupt("Variable length exceeds int range");
            }
            value |= (next & 0x7F) << (index * 7);
            if ((next & 0x80) == 0) {
                return value;
            }
        }
        throw corrupt("Invalid variable length");
    }

    private void require(int length) {
        if (length < 0 || buffer.readableBytes() < length) {
            throw corrupt("Truncated HybridRow payload");
        }
    }

    private static CorruptedFrameException corrupt(String message) {
        return new CorruptedFrameException(message);
    }
}
