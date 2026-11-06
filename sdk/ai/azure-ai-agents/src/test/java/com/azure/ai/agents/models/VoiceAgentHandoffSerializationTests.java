// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentHandoffSerializationTests {
    private static final String EVENT_FIELDS = "\"event_id\":\"event\",\"handoff_id\":\"handoff\",\"edge_id\":\"edge\","
        + "\"from_node_id\":\"$entrypoint\",\"to_node_id\":\"specialist\",\"from_model\":\"source\","
        + "\"to_model\":\"target\",\"tool_call_id\":\"call\",\"node_generation\":2";

    @Test
    public void abortedEventBridgesOpenAIError() throws IOException {
        String json = "{\"type\":\"session.handoff.aborted\"," + EVENT_FIELDS + ",\"reason\":\"error\","
            + "\"error\":{\"type\":\"server_error\",\"message\":\"Preparation failed\","
            + "\"code\":\"handoff_failed\",\"param\":null}}";
        VoiceAgentSessionHandoffAbortedEvent aborted = assertInstanceOf(VoiceAgentSessionHandoffAbortedEvent.class,
            UnionTypeSerializationTestUtils.deserialize(json, RealtimeServerEvent::fromJson));
        assertEquals("Preparation failed", aborted.getError().message());
        String serialized = UnionTypeSerializationTestUtils.serialize(aborted);
        assertTrue(serialized.contains("\"message\":\"Preparation failed\""));
        VoiceAgentSessionHandoffAbortedEvent result = assertInstanceOf(VoiceAgentSessionHandoffAbortedEvent.class,
            UnionTypeSerializationTestUtils.deserialize(serialized, RealtimeServerEvent::fromJson));
        assertEquals(VoiceAgentHandoffAbortReason.ERROR, result.getReason());
        assertEquals("server_error", result.getError().type());
        assertEquals("handoff_failed", result.getError().code().get());
    }

    @Test
    public void abortedEventAllowsAbsentAndNullError() throws IOException {
        for (String error : new String[] { "", ",\"error\":null" }) {
            String json = "{\"type\":\"session.handoff.aborted\"," + EVENT_FIELDS + ",\"reason\":\"user_interruption\""
                + error + "}";
            VoiceAgentSessionHandoffAbortedEvent value = assertInstanceOf(VoiceAgentSessionHandoffAbortedEvent.class,
                UnionTypeSerializationTestUtils.deserialize(json, RealtimeServerEvent::fromJson));
            assertNull(value.getError());
            assertEquals(VoiceAgentHandoffAbortReason.USER_INTERRUPTION, value.getReason());
            VoiceAgentSessionHandoffAbortedEvent result
                = assertInstanceOf(VoiceAgentSessionHandoffAbortedEvent.class, UnionTypeSerializationTestUtils
                    .deserialize(UnionTypeSerializationTestUtils.serialize(value), RealtimeServerEvent::fromJson));
            assertNull(result.getError());
        }
    }
}
