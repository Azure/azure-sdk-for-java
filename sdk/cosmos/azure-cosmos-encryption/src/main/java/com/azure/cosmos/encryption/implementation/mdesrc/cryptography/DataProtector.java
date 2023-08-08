/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * The base class used for algorithms that do encryption and decryption of serialized object data.
 *
 */
public abstract class DataProtector {
    /**
     * Decrypts ciphertext
     *
     * @param ciphertext
     *        data to be decrypted
     * @return plain text
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public abstract byte[] decrypt(byte[] ciphertext) throws MicrosoftDataEncryptionException;

    /**
     * Encrypts a plaintext array of bytes
     *
     * @param plaintext
     *        text to be encrypted
     * @return cipher text
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public abstract byte[] encrypt(byte[] plaintext) throws MicrosoftDataEncryptionException;
}
