// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


import java.io.IOException;

public class JsonSerializableDeserializer extends JsonDeserializer<JsonSerializable<?>> {
    @Override
    public JsonSerializable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        new JacksonJsonReader(p, null, null, false, null);
        return null;
    }
}
