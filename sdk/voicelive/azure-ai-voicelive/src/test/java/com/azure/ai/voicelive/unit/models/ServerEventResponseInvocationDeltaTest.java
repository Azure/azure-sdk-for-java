// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.ServerEventResponseInvocationDelta;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionServerEvent;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the hosted-agent {@link ServerEventResponseInvocationDelta} passthrough event.
 */
class ServerEventResponseInvocationDeltaTest {

    @Test
    void testServerEventResponseInvocationDeltaPolymorphic() {
        String json = "{\"type\":\"response.invocation.delta\",\"event_id\":\"inv1\","
            + "\"delta\":{\"event\":\"thread.run.created\",\"data\":{\"id\":\"run-1\"}}}";

        SessionServerEvent event = BinaryData.fromString(json).toObject(SessionServerEvent.class);

        assertTrue(event instanceof ServerEventResponseInvocationDelta);
        ServerEventResponseInvocationDelta inv = (ServerEventResponseInvocationDelta) event;
        assertEquals(ServerEventType.RESPONSE_INVOCATION_DELTA, inv.getType());
        assertEquals("inv1", inv.getEventId());
        assertNotNull(inv.getDelta());
        assertTrue(inv.getDelta().containsKey("event"));
        assertNotNull(inv.getDelta().get("event"));
    }
}
