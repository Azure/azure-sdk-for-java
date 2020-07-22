// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson.implementation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class OffsetTimeAdapter implements JsonSerializer<OffsetTime>, JsonDeserializer<OffsetTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_TIME;
    @Override
    public OffsetTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        return FORMATTER.parse(json.getAsString(), OffsetTime::from);
    }

    @Override
    public JsonElement serialize(OffsetTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(FORMATTER.format(src));
    }

    OffsetTimeAdapter() {
    }
}
