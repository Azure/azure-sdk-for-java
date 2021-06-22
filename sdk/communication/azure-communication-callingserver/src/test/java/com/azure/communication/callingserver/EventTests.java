// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.AddParticipantResultEventInternal;
import com.azure.communication.callingserver.implementation.models.CallConnectionStateChangedEventInternal;
import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.events.AddParticipantResultEvent;
import com.azure.communication.callingserver.models.events.CallConnectionStateChangedEvent;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventTests {
    @Test
    public void callConnectionStateChangedEventTest() throws JsonProcessingException {
        var serverCallId = UUID.randomUUID().toString();
        var callConnectionId = UUID.randomUUID().toString();
        CallConnectionStateChangedEventInternal internalEvent =
            new CallConnectionStateChangedEventInternal()
                .setCallConnectionId(callConnectionId)
                .setServerCallId(serverCallId)
                .setCallConnectionState(CallConnectionState.CONNECTED);

        BinaryData binaryData = getBinaryData(internalEvent);
        CallConnectionStateChangedEvent event =
            assertDoesNotThrow(() -> CallConnectionStateChangedEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getCallConnectionId(), callConnectionId);
        assertEquals(event.getServerCallId(), serverCallId);
        assertEquals(event.getCallConnectionState(), CallConnectionState.CONNECTED);
    }

    @Test
    public void addParticipantResultEventTest() throws JsonProcessingException {
        var operationContext = UUID.randomUUID().toString();
        var message = "Participant added.";
        AddParticipantResultEventInternal internalEvent =
            new AddParticipantResultEventInternal()
                .setOperationContext(operationContext)
                .setStatus(OperationStatus.COMPLETED)
                .setResultInfo(new ResultInfoInternal().setCode(100).setSubcode(200).setMessage(message));

        BinaryData binaryData = getBinaryData(internalEvent);
        AddParticipantResultEvent event =
            assertDoesNotThrow(() -> AddParticipantResultEvent.deserialize(binaryData));

        assertNotNull(event);
        assertEquals(event.getOperationContext(), operationContext);
        assertEquals(event.getStatus(), OperationStatus.COMPLETED);
        assertNotNull(event.getResultInfo());
        assertEquals(event.getResultInfo().getCode(), 100);
        assertEquals(event.getResultInfo().getSubcode(), 200);
        assertEquals(event.getResultInfo().getMessage(), message);

    }

    private BinaryData getBinaryData(Object eventObject) throws JsonProcessingException {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonData = objectWriter.writeValueAsString(eventObject);
        return BinaryData.fromString(jsonData);
    }
}
