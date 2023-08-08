// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

enum HashAlgorithm {

    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512");

    private String value;

    /**
     * Creates a custom value for EncryptionAlgorithm.
     *
     * @param value the custom value
     */
    HashAlgorithm(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public int hash() {
        return value.hashCode();
    }
}
