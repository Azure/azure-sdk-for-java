// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

final class AuthenticationRegionInfo {

    /**
     * The cipher text length.
     */
    @JsonProperty(value = "EncryptionRegionLength")
    private String encryptionRegionLength;

    /**
     * The nonce length.
     */
    @JsonProperty(value = "NonceLength")
    private String nonceLength;

    AuthenticationRegionInfo() {
    }

    /**
     * Creates a new AuthenticationBlockInfo.
     *
     * @param ciphertextLength The length of the cipher text.
     * @param nonceLength The length of the nonce.
     */
    AuthenticationRegionInfo(String ciphertextLength, String nonceLength) {
        this.encryptionRegionLength = ciphertextLength;
        this.nonceLength = nonceLength;
    }

    /**
     * Gets the ciphertextLength property.
     *
     * @return The ciphertextLength property.
     */
    public String getEncryptionRegionLength() {
        return encryptionRegionLength;
    }

    /**
     * Gets the nonceLength property.
     *
     * @return The nonceLength property.
     */
    public String getNonceLength() {
        return nonceLength;
    }
}
