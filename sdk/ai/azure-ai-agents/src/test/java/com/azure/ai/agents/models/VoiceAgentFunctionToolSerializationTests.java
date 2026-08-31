// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentFunctionToolSerializationTests {

    @Test
    public void parametersPreserveArbitraryJsonSchema() throws IOException {
        String schema = "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"},"
            + "\"limit\":{\"type\":\"integer\",\"minimum\":1}},\"required\":[\"city\"],"
            + "\"additionalProperties\":false}";
        String json = "{\"name\":\"lookup\",\"type\":\"function\",\"parameters\":" + schema + "}";

        VoiceAgentFunctionTool tool = deserializeFromJson(json);

        assertEquals(schema, tool.getParameters().toString());
        String serialized = serializeToJson(tool);
        assertTrue(serialized.contains("\"parameters\":" + schema), "Unexpected JSON: " + serialized);
        assertEquals(schema, deserializeFromJson(serialized).getParameters().toString());
    }

    private static String serializeToJson(VoiceAgentFunctionTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private static VoiceAgentFunctionTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VoiceAgentFunctionTool.fromJson(jsonReader);
        }
    }
}
