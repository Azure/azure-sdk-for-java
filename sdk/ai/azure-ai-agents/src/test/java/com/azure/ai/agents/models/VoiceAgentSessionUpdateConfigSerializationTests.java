// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VoiceAgentSessionUpdateConfigSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentSessionUpdateConfig longResult
            = roundTrip(new VoiceAgentSessionUpdateConfig().setMaxOutputTokens(1024L));
        assertEquals(1024L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentSessionUpdateConfig stringResult
            = roundTrip(new VoiceAgentSessionUpdateConfig().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentSessionUpdateConfig config
            = new VoiceAgentSessionUpdateConfig().setToolChoice(ToolChoiceOptions.AUTO);
        assertEquals(ToolChoiceOptions.AUTO, config.getToolChoiceAsToolChoiceOptions());

        VoiceAgentSessionUpdateConfig optionsResult = roundTrip(config);
        assertEquals(ToolChoiceOptions.AUTO, optionsResult.getToolChoiceAsToolChoiceOptions());
        assertNull(optionsResult.getToolChoiceAsToolChoiceFunction());
        assertNull(optionsResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentSessionUpdateConfig functionResult = roundTrip(
            new VoiceAgentSessionUpdateConfig().setToolChoice(ToolChoiceFunction.builder().name("lookup").build()));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentSessionUpdateConfig mcpResult = roundTrip(
            new VoiceAgentSessionUpdateConfig().setToolChoice(ToolChoiceMcp.builder().serverLabel("server").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentSessionUpdateConfig result = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"realtime\"}",
            VoiceAgentSessionUpdateConfig::fromJson);
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

    private VoiceAgentSessionUpdateConfig roundTrip(VoiceAgentSessionUpdateConfig value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentSessionUpdateConfig::fromJson);
    }
}
