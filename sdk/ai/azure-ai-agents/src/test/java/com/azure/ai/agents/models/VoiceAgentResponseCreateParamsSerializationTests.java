// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VoiceAgentResponseCreateParamsSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentResponseCreateParams longResult
            = roundTrip(new VoiceAgentResponseCreateParams().setMaxOutputTokens(4096L));
        assertEquals(4096L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentResponseCreateParams stringResult
            = roundTrip(new VoiceAgentResponseCreateParams().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentResponseCreateParams optionsResult
            = roundTrip(new VoiceAgentResponseCreateParams().setToolChoice(ToolChoiceOptions.REQUIRED));
        assertEquals(ToolChoiceOptions.REQUIRED, optionsResult.getToolChoiceAsToolChoiceOptions());
        assertNull(optionsResult.getToolChoiceAsToolChoiceFunction());
        assertNull(optionsResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentResponseCreateParams functionResult = roundTrip(
            new VoiceAgentResponseCreateParams().setToolChoice(ToolChoiceFunction.builder().name("lookup").build()));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentResponseCreateParams mcpResult = roundTrip(
            new VoiceAgentResponseCreateParams().setToolChoice(ToolChoiceMcp.builder().serverLabel("server").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentResponseCreateParams result
            = UnionTypeSerializationTestUtils.deserialize("{}", VoiceAgentResponseCreateParams::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());

        result.setMaxOutputTokens("inf").setMaxOutputTokens((String) null);
        result.setToolChoice(ToolChoiceOptions.AUTO).setToolChoice((ToolChoiceOptions) null);
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
    }

    private VoiceAgentResponseCreateParams roundTrip(VoiceAgentResponseCreateParams value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentResponseCreateParams::fromJson);
    }
}
