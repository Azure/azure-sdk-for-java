/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information. 
 */

package com.microsoft.azure.keyvault.webkey;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * The base64 URL JSON deserializer.
 */
public class Base64UrlJsonDeserializer extends JsonDeserializer<byte[]> {

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