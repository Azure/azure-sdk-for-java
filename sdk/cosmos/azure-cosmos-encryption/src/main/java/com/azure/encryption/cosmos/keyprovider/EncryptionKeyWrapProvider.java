// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.encryption.cosmos.keyprovider;


public abstract class EncryptionKeyWrapProvider {
    private EncryptionKeyStoreProviderImpl encryptionKeyStoreProviderImpl;
    public EncryptionKeyWrapProvider () {
        this.encryptionKeyStoreProviderImpl = new EncryptionKeyStoreProviderImpl(this);
    }

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
        KeyProviderBridgeHelpers.EncryptionKeyWrapProviderHelper.setPEncryptionKeyWrapProviderAccessor(new KeyProviderBridgeHelpers.EncryptionKeyWrapProviderHelper.EncryptionKeyWrapProviderAccessor() {
            @Override
            public EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
                return encryptionKeyWrapProvider.getEncryptionKeyStoreProviderImpl();
            }
        });
    }
}
