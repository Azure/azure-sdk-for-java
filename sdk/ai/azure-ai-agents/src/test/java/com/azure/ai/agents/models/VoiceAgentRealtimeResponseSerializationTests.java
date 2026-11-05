// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentRealtimeResponseSerializationTests {

    @Test
    public void objectTypeRoundTripsUnderObjectWireName() throws IOException {
        VoiceAgentRealtimeResponse response = UnionTypeSerializationTestUtils
            .deserialize("{\"object\":\"realtime.response\"}", VoiceAgentRealtimeResponse::fromJson);
        assertEquals(VoiceResponseObjectType.REALTIME_RESPONSE, response.getObjectType());

        String serialized = UnionTypeSerializationTestUtils.serialize(response);
        assertTrue(serialized.contains("\"object\":\"realtime.response\""));
        assertFalse(serialized.contains("\"objectType\""));
        assertEquals(response.getObjectType(),
            UnionTypeSerializationTestUtils.deserialize(serialized, VoiceAgentRealtimeResponse::fromJson)
                .getObjectType());
        assertNull(
            UnionTypeSerializationTestUtils.deserialize("{}", VoiceAgentRealtimeResponse::fromJson).getObjectType());
        assertNull(
            UnionTypeSerializationTestUtils.deserialize("{\"object\":null}", VoiceAgentRealtimeResponse::fromJson)
                .getObjectType());
    }

}
