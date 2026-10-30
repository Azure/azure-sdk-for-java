// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class UnionTypeSerializationTestUtils {

    @FunctionalInterface
    interface Deserializer<T> {
        T deserialize(JsonReader reader) throws IOException;
    }

    private UnionTypeSerializationTestUtils() {
    }

    static String serialize(JsonSerializable<?> model) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            model.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    static <T> T deserialize(String json, Deserializer<T> deserializer) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return deserializer.deserialize(jsonReader);
        }
    }
}
