// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * Abstract base class for all asymmetric encryption implementation.
 */
abstract class AsymmetricEncryptionAlgorithm extends LocalEncryptionAlgorithm {
    /**
     * Constructor.
     *
     * @param name The name of the algorithm.
     */
    protected AsymmetricEncryptionAlgorithm(String name) {
        super(name);
    }

    /**
     * Creates an {@link ICryptoTransform} implementation for encryption that uses the specified {@link KeyPair} and the
     * default {@link Provider} provider.
     *
     * @param keyPair The key pair to use.
     * @return An {@link ICryptoTransform} implementation.
     *
     * @throws InvalidKeyException If the provided key is not valid.
     * @throws NoSuchAlgorithmException If the provided algorithm is not found.
     * @throws NoSuchPaddingException If the padding wrongly set.
     */
    public abstract ICryptoTransform createEncryptor(KeyPair keyPair)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;

    /**
     * Creates an {@link ICryptoTransform} implementation for encryption that uses the specified {@link KeyPair} and
     * {@link Provider}.
     *
     * @param keyPair The key pair to use.
     * @param provider The provider to use.
     * @return An {@link ICryptoTransform} implementation.
     *
     * @throws InvalidKeyException If the provided key is not valid.
     * @throws NoSuchAlgorithmException If the provided algorithm is not found.
     * @throws NoSuchPaddingException If the padding wrongly set.
     */
    public abstract ICryptoTransform createEncryptor(KeyPair keyPair, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;

    /**
     * Creates an {@link ICryptoTransform} implementation for decryption that uses the specified {@link KeyPair} and the
     * default {@link Provider} provider.
     *
     * @param keyPair The key pair to use.
     * @return An {@link ICryptoTransform} implementation.
     *
     * @throws InvalidKeyException If the provided key is not valid.
     * @throws NoSuchAlgorithmException If the provided algorithm is not found.
     * @throws NoSuchPaddingException If the padding wrongly set.
     */
    public abstract ICryptoTransform createDecryptor(KeyPair keyPair)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;

    /**
     * Creates a {@link ICryptoTransform} implementation for decryption that
     * uses the specified {@link KeyPair} and {@link Provider}.
     *
     * @param keyPair The key pair to use.
     * @param provider The provider to use.
     * @return An {@link ICryptoTransform} implementation.
     *
     * @throws InvalidKeyException If the provided key is not valid.
     * @throws NoSuchAlgorithmException If the provided algorithm is not found.
     * @throws NoSuchPaddingException If the padding wrongly set.
     */
    public abstract ICryptoTransform createDecryptor(KeyPair keyPair, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException;
}
