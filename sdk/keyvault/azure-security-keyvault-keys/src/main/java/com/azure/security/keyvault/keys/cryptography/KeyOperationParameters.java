// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Base64Url;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key operations parameters.
 */
class KeyOperationParameters {
    /**
     * algorithm identifier. Possible values include: 'RSA-OAEP',
     * 'RSA-OAEP-256', 'RSA1_5'.
     */
    @JsonProperty(value = "alg", required = true)
    private EncryptionAlgorithm algorithm;

    /**
     * The value property.
     */
    @JsonProperty(value = "value", required = true)
    private Base64Url value;

    /**
     * Initialization vector for symmetric algorithms.
     */
    @JsonProperty(value = "iv")
    private byte[] iv;

    /**
     * Additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     */
    @JsonProperty(value = "aad")
    private byte[] additionalAuthenticatedData;

    /**
     * The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    @JsonProperty(value = "tag")
    private byte[] authenticationTag;

    /**
     * Get the algorithm value.
     *
     * @return the algorithm value
     */
    public EncryptionAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Set the algorithm value.
     *
     * @param algorithm the algorithm value to set
     * @return the KeyOperationsParameters object itself.
     */
    public KeyOperationParameters setAlgorithm(EncryptionAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public byte[] getValue() {
        if (this.value == null) {
            return new byte[0];
        }
        return this.value.decodedBytes();
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the KeyOperationsParameters object itself.
     */
    public KeyOperationParameters setValue(byte[] value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = Base64Url.encode(value);
        }
        return this;
    }

    /**
     * Get the initialization vector to be used in the cryptographic operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        return iv;
    }

    /**
     * Set the initialization vector to be used in the cryptographic operation using a symmetric algorithm.
     *
     * @param iv The initialization vector to set.
     * @return The updated {@link KeyOperationParameters} object.
     */
    public KeyOperationParameters setIv(byte[] iv) {
        this.iv = iv;
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
     * @return The updated {@link KeyOperationParameters} object.
     */
    public KeyOperationParameters setAdditionalAuthenticatedData(byte[] additionalAuthenticatedData) {
        this.additionalAuthenticatedData = additionalAuthenticatedData;
        return this;
    }

    /**
     * Get the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The authentication tag.
     */
    public byte[] getAuthenticationTag() {
        return authenticationTag;
    }

    /**
     * Set the tag to authenticate when performing decryption with an authenticated algorithm.
     *
     * @param authenticationTag The tag to set.
     * @return The updated {@link KeyOperationParameters} object.
     */
    public KeyOperationParameters setAuthenticationTag(byte[] authenticationTag) {
        this.authenticationTag = authenticationTag;
        return this;
    }
}
