// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Represents the encryption data that is stored on the service.
 */
final class EncryptionData {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The blob encryption mode.
     */
    @JsonProperty(value = "EncryptionMode")
    private String encryptionMode;

    /**
     * A {@link WrappedKey} object that stores the wrapping algorithm, key identifier and the encrypted key
     */
    @JsonProperty(value = "WrappedContentKey", required = true)
    private WrappedKey wrappedContentKey;

    /**
     * The encryption agent.
     */
    @JsonProperty(value = "EncryptionAgent", required = true)
    private EncryptionAgent encryptionAgent;

    /**
     * The content encryption IV.
     */
    @JsonProperty(value = "ContentEncryptionIV", required = true)
    private byte[] contentEncryptionIV;

    /**
     * Metadata for encryption.  Currently used only for storing the encryption library, but may contain other data.
     */
    @JsonProperty(value = "KeyWrappingMetadata", required = true)
    private Map<String, String> keyWrappingMetadata;

    /**
     * Initializes a new instance of the {@link EncryptionData} class.
     */
    EncryptionData() {
    }

    /**
     * Initializes a new instance of the {@link EncryptionData} class using the specified wrappedContentKey,
     * encryptionAgent, contentEncryptionIV, and keyWrappingMetadata.
     *
     * @param encryptionMode The blob encryption mode.
     * @param wrappedContentKey The {@link WrappedKey}.
     * @param encryptionAgent The {@link EncryptionAgent}.
     * @param contentEncryptionIV The content encryption IV.
     * @param keyWrappingMetadata Metadata for encryption.
     */
    EncryptionData(String encryptionMode, WrappedKey wrappedContentKey, EncryptionAgent encryptionAgent,
        byte[] contentEncryptionIV, Map<String, String> keyWrappingMetadata) {
        this.encryptionMode = encryptionMode;
        this.wrappedContentKey = wrappedContentKey;
        this.encryptionAgent = encryptionAgent;
        this.contentEncryptionIV = contentEncryptionIV;
        this.keyWrappingMetadata = keyWrappingMetadata;
    }

    /**
     * Gets the encryption mode
     *
     * @return encryption mode
     */
    String getEncryptionMode() {
        return this.encryptionMode;
    }

    /**
     * Gets the wrapped key that is used to store the wrapping algorithm, key identifier and the encrypted key bytes.
     *
     * @return A {@link WrappedKey} object that stores the wrapping algorithm, key identifier and the encrypted
     *         key bytes.
     */
    WrappedKey getWrappedContentKey() {
        return this.wrappedContentKey;
    }

    /**
     * Gets the encryption agent that is used to identify the encryption protocol version and encryption algorithm.
     *
     * @return an {@Link EncryptionAgent}.
     */
    EncryptionAgent getEncryptionAgent() {
        return this.encryptionAgent;
    }

    /**
     * Gets the content encryption IV.
     *
     * @return The content encryption IV.
     */
    byte[] getContentEncryptionIV() {
        return this.contentEncryptionIV;
    }

    /**
     * Gets the metadata for encryption.
     *
     * @return A HashMap containing the encryption metadata in a key-value format.
     */
    Map<String, String> getKeyWrappingMetadata() {
        return this.keyWrappingMetadata;
    }

    /**
     * Sets the encryption mode
     *
     * @param encryptionMode The encryption mode
     *
     * @return this
     */
    EncryptionData setEncryptionMode(String encryptionMode) {
        this.encryptionMode = encryptionMode;
        return this;
    }

    /**
     * Sets the wrapped key that is used to store the wrapping algorithm, key identifier and the encrypted key bytes.
     *
     * @param wrappedContentKey A {@link WrappedKey} object that stores the wrapping algorithm, key identifier and the
     *         encrypted key bytes.
     *
     * @return this
     */
    EncryptionData setWrappedContentKey(WrappedKey wrappedContentKey) {
        this.wrappedContentKey = wrappedContentKey;
        return this;
    }

    /**
     * Sets the encryption agent that is used to identify the encryption protocol version and encryption algorithm.
     *
     * @param encryptionAgent The {@link EncryptionAgent}.
     *
     * @return this
     */
    EncryptionData setEncryptionAgent(EncryptionAgent encryptionAgent) {
        this.encryptionAgent = encryptionAgent;
        return this;
    }

    /**
     * Sets the content encryption IV.
     *
     * @param contentEncryptionIV The content encryption IV.
     *
     * @return this
     */
    EncryptionData setContentEncryptionIV(byte[] contentEncryptionIV) {
        this.contentEncryptionIV = contentEncryptionIV;
        return this;
    }

    /**
     * Sets the metadata for encryption.
     *
     * @param keyWrappingMetadata A HashMap containing the encryption metadata in a key-value format.
     *
     * @return this
     */
    EncryptionData setKeyWrappingMetadata(Map<String, String> keyWrappingMetadata) {
        this.keyWrappingMetadata = keyWrappingMetadata;
        return this;
    }

    String toJsonString() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    static EncryptionData fromJsonString(String jsonString) throws JsonProcessingException {
        return MAPPER.readValue(jsonString, EncryptionData.class);
    }
}
