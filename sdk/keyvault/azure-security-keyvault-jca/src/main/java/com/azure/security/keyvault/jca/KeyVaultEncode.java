// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.math.BigInteger;

/**
 * encode signature
 *
 * @author RujunChen
 * @since 4.0
 * @see  <a href="https://docs.microsoft.com/windows/win32/seccertenroll/about-der-encoding-of-asn-1-types">reference doc</a>
 */
public class KeyVaultEncode {

    private static final byte TAG_INTEGER = 0x02;
    private static final byte TAG_SEQUENCE = 0x30;

    /**
     * Decode signatures imitating ECUtil
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
     * Sign the code
     * @param bytes signature obtained
     * @param offset the offset in the byte array
     * @param length the number of bytes to convert
     * @return decoded signatures
     */
    static byte[] toBigIntegerBytesWithLengthPrefix(byte[] bytes, int offset, int length) {
        byte[] magnitude = new byte[length];
        System.arraycopy(bytes, offset, magnitude, 0, length);
        BigInteger bigInteger = new BigInteger(1, magnitude);
        byte[] bigIntegerArray = bigInteger.toByteArray();
        return concatBytes(buildLengthBytes(TAG_INTEGER, bigIntegerArray.length), bigIntegerArray);
    }

    /**
     * Complement integer values
     * @param bytes1 integer value
     * @param bytes2 Complement array
     * @return decoded signatures
     */
    static byte[] concatBytes(byte[] bytes1, byte[] bytes2) {
        byte[] result = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, result, 0, bytes1.length);
        System.arraycopy(bytes2, 0, result, bytes1.length, bytes2.length);
        return result;
    }

    /**
     * Integrate integer values
     * @param bytes1 integer value1
     * @param bytes2 integer value2
     * @param bytes3 integer value3
     * @return decoded signatures
     */
    static byte[] concatBytes(byte[] bytes1, byte[] bytes2, byte[] bytes3) {
        byte[] result = new byte[bytes1.length + bytes2.length + bytes3.length];
        System.arraycopy(bytes1, 0, result, 0, bytes1.length);
        System.arraycopy(bytes2, 0, result, bytes1.length, bytes2.length);
        System.arraycopy(bytes3, 0, result, bytes1.length + bytes2.length, bytes3.length);
        return result;
    }

    /**
     * Get the result integer value
     * @param tag tag value
     * @param len the content length
     * @return integer value
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
