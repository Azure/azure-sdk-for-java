// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.EncryptionAlgorithm.AES_CBC_256;
import static com.azure.storage.blob.specialized.cryptography.EncryptionAlgorithm.AES_GCM_256;

/**
 * Represents the encryption data that is stored on the service.
 */
final class EncryptionData {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptionData.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
     * The authentication block info.
     */
    @JsonProperty(value = "EncryptedRegionInfo")
    private EncryptedRegionInfo encryptedRegionInfo;

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
     * @param encryptedRegionInfo The encrypted region info.
     * @param keyWrappingMetadata Metadata for encryption.
     */
    EncryptionData(String encryptionMode, WrappedKey wrappedContentKey, EncryptionAgent encryptionAgent,
        byte[] contentEncryptionIV, EncryptedRegionInfo encryptedRegionInfo,
        Map<String, String> keyWrappingMetadata) {
        this.encryptionMode = encryptionMode;
        this.wrappedContentKey = wrappedContentKey;
        this.encryptionAgent = encryptionAgent;
        this.contentEncryptionIV = contentEncryptionIV;
        this.encryptedRegionInfo = encryptedRegionInfo;
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
     * @return an {@link EncryptionAgent}.
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
     * Gets the authenticationBlockInfo property.
     *
     * @return The authenticationBlockInfo property.
     */
    EncryptedRegionInfo getEncryptedRegionInfo() {
        return encryptedRegionInfo;
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

    /**
     * Sets the authenticationBlockInfo property.
     *
     * @param encryptedRegionInfo The authenticationBlockInfo value to set.
     * @return The updated object
     */
    EncryptionData setEncryptedRegionInfo(EncryptedRegionInfo encryptedRegionInfo) {
        this.encryptedRegionInfo = encryptedRegionInfo;
        return this;
    }

    String toJsonString() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    /*
    Validates that encryption data is present if the client requires encryption and that appropriate values are present
    for the given protocol version.
     */
    static EncryptionData getAndValidateEncryptionData(String encryptionDataString, boolean requiresEncryption) {
        if (encryptionDataString == null) {
            if (requiresEncryption) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("'requiresEncryption' set to true but "
                    + "downloaded data is not encrypted."));
            }
            return null;
        }

        try {
            EncryptionData encryptionData = MAPPER.readValue(encryptionDataString, EncryptionData.class);
            if (encryptionData.getEncryptionAgent().getProtocol().equals(ENCRYPTION_PROTOCOL_V1)) {
                Objects.requireNonNull(encryptionData.getContentEncryptionIV(),
                    "contentEncryptionIV in encryptionData cannot be null");
                Objects.requireNonNull(encryptionData.getWrappedContentKey().getEncryptedKey(), "encryptedKey in "
                    + "encryptionData.wrappedContentKey cannot be null");
                if (!encryptionData.getEncryptionAgent().getAlgorithm().equals(AES_CBC_256)) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Encryption algorithm does not match v1 protocol: "
                            + encryptionData.getEncryptionAgent().getAlgorithm()));
                }
            } else if (encryptionData.getEncryptionAgent().getProtocol().equals(ENCRYPTION_PROTOCOL_V2)) {
                Objects.requireNonNull(encryptionData.getWrappedContentKey().getEncryptedKey(), "encryptedKey in "
                    + "encryptionData.wrappedContentKey cannot be null");
                if (!encryptionData.getEncryptionAgent().getAlgorithm().equals(AES_GCM_256)) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Encryption algorithm does not match v2 protocol: "
                            + encryptionData.getEncryptionAgent().getAlgorithm()));
                }
            } else {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                    "Invalid Encryption Agent. This version of the client library does not understand the "
                        + "Encryption Agent set on the blob message: %s",
                    encryptionDataString)));
            }

            return encryptionData;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    static EncryptionData fromJsonString(String jsonString)
        throws JsonProcessingException {
        return MAPPER.readValue(jsonString, EncryptionData.class);
    }
}
