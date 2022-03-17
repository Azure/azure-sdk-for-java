// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation.keyprovider;

import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.EncryptionKeyStoreProvider;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.KeyEncryptionKeyAlgorithm;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;

public class EncryptionKeyStoreProviderImpl extends EncryptionKeyStoreProvider {
    private final KeyEncryptionKeyResolver keyEncryptionKeyResolver;
    private final String keyEncryptionKeyProviderName;
    public static final String RSA_OAEP = "RSA-OAEP";

    public EncryptionKeyStoreProviderImpl(KeyEncryptionKeyResolver keyEncryptionKeyResolver, String keyEncryptionKeyProviderName) {
        this.keyEncryptionKeyResolver = keyEncryptionKeyResolver;
        this.keyEncryptionKeyProviderName = keyEncryptionKeyProviderName;
    }

    /**
     * Getter for provider name.
     *
     * @return provider name
     */
    @Override
    public String getProviderName() {
        return this.keyEncryptionKeyProviderName;
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
        return this.keyEncryptionKeyResolver.buildKeyEncryptionKey(encryptionKeyId).unwrapKey(getNameForKeyEncryptionKeyAlgorithm(algorithm), encryptedKey);
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
        return this.keyEncryptionKeyResolver.buildKeyEncryptionKey(encryptionKeyId).wrapKey(getNameForKeyEncryptionKeyAlgorithm(algorithm), key);
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

    private static String getNameForKeyEncryptionKeyAlgorithm(KeyEncryptionKeyAlgorithm keyEncryptionKeyAlgorithm) {
        if(keyEncryptionKeyAlgorithm == KeyEncryptionKeyAlgorithm.RSA_OAEP) {
            return RSA_OAEP;
        }

        throw new IllegalArgumentException(String.format("Unexpected algorithm '%s'", keyEncryptionKeyAlgorithm));
    }
}
