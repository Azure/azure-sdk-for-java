// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

public class EncryptionUtils {
    public static byte[] serializeJsonToByteArray(ObjectMapper objectMapper, Object object) {
        return toByteArray(com.azure.cosmos.implementation.Utils.serializeJsonToByteBuffer(objectMapper, object));
    }

    public static byte[] toByteArray(ByteBuffer buf) {
        buf.position(0);
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        return arr;
    }
}
