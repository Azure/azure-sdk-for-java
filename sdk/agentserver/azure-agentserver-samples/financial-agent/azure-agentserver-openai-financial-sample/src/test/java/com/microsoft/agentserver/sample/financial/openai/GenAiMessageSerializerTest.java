// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenAiMessageSerializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void toolArgumentsAreSerializedAsStructuredJson() throws Exception {
        var message = GenAiMessageSerializer.assistantToolCallsMessage(
            List.of(new GenAiMessageSerializer.ToolCall(
                "call-1", "withdraw", "{\"user\":\"Alice\",\"amount\":12.5}")),
            "tool_calls");

        JsonNode json = MAPPER.readTree(GenAiMessageSerializer.toJson(List.of(message)));
        JsonNode arguments = json.get(0).get("parts").get(0).get("arguments");
        assertTrue(arguments.isObject());
        assertEquals("Alice", arguments.get("user").asText());
        assertEquals(12.5, arguments.get("amount").asDouble());
    }

    @Test
    void malformedToolArgumentsAreRetainedInsteadOfDropped() throws Exception {
        var message = GenAiMessageSerializer.assistantToolCallsMessage(
            List.of(new GenAiMessageSerializer.ToolCall("call-1", "withdraw", "not-json")),
            "tool_calls");

        JsonNode json = MAPPER.readTree(GenAiMessageSerializer.toJson(List.of(message)));
        assertEquals("not-json", json.get(0).get("parts").get(0).get("arguments").asText());
    }
}
