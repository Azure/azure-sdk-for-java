// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class JsonSerializableSerializer extends JsonSerializer<JsonSerializable<?>> {
    @Override
    public void serialize(JsonSerializable<?> value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        new JacksonJsonWriter(gen).writeJson(value);
    }
}
