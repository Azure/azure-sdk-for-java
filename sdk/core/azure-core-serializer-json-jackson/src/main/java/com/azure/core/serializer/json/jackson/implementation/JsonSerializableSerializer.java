// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Implementation of Jackson's {@link JsonSerializer} that is capable of handling {@link JsonSerializable} types.
 */
@SuppressWarnings("rawtypes")
public class JsonSerializableSerializer extends JsonSerializer<JsonSerializable> {
    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        // Do not use this JacksonJsonWriter in a try-with-resources block as closing it closes the underlying
        // JsonGenerator which could cause problems in further serialization done.
        new JacksonJsonWriter(gen).writeJson(value);
    }

    @Override
    public Class<JsonSerializable> handledType() {
        return JsonSerializable.class;
    }
}
