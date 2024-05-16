/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * Defines the abtract class for a Data encryption key store provider. Extend this class to implement a custom key store
 * provider.
 *
 */
public abstract class EncryptionKeyStoreProvider {

    /**
     * A cache of key encryption keys (once they are unwrapped). This is useful for rapidly decrypting multiple data
     * values.
     */
    protected Map<String, byte[]> dataEncryptionKeyCache = new ConcurrentHashMap<String, byte[]>();

    /**
     * A cache for storing the results of signature verification of key encryption key metadata.
     */
    protected Map<Quadruple<String, String, Boolean, String>, Boolean> keyEncryptionKeyMetadataSignatureVerificationCache = new ConcurrentHashMap<Quadruple<String, String, Boolean, String>, Boolean>() {};

    /**
     * The unique name that identifies a particular implementation of the abstract EncryptionKeyStoreProvider.
     */
    protected String providerName;

    /**
     * Getter for provider name.
     *
     * @return provider name
     */
    public abstract String getProviderName();

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
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public abstract byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm,
                                     byte[] encryptedKey) throws MicrosoftDataEncryptionException;

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
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public abstract byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm,
                                   byte[] key) throws MicrosoftDataEncryptionException;

    /**
     * When implemented in a derived class, digitally signs the key encryption key metadata with the key encryption key
     * referenced by the encryptionKeyId parameter. The input values used to generate the signature should be the
     * specified values of the encryptionKeyId and allowEnclaveComputations parameters.
     *
     * @param encryptionKeyId
     *        The key Id tells the provider where to find the key.
     * @param allowEnclaveComputations
     *        Indicates whether the key encryption key supports enclave computations.
     * @return The signature of the key encryption key metadata.
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public abstract byte[] sign(String encryptionKeyId, boolean allowEnclaveComputations) throws MicrosoftDataEncryptionException;

    /**
     * When implemented in a derived class, this method is expected to verify the specified signature is valid for the
     * key encryption key with the specified encryptionKeyId and the specified enclave behavior.
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
    public abstract boolean verify(String encryptionKeyId, boolean allowEnclaveComputations,
                                   byte[] signature) throws MicrosoftDataEncryptionException;

    /**
     * Adds an encryption key to the cache if it doesn't exist.
     *
     * @param encryptedDataEncryptionKey
     *        encryptionKey
     * @param createItem
     *        value of key
     * @return true if inserted, false if not.
     */
    protected byte[] getOrCreateDataEncryptionKey(String encryptedDataEncryptionKey, byte[] createItem) {
        return dataEncryptionKeyCache.put(encryptedDataEncryptionKey, createItem);
    }

    /**
     * Adds a key signature to the cache.
     *
     * @param keyInformation
     *        key information
     * @param createItem
     *        value
     * @return true if inserted, false if not.
     */
    protected boolean getOrCreateSignatureVerificationResult(Quadruple<String, String, Boolean, String> keyInformation,
            boolean createItem) {
        return keyEncryptionKeyMetadataSignatureVerificationCache.put(keyInformation, createItem);
    }
}
