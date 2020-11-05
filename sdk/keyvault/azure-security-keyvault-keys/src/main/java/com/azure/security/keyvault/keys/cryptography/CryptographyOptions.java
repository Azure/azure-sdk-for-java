// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents optional parameters for cryptographic operations.
 */
@Fluent
public class CryptographyOptions {
    /**
     * Initialization vector for symmetric algorithms.
     */
    @JsonProperty(value = "iv")
    private byte[] initializationVector;

    /**
     * Additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     */
    @JsonProperty(value = "aad")
    private byte[] additionalAuthenticatedData;

    /**
     * The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    @JsonProperty(value = "tag")
    private byte[] tag;

    /**
     * Get the initialization vector to be used in the cryptographic operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getInitializationVector() {
        return initializationVector;
    }

    /**
     * Set the initialization vector to be used in the cryptographic operation using a symmetric algorithm.
     *
     * @param initializationVector The initialization vector to set.
     * @return The updated {@link CryptographyOptions} object.
     */
    public CryptographyOptions setInitializationVector(byte[] initializationVector) {
        this.initializationVector = initializationVector;
        return this;
    }

    /**
     * Get additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        return additionalAuthenticatedData;
    }

    /**
     * Set additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     *
     * @param additionalAuthenticatedData The additional authenticated data.
     * @return The updated {@link CryptographyOptions} object.
     */
    public CryptographyOptions setAdditionalAuthenticatedData(byte[] additionalAuthenticatedData) {
        this.additionalAuthenticatedData = additionalAuthenticatedData;
        return this;
    }

    /**
     * Get the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The tag.
     */
    public byte[] getTag() {
        return tag;
    }

    /**
     * Set the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @param tag The tag to set.
     * @return The updated {@link CryptographyOptions} object.
     */
    public CryptographyOptions setTag(byte[] tag) {
        this.tag = tag;
        return this;
    }
}
