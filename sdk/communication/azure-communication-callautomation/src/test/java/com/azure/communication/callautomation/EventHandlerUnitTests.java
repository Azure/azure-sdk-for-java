// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.events.ParticipantsUpdated;
import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.PlayFailed;
import com.azure.communication.callautomation.models.events.ReasonCode;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.events.RecordingStateChanged;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventHandlerUnitTests {
    static final String EVENT_PARTICIPANT_UPDATED = "{\"id\":\"61069ef9-5ca9-457f-ac36-e2bb5e8400ca\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\",\"type\":\"Microsoft.Communication.ParticipantsUpdated\",\"data\":{\"participants\":[{\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\"}},{\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\"}}],\"type\":\"participantsUpdated\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.9129474+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\"}";
    static final String EVENT_CALL_CONNECTED = "{\"id\":\"46116fb7-27e0-4a99-9478-a659c8fd4815\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/CallConnected\",\"type\":\"Microsoft.Communication.CallConnected\",\"data\":{\"type\":\"callConnected\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.8174402+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/CallConnected\"}";

    @Test
    public void parseEvent() {
        CallAutomationEventBase callAutomationEventBase = EventHandler.parseEvent(EVENT_PARTICIPANT_UPDATED);

        assertNotNull(callAutomationEventBase);
        assertEquals(callAutomationEventBase.getClass(), ParticipantsUpdated.class);
        assertNotNull(((ParticipantsUpdated) callAutomationEventBase).getParticipants());
    }

    @Test
    public void parseEventList() {
        List<CallAutomationEventBase> callAutomationEventBaseList = EventHandler.parseEventList("["
            + EVENT_CALL_CONNECTED + "," + EVENT_PARTICIPANT_UPDATED + "]");

        assertNotNull(callAutomationEventBaseList);
        assertEquals(callAutomationEventBaseList.get(0).getClass(), CallConnected.class);
        assertEquals(callAutomationEventBaseList.get(1).getClass(), ParticipantsUpdated.class);
        assertNotNull(callAutomationEventBaseList.get(0).getCallConnectionId());
    }

    @Test
    public void parseRecordingStateChangedEvent() {
        String receivedEvent = "[\n"
            + "    {\n"
            + "        \"id\": \"bf59843a-888f-47ca-8d1c-885c1f5e71dc\",\n"
            + "        \"source\": \"calling/recordings/serverCallId/recordingId/recordingId/RecordingStateChanged\",\n"
            + "        \"type\": \"Microsoft.Communication.RecordingStateChanged\",\n"
            + "        \"data\": {\n"
            + "            \"type\": \"recordingStateChanged\",\n"
            + "            \"recordingId\": \"recordingId\",\n"
            + "            \"state\": \"active\",\n"
            + "            \"startDateTime\": \"2022-08-11T23:42:45.4394211+00:00\",\n"
            + "            \"callConnectionId\": \"callConnectionId\",\n"
            + "            \"serverCallId\": \"serverCallId\",\n"
            + "            \"correlationId\": \"correlationId\"\n"
            + "        },\n"
            + "        \"time\": \"2022-08-11T23:42:45.5346632+00:00\",\n"
            + "        \"specversion\": \"1.0\",\n"
            + "        \"datacontenttype\": \"application/json\",\n"
            + "        \"subject\": \"calling/recordings/serverCallId/recordingId/recordingId\"\n"
            + "    }\n"
            + "]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        RecordingStateChanged recordingEvent = (RecordingStateChanged) event;
        assertNotNull(recordingEvent);
        assertEquals("serverCallId", recordingEvent.getServerCallId());
        assertEquals("recordingId", recordingEvent.getRecordingId());
        assertEquals(RecordingState.ACTIVE, recordingEvent.getRecordingState());
    }

    @Test
    public void parsePlayCompletedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/PlayCompleted\",\n"
            + "\"type\": \"Microsoft.Communication.PlayCompleted\",\n"
            + "\"data\": {\n"
            + "\"resultInformation\": {\n"
            + "\"code\": 200,\n"
            + "\"subCode\": 0,\n"
            + "\"message\": \"Action completed successfully.\"\n"
            + "},\n"
            + "\"type\": \"playCompletedEvent\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        PlayCompleted playCompletedEvent = (PlayCompleted) event;
        assertNotNull(playCompletedEvent);
        assertEquals("serverCallId", playCompletedEvent.getServerCallId());
        assertEquals(200, playCompletedEvent.getResultInformation().getCode());
        assertEquals(ReasonCode.COMPLETED_SUCCESSFULLY, playCompletedEvent.getReasonCode());
    }

    @Test
    public void parsePlayFailedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/PlayFailed\",\n"
            + "\"type\": \"Microsoft.Communication.PlayFailed\",\n"
            + "\"data\": {\n"
            + "\"resultInformation\": {\n"
            + "\"code\": 400,\n"
            + "\"subCode\": 8536,\n"
            + "\"message\": \"Action failed, file could not be downloaded.\"\n"
            + "},\n"
            + "\"type\": \"playFailedEvent\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        PlayFailed playFailedEvent = (PlayFailed) event;
        assertNotNull(playFailedEvent);
        assertEquals("serverCallId", playFailedEvent.getServerCallId());
        assertEquals(400, playFailedEvent.getResultInformation().getCode());
        assertEquals(ReasonCode.Play.DOWNLOAD_FAILED, playFailedEvent.getReasonCode());
    }

    @Test
    public void parsePlayCanceledEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/PlayCanceled\",\n"
            + "\"type\": \"Microsoft.Communication.PlayCanceled\",\n"
            + "\"data\": {\n"
            + "\"type\": \"playCanceledEvent\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        PlayCanceled playCanceledEvent = (PlayCanceled) event;
        assertNotNull(playCanceledEvent);
        assertEquals("serverCallId", playCanceledEvent.getServerCallId());
    }
    @Test
    public void parseRecognizeCompletedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/RecognizeCompleted\",\n"
            + "\"type\": \"Microsoft.Communication.RecognizeCompleted\",\n"
            + "\"data\": {\n"
            + "\"resultInformation\": {\n"
            + "\"code\": 200,\n"
            + "\"subCode\": 0,\n"
            + "\"message\": \"Action completed successfully.\"\n"
            + "},\n"
            + "\"type\": \"recognizeCompletedEvent\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        RecognizeCompleted recognizeCompletedEvent = (RecognizeCompleted) event;
        assertNotNull(recognizeCompletedEvent);
        assertEquals("serverCallId", recognizeCompletedEvent.getServerCallId());
        assertEquals(200, recognizeCompletedEvent.getResultInformation().getCode());
        assertEquals(ReasonCode.COMPLETED_SUCCESSFULLY, recognizeCompletedEvent.getReasonCode());
    }

    @Test
    public void parseRecognizeFailedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/RecognizeCompleted\",\n"
            + "\"type\": \"Microsoft.Communication.RecognizeFailed\",\n"
            + "\"data\": {\n"
            + "\"resultInformation\": {\n"
            + "\"code\": 400,\n"
            + "\"subCode\": 8510,\n"
            + "\"message\": \"Action failed, initial silence timeout reached.\"\n"
            + "},\n"
            + "\"type\": \"recognizeFailedEvent\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        RecognizeFailed recognizeFailedEvent = (RecognizeFailed) event;
        assertNotNull(recognizeFailedEvent);
        assertEquals("serverCallId", recognizeFailedEvent.getServerCallId());
        assertEquals(400, recognizeFailedEvent.getResultInformation().getCode());
        assertEquals(ReasonCode.Recognize.INITIAL_SILENCE_TIMEOUT, recognizeFailedEvent.getReasonCode());
    }

    @Test
    public void parseRecognizeCanceledEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/RecognizeCanceled\",\n"
            + "\"type\": \"Microsoft.Communication.RecognizeCanceled\",\n"
            + "\"data\": {\n"
            + "\"type\": \"recognizeCanceledEvent\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = EventHandler.parseEvent(receivedEvent);
        assertNotNull(event);
        RecognizeCanceled recognizeCanceledEvent = (RecognizeCanceled) event;
        assertNotNull(recognizeCanceledEvent);
        assertEquals("serverCallId", recognizeCanceledEvent.getServerCallId());
    }
}
