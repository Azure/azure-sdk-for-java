// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.mdesupport;

import com.azure.cosmos.implementation.Warning;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.encryption.mdesupport
 **/
@Warning(value = INTERNAL_USE_ONLY_WARNING)
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
     * @throws MicrosoftDataEncryptionException
     *         on error
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
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public abstract byte[] wrapKey(String encryptionKeyId, String keyEncryptionKeyAlgorithm, byte[] key);

    private EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl() {
        return encryptionKeyStoreProviderImpl;
    }

    static {
        MdeSupportBridgeHelpers.EncryptionKeyWrapProviderHelper.setPEncryptionKeyWrapProviderAccessor(new MdeSupportBridgeHelpers.EncryptionKeyWrapProviderHelper.EncryptionKeyWrapProviderAccessor() {
            @Override
            public EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
                return encryptionKeyWrapProvider.getEncryptionKeyStoreProviderImpl();
            }
        });
    }
}
