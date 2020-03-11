// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;

/**
 * The base64 URL JSON serializer.
 */
class Base64UrlJsonSerializer extends JsonSerializer<byte[]> {
    @Override
    public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
        String text;
        if (value == null) {
            text = null;
        } else if (value.length == 0) {
            text = "";
        } else {
            text = Base64.getEncoder().encodeToString(value);
        }
        jgen.writeString(text);
    }
}
