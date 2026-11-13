// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.openai.models.live.FunctionTool;
import com.openai.models.live.ResponsesDelegationConfig;
import com.openai.models.live.ResponsesDelegationConfig.ToolChoice.LiveToolChoiceEnum;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentLiveResponsesDelegationSerializationTests {
    @Test
    public void openAISettingsRoundTripThroughDelegation() throws IOException {
        ResponsesDelegationConfig responses = ResponsesDelegationConfig.builder()
            .model("responses-deployment")
            .instructions("Find the weather.")
            .maxOutputTokens(256L)
            .parallelToolCalls(false)
            .addTool(FunctionTool.builder().name("get_weather").description("Get the weather.").build())
            .toolChoice(LiveToolChoiceEnum.AUTO)
            .build();
        VoiceAgentLiveResponsesDelegation original
            = new VoiceAgentLiveResponsesDelegation(responses).setAcknowledgementDelay(Duration.ofMillis(400));

        String json = UnionTypeSerializationTestUtils.serialize(original);
        assertTrue(json.contains("\"responses\":{"));
        assertTrue(json.contains("\"tools\":[{\""));
        assertTrue(json.contains("\"type\":\"function\""));
        assertTrue(json.contains("\"tool_choice\":\"auto\""));
        assertTrue(json.contains("\"acknowledgement_delay_ms\":400"));
        VoiceAgentLiveResponsesDelegation result = assertInstanceOf(VoiceAgentLiveResponsesDelegation.class,
            UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentLiveDelegation::fromJson));
        assertEquals(VoiceAgentLiveDelegationType.RESPONSES, result.getType());
        assertEquals(Duration.ofMillis(400), result.getAcknowledgementDelay());
        assertEquals(responses, result.getResponses());
        assertEquals("get_weather", result.getResponses().tools().get().get(0).asFunction().name());
        assertEquals(LiveToolChoiceEnum.AUTO, result.getResponses().toolChoice().get().asLiveToolChoiceEnum());
    }

    @Test
    public void namedToolChoiceAndToolSchemaRetainWireShape() throws IOException {
        String json = "{\"type\":\"responses\",\"responses\":{\"model\":\"responses-deployment\","
            + "\"instructions\":\"Use tools.\",\"tools\":[{\"type\":\"function\",\"name\":\"get_weather\","
            + "\"parameters\":{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}}],"
            + "\"tool_choice\":{\"type\":\"function\",\"name\":\"get_weather\"}}}";
        VoiceAgentLiveResponsesDelegation value
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentLiveResponsesDelegation::fromJson);
        assertEquals("get_weather", value.getResponses().toolChoice().get().asLiveFunctionToolChoiceParam().name());

        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertEquals(BinaryData.fromString(json).toObject(Object.class),
            BinaryData.fromString(serialized).toObject(Object.class));
    }

    @Test
    public void absentAndNullResponsesRemainNull() throws IOException {
        for (String responses : new String[] { "", ",\"responses\":null" }) {
            VoiceAgentLiveResponsesDelegation value = UnionTypeSerializationTestUtils
                .deserialize("{\"type\":\"responses\"" + responses + "}", VoiceAgentLiveResponsesDelegation::fromJson);
            assertNull(value.getResponses());
            VoiceAgentLiveResponsesDelegation result = UnionTypeSerializationTestUtils.deserialize(
                UnionTypeSerializationTestUtils.serialize(value), VoiceAgentLiveResponsesDelegation::fromJson);
            assertNull(result.getResponses());
        }
    }

    @Test
    public void acknowledgementDelayPreservesNullAndZeroInCustomizedSerializer() throws IOException {
        for (String delay : new String[] {
            "",
            ",\"acknowledgement_delay_ms\":null",
            ",\"acknowledgement_delay_ms\":0" }) {
            VoiceAgentLiveResponsesDelegation value = UnionTypeSerializationTestUtils
                .deserialize("{\"type\":\"responses\"" + delay + "}", VoiceAgentLiveResponsesDelegation::fromJson);
            Duration expected = delay.endsWith(":0") ? Duration.ZERO : null;
            assertEquals(expected, value.getAcknowledgementDelay());
            VoiceAgentLiveResponsesDelegation result = UnionTypeSerializationTestUtils.deserialize(
                UnionTypeSerializationTestUtils.serialize(value), VoiceAgentLiveResponsesDelegation::fromJson);
            assertEquals(expected, result.getAcknowledgementDelay());
        }
    }
}
