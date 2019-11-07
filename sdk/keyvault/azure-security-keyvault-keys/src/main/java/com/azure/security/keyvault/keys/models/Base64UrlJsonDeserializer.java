// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

/**
 * The base64 URL JSON deserializer.
 */
class Base64UrlJsonDeserializer extends JsonDeserializer<byte[]> {

    static final Base64 BASE64 = new Base64(-1, null, true);

    @Override
    public byte[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String text = jp.getText();
        if (text != null) {
            return BASE64.decode(text);
        }
        return null;
    }

}
