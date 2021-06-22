// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.CallConnectionStateChangedEventInternal;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.communication.callingserver.models.events.CallConnectionStateChangedEvent;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventTests {
    @Test
    public void callConnectionStateChangedEventTest() throws JsonProcessingException {
        String serverCallId = UUID.randomUUID().toString();
        String callConnectionId = UUID.randomUUID().toString();
        CallConnectionStateChangedEventInternal internalEvent =
            new CallConnectionStateChangedEventInternal()
                .setCallConnectionId(callConnectionId)
                .setServerCallId(serverCallId)
                .setCallConnectionState(CallConnectionState.CONNECTED);

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonData = objectWriter.writeValueAsString(internalEvent);
        BinaryData binaryData = BinaryData.fromString(jsonData);

        CallConnectionStateChangedEvent event = CallConnectionStateChangedEvent.deserialize(binaryData);

        assertNotNull(event);
        assertEquals(event.getCallConnectionId(), callConnectionId);
        assertEquals(event.getServerCallId(), serverCallId);
        assertEquals(event.getCallConnectionState(), CallConnectionState.CONNECTED);
    }
}
