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
final class EncryptionDataV2 implements EncryptionData{
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
    @JsonProperty(value = "AuthenticationRegionInfo")
    private AuthenticationRegionInfo authenticationRegionInfo;

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
     * @param encryptionAgent The {@link EncryptionAgent}.
     * @param encryptionMode The blob encryption mode.
     * @param authenticationRegionInfo The {@link AuthenticationRegionInfo}.
     * @param keyWrappingMetadata Metadata for encryption.`
     * @param wrappedContentKey The {@link WrappedKey}.
     */
    EncryptionDataV2(EncryptionAgent encryptionAgent, String encryptionMode,
        AuthenticationRegionInfo authenticationRegionInfo, Map<String, String> keyWrappingMetadata,
        WrappedKey wrappedContentKey) {
        this.encryptionAgent = encryptionAgent;
        this.encryptionMode = encryptionMode;
        this.authenticationRegionInfo = authenticationRegionInfo;
        this.keyWrappingMetadata = keyWrappingMetadata;
        this.wrappedContentKey = wrappedContentKey;
    }

    /**
     * Gets the encryptionAgent property.
     *
     * @return The encryptionAgent property.
     */
    @Override
    public EncryptionAgent getEncryptionAgent() {
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
    @Override
    public String getEncryptionMode() {
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
    AuthenticationRegionInfo getAuthenticationRegionInfo() {
        return authenticationRegionInfo;
    }

    /**
     * Sets the authenticationBlockInfo property.
     *
     * @param authenticationRegionInfo The authenticationBlockInfo value to set.
     * @return The updated object
     */
    EncryptionDataV2 setAuthenticationBlockInfo(AuthenticationRegionInfo authenticationRegionInfo) {
        this.authenticationRegionInfo = authenticationRegionInfo;
        return this;
    }

    /**
     * Gets the keyWrappingMetadata property.
     *
     * @return The keyWrappingMetadata property.
     */
    @Override
    public Map<String, String> getKeyWrappingMetadata() {
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
    @Override
    public WrappedKey getWrappedContentKey() {
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

    @Override
    public String toJsonString() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }
}
