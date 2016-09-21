/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.NoSuchPaddingException;

/**
 * Abstract base class for all key wrap algorithms.
 *
 */
public abstract class KeyWrapAlgorithm extends Algorithm {

    /**
     * Constructor.
     * 
     * @param name The name of the algorithm.
     */
    protected KeyWrapAlgorithm(String name) {
        super(name);
    }

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption.
     * Uses the default AES-KW initialization vector.
     * @param key
     * 			The AES key material to be used.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateEncryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption that
     * uses the specified provider for the Java Security API. Uses the default AES-KW initialization vector.
	 *
     * @param key
     * 			The AES key material to be used.
     * @param provider
     * 			The provider to use.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateEncryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption
     * using the supplied initialization vector.
     * @param key
     * 			The AES key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateEncryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     * 			The AES key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @param provider
     * 			The provider to use.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption.
     * Uses the default AES-KW initialization vector.
     * @param key
     * 			The AES key material to be used.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateDecryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption that
     * uses the specified provider for the Java Security API. Uses the default AES-KW initialization vector.
	 *
     * @param key
     * 			The AES key material to be used.
     * @param provider
     * 			The provider to use.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateDecryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption
     * using the supplied initialization vector.
     * @param key
     * 			The AES key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateDecryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     * 			The AES key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @param provider
     * 			The provider to use.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException;
}
