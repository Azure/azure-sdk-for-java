// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.keyprovider;

import com.azure.comsos.encryption.implementation.keyprovider.EncryptionKeyStoreProviderImpl;
import com.azure.cosmos.encryption.implementation.EncryptionImplementationBridgeHelpers;

/**
 * Base class for all key store providers. A custom provider must derive from this
 * class and override its member functions.
 */
public abstract class EncryptionKeyWrapProvider {
    private EncryptionKeyStoreProviderImpl encryptionKeyStoreProviderImpl;


    /**
     * Constructs an EncryptionKeyWrapProvider which is the base class for all the key store providers.
     * A custom provider must derive from this class and override its member functions.
     */
    public EncryptionKeyWrapProvider () {
        this.encryptionKeyStoreProviderImpl = new EncryptionKeyStoreProviderImpl(this);
    }

    /**
     * Gets the unique name that identifies a particular implementation of the abstract {@link EncryptionKeyWrapProvider}.
     *
     * @return providerName
     */
    public abstract String getProviderName();

    /**
     * Unwraps the specified encryptedKey of a data encryption key. The encrypted value is expected to be encrypted
     * using the key encryption key with the specified encryptionKeyId and using the specified algorithm.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param keyEncryptionKeyAlgorithm
     *        The encryption algorithm.
     * @param encryptedKey
     *        The ciphertext key.
     * @return The unwrapped data encryption key.
     */
    public abstract byte[] unwrapKey(String encryptionKeyId, String keyEncryptionKeyAlgorithm, byte[] encryptedKey);

    /**
     * Wraps a data encryption key using the key encryption key with the specified encryptionKeyId and using the
     * specified algorithm.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param keyEncryptionKeyAlgorithm
     *        The encryption algorithm.
     * @param key
     *        The plaintext key
     * @return The wrapped data encryption key.
     */
    public abstract byte[] wrapKey(String encryptionKeyId, String keyEncryptionKeyAlgorithm, byte[] key);

    private EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl() {
        return encryptionKeyStoreProviderImpl;
    }

    static {
        EncryptionImplementationBridgeHelpers.EncryptionKeyWrapProviderHelper.setEncryptionKeyWrapProviderAccessor(new EncryptionImplementationBridgeHelpers.EncryptionKeyWrapProviderHelper.EncryptionKeyWrapProviderAccessor() {
            @Override
            public EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
                return encryptionKeyWrapProvider.getEncryptionKeyStoreProviderImpl();
            }
        });
    }
}
