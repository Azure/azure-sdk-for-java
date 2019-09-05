// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CustomerProvidedKey {

    /**
     * Base64 encoded string of the encryption key.
     */
    private final String key;

    /**
     * Base64 encoded string of the encryption key's SHA256 hash.
     */
    private final String keySHA256;

    /**
     * The algorithm for Azure Blob Storage to encrypt with.
     * Azure Blob Storage only offers AES256 encryption.
     */
    private final String encryptionAlgorithm = "AES256";


    /**
     * Creates a new wrapper for a client provided key.
     *
     * @param key The encryption key encoded as a base64 string.
     * @throws NoSuchAlgorithmException Throws if MessageDigest "SHA-256" cannot be found.
     */
    public CustomerProvidedKey(String key) throws NoSuchAlgorithmException {

        this.key = key;

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyhash = sha256.digest(Base64.getDecoder().decode(key));
        this.keySHA256 = Base64.getEncoder().encodeToString(keyhash);
    }

    /**
     * Creates a new wrapper for a client provided key.
     *
     * @param key The encryption key bytes.
     *
     * @throws NoSuchAlgorithmException Throws if MessageDigest "SHA-256" cannot be found.
     */
    public CustomerProvidedKey(byte[] key) throws NoSuchAlgorithmException {

        this.key = Base64.getEncoder().encodeToString(key);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyhash = sha256.digest(key);
        this.keySHA256 = Base64.getEncoder().encodeToString(keyhash);
    }


    /**
     * Gets the encryption key.
     *
     * @return A base64 encoded string of the encryption key.
     */
    public String key() {
        return key;
    }

    /**
     * Gets the encryption key's hash.
     *
     * @return A base64 encoded string of the encryption key hash.
     */
    public String keySHA256() {
        return keySHA256;
    }

    /**
     * Gets the algorithm to use this key with.
     *
     * @return A label for the encryption algorithm, as understood by Azure Storage.
     */
    public String encryptionAlgorithm() {
        return encryptionAlgorithm;
    }
}
