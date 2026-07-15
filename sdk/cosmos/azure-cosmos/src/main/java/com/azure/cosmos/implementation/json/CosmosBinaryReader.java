// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.json;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Decodes the core Cosmos Binary JSON value types. */
final class CosmosBinaryReader {
    private static final int MAX_NESTING_DEPTH = 128;
    private static final String[] SYSTEM_STRINGS = {
        "$s", "$t", "$v", "_attachments", "_etag", "_rid", "_self", "_ts",
        "attachments/", "coordinates", "geometry", "GeometryCollection", "id", "url",
        "Value", "label", "LineString", "link", "MultiLineString", "MultiPoint",
        "MultiPolygon", "name", "Name", "Type", "Point", "Polygon", "properties",
        "type", "value", "Feature", "FeatureCollection", "_id"
    };

    private final byte[] bytes;
    private int position;

    private CosmosBinaryReader(byte[] bytes) {
        this.bytes = bytes;
    }

    static Object decode(byte[] document) {
        if (document == null) {
            throw new NullPointerException("document");
        }
        CosmosBinaryReader reader = new CosmosBinaryReader(document);
        reader.require(reader.readUnsignedByte() == 0x80, "Not Cosmos Binary format");
        Object value = reader.readValue(0);
        reader.require(reader.position == document.length, "Trailing content");
        return value;
    }

    static byte[] readBinaryProperty(byte[] document, String propertyName) {
        Object decoded = decode(document);
        if (!(decoded instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Cosmos document root is not an object");
        }
        Object value = ((Map<?, ?>) decoded).get(propertyName);
        if (!(value instanceof CosmosBinary)) {
            throw new IllegalArgumentException("Property is not native binary: " + propertyName);
        }
        return ((CosmosBinary) value).toByteArray();
    }

    static String[] systemStrings() {
        return SYSTEM_STRINGS.clone();
    }

    private Object readValue(int depth) {
        require(depth <= MAX_NESTING_DEPTH, "Maximum nesting depth exceeded");
        int marker = readUnsignedByte();
        if (marker < 0x20) {
            return (long) marker;
        }
        if (isStringMarker(marker)) {
            return readString(marker);
        }
        switch (marker) {
            case 0xC7: return readUnsignedLong();
            case 0xC8: return (long) readUnsignedByte();
            case 0xC9: return (long) readSignedInteger(2);
            case 0xCA: return (long) readSignedInteger(4);
            case 0xCB: return readSignedLong();
            case 0xCC: return Double.longBitsToDouble(readLongBits());
            case 0xCD: return (double) Float.intBitsToFloat((int) readUnsignedInteger(4));
            case 0xCE: return Double.longBitsToDouble(readLongBits());
            case 0xCF: return readFloat16();
            case 0xD0: return null;
            case 0xD1: return false;
            case 0xD2: return true;
            case 0xD3: return readGuid();
            case 0xD7: return (long) readUnsignedByte();
            case 0xD8: return (long) (byte) readUnsignedByte();
            case 0xD9: return (long) readSignedInteger(2);
            case 0xDA: return (long) readSignedInteger(4);
            case 0xDB: return readSignedLong();
            case 0xDC: return readUnsignedInteger(4);
            case 0xDD: return readBinary(1);
            case 0xDE: return readBinary(2);
            case 0xDF: return readBinary(4);
            case 0xE0: return new ArrayList<>();
            case 0xE1: return readSingleArray(depth + 1);
            case 0xE2: return readArray(readLength(1), -1, depth + 1);
            case 0xE3: return readArray(readLength(2), -1, depth + 1);
            case 0xE4: return readArray(readLength(4), -1, depth + 1);
            case 0xE5: return readArray(readLength(1), readLength(1), depth + 1);
            case 0xE6: return readArray(readLength(2), readLength(2), depth + 1);
            case 0xE7: return readArray(readLength(4), readLength(4), depth + 1);
            case 0xE8: return new LinkedHashMap<>();
            case 0xE9: return readSingleObject(depth + 1);
            case 0xEA: return readObject(readLength(1), -1, depth + 1);
            case 0xEB: return readObject(readLength(2), -1, depth + 1);
            case 0xEC: return readObject(readLength(4), -1, depth + 1);
            case 0xED: return readObject(readLength(1), readLength(1), depth + 1);
            case 0xEE: return readObject(readLength(2), readLength(2), depth + 1);
            case 0xEF: return readObject(readLength(4), readLength(4), depth + 1);
            case 0xF0: return readUniformNumberArray(1);
            case 0xF1: return readUniformNumberArray(2);
            case 0xF2: return readUniformNumberArrayArray(1);
            case 0xF3: return readUniformNumberArrayArray(2);
            default:
                throw unsupported(marker);
        }
    }

    private List<Object> readSingleArray(int depth) {
        List<Object> values = new ArrayList<>(1);
        values.add(readValue(depth));
        return values;
    }

    private List<Object> readArray(int payloadLength, int expectedCount, int depth) {
        int end = scopeEnd(payloadLength);
        require(expectedCount < 0 || expectedCount <= payloadLength, "Array count exceeds payload");
        List<Object> values = new ArrayList<>(expectedCount < 0 ? 4 : expectedCount);
        while (position < end) {
            values.add(readValue(depth));
        }
        require(position == end, "Array length mismatch");
        require(expectedCount < 0 || values.size() == expectedCount, "Array count mismatch");
        return values;
    }

    private Map<String, Object> readSingleObject(int depth) {
        Map<String, Object> values = new LinkedHashMap<>(1);
        values.put(readFieldName(), readValue(depth));
        return values;
    }

    private Map<String, Object> readObject(int payloadLength, int expectedCount, int depth) {
        int end = scopeEnd(payloadLength);
        require(expectedCount < 0 || expectedCount <= payloadLength / 2, "Object count exceeds payload");
        Map<String, Object> values = new LinkedHashMap<>(expectedCount < 0 ? 4 : expectedCount);
        int actualCount = 0;
        while (position < end) {
            values.put(readFieldName(), readValue(depth));
            actualCount++;
        }
        require(position == end, "Object length mismatch");
        require(expectedCount < 0 || actualCount == expectedCount, "Object count mismatch");
        return values;
    }

    private List<Object> readUniformNumberArray(int countWidth) {
        int itemMarker = readUnsignedByte();
        int count = readLength(countWidth);
        validateUniformNumberCount(itemMarker, count);
        return readUniformNumbers(itemMarker, count);
    }

    private List<Object> readUniformNumberArrayArray(int countWidth) {
        int nestedMarker = readUnsignedByte();
        require(
            nestedMarker == (countWidth == 1 ? 0xF0 : 0xF1),
            "Uniform nested array marker does not match count width");
        int itemMarker = readUnsignedByte();
        int innerCount = readLength(countWidth);
        int outerCount = readLength(countWidth);
        int itemWidth = uniformNumberWidth(itemMarker);
        long totalCount = (long) innerCount * outerCount;
        require(totalCount <= (bytes.length - position) / itemWidth, "Uniform nested array count exceeds input");
        require(totalCount <= Integer.MAX_VALUE, "Uniform nested array is too large");
        List<Object> result = new ArrayList<>(outerCount);
        for (int index = 0; index < outerCount; index++) {
            result.add(readUniformNumbers(itemMarker, innerCount));
        }
        return result;
    }

    private void validateUniformNumberCount(int itemMarker, int count) {
        int itemWidth = uniformNumberWidth(itemMarker);
        require(count <= (bytes.length - position) / itemWidth, "Uniform array count exceeds input");
    }

    private int uniformNumberWidth(int itemMarker) {
        switch (itemMarker) {
            case 0xD7:
            case 0xD8: return 1;
            case 0xD9: return 2;
            case 0xDA:
            case 0xCD: return 4;
            case 0xDB:
            case 0xCE: return 8;
            default: throw unsupported(itemMarker);
        }
    }

    private List<Object> readUniformNumbers(int itemMarker, int count) {
        List<Object> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            switch (itemMarker) {
                case 0xD7: values.add((long) readUnsignedByte()); break;
                case 0xD8: values.add((long) (byte) readUnsignedByte()); break;
                case 0xD9: values.add((long) readSignedInteger(2)); break;
                case 0xDA: values.add((long) readSignedInteger(4)); break;
                case 0xDB: values.add(readSignedLong()); break;
                case 0xCD: values.add((double) Float.intBitsToFloat((int) readUnsignedInteger(4))); break;
                case 0xCE: values.add(Double.longBitsToDouble(readLongBits())); break;
                default: throw unsupported(itemMarker);
            }
        }
        return values;
    }

    private String readFieldName() {
        int marker = readUnsignedByte();
        require(isStringMarker(marker), String.format("Object key marker is 0x%02X", marker));
        return readString(marker);
    }

    private boolean isStringMarker(int marker) {
        return (marker >= 0x20 && marker < 0x68)
                || (marker >= 0x71 && marker <= 0xC6);
    }

    private String readString(int marker) {
        if (marker >= 0x20 && marker < 0x40) {
            return SYSTEM_STRINGS[marker - 0x20];
        }
        if (marker >= 0x40 && marker < 0x68) {
            throw unsupported(marker); // User dictionary strings need external dictionary state.
        }
        if (marker >= 0x71 && marker <= 0x7F) {
            return readEncodedString(marker);
        }
        if (marker >= 0xC3 && marker <= 0xC6) {
            return readReferenceString(marker - 0xC2);
        }
        int length;
        if (marker >= 0x80 && marker < 0xC0) {
            length = marker - 0x80;
        } else if (marker == 0xC0) {
            length = readLength(1);
        } else if (marker == 0xC1) {
            length = readLength(2);
        } else if (marker == 0xC2) {
            length = readLength(4);
        } else {
            throw unsupported(marker);
        }
        byte[] utf8 = take(length);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(utf8))
                .toString();
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("Invalid UTF-8 string at byte " + position, error);
        }
    }

    private String readEncodedString(int marker) {
        switch (marker) {
            case 0x71: return readBase64String(1, false);
            case 0x72: return readBase64String(2, false);
            case 0x73: return readBase64String(1, true);
            case 0x74: return readBase64String(2, true);
            case 0x75: return readGuidString(false, false);
            case 0x76: return readGuidString(true, false);
            case 0x77: return readGuidString(false, true);
            case 0x78: return readFourBitString("0123456789abcdef");
            case 0x79: return readFourBitString("0123456789ABCDEF");
            case 0x7A: return readFourBitString(" 0123456789:-.TZ");
            case 0x7B: return readPackedString(4, true);
            case 0x7C: return readPackedString(5, true);
            case 0x7D: return readPackedString(6, true);
            case 0x7E: return readPackedString(7, false);
            case 0x7F: return readPackedString(7, false, 2);
            default: throw unsupported(marker);
        }
    }

    private String readBase64String(int lengthWidth, boolean urlSafe) {
        int groups = readLength(lengthWidth);
        int paddingMarker = readUnsignedByte();
        int padding = paddingMarker > 2 ? (~paddingMarker) & 0xFF : paddingMarker;
        int valueLength = groups * 4 - (paddingMarker > 2 ? padding : 0);
        int byteCount = (groups * 4 - padding) * 3 / 4;
        byte[] raw = take(byteCount);
        String encoded = java.util.Base64.getEncoder().encodeToString(raw);
        encoded = encoded.substring(0, valueLength);
        return urlSafe ? encoded.replace('+', '-').replace('/', '_') : encoded;
    }

    private String readGuidString(boolean upperCase, boolean quoted) {
        byte[] value = take(16);
        StringBuilder result = new StringBuilder(quoted ? 38 : 36);
        if (quoted) {
            result.append('"');
        }
        for (int index = 0; index < value.length; index++) {
            if (index == 4 || index == 6 || index == 8 || index == 10) {
                result.append('-');
            }
            int packed = value[index] & 0xFF;
            result.append(hexDigit(packed & 0x0F, upperCase));
            result.append(hexDigit(packed >>> 4, upperCase));
        }
        if (quoted) {
            result.append('"');
        }
        return result.toString();
    }

    private String readFourBitString(String alphabet) {
        int characterCount = readLength(1);
        byte[] packed = take(compressedLength(characterCount, 4));
        StringBuilder result = new StringBuilder(characterCount);
        for (int index = 0; index < characterCount; index++) {
            int value = packed[index / 2] & 0xFF;
            int alphabetIndex = index % 2 == 0 ? value & 0x0F : value >>> 4;
            result.append(alphabet.charAt(alphabetIndex));
        }
        return result.toString();
    }

    private String readPackedString(int bits, boolean hasBaseCharacter) {
        return readPackedString(bits, hasBaseCharacter, 1);
    }

    private String readPackedString(int bits, boolean hasBaseCharacter, int lengthWidth) {
        int characterCount = readLength(lengthWidth);
        int baseCharacter = hasBaseCharacter ? readUnsignedByte() : 0;
        byte[] packed = take(compressedLength(characterCount, bits));
        StringBuilder result = new StringBuilder(characterCount);
        long bitBuffer = 0;
        int bufferedBits = 0;
        int sourceIndex = 0;
        int mask = (1 << bits) - 1;
        for (int index = 0; index < characterCount; index++) {
            while (bufferedBits < bits) {
                bitBuffer |= (long) (packed[sourceIndex++] & 0xFF) << bufferedBits;
                bufferedBits += 8;
            }
            result.append((char) ((bitBuffer & mask) + baseCharacter));
            bitBuffer >>>= bits;
            bufferedBits -= bits;
        }
        return result.toString();
    }

    private static int compressedLength(int characterCount, int bits) {
        long length = ((long) characterCount * bits + 7) / 8;
        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Compressed string is too large");
        }
        return (int) length;
    }

    private static char hexDigit(int value, boolean upperCase) {
        return (char) (value < 10 ? '0' + value : (upperCase ? 'A' : 'a') + value - 10);
    }

    private String readReferenceString(int width) {
        int targetOffset = readLength(width);
        require(targetOffset < bytes.length, "Reference string target exceeds input");
        int returnPosition = position;
        position = targetOffset;
        int targetMarker = readUnsignedByte();
        require(isStringMarker(targetMarker), "Reference string target is not a string");
        require(targetMarker < 0xC3 || targetMarker > 0xC6, "Reference string target is another reference");
        String value = readString(targetMarker);
        position = returnPosition;
        return value;
    }

    private CosmosBinary readBinary(int width) {
        return CosmosBinary.fromBytes(take(readLength(width)));
    }

    private int scopeEnd(int payloadLength) {
        require(payloadLength <= bytes.length - position, "Scope exceeds input");
        return position + payloadLength;
    }

    private int readUnsignedByte() {
        require(position < bytes.length, "Unexpected end of input");
        return bytes[position++] & 0xFF;
    }

    private int readLength(int width) {
        long value = readUnsignedInteger(width);
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Value is too large at byte " + position);
        }
        return (int) value;
    }

    private long readUnsignedInteger(int width) {
        require(position + width <= bytes.length, "Unexpected end of integer");
        long value = 0;
        for (int index = 0; index < width; index++) {
            value |= (long) (bytes[position++] & 0xFF) << (index * 8);
        }
        return value;
    }

    private int readSignedInteger(int width) {
        long value = readUnsignedInteger(width);
        int shift = (Long.BYTES - width) * 8;
        return (int) (value << shift >> shift);
    }

    private long readSignedLong() {
        return readLongBits();
    }

    private Number readUnsignedLong() {
        byte[] littleEndian = take(8);
        byte[] bigEndian = new byte[8];
        for (int index = 0; index < 8; index++) {
            bigEndian[index] = littleEndian[7 - index];
        }
        BigInteger value = new BigInteger(1, bigEndian);
        return value.bitLength() < 64 ? value.longValue() : value;
    }

    private double readFloat16() {
        int bits = (int) readUnsignedInteger(2);
        int sign = (bits >>> 15) & 1;
        int exponent = (bits >>> 10) & 0x1F;
        int fraction = bits & 0x3FF;
        double value;
        if (exponent == 0) {
            value = Math.scalb((double) fraction, -24);
        } else if (exponent == 0x1F) {
            value = fraction == 0 ? Double.POSITIVE_INFINITY : Double.NaN;
        } else {
            value = Math.scalb((double) (0x400 + fraction), exponent - 25);
        }
        return sign == 0 ? value : -value;
    }

    private String readGuid() {
        byte[] value = take(16);
        return String.format(
            "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
            value[3] & 0xFF, value[2] & 0xFF, value[1] & 0xFF, value[0] & 0xFF,
            value[5] & 0xFF, value[4] & 0xFF,
            value[7] & 0xFF, value[6] & 0xFF,
            value[8] & 0xFF, value[9] & 0xFF,
            value[10] & 0xFF, value[11] & 0xFF, value[12] & 0xFF,
            value[13] & 0xFF, value[14] & 0xFF, value[15] & 0xFF);
    }

    private long readLongBits() {
        require(position + 8 <= bytes.length, "Unexpected end of 64-bit value");
        long value = 0;
        for (int index = 0; index < 8; index++) {
            value |= (long) (bytes[position++] & 0xFF) << (index * 8);
        }
        return value;
    }

    private byte[] take(int length) {
        require(length <= bytes.length - position, "Unexpected end of value");
        byte[] value = Arrays.copyOfRange(bytes, position, position + length);
        position += length;
        return value;
    }

    private IllegalArgumentException unsupported(int marker) {
        return new IllegalArgumentException(
                String.format("Unsupported Cosmos Binary marker 0x%02X at byte %d", marker, position - 1));
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message + " at byte " + position);
        }
    }
}
