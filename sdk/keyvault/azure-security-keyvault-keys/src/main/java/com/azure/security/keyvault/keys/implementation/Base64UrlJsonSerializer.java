// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;

/**
 * The Base64 URL JSON serializer.
 */
public class Base64UrlJsonSerializer extends JsonSerializer<byte[]> {
    @Override
    public void serialize(byte[] value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException {

        String text;

        if (value == null) {
            text = null;
        } else if (value.length == 0) {
            text = "";
        } else {
            text = Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        }

        jsonGenerator.writeString(text);
    }
}
