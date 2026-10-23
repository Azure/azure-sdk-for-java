// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VoiceAgentLlmGeneratedGreetingConfigurationSerializationTests {

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfiguration config
            = new VoiceAgentLlmGeneratedGreetingConfiguration("Say hello").setToolChoice(ToolChoiceOptions.NONE);
        assertEquals(ToolChoiceOptions.NONE, config.getToolChoiceAsToolChoiceOptions());

        VoiceAgentLlmGeneratedGreetingConfiguration optionsResult = roundTrip(config);
        assertEquals(ToolChoiceOptions.NONE, optionsResult.getToolChoiceAsToolChoiceOptions());
        assertNull(optionsResult.getToolChoiceAsToolChoiceFunction());
        assertNull(optionsResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentLlmGeneratedGreetingConfiguration functionResult
            = roundTrip(new VoiceAgentLlmGeneratedGreetingConfiguration("Say hello")
                .setToolChoice(ToolChoiceFunction.builder().name("greet").build()));
        assertEquals("greet", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentLlmGeneratedGreetingConfiguration mcpResult
            = roundTrip(new VoiceAgentLlmGeneratedGreetingConfiguration("Say hello")
                .setToolChoice(ToolChoiceMcp.builder().serverLabel("server").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullToolChoiceReturnsNull() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfiguration result
            = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"llm_generated\",\"prompt\":\"Say hello\"}",
                VoiceAgentLlmGeneratedGreetingConfiguration::fromJson);
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());

        result.setToolChoice(ToolChoiceOptions.AUTO).setToolChoice((ToolChoiceOptions) null);
        assertNull(result.getToolChoiceAsToolChoiceOptions());
    }

    private VoiceAgentLlmGeneratedGreetingConfiguration roundTrip(VoiceAgentLlmGeneratedGreetingConfiguration value)
        throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentLlmGeneratedGreetingConfiguration::fromJson);
    }
}
