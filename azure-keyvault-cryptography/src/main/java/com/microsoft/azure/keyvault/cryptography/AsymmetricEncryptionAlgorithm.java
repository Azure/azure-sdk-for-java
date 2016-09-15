/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.NoSuchPaddingException;

/**
 * Abstract base class for all asymmetric encryption algorithms.
 *
 */
public abstract class AsymmetricEncryptionAlgorithm extends EncryptionAlgorithm {

    /**
     * Constructor.
     * 
     * @param name The name of the algorithm.
     */
    protected AsymmetricEncryptionAlgorithm(String name) {
        super(name);
    }

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption that
     * uses the specified {@link java.security.KeyPair} and the default {@link java.security.Provider} provider.
	 *
     * @param keyPair
     * 			The key pair to use.
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public abstract ICryptoTransform CreateEncryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption that
     * uses the specified {@link java.security.KeyPair} and {@link java.security.Provider}.
	 *
     * @param keyPair
     * 			The key pair to use.
     * @param provider
     * 			The provider to use.
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public abstract ICryptoTransform CreateEncryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption that
     * uses the specified {@link java.security.KeyPair} and the default {@link java.security.Provider} provider.
	 *
     * @param keyPair
     * 			The key pair to use.
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public abstract ICryptoTransform CreateDecryptor(KeyPair keyPair) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption that
     * uses the specified {@link java.security.KeyPair} and {@link java.security.Provider}.
	 *
     * @param keyPair
     * 			The key pair to use.
     * @param provider
     * 			The provider to use.
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public abstract ICryptoTransform CreateDecryptor(KeyPair keyPair, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;
}
