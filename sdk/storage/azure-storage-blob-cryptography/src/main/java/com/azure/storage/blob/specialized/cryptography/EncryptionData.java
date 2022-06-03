// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

interface EncryptionData {
    ObjectSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);
    ClientLogger LOGGER = new ClientLogger(EncryptionData.class);

    String getEncryptionMode();
    WrappedKey getWrappedContentKey();
    EncryptionAgent getEncryptionAgent();
    Map<String, String> getKeyWrappingMetadata();
    String toJsonString() throws JsonProcessingException;

    static EncryptionData fromJsonString(String jsonString, Class<? extends EncryptionData> clazz)
        throws JsonProcessingException {
        return SERIALIZER.deserializeFromBytes(jsonString.getBytes(StandardCharsets.UTF_8),
            TypeReference.createInstance(clazz));
    }

    static EncryptionData getAndValidateEncryptionData(String encryptionDataString, boolean requiresEncryption) {
        if (encryptionDataString == null) {
            if (requiresEncryption) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("'requiresEncryption' set to true but "
                    + "downloaded data is not encrypted."));
            }
            return null;
        }

        try {
            EncryptionData encryptionData;
            if (encryptionDataString.contains("\"Protocol\":\"1.0\",")) {
                encryptionData = EncryptionData.fromJsonString(encryptionDataString, EncryptionDataV1.class);
                Objects.requireNonNull(((EncryptionDataV1) encryptionData).getContentEncryptionIV(),
                    "contentEncryptionIV in encryptionData cannot be null");
                Objects.requireNonNull(encryptionData.getWrappedContentKey().getEncryptedKey(), "encryptedKey in "
                    + "encryptionData.wrappedContentKey cannot be null");
            } else if (encryptionDataString.contains("\"Protocol\":\"2.0\",")) {
                encryptionData = EncryptionData.fromJsonString(encryptionDataString, EncryptionDataV2.class);
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
}
