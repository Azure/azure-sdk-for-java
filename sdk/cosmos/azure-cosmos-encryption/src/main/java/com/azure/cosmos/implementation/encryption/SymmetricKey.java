// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

/**
 * Base class containing raw key bytes for symmetric key algorithms. Some encryption algorithms can use the key directly while others derive sub keys from this.
 * If an algorithm needs to derive more keys, have a derived class from this and use it in the corresponding encryption algorithm.
 */
class SymmetricKey {

    /**
     * The underlying key material
     */
    protected final byte[] rootKey;

    /**
     * Constructor that initializes the root key.
     *
     * @param rootKey root key
     */
    SymmetricKey(byte[] rootKey) {
        // Key validation
        if (rootKey == null || rootKey.length == 0) {
            throw new IllegalArgumentException("rootKey");
        }

        this.rootKey = rootKey;
    }

    /**
     * Gets a copy of the plain text key
     * This is needed for actual encryption/decryption.
     *
     * @return root key byte array
     */
    protected byte[] getRootKey() {
        return this.rootKey;
    }

    /**
     * Computes SHA256 value of the plain text key bytes
     *
     * @return A string containing SHA256 hash of the root key
     */
    protected String getKeyHash() {
        return SecurityUtility.getSHA256Hash(this.getRootKey());
    }

    /**
     * Gets the length of the root key
     *
     * @return Returns the length of the root key
     */
    int getLength() {
        return this.rootKey.length;
    }
}
