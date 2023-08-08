// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;

public class BinaryDataJsonSerializer extends JsonSerializer<BinaryData> {
    @Override
    public void serialize(BinaryData value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException {
        String text;

        if (value == null) {
            text = null;
        } else {
            byte[] bytes = value.toBytes();

            if (bytes == null) {
                text = null;
            } else if (bytes.length == 0) {
                text = "";
            } else {
                text = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            }
        }

        jsonGenerator.writeString(text);
    }
}
