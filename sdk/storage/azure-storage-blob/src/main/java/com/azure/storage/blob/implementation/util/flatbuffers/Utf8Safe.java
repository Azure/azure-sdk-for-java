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
 * Safe UTF-8 implementation used by the FlatBuffers table runtime.
 */
public final class Utf8Safe extends Utf8 {
    private static int computeEncodedLength(CharSequence sequence) {
        int utf16Length = sequence.length();
        int utf8Length = utf16Length;
        int index = 0;
        while (index < utf16Length && sequence.charAt(index) < 0x80) {
            index++;
        }
        for (; index < utf16Length; index++) {
            char value = sequence.charAt(index);
            if (value < 0x800) {
                utf8Length += (0x7f - value) >>> 31;
            } else {
                utf8Length += encodedLengthGeneral(sequence, index);
                break;
            }
        }
        if (utf8Length < utf16Length) {
            throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (utf8Length + (1L << 32)));
        }
        return utf8Length;
    }

    private static int encodedLengthGeneral(CharSequence sequence, int start) {
        int utf16Length = sequence.length();
        int utf8Length = 0;
        for (int index = start; index < utf16Length; index++) {
            char value = sequence.charAt(index);
            if (value < 0x800) {
                utf8Length += (0x7f - value) >>> 31;
            } else {
                utf8Length += 2;
                if (Character.MIN_SURROGATE <= value && value <= Character.MAX_SURROGATE) {
                    int codePoint = Character.codePointAt(sequence, index);
                    if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                        throw new UnpairedSurrogateException(index, utf16Length);
                    }
                    index++;
                }
            }
        }
        return utf8Length;
    }

    @Override
    public int encodedLength(CharSequence input) {
        return computeEncodedLength(input);
    }

    @Override
    public String decodeUtf8(ByteBuffer buffer, int offset, int length) {
        if ((offset | length | buffer.limit() - offset - length) < 0) {
            throw new ArrayIndexOutOfBoundsException(
                String.format("buffer limit=%d, index=%d, limit=%d", buffer.limit(), offset, length));
        }
        char[] result = new char[length];
        int resultPosition = 0;
        int limit = offset + length;
        while (offset < limit) {
            byte value = buffer.get(offset);
            if (!DecodeUtil.isOneByte(value)) {
                break;
            }
            offset++;
            DecodeUtil.handleOneByte(value, result, resultPosition++);
        }
        while (offset < limit) {
            byte byte1 = buffer.get(offset++);
            if (DecodeUtil.isOneByte(byte1)) {
                DecodeUtil.handleOneByte(byte1, result, resultPosition++);
                while (offset < limit) {
                    byte value = buffer.get(offset);
                    if (!DecodeUtil.isOneByte(value)) {
                        break;
                    }
                    offset++;
                    DecodeUtil.handleOneByte(value, result, resultPosition++);
                }
            } else if (DecodeUtil.isTwoBytes(byte1)) {
                if (offset >= limit) {
                    throw new IllegalArgumentException("Invalid UTF-8");
                }
                DecodeUtil.handleTwoBytes(byte1, buffer.get(offset++), result, resultPosition++);
            } else if (DecodeUtil.isThreeBytes(byte1)) {
                if (offset >= limit - 1) {
                    throw new IllegalArgumentException("Invalid UTF-8");
                }
                DecodeUtil.handleThreeBytes(byte1, buffer.get(offset++), buffer.get(offset++), result,
                    resultPosition++);
            } else {
                if (offset >= limit - 2) {
                    throw new IllegalArgumentException("Invalid UTF-8");
                }
                DecodeUtil.handleFourBytes(byte1, buffer.get(offset++), buffer.get(offset++), buffer.get(offset++),
                    result, resultPosition++);
                resultPosition++;
            }
        }
        return new String(result, 0, resultPosition);
    }

    @Override
    public void encodeUtf8(CharSequence input, ByteBuffer output) {
        int inputLength = input.length();
        for (int index = 0; index < inputLength; index++) {
            char value = input.charAt(index);
            if (value < 0x80) {
                output.put((byte) value);
            } else if (value < 0x800) {
                output.put((byte) (0xC0 | (value >>> 6)));
                output.put((byte) (0x80 | (0x3F & value)));
            } else if (value < Character.MIN_SURROGATE || value > Character.MAX_SURROGATE) {
                output.put((byte) (0xE0 | (value >>> 12)));
                output.put((byte) (0x80 | (0x3F & (value >>> 6))));
                output.put((byte) (0x80 | (0x3F & value)));
            } else {
                if (index + 1 == inputLength || !Character.isSurrogatePair(value, input.charAt(index + 1))) {
                    throw new UnpairedSurrogateException(index, inputLength);
                }
                int codePoint = Character.toCodePoint(value, input.charAt(++index));
                output.put((byte) (0xF0 | (codePoint >>> 18)));
                output.put((byte) (0x80 | (0x3F & (codePoint >>> 12))));
                output.put((byte) (0x80 | (0x3F & (codePoint >>> 6))));
                output.put((byte) (0x80 | (0x3F & codePoint)));
            }
        }
    }
}
