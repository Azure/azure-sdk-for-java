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
    private final byte[] initializationVector;

    /**
     * Additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     */
    @JsonProperty(value = "aad")
    private final byte[] additionalAuthenticatedData;

    /**
     * The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    @JsonProperty(value = "tag")
    private final byte[] tag;

    /**
     * Creates an instance of {@link CryptographyOptions} with the given parameters.
     *
     * @param initializationVector Initialization vector for symmetric algorithms.
     * @param additionalAuthenticatedData Additional data to authenticate but not encrypt/decrypt when using
     * authenticated crypto algorithms.
     * @param tag The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    public CryptographyOptions(byte[] initializationVector, byte[] additionalAuthenticatedData, byte[] tag) {
        this.initializationVector = new byte[initializationVector.length];
        this.additionalAuthenticatedData = new byte[additionalAuthenticatedData.length];
        this.tag = new byte[tag.length];

        System.arraycopy(initializationVector, 0, this.initializationVector, 0, initializationVector.length);
        System.arraycopy(additionalAuthenticatedData, 0, this.additionalAuthenticatedData, 0,
            additionalAuthenticatedData.length);
        System.arraycopy(tag, 0, this.tag, 0, tag.length);
    }

    /**
     * Get the initialization vector to be used in the cryptographic operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getInitializationVector() {
        return initializationVector.clone();
    }

    /**
     * Get additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        return additionalAuthenticatedData.clone();
    }

    /**
     * Get the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The tag.
     */
    public byte[] getTag() {
        return tag.clone();
    }
}
