// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

interface EncryptionData {
    ObjectMapper MAPPER = new ObjectMapper();

    String getEncryptionMode();
    WrappedKey getWrappedContentKey();
    EncryptionAgent getEncryptionAgent();
    Map<String, String> getKeyWrappingMetadata();
    String toJsonString() throws JsonProcessingException;

    static EncryptionData fromJsonString(String jsonString, Class<? extends EncryptionData> clazz)
        throws JsonProcessingException {
        return MAPPER.readValue(jsonString, clazz);
    }
}
