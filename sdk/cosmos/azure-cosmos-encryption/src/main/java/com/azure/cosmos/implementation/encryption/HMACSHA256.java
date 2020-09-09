// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class HMACSHA256 implements Closeable {
    private static final String ALGO_NAME = "HMACSHA256";
    private final Mac mac;
    private byte[] hashValue;

    public HMACSHA256(byte[] key) {
        mac = getMac(ALGO_NAME);
        SecretKey secretKey = new SecretKeySpec(key, ALGO_NAME);
        try {
            mac.init(secretKey);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Mac getMac(String algo) {
        try {
            return Mac.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        // No op
    }

    public byte[] computeHash(byte[] plainText) {
        return mac.doFinal(plainText);
    }

    /**
     * Computes the hash value for the specified region of the input byte array and copies the specified region of the input byte array to the specified region of the output byte array.
     *
     * @param inputBuffer
     * @param inputOffset
     * @param inputCount
     * @param outputBuffer
     * @param outputOffset
     * @return
     */
    public int transformBlock(byte[] inputBuffer, int inputOffset, int inputCount, byte[] outputBuffer, int outputOffset) {
        mac.update(inputBuffer, inputOffset, inputCount);
        if ((outputBuffer != null) && ((inputBuffer != outputBuffer) || (inputOffset != outputOffset))) {
            // We let BlockCopy do the destination array validation
            System.arraycopy(inputBuffer, inputOffset, outputBuffer, outputOffset, inputCount);
        }
        return inputCount;
    }

    /**
     * Computes the hash value for the specified region of the specified byte array.
     *
     * @param inputBuffer
     * @param inputOffset
     * @param inputCount
     * @return
     */
    public byte[] transformFinalBlock(byte[] inputBuffer, int inputOffset, int inputCount) {
        mac.update(inputBuffer, inputOffset, inputCount);
        hashValue = mac.doFinal();
        return hashValue;
    }

    public byte[] getHash() {
        return hashValue;
    }
}
