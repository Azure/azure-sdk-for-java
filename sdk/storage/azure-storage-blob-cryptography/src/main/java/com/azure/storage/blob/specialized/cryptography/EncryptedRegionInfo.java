// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;

final class EncryptedRegionInfo {

    /**
     * The cipher text length.
     */
    @JsonProperty(value = "DataLength")
    private int dataLength;

    /**
     * The nonce length.
     */
    @JsonProperty(value = "NonceLength")
    private int nonceLength;

    /**
     * The tag length
     */
    @JsonProperty(value = "TagLength")
    private int tagLength;

    EncryptedRegionInfo() {
    }

    /**
     * Creates a new AuthenticationBlockInfo.
     *
     * @param ciphertextLength The length of the cipher text.
     * @param nonceLength The length of the nonce.
     */
    EncryptedRegionInfo(int ciphertextLength, int nonceLength, int tagLength) {
        this.dataLength = ciphertextLength;
        this.nonceLength = nonceLength;
        this.tagLength = tagLength;
    }

    /**
     * Gets the ciphertextLength property.
     *
     * @return The ciphertextLength property.
     */
    public int getDataLength() {
        return dataLength;
    }

    /**
     * Gets the nonceLength property.
     *
     * @return The nonceLength property.
     */
    public int getNonceLength() {
        return nonceLength;
    }

    /**
     * Gets the tagLength property.
     *
     * @return The tagLength property.
     */
    public int getTagLength() {
        return tagLength;
    }
}
