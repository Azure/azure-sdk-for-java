// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VoiceAgentDefinitionSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentDefinition longResult = roundTrip(new VoiceAgentDefinition().setMaxOutputTokens(512L));
        assertEquals(512L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentDefinition stringResult = roundTrip(new VoiceAgentDefinition().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentDefinition stringResult = roundTrip(new VoiceAgentDefinition().setToolChoice(ToolChoiceOptions.AUTO));
        assertEquals(ToolChoiceOptions.AUTO, stringResult.getToolChoiceAsToolChoiceOptions());
        assertNull(stringResult.getToolChoiceAsToolChoiceFunction());
        assertNull(stringResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentDefinition functionResult
            = roundTrip(new VoiceAgentDefinition().setToolChoice(ToolChoiceFunction.builder().name("lookup").build()));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentDefinition mcpResult = roundTrip(new VoiceAgentDefinition()
            .setToolChoice(ToolChoiceMcp.builder().serverLabel("server").name("search").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentDefinition result
            = UnionTypeSerializationTestUtils.deserialize("{\"kind\":\"voice\"}", VoiceAgentDefinition::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());

        VoiceAgentDefinition cleared = new VoiceAgentDefinition().setMaxOutputTokens("inf")
            .setMaxOutputTokens((String) null)
            .setToolChoice(ToolChoiceOptions.AUTO)
            .setToolChoice((ToolChoiceOptions) null);
        assertNull(cleared.getMaxOutputTokensAsString());
        assertNull(cleared.getToolChoiceAsToolChoiceOptions());
    }

    private VoiceAgentDefinition roundTrip(VoiceAgentDefinition value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentDefinition::fromJson);
    }
}
