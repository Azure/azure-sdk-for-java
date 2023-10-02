// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.codesnippets;

import com.typespec.json.JsonProviders;
import com.typespec.json.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class ReadingJsonExamples {
    public ComputerMemory readJsonByteArray() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonByteArray
        // Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
        byte[] json = ("{\"memoryInBytes\":10000000000,\"clockSpeedInHertz\":4800000000,"
            + "\"manufacturer\":\"Memory Corp\",\"errorCorrecting\":true}").getBytes(StandardCharsets.UTF_8);

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return ComputerMemory.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonByteArray
    }

    public ComputerProcessor readJsonString() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonString
        String json = "{\"cores\":16,\"threads\":32,\"manufacturer\":\"Processor Corp\","
            + "\"clockSpeedInHertz\":5000000000,\"releaseDate\":null}";

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return ComputerProcessor.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonString
    }

    public VmStatistics readJsonInputStream() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonInputStream
        // Sample uses String.getBytes as a convenience to show the JSON string in a human-readable form.
        InputStream json = new ByteArrayInputStream(("{\"VMSize\":\"large\",\"Processor\":{\"cores\":8,"
            + "\"threads\"16\",\"manufacturer\":\"Processor Corp\",\"clockSpeedInHertz\":4000000000,"
            + "\"releaseDate\":\"2023-01-01\"},\"Memory\":{\"memoryInBytes\":10000000000,"
            + "\"clockSpeedInHertz\":4800000000,\"manufacturer\":\"Memory Corp\",\"errorCorrecting\":true},"
            + "\"AcceleratedNetwork\":true,\"CloudProvider\":\"Azure\",\"Available\":true}")
            .getBytes(StandardCharsets.UTF_8));

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VmStatistics.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonInputStream
    }

    public VmStatistics readJsonReader() throws IOException {
        // BEGIN: com.azure.json.JsonReader.readJsonReader
        Reader json = new StringReader("{\"VMSize\":\"large\",\"Processor\":{\"cores\":8,\"threads\"16\","
            + "\"manufacturer\":\"Processor Corp\",\"clockSpeedInHertz\":4000000000,\"releaseDate\":\"2023-01-01\"},"
            + "\"Memory\":{\"memoryInBytes\":10000000000,\"clockSpeedInHertz\":4800000000,"
            + "\"manufacturer\":\"Memory Corp\",\"errorCorrecting\":true},\"AcceleratedNetwork\":true,"
            + "\"CloudProvider\":\"Azure\",\"Available\":true}");

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VmStatistics.fromJson(jsonReader);
        }
        // END: com.azure.json.JsonReader.readJsonReader
    }
}
