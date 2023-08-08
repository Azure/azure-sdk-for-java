// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.math.BigInteger;

/**
 * encode signature
 *
 * @see <a href="https://docs.microsoft.com/windows/win32/seccertenroll/about-der-encoding-of-asn-1-types">reference doc</a>
 */
public class KeyVaultEncode {

    private static final byte TAG_INTEGER = 0x02;
    private static final byte TAG_SEQUENCE = 0x30;

    /**
     * Decode signatures imitating ECUtil
     *
     * @param signature signature get by keyvault
     * @return decoded signatures
     */
    public static byte[] encodeByte(byte[] signature) {
        int halfLength = signature.length >> 1;
        byte[] leftResult = toBigIntegerBytesWithLengthPrefix(signature, 0, halfLength);
        byte[] rightResult = toBigIntegerBytesWithLengthPrefix(signature, halfLength, halfLength);
        byte[] resultLengthBytes = buildLengthBytes(TAG_SEQUENCE, leftResult.length + rightResult.length);
        return concatBytes(resultLengthBytes, leftResult, rightResult);
    }

    /**
     * Convert to big integer bytes.
     *
     * @param bytes signature obtained
     * @param offset the offset in the byte array
     * @param length the number of bytes to convert
     * @return the byte array which holds the big integer.
     */
    static byte[] toBigIntegerBytesWithLengthPrefix(byte[] bytes, int offset, int length) {
        byte[] magnitude = new byte[length];
        System.arraycopy(bytes, offset, magnitude, 0, length);
        BigInteger bigInteger = new BigInteger(1, magnitude);
        byte[] bigIntegerArray = bigInteger.toByteArray();
        return concatBytes(buildLengthBytes(TAG_INTEGER, bigIntegerArray.length), bigIntegerArray);
    }

    /**
     * Concatenate 2 byte arrays.
     *
     * @param bytes1 the first byte array
     * @param bytes2 the second byte array
     * @return the concatenated array
     */
    static byte[] concatBytes(byte[] bytes1, byte[] bytes2) {
        byte[] result = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, result, 0, bytes1.length);
        System.arraycopy(bytes2, 0, result, bytes1.length, bytes2.length);
        return result;
    }

    /**
     * Concatenate 3 byte arrays.
     *
     * @param bytes1 the first byte array
     * @param bytes2 the second byte array
     * @param bytes3 the third byte array
     * @return the concatenated array
     */
    static byte[] concatBytes(byte[] bytes1, byte[] bytes2, byte[] bytes3) {
        byte[] result = new byte[bytes1.length + bytes2.length + bytes3.length];
        System.arraycopy(bytes1, 0, result, 0, bytes1.length);
        System.arraycopy(bytes2, 0, result, bytes1.length, bytes2.length);
        System.arraycopy(bytes3, 0, result, bytes1.length + bytes2.length, bytes3.length);
        return result;
    }

    /**
     * Create byte array with specific prefix value and hold the length value.
     *
     * @param tag the tag value
     * @param len the content length
     * @return the integer value
     */
    static byte[] buildLengthBytes(byte tag, int len) {
        if (len < 128) {
            return new byte[] {tag, ((byte) len)};
        } else if (len < (1 << 8)) {
            return new byte[] {tag, (byte) 0x081, (byte) len};
        } else if (len < (1 << 16)) {
            return new byte[] {tag, (byte) 0x082, (byte) (len >> 8), (byte) len};
        } else if (len < (1 << 24)) {
            return new byte[] {tag, (byte) 0x083, (byte) (len >> 16), (byte) (len >> 8), (byte) len};
        } else {
            return new byte[] {tag, (byte) 0x084, (byte) (len >> 24), (byte) (len >> 16), (byte) (len >> 8), (byte) len};
        }
    }

}
