/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Adapted by Microsoft from FlatBuffers Java 25.2.10 for the internal Blob Storage Arrow reader.

package com.azure.storage.blob.implementation.util.flatbuffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Comparator;

import static com.azure.storage.blob.implementation.util.flatbuffers.Constants.FILE_IDENTIFIER_LENGTH;
import static com.azure.storage.blob.implementation.util.flatbuffers.Constants.SIZEOF_INT;

/// @cond FLATBUFFERS_INTERNAL

/**
 * Base class for tables in generated FlatBuffers code.
 */
public class Table {
    protected int bb_pos;
    protected ByteBuffer bb;
    private int vtableStart;
    private int vtableSize;
    private final Utf8 utf8 = Utf8.getDefault();

    public ByteBuffer getByteBuffer() {
        return bb;
    }

    protected int __offset(int vtableOffset) {
        return vtableOffset < vtableSize ? bb.getShort(vtableStart + vtableOffset) : 0;
    }

    protected static int __offset(int vtableOffset, int offset, ByteBuffer byteBuffer) {
        int vtable = byteBuffer.capacity() - offset;
        return byteBuffer.getShort(vtable + vtableOffset - byteBuffer.getInt(vtable)) + vtable;
    }

    protected int __indirect(int offset) {
        return offset + bb.getInt(offset);
    }

    protected static int __indirect(int offset, ByteBuffer byteBuffer) {
        return offset + byteBuffer.getInt(offset);
    }

    protected String __string(int offset) {
        return __string(offset, bb, utf8);
    }

    protected static String __string(int offset, ByteBuffer byteBuffer, Utf8 utf8) {
        offset += byteBuffer.getInt(offset);
        int length = byteBuffer.getInt(offset);
        return utf8.decodeUtf8(byteBuffer, offset + SIZEOF_INT, length);
    }

    protected int __vector_len(int offset) {
        offset += bb_pos;
        offset += bb.getInt(offset);
        return bb.getInt(offset);
    }

    protected int __vector(int offset) {
        offset += bb_pos;
        return offset + bb.getInt(offset) + SIZEOF_INT;
    }

    protected ByteBuffer __vector_as_bytebuffer(int vectorOffset, int elementSize) {
        int offset = __offset(vectorOffset);
        if (offset == 0) {
            return null;
        }
        ByteBuffer byteBuffer = bb.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int vectorStart = __vector(offset);
        byteBuffer.position(vectorStart);
        byteBuffer.limit(vectorStart + __vector_len(offset) * elementSize);
        return byteBuffer;
    }

    protected ByteBuffer __vector_in_bytebuffer(ByteBuffer byteBuffer, int vectorOffset, int elementSize) {
        int offset = __offset(vectorOffset);
        if (offset == 0) {
            return null;
        }
        int vectorStart = __vector(offset);
        byteBuffer.rewind();
        byteBuffer.limit(vectorStart + __vector_len(offset) * elementSize);
        byteBuffer.position(vectorStart);
        return byteBuffer;
    }

    protected Table __union(Table table, int offset) {
        return __union(table, offset, bb);
    }

    protected static Table __union(Table table, int offset, ByteBuffer byteBuffer) {
        table.__reset(__indirect(offset, byteBuffer), byteBuffer);
        return table;
    }

    protected static boolean __has_identifier(ByteBuffer byteBuffer, String identifier) {
        if (identifier.length() != FILE_IDENTIFIER_LENGTH) {
            throw new AssertionError("FlatBuffers: file identifier must be length " + FILE_IDENTIFIER_LENGTH);
        }
        for (int index = 0; index < FILE_IDENTIFIER_LENGTH; index++) {
            if (identifier.charAt(index) != (char) byteBuffer.get(byteBuffer.position() + SIZEOF_INT + index)) {
                return false;
            }
        }
        return true;
    }

    protected void sortTables(int[] offsets, final ByteBuffer byteBuffer) {
        Integer[] boxedOffsets = new Integer[offsets.length];
        for (int index = 0; index < offsets.length; index++) {
            boxedOffsets[index] = offsets[index];
        }
        Arrays.sort(boxedOffsets, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                return keysCompare(first, second, byteBuffer);
            }
        });
        for (int index = 0; index < offsets.length; index++) {
            offsets[index] = boxedOffsets[index];
        }
    }

    protected int keysCompare(Integer first, Integer second, ByteBuffer byteBuffer) {
        return 0;
    }

    protected static int compareStrings(int firstOffset, int secondOffset, ByteBuffer byteBuffer) {
        firstOffset += byteBuffer.getInt(firstOffset);
        secondOffset += byteBuffer.getInt(secondOffset);
        int firstLength = byteBuffer.getInt(firstOffset);
        int secondLength = byteBuffer.getInt(secondOffset);
        int firstStart = firstOffset + SIZEOF_INT;
        int secondStart = secondOffset + SIZEOF_INT;
        int length = Math.min(firstLength, secondLength);
        for (int index = 0; index < length; index++) {
            if (byteBuffer.get(index + firstStart) != byteBuffer.get(index + secondStart)) {
                return byteBuffer.get(index + firstStart) - byteBuffer.get(index + secondStart);
            }
        }
        return firstLength - secondLength;
    }

    protected static int compareStrings(int offset, byte[] key, ByteBuffer byteBuffer) {
        offset += byteBuffer.getInt(offset);
        int valueLength = byteBuffer.getInt(offset);
        int keyLength = key.length;
        int start = offset + SIZEOF_INT;
        int length = Math.min(valueLength, keyLength);
        for (int index = 0; index < length; index++) {
            if (byteBuffer.get(index + start) != key[index]) {
                return byteBuffer.get(index + start) - key[index];
            }
        }
        return valueLength - keyLength;
    }

    protected void __reset(int index, ByteBuffer byteBuffer) {
        bb = byteBuffer;
        if (bb == null) {
            bb_pos = 0;
            vtableStart = 0;
            vtableSize = 0;
        } else {
            bb_pos = index;
            vtableStart = bb_pos - bb.getInt(bb_pos);
            vtableSize = bb.getShort(vtableStart);
        }
    }

    public void __reset() {
        __reset(0, null);
    }
}

/// @endcond
