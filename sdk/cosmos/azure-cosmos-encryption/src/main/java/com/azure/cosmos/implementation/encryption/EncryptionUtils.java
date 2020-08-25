// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
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

    public static URI toURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
