// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Contains the customer provided key information used to encrypt a blob's content on the server.
 */
@Immutable
public class CustomerProvidedKey {
    private final ClientLogger logger = new ClientLogger(CustomerProvidedKey.class);

    /**
     * Base64 encoded string of the encryption key.
     */
    private final String key;

    /**
     * Base64 encoded string of the encryption key's SHA256 hash.
     */
    private final String keySha256;

    /**
     * The algorithm for Azure Blob Storage to encrypt with.
     * Azure Blob Storage only offers AES256 encryption.
     */
    private final EncryptionAlgorithmType encryptionAlgorithm = EncryptionAlgorithmType.AES256;

    /**
     * Creates a new wrapper for a client provided key.
     *
     * @param key The encryption key encoded as a base64 string.
     * @throws RuntimeException If "SHA-256" cannot be found.
     */
    public CustomerProvidedKey(String key) {

        this.key = key;

        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
        byte[] keyhash = sha256.digest(Base64.getDecoder().decode(key));
        this.keySha256 = Base64.getEncoder().encodeToString(keyhash);
    }

    /**
     * Creates a new wrapper for a client provided key.
     *
     * @param key The encryption key bytes.
     *
     * @throws RuntimeException If "SHA-256" cannot be found.
     */
    public CustomerProvidedKey(byte[] key) {

        this.key = Base64.getEncoder().encodeToString(key);

        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
        byte[] keyhash = sha256.digest(key);
        this.keySha256 = Base64.getEncoder().encodeToString(keyhash);
    }

    /**
     * Gets the encryption key.
     *
     * @return A base64 encoded string of the encryption key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the encryption key's hash.
     *
     * @return A base64 encoded string of the encryption key hash.
     */
    public String getKeySha256() {
        return keySha256;
    }

    /**
     * Gets the algorithm to use this key with.
     *
     * @return A label for the encryption algorithm, as understood by Azure Storage.
     */
    public EncryptionAlgorithmType getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
}
