// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models.webkey;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

/**
 * The base64 URL JSON serializer.
 */
class Base64UrlJsonSerializer extends JsonSerializer<byte[]> {

    static final Base64 BASE64 = new Base64(-1, null, true);

    @Override
    public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
        String text;
        if (value == null) {
            text = null;
        } else if (value.length == 0) {
            text = "";
        } else {
            text = BASE64.encodeAsString(value);
        }
        jgen.writeString(text);
    }

}
