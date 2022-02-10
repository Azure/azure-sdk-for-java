// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.encryption.cosmos.keyprovider;


import com.azure.encryption.cosmos.implementation.mdesrc.cryptography.EncryptionKeyStoreProvider;
import com.azure.encryption.cosmos.implementation.mdesrc.cryptography.KeyEncryptionKeyAlgorithm;
import com.azure.encryption.cosmos.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;

class EncryptionKeyStoreProviderImpl extends EncryptionKeyStoreProvider {
    private final EncryptionKeyWrapProvider encryptionKeyWrapProvider;
    public EncryptionKeyStoreProviderImpl(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
        this.encryptionKeyWrapProvider = encryptionKeyWrapProvider;
    }

    /**
     * Getter for provider name.
     *
     * @return provider name
     */
    @Override
    public String getProviderName() {
        return this.encryptionKeyWrapProvider.getProviderName();
    }

    /**
     * Unwraps the specified encryptedKey of a data encryption key. The encrypted value is expected to be encrypted
     * using the key encryption key with the specified encryptionKeyId and using the specified algorithm.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param algorithm
     *        The encryption algorithm.
     * @param encryptedKey
     *        The ciphertext key.
     * @return The unwrapped data encryption key.
     */
    @Override
    public byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] encryptedKey) {
        return this.encryptionKeyWrapProvider.unwrapKey(encryptionKeyId, algorithm.toString(), encryptedKey);
    }

    /**
     * Wraps a data encryption key using the key encryption key with the specified encryptionKeyId and using the
     * specified algorithm.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param algorithm
     *        The encryption algorithm.
     * @param key
     *        The plaintext key
     * @return The wrapped data encryption key.
     */
    @Override
    public byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] key) {
        return this.encryptionKeyWrapProvider.wrapKey(encryptionKeyId, algorithm.toString(), key);
    }

    /**
     *  The public facing Cosmos Encryption library interface does not expose this method, hence not supported.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param allowEnclaveComputations
     *        Indicates whether the key encryption key supports enclave computations.
     * @return The signature of the key encryption key metadata.
     */
    @Override
    public byte[] sign(String encryptionKeyId, boolean allowEnclaveComputations) {
        return new byte[0]; // "The Sign operation is not supported. " ;
    }

    /**
     *  The public facing Cosmos Encryption library interface does not expose this method, hence not supported.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param allowEnclaveComputations
     *        Indicates whether the key encryption key supports enclave computations.
     * @param signature
     *        The signature of the key encryption key metadata.
     * @return true if matching, false if not.
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    @Override
    public boolean verify(String encryptionKeyId, boolean allowEnclaveComputations, byte[] signature) throws MicrosoftDataEncryptionException {
        throw new MicrosoftDataEncryptionException("The Verify operation is not supported. ");
    }
}
