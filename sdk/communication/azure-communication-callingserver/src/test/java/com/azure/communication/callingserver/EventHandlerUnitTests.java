// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.RecordingState;
import com.azure.communication.callingserver.models.events.CallAutomationEventBase;
import com.azure.communication.callingserver.models.events.RecordingStateChangedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventHandlerUnitTests {

    @Test
    public void parseRecordingStateChangedEvent() {
        String receivedEvent = "[\n" +
            "    {\n" +
            "        \"id\": \"bf59843a-888f-47ca-8d1c-885c1f5e71dc\",\n" +
            "        \"source\": \"calling/recordings/serverCallId/recordingId/recordingId/RecordingStateChanged\",\n" +
            "        \"type\": \"Microsoft.Communication.CallRecordingStateChanged\",\n" +
            "        \"data\": {\n" +
            "            \"type\": \"recordingStateChanged\",\n" +
            "            \"recordingId\": \"recordingId\",\n" +
            "            \"state\": \"active\",\n" +
            "            \"startDateTime\": \"2022-08-11T23:42:45.4394211+00:00\",\n" +
            "            \"callConnectionId\": \"callConnectionId\",\n" +
            "            \"serverCallId\": \"serverCallId\",\n" +
            "            \"correlationId\": \"correlationId\"\n" +
            "        },\n" +
            "        \"time\": \"2022-08-11T23:42:45.5346632+00:00\",\n" +
            "        \"specversion\": \"1.0\",\n" +
            "        \"datacontenttype\": \"application/json\",\n" +
            "        \"subject\": \"calling/recordings/serverCallId/recordingId/recordingId/RecordingStateChanged\"\n" +
            "    }\n" +
            "]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        RecordingStateChangedEvent recordingEvent = (RecordingStateChangedEvent) event;
        assertNotNull(recordingEvent);
        assertEquals("serverCallId", recordingEvent.getServerCallId());
        assertEquals("recordingId", recordingEvent.getRecordingId());
        assertEquals(RecordingState.ACTIVE, recordingEvent.getRecordingState());
    }
}
