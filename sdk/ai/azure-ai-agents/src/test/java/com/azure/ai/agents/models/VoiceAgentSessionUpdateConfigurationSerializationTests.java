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

public class VoiceAgentSessionUpdateConfigurationSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentSessionUpdateConfiguration longResult
            = roundTrip(new VoiceAgentSessionUpdateConfiguration().setMaxOutputTokens(1024L));
        assertEquals(1024L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentSessionUpdateConfiguration stringResult
            = roundTrip(new VoiceAgentSessionUpdateConfiguration().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentSessionUpdateConfiguration config
            = new VoiceAgentSessionUpdateConfiguration().setToolChoice(ToolChoiceOptions.AUTO);
        assertEquals(ToolChoiceOptions.AUTO, config.getToolChoiceAsToolChoiceOptions());

        VoiceAgentSessionUpdateConfiguration optionsResult = roundTrip(config);
        assertEquals(ToolChoiceOptions.AUTO, optionsResult.getToolChoiceAsToolChoiceOptions());
        assertNull(optionsResult.getToolChoiceAsToolChoiceFunction());
        assertNull(optionsResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentSessionUpdateConfiguration functionResult = roundTrip(new VoiceAgentSessionUpdateConfiguration()
            .setToolChoice(ToolChoiceFunction.builder().name("lookup").build()));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentSessionUpdateConfiguration mcpResult = roundTrip(new VoiceAgentSessionUpdateConfiguration()
            .setToolChoice(ToolChoiceMcp.builder().serverLabel("server").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentSessionUpdateConfiguration result = UnionTypeSerializationTestUtils
            .deserialize("{\"type\":\"realtime\"}", VoiceAgentSessionUpdateConfiguration::fromJson);
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

    private VoiceAgentSessionUpdateConfiguration roundTrip(VoiceAgentSessionUpdateConfiguration value)
        throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentSessionUpdateConfiguration::fromJson);
    }
}
