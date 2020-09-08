// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.AeadAes256CbcHmac256AlgorithmProvider;

import java.lang.reflect.InvocationTargetException;

/**
 * Abstraction for a data encryption key for use in client-side encryption. See https://aka.ms/CosmosClientEncryption
 * for more information on client-side encryption support in Azure Cosmos DB.
 */
public interface DataEncryptionKey {

    /**
     * Gets Raw key bytes of the data encryption key.
     *
     * @return raw key as byte array.
     */
    byte[] getRawKey();

    /**
     * Gets Encryption algorithm to be used with this data encryption key.
     *
     * @return encryption algorithm.
     */
    String getEncryptionAlgorithm();

    /**
     * Encrypts the plainText with a data encryption key.
     *
     * @param plainText Plain text value to be encrypted.
     * @return encrypted data.
     */
    byte[] encryptData(byte[] plainText);

    /**
     * Decrypts the cipherText with a data encryption key.
     *
     * @param cipherText Ciphertext value to be decrypted.
     * @return Plain text.
     */
    byte[] decryptData(byte[] cipherText);

    /**
     * Generates raw data encryption key bytes suitable for use with the provided encryption algorithm.
     *
     * @param encryptionAlgorithm Encryption algorithm the returned key is intended to be used with.
     * @return New instance of data encryption key.
     */
    static byte[] generate(String encryptionAlgorithm) {
        if (!StringUtils.equals(encryptionAlgorithm, CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED)) {
            throw new IllegalArgumentException(String.format("Encryption algorithm not supported: {%s}",
                encryptionAlgorithm));
        }

        byte[] rawKey = new byte[32];

        AeadAes256CbcHmac256AlgorithmProvider.generateRandomBytes(rawKey);
        return rawKey;
    }

    /**
     * Creates a new instance of data encryption key given the raw key bytes suitable for use with the provided
     * encryption algorithm.
     *
     * @param rawKey Raw key bytes.
     * @param encryptionAlgorithm Encryption algorithm the returned key is intended to be used with.
     * @return New instance of data encryption key.
     */
    static DataEncryptionKey create(
        byte[] rawKey,
        String encryptionAlgorithm) {
        if (rawKey == null) {
            throw new NullPointerException("rawKey");
        }

        if (!StringUtils.equals(encryptionAlgorithm, CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED)) {
            throw new IllegalArgumentException(String.format("Encryption algorithm not supported: {%s}",
                encryptionAlgorithm));
        }

        return AeadAes256CbcHmac256AlgorithmProvider.createAlgorithm(rawKey, EncryptionType.RANDOMIZED, /**version*/ (byte) 1);
    }
}
