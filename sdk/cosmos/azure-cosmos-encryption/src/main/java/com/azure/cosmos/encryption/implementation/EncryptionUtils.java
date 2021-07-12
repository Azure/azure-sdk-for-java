// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

public class EncryptionUtils {
    private static final ObjectMapper simpleObjectMapper = new ObjectMapper();

    static {
        EncryptionUtils.simpleObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EncryptionUtils.simpleObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        EncryptionUtils.simpleObjectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        EncryptionUtils.simpleObjectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        EncryptionUtils.simpleObjectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
    }

    public static byte[] serializeJsonToByteArray(ObjectMapper objectMapper, Object object) {
        return toByteArray(Utils.serializeJsonToByteBuffer(objectMapper, object));
    }

    public static ObjectMapper getSimpleObjectMapper() {
        return EncryptionUtils.simpleObjectMapper;
    }

    public static byte[] toByteArray(ByteBuffer buf) {
        buf.position(0);
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        return arr;
    }
}
