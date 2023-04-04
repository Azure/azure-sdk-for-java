// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class ReadingJsonExamples {
    public FluentJsonSerializableExample readJsonByteArray() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonByteArray
        // Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
        byte[] json = "{\"int\":10,\"boolean\":true,\"string\":\"hello\",\"aNullableDecimal\":null}"
            .getBytes(StandardCharsets.UTF_8);

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return FluentJsonSerializableExample.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonByteArray
    }

    public FluentJsonSerializableExample readJsonString() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonString
        String json = "{\"int\":10,\"boolean\":true,\"string\":\"hello\",\"aNullableDecimal\":null}";

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return FluentJsonSerializableExample.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonString
    }

    public FluentJsonSerializableExample readJsonInputStream() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonInputStream
        // Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
        InputStream json = new ByteArrayInputStream(
            "{\"int\":10,\"boolean\":true,\"string\":\"hello\",\"aNullableDecimal\":null}"
                .getBytes(StandardCharsets.UTF_8));

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return FluentJsonSerializableExample.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonInputStream
    }

    public FluentJsonSerializableExample readJsonReader() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonReader
        Reader json = new StringReader("{\"int\":10,\"boolean\":true,\"string\":\"hello\",\"aNullableDecimal\":null}");

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return FluentJsonSerializableExample.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonReader
    }
}
