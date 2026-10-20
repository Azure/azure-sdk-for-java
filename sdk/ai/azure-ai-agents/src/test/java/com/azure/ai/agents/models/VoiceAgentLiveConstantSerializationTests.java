// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VoiceAgentLiveConstantSerializationTests {
    @Test
    public void functionToolChoiceRetainsType() throws IOException {
        assertConstant(new LiveFunctionToolChoiceParam("weather"), "type", "function",
            LiveFunctionToolChoiceParam::getType, LiveFunctionToolChoiceParam::fromJson);
    }

    @Test
    public void functionToolInputRetainsType() throws IOException {
        assertConstant(new LiveFunctionToolInputParam("weather"), "type", "function",
            LiveFunctionToolInputParam::getType, LiveFunctionToolInputParam::fromJson);
    }

    @Test
    public void mcpToolChoiceRetainsType() throws IOException {
        assertConstant(new LiveMCPToolChoiceParam("server", "weather"), "type", "mcp", LiveMCPToolChoiceParam::getType,
            LiveMCPToolChoiceParam::fromJson);
    }

    @Test
    public void webSearchToolRetainsType() throws IOException {
        assertConstant(new LiveWebSearchToolInputParam(), "type", "web_search", LiveWebSearchToolInputParam::getType,
            LiveWebSearchToolInputParam::fromJson);
    }

    @Test
    public void conversationRetainsObjectType() throws IOException {
        VoiceAgentLiveConversation conversation = UnionTypeSerializationTestUtils
            .deserialize("{\"id\":\"conversation-test\"}", VoiceAgentLiveConversation::fromJson);
        assertConstant(conversation, "object", "voice.conversation", VoiceAgentLiveConversation::getObjectType,
            VoiceAgentLiveConversation::fromJson);
    }

    @Test
    public void definitionRetainsModelType() throws IOException {
        assertConstant(new VoiceAgentLiveDefinition(VoiceAgentSubProtocol.LIVE, "gpt-live"), "model_type",
            "self_deployed", VoiceAgentLiveDefinition::getModelType, VoiceAgentLiveDefinition::fromJson);
    }

    @Test
    public void managedAgentDelegationRetainsType() throws IOException {
        assertConstant(new VoiceAgentLiveManagedAgentDelegation("weather-agent"), "type", "foundry_managed_agent",
            VoiceAgentLiveManagedAgentDelegation::getType, VoiceAgentLiveManagedAgentDelegation::fromJson);
    }

    private static <T extends JsonSerializable<?>> void assertConstant(T model, String property, String expected,
        Function<T, String> getter, UnionTypeSerializationTestUtils.Deserializer<T> deserializer) throws IOException {
        assertEquals(expected, getter.apply(model));
        String json = UnionTypeSerializationTestUtils.serialize(model);
        try (JsonReader reader = JsonProviders.createReader(json)) {
            assertEquals(expected, reader.readMap(JsonReader::readUntyped).get(property));
        }
        T roundTripped = UnionTypeSerializationTestUtils.deserialize(json, deserializer);
        assertEquals(expected, getter.apply(roundTripped));
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(roundTripped));
    }
}
