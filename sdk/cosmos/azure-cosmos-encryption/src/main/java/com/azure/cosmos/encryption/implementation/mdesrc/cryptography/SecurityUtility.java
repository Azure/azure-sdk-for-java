/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * Various security utilities.
 *
 */
class SecurityUtility {

    static final int GONE = 410;
    static final int TOO_MANY_RESQUESTS = 429;
    static final int NOT_FOUND = 404;
    static final int INTERNAL_SERVER_ERROR = 500;
    static final int NETWORK_CONNECT_TIMEOUT_ERROR = 599;

    /**
     * Give the hash of given plain text
     *
     * @param plainText
     * @param key
     * @param length
     * @return hash of the plain text using provided key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    static byte[] getHMACWithSHA256(byte[] plainText, byte[] key,
            int length) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] computedHash;
        byte[] hash = new byte[length];
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec ivkeySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(ivkeySpec);
        computedHash = mac.doFinal(plainText);
        // truncating hash if needed
        System.arraycopy(computedHash, 0, hash, 0, hash.length);
        return hash;
    }

    /**
     * Compare two arrays
     *
     * @param buffer1
     *        first array
     * @param buffer2
     *        second array
     * @param buffer2Index
     * @param lengthToCompare
     * @return true if array contains same bytes otherwise false
     */
    static boolean compareBytes(byte[] buffer1, byte[] buffer2, int buffer2Index, int lengthToCompare) {
        if (null == buffer1 || null == buffer2) {
            return false;
        }

        if ((buffer2.length - buffer2Index) < lengthToCompare) {
            return false;
        }

        for (int index = 0; index < buffer1.length && index < lengthToCompare; ++index) {
            if (buffer1[index] != buffer2[buffer2Index + index]) {
                return false;
            }
        }
        return true;

    }
}
