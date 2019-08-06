// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * Abstract base class for all key wrap implementation.
 *
 */
abstract class LocalKeyWrapAlgorithm extends Algorithm {

    /*
     * Constructor.
     *
     * @param name The name of the algorithm.
     */
    LocalKeyWrapAlgorithm(String name) {
        super(name);
    }

    /*
     * Creates a {@link ICryptoTransform} implementation for encryption.
     * Uses the default AES-KW initialization vector.
     * @param key
     *         The AES key material to be used.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createEncryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for encryption that
     * uses the specified provider for the Java Security API. Uses the default AES-KW initialization vector.
     *
     * @param key
     *         The AES key material to be used.
     * @param provider
     *         The provider to use.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createEncryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for encryption
     * using the supplied initialization vector.
     * @param key
     *         The AES key material to be used.
     * @param iv
     *         The initialization vector to be used.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createEncryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for encryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     *         The AES key material to be used.
     * @param iv
     *         The initialization vector to be used.
     * @param provider
     *         The provider to use.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for decryption.
     * Uses the default AES-KW initialization vector.
     * @param key
     *         The AES key material to be used.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createDecryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for decryption that
     * uses the specified provider for the Java Security API. Uses the default AES-KW initialization vector.
     *
     * @param key
     *         The AES key material to be used.
     * @param provider
     *         The provider to use.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createDecryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for decryption
     * using the supplied initialization vector.
     * @param key
     *         The AES key material to be used.
     * @param iv
     *         The initialization vector to be used.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createDecryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for decryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     *         The AES key material to be used.
     * @param iv
     *         The initialization vector to be used.
     * @param provider
     *         The provider to use.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;
}
