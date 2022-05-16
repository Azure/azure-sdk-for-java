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
public class EncryptionDataV2 {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The encryption agent.
     */
    @JsonProperty(value = "EncryptionAgent", required = true)
    private EncryptionAgent encryptionAgent;

    /**
     * The blob encryption mode.
     */
    @JsonProperty(value = "EncryptionMode")
    private String encryptionMode;

    /**
     * The authentication block info.
     */
    @JsonProperty(value = "AuthenticationBlockInfo")
    private AuthenticationBlockInfo authenticationBlockInfo;

    /**
     * Metadata for encryption.  Currently used only for storing the encryption library, but may contain other data.
     */
    @JsonProperty(value = "KeyWrappingMetadata", required = true)
    private Map<String, String> keyWrappingMetadata;

    /**
     * A {@link WrappedKey} object that stores the wrapping algorithm, key identifier and the encrypted key
     */
    @JsonProperty(value = "WrappedContentKey", required = true)
    private WrappedKey wrappedContentKey;

    /**
     * Initializes a new instance of {@code EncryptionDataV2}.
     */
    EncryptionDataV2() {
    }

    /**
     * Initializes a new instance of the {@link EncryptionDataV1} class using the specified wrappedContentKey,
     * encryptionAgent, contentEncryptionIV, and keyWrappingMetadata.
     *
     * @param agent The {@link EncryptionAgent}.
     * @param mode The blob encryption mode.
     * @param blockInfo The {@link AuthenticationBlockInfo}.
     * @param keyWrappingMetadata Metadata for encryption.`
     * @param wrappedKey The {@link WrappedKey}.
     */
    EncryptionDataV2(EncryptionAgent agent, String mode, AuthenticationBlockInfo blockInfo,
        Map<String, String> keyWrappingMetadata, WrappedKey wrappedKey) {
        this.encryptionAgent = agent;
        this.encryptionMode = mode;
        this.authenticationBlockInfo = blockInfo;
        this.keyWrappingMetadata = keyWrappingMetadata;
        this.wrappedContentKey = wrappedKey;
    }

    /**
     * Gets the encryptionAgent property.
     *
     * @return The encryptionAgent property.
     */
    EncryptionAgent getEncryptionAgent() {
        return encryptionAgent;
    }

    /**
     * Sets the encryptionAgent property.
     *
     * @param encryptionAgent The encryptionAgent value to set.
     * @return The updated object
     */
    EncryptionDataV2 setEncryptionAgent(EncryptionAgent encryptionAgent) {
        this.encryptionAgent = encryptionAgent;
        return this;
    }

    /**
     * Gets the encryptionMode property.
     *
     * @return The encryptionMode property.
     */
    String getEncryptionMode() {
        return encryptionMode;
    }

    /**
     * Sets the encryptionMode property.
     *
     * @param encryptionMode The encryptionMode value to set.
     * @return The updated object
     */
    EncryptionDataV2 setEncryptionMode(String encryptionMode) {
        this.encryptionMode = encryptionMode;
        return this;
    }

    /**
     * Gets the authenticationBlockInfo property.
     *
     * @return The authenticationBlockInfo property.
     */
    AuthenticationBlockInfo getAuthenticationBlockInfo() {
        return authenticationBlockInfo;
    }

    /**
     * Sets the authenticationBlockInfo property.
     *
     * @param authenticationBlockInfo The authenticationBlockInfo value to set.
     * @return The updated object
     */
    EncryptionDataV2 setAuthenticationBlockInfo(AuthenticationBlockInfo authenticationBlockInfo) {
        this.authenticationBlockInfo = authenticationBlockInfo;
        return this;
    }

    /**
     * Gets the keyWrappingMetadata property.
     *
     * @return The keyWrappingMetadata property.
     */
    Map<String, String> getKeyWrappingMetadata() {
        return keyWrappingMetadata;
    }

    /**
     * Sets the keyWrappingMetadata property.
     *
     * @param keyWrappingMetadata The keyWrappingMetadata value to set.
     * @return The updated object
     */
    EncryptionDataV2 setKeyWrappingMetadata(Map<String, String> keyWrappingMetadata) {
        this.keyWrappingMetadata = keyWrappingMetadata;
        return this;
    }

    /**
     * Gets the wrappedContentKey property.
     *
     * @return The wrappedContentKey property.
     */
    WrappedKey getWrappedContentKey() {
        return wrappedContentKey;
    }

    /**
     * Sets the wrappedContentKey property.
     *
     * @param wrappedContentKey The wrappedContentKey value to set.
     * @return The updated object
     */
    EncryptionDataV2 setWrappedContentKey(WrappedKey wrappedContentKey) {
        this.wrappedContentKey = wrappedContentKey;
        return this;
    }

    String toJsonString() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    static EncryptionDataV1 fromJsonString(String jsonString) throws JsonProcessingException {
        return MAPPER.readValue(jsonString, EncryptionDataV1.class);
    }
}
