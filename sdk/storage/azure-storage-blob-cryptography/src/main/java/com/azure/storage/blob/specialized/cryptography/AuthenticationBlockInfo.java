// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthenticationBlockInfo {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The cipher text length.
     */
    @JsonProperty(value = "CipherTextLength")
    private int encryptionRegionLength;

    /**
     * The nonce length.
     */
    @JsonProperty(value = "NonceLength")
    private int nonceLength;

    /**
     * Creates a new AuthenticationBlockInfo.
     *
     * @param ciphertextLength The length of the cipher text.
     * @param nonceLength The length of the nonce.
     */
    AuthenticationBlockInfo(int ciphertextLength, int nonceLength) {
        this.encryptionRegionLength = ciphertextLength;
        this.nonceLength = nonceLength;
    }

    /**
     * Gets the ciphertextLength property.
     *
     * @return The ciphertextLength property.
     */
    public int getEncryptionRegionLength() {
        return encryptionRegionLength;
    }

    /**
     * Gets the nonceLength property.
     *
     * @return The nonceLength property.
     */
    public int getNonceLength() {
        return nonceLength;
    }
}
