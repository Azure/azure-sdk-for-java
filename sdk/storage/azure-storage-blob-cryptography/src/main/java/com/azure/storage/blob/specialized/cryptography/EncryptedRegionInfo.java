// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;

final class EncryptedRegionInfo {

    /**
     * The cipher text length.
     */
    @JsonProperty(value = "DataLength")
    private String encryptionRegionLength;

    /**
     * The nonce length.
     */
    @JsonProperty(value = "NonceLength")
    private String nonceLength;

    /**
     * The tag length
     */
    @JsonProperty(value = "TagLength")
    private String tagLength;

    EncryptedRegionInfo() {
    }

    /**
     * Creates a new AuthenticationBlockInfo.
     *
     * @param ciphertextLength The length of the cipher text.
     * @param nonceLength The length of the nonce.
     */
    EncryptedRegionInfo(String ciphertextLength, String nonceLength, String tagLength) {
        this.encryptionRegionLength = ciphertextLength;
        this.nonceLength = nonceLength;
        this.tagLength = tagLength;
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

    /**
     * Gets the tagLength property.
     *
     * @return The tagLength property.
     */
    public String getTagLength() {
        return tagLength;
    }
}
