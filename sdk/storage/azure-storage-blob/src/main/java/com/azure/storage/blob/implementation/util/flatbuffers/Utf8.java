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

/**
 * UTF-8 encoder and decoder used by the FlatBuffers table runtime.
 */
public abstract class Utf8 {
    private static Utf8 defaultInstance;

    public abstract int encodedLength(CharSequence sequence);

    public abstract void encodeUtf8(CharSequence input, ByteBuffer output);

    public abstract String decodeUtf8(ByteBuffer buffer, int offset, int length);

    public static Utf8 getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new Utf8Safe();
        }
        return defaultInstance;
    }

    public static void setDefault(Utf8 instance) {
        defaultInstance = instance;
    }

    static final class DecodeUtil {
        static boolean isOneByte(byte value) {
            return value >= 0;
        }

        static boolean isTwoBytes(byte value) {
            return value < (byte) 0xE0;
        }

        static boolean isThreeBytes(byte value) {
            return value < (byte) 0xF0;
        }

        static void handleOneByte(byte byte1, char[] result, int resultPosition) {
            result[resultPosition] = (char) byte1;
        }

        static void handleTwoBytes(byte byte1, byte byte2, char[] result, int resultPosition) {
            if (byte1 < (byte) 0xC2 || isNotTrailingByte(byte2)) {
                throw new IllegalArgumentException("Invalid UTF-8");
            }
            result[resultPosition] = (char) (((byte1 & 0x1F) << 6) | trailingByteValue(byte2));
        }

        static void handleThreeBytes(byte byte1, byte byte2, byte byte3, char[] result, int resultPosition) {
            if (isNotTrailingByte(byte2)
                || (byte1 == (byte) 0xE0 && byte2 < (byte) 0xA0)
                || (byte1 == (byte) 0xED && byte2 >= (byte) 0xA0)
                || isNotTrailingByte(byte3)) {
                throw new IllegalArgumentException("Invalid UTF-8");
            }
            result[resultPosition]
                = (char) (((byte1 & 0x0F) << 12) | (trailingByteValue(byte2) << 6) | trailingByteValue(byte3));
        }

        static void handleFourBytes(byte byte1, byte byte2, byte byte3, byte byte4, char[] result, int resultPosition) {
            if (isNotTrailingByte(byte2)
                || (((byte1 << 28) + (byte2 - (byte) 0x90)) >> 30) != 0
                || isNotTrailingByte(byte3)
                || isNotTrailingByte(byte4)) {
                throw new IllegalArgumentException("Invalid UTF-8");
            }
            int codePoint = ((byte1 & 0x07) << 18) | (trailingByteValue(byte2) << 12) | (trailingByteValue(byte3) << 6)
                | trailingByteValue(byte4);
            result[resultPosition] = Character.highSurrogate(codePoint);
            result[resultPosition + 1] = Character.lowSurrogate(codePoint);
        }

        private static boolean isNotTrailingByte(byte value) {
            return value > (byte) 0xBF;
        }

        private static int trailingByteValue(byte value) {
            return value & 0x3F;
        }
    }

    static final class UnpairedSurrogateException extends IllegalArgumentException {
        UnpairedSurrogateException(int index, int length) {
            super("Unpaired surrogate at index " + index + " of " + length);
        }
    }
}
