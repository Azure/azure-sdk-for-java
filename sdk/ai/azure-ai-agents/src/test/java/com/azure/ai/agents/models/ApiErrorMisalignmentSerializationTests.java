// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.ErrorObject.Misalignment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiErrorMisalignmentSerializationTests {

    @Test
    void misalignmentRoundTripsAsOpenAIModel() throws IOException {
        String json = "{\"code\":\"misalignment\",\"message\":\"blocked\",\"misalignment\":{"
            + "\"error_type\":\"potentially_unintended_data_access\"," + "\"detailed_explanation\":\"explanation\","
            + "\"steer\":{\"message\":\"continue safely\"}}}";

        ApiError error = deserialize(json);
        Misalignment misalignment = error.getMisalignment();

        assertNotNull(misalignment);
        assertEquals("potentially_unintended_data_access", misalignment.errorType().get().asString());
        assertEquals("explanation", misalignment.detailedExplanation().get());
        assertEquals("continue safely", misalignment.steer().get().message());

        String serialized = serialize(error);
        assertTrue(serialized.contains("\"error_type\":\"potentially_unintended_data_access\""));
        assertTrue(serialized.contains("\"detailed_explanation\":\"explanation\""));
        assertTrue(serialized.contains("\"steer\":{\"message\":\"continue safely\""));

        assertEquals("explanation", deserialize(serialized).getMisalignment().detailedExplanation().get());
    }

    private static ApiError deserialize(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return ApiError.fromJson(reader);
        }
    }

    private static String serialize(ApiError error) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            error.toJson(writer);
        }
        return outputStream.toString("UTF-8");
    }
}
