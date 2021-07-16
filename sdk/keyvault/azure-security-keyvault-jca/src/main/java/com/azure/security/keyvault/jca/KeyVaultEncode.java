// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.math.BigInteger;

/**
 * encode signature
 */
public class KeyVaultEncode {

    private static final byte TAG_SEQUENCE = 0x30;

    private static final byte TAG_INTEGER = 0x02;

    /**
     * Decode signatures imitating ECUtil
     * @param signature signature get by keyvault
     * @return decoded signatures
     */
    public static byte[] encodeByte(byte[] signature) {
        int n = signature.length >> 1;
        byte[] rByteContext = new byte[n];
        System.arraycopy(signature, 0, rByteContext, 0, n);
        BigInteger r = new BigInteger(1, rByteContext);
        rByteContext = r.toByteArray();

        byte[] sByteContext = new byte[n];
        System.arraycopy(signature, n, sByteContext, 0, n);
        BigInteger s = new BigInteger(1, sByteContext);
        sByteContext = s.toByteArray();

        byte[] rTag = setTagWithContextLength(TAG_INTEGER, rByteContext.length);
        byte[] sTag = setTagWithContextLength(TAG_INTEGER, sByteContext.length);

        byte[] rResult = concatByte(rTag, rByteContext);
        byte[] sResult = concatByte(sTag, sByteContext);

        int length = rResult.length + sResult.length;

        byte[] resultTag = setTagWithContextLength(TAG_SEQUENCE, length);
        byte[] result = new byte[length + resultTag.length];

        System.arraycopy(resultTag, 0, result, 0, resultTag.length);
        System.arraycopy(rResult, 0, result, resultTag.length, rResult.length);
        System.arraycopy(sResult, 0, result, rResult.length + resultTag.length, sResult.length);

        return result;
    }

    /**
     * concat two byte[]
     * @param tag result's tag
     * @param context result's tag
     * @return result of concat byte[]
     */
    private static byte[] concatByte(byte[] tag, byte[] context) {
        byte[] rResult = new byte[tag.length + context.length];
        System.arraycopy(tag, 0, rResult, 0, tag.length);
        System.arraycopy(context, 0, rResult, tag.length, context.length);
        return rResult;
    }

    /**
     * get tag
     * @param tag tag type
     * @param len context length
     * @return tag's Context
     */
    private static byte[] setTagWithContextLength(byte tag, int len) {
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
