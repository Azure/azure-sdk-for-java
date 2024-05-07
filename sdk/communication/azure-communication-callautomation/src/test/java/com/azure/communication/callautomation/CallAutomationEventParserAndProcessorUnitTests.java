// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.ChoiceResult;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.events.AnswerFailed;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CallTransferAccepted;
import com.azure.communication.callautomation.models.events.CancelAddParticipantFailed;
import com.azure.communication.callautomation.models.events.CancelAddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionStopped;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneFailed;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneReceived;
import com.azure.communication.callautomation.models.events.CreateCallFailed;
import com.azure.communication.callautomation.models.events.DialogCompleted;
import com.azure.communication.callautomation.models.events.DialogConsent;
import com.azure.communication.callautomation.models.events.DialogFailed;
import com.azure.communication.callautomation.models.events.DialogHangup;
import com.azure.communication.callautomation.models.events.DialogLanguageChange;
import com.azure.communication.callautomation.models.events.DialogSensitivityUpdate;
import com.azure.communication.callautomation.models.events.DialogStarted;
import com.azure.communication.callautomation.models.events.DialogTransfer;
import com.azure.communication.callautomation.models.events.HoldFailed;
import com.azure.communication.callautomation.models.events.ParticipantsUpdated;
import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.PlayFailed;
import com.azure.communication.callautomation.models.events.ReasonCode;
import com.azure.communication.callautomation.models.events.ReasonCode.Recognize;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.events.RecordingStateChanged;
import com.azure.communication.callautomation.models.events.RemoveParticipantFailed;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceeded;
import com.azure.communication.callautomation.models.events.SendDtmfTonesCompleted;
import com.azure.communication.callautomation.models.events.SendDtmfTonesFailed;
import com.azure.communication.callautomation.models.events.TeamsComplianceRecordingStateChanged;
import com.azure.communication.callautomation.models.events.TeamsRecordingStateChanged;
import com.azure.communication.callautomation.models.events.TranscriptionFailed;
import com.azure.communication.callautomation.models.events.TranscriptionResumed;
import com.azure.communication.callautomation.models.events.TranscriptionStarted;
import com.azure.communication.callautomation.models.events.TranscriptionStatus;
import com.azure.communication.callautomation.models.events.TranscriptionStatusDetails;
import com.azure.communication.callautomation.models.events.TranscriptionStopped;
import com.azure.communication.callautomation.models.events.TranscriptionUpdated;
import com.azure.communication.callautomation.models.events.MediaStreamingStarted;
import com.azure.communication.callautomation.models.events.MediaStreamingStopped;
import com.azure.communication.callautomation.models.events.MediaStreamingFailed;
import com.azure.communication.callautomation.models.events.MediaStreamingStatus;
import com.azure.communication.callautomation.models.events.MediaStreamingStatusDetails;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallAutomationEventParserAndProcessorUnitTests {
    static final String EVENT_PARTICIPANT_UPDATED = "{\"id\":\"61069ef9-5ca9-457f-ac36-e2bb5e8400ca\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\",\"type\":\"Microsoft.Communication.ParticipantsUpdated\",\"data\":{\"participants\":[{\"identifier\": {\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\"}}, \"isMuted\": false, \"isOnHold\": false},{\"identifier\": {\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\"}}, \"isMuted\": false, \"isOnHold\": false}],\"type\":\"participantsUpdated\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.9129474+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\"}";
    static final String EVENT_CALL_CONNECTED = "{\"id\":\"46116fb7-27e0-4a99-9478-a659c8fd4815\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/CallConnected\",\"type\":\"Microsoft.Communication.CallConnected\",\"data\":{\"type\":\"callConnected\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.8174402+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/CallConnected\"}";
    static final String EVENT_RECOGNIZE_DTMF = "[{\"id\":\"ac2cb537-2d62-48bf-909e-cc93534c4258\",\"source\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"type\":\"Microsoft.Communication.RecognizeCompleted\",\"data\":{\"eventSource\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"operationContext\":\"OperationalContextValue-1118-1049\",\"resultInformation\":{\"code\":200,\"subCode\":8533,\"message\":\"Action completed, DTMF option matched.\"},\"recognitionType\":\"dtmf\",\"dtmfResult\":{\"tones\":[\"five\", \"six\", \"pound\"]},\"choiceResult\":{\"label\":\"Marketing\"},\"callConnectionId\":\"401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"serverCallId\":\"serverCallId\",\"correlationId\":\"d4f4c1be-59d8-4850-b9bf-ee564c15839d\"},\"time\":\"2022-11-22T01:41:44.5582769+00:00\",\"specversion\":\"1.0\",\"subject\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\"}]";
    static final String EVENT_RECOGNIZE_CHOICE = "[{\"id\":\"e25b99ef-3632-45bb-96d1-d9191547ff33\",\"source\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"type\":\"Microsoft.Communication.RecognizeCompleted\",\"data\":{\"eventSource\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"operationContext\":\"OperationalContextValue-1118-1049\",\"resultInformation\":{\"code\":200,\"subCode\":8545,\"message\":\"Action completed, Recognized phrase matches a valid option.\"},\"recognitionType\":\"choices\",\"choiceResult\":{\"label\":\"Support\",\"recognizedPhrase\":\"customer help\"},\"callConnectionId\":\"401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"serverCallId\":\"serverCallId\",\"correlationId\":\"d4f4c1be-59d8-4850-b9bf-ee564c15839d\"},\"time\":\"2022-11-22T01:41:00.1967145+00:00\",\"specversion\":\"1.0\",\"subject\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\"}]";

    @Test
    public void parseEvent() {
        CallAutomationEventBase callAutomationEvent = CallAutomationEventParser.parseEvents(EVENT_PARTICIPANT_UPDATED).get(0);

        assertNotNull(callAutomationEvent);
        assertEquals(callAutomationEvent.getClass(), ParticipantsUpdated.class);
        ParticipantsUpdated participantsUpdated = (ParticipantsUpdated) callAutomationEvent;
        assertNotNull((participantsUpdated).getParticipants());
        participantsUpdated.getParticipants().forEach(participant -> {
            assertNotNull(participant);
            assertNotNull(participant.getIdentifier());
            assertNotNull(participant.isMuted());
            assertNotNull(participant.isOnHold());
        });
    }

    @Test
    public void parseEventList() {
        List<CallAutomationEventBase> callAutomationEventList = CallAutomationEventParser.parseEvents("["
            + EVENT_CALL_CONNECTED + "," + EVENT_PARTICIPANT_UPDATED + "]");

        assertNotNull(callAutomationEventList);
        assertEquals(callAutomationEventList.get(0).getClass(), CallConnected.class);
        assertEquals(callAutomationEventList.get(1).getClass(), ParticipantsUpdated.class);
        assertNotNull(callAutomationEventList.get(0).getCallConnectionId());
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
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        RecordingStateChanged recordingEvent = (RecordingStateChanged) event;
        assertNotNull(recordingEvent);
        assertEquals("serverCallId", recordingEvent.getServerCallId());
        assertEquals("recordingId", recordingEvent.getRecordingId());
        assertEquals(RecordingState.ACTIVE, recordingEvent.getRecordingState());
    }

    @Test
    public void parseTeamsComplianceRecordingStateChangedEvent() {
        String receivedEvent = "[\n"
            + "    {\n"
            + "        \"id\": \"bf59843a-888f-47ca-8d1c-885c1f5e71dc\",\n"
            + "        \"source\": \"calling/recordings/serverCallId/recordingId/recordingId/TeamsComplianceRecordingStateChanged\",\n"
            + "        \"type\": \"Microsoft.Communication.TeamsComplianceRecordingStateChanged\",\n"
            + "        \"data\": {\n"
            + "            \"type\": \"teamsComplianceRecordingStateChanged\",\n"
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
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        TeamsComplianceRecordingStateChanged recordingEvent = (TeamsComplianceRecordingStateChanged) event;
        assertNotNull(recordingEvent);
        assertEquals("serverCallId", recordingEvent.getServerCallId());
        assertEquals("recordingId", recordingEvent.getRecordingId());
        assertEquals(RecordingState.ACTIVE, recordingEvent.getRecordingState());
    }

    @Test
    public void parseTeamsRecordingStateChangedEvent() {
        String receivedEvent = "[\n"
            + "    {\n"
            + "        \"id\": \"bf59843a-888f-47ca-8d1c-885c1f5e71dc\",\n"
            + "        \"source\": \"calling/recordings/serverCallId/recordingId/recordingId/TeamsRecordingStateChanged\",\n"
            + "        \"type\": \"Microsoft.Communication.TeamsRecordingStateChanged\",\n"
            + "        \"data\": {\n"
            + "            \"type\": \"teamsRecordingStateChanged\",\n"
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
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        TeamsRecordingStateChanged recordingEvent = (TeamsRecordingStateChanged) event;
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
            + "\"type\": \"playCompleted\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        PlayCompleted playCompleted = (PlayCompleted) event;
        assertNotNull(playCompleted);
        assertEquals("serverCallId", playCompleted.getServerCallId());
        assertEquals(200, playCompleted.getResultInformation().getCode());
        assertEquals(ReasonCode.COMPLETED_SUCCESSFULLY, playCompleted.getReasonCode());
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
            + "\"type\": \"playFailed\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        PlayFailed playFailed = (PlayFailed) event;
        assertNotNull(playFailed);
        assertEquals("serverCallId", playFailed.getServerCallId());
        assertEquals(400, playFailed.getResultInformation().getCode());
        assertEquals(ReasonCode.Play.DOWNLOAD_FAILED, playFailed.getReasonCode());
    }

    @Test
    public void parsePlayCanceledEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/PlayCanceled\",\n"
            + "\"type\": \"Microsoft.Communication.PlayCanceled\",\n"
            + "\"data\": {\n"
            + "\"type\": \"playCanceled\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        PlayCanceled playCanceled = (PlayCanceled) event;
        assertNotNull(playCanceled);
        assertEquals("serverCallId", playCanceled.getServerCallId());
    }
    @Test
    public void parseRecognizeCompletedWithChoiceEvent() {
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(EVENT_RECOGNIZE_CHOICE).get(0);
        assertNotNull(event);
        RecognizeCompleted recognizeCompleted = (RecognizeCompleted) event;
        assertNotNull(recognizeCompleted);
        Optional<RecognizeResult> choiceResult = recognizeCompleted.getRecognizeResult();
        assertInstanceOf(ChoiceResult.class, choiceResult.get());
        assertEquals("serverCallId", recognizeCompleted.getServerCallId());
        assertEquals(200, recognizeCompleted.getResultInformation().getCode());
        assertEquals(Recognize.SPEECH_OPTION_MATCHED, recognizeCompleted.getReasonCode());
    }

    @Test
    public void parseRecognizeCompletedWithDtmfEvent() {
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(EVENT_RECOGNIZE_DTMF).get(0);
        assertNotNull(event);
        RecognizeCompleted recognizeCompleted = (RecognizeCompleted) event;
        Optional<RecognizeResult> dtmfResult = recognizeCompleted.getRecognizeResult();
        DtmfResult tonesResult = (DtmfResult) dtmfResult.get();
        assertInstanceOf(DtmfResult.class, dtmfResult.get());
        String tonesInString = tonesResult.convertToString();
        assertEquals(tonesInString, "56#");
        assertNotNull(recognizeCompleted);
        assertEquals("serverCallId", recognizeCompleted.getServerCallId());
        assertEquals(200, recognizeCompleted.getResultInformation().getCode());
        assertEquals(Recognize.DMTF_OPTION_MATCHED, recognizeCompleted.getReasonCode());
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
            + "\"type\": \"recognizeFailed\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        RecognizeFailed recognizeFailed = (RecognizeFailed) event;
        assertNotNull(recognizeFailed);
        assertEquals("serverCallId", recognizeFailed.getServerCallId());
        assertEquals(400, recognizeFailed.getResultInformation().getCode());
        assertEquals(ReasonCode.Recognize.INITIAL_SILENCE_TIMEOUT, recognizeFailed.getReasonCode());
    }

    @Test
    public void parseRecognizeCanceledEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/RecognizeCanceled\",\n"
            + "\"type\": \"Microsoft.Communication.RecognizeCanceled\",\n"
            + "\"data\": {\n"
            + "\"type\": \"recognizeCanceled\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        RecognizeCanceled recognizeCanceled = (RecognizeCanceled) event;
        assertNotNull(recognizeCanceled);
        assertEquals("serverCallId", recognizeCanceled.getServerCallId());
    }

    @Test
    public void parseAndProcessRemoveParticipantSucceededEvent() {
        String receivedEvent = "[{\n"
                + "\"id\": \"c3220fa3-79bd-473e-96a2-3ecb5be7d71f\",\n"
                + "\"source\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\",\n"
                + "\"type\": \"Microsoft.Communication.RemoveParticipantSucceeded\",\n"
                + "\"data\": {\n"
                + "\"operationContext\": \"context\",\n"
                + "\"participant\": {\n"
                + "\"rawId\": \"rawId\",\n"
                + "\"phoneNumber\": {\n"
                + "\"value\": \"value\"\n"
                + "}\n"
                + "},\n"
                + "\"callConnectionId\": \"callConnectionId\",\n"
                + "\"serverCallId\": \"serverCallId\",\n"
                + "\"correlationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\"\n"
                + "},\n"
                + "\"time\": \"2023-03-22T16:57:09.287755+00:00\",\n"
                + "\"specversion\": \"1.0\",\n"
                + "\"datacontenttype\": \"application/json\",\n"
                + "\"subject\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\"\n"
                + "}]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);

        RemoveParticipantSucceeded removeParticipantSucceeded = (RemoveParticipantSucceeded) event;

        assertNotNull(removeParticipantSucceeded);
        assertEquals("serverCallId", removeParticipantSucceeded.getServerCallId());
        assertEquals("callConnectionId", removeParticipantSucceeded.getCallConnectionId());
        assertEquals("rawId", removeParticipantSucceeded.getParticipant().getRawId());

        CallAutomationEventProcessor callAutomationEventProcessor = new CallAutomationEventProcessor();
        callAutomationEventProcessor.processEvents(receivedEvent);
        RemoveParticipantSucceeded eventFromProcessor = callAutomationEventProcessor.waitForEventProcessor(
            removeParticipantSucceeded.getCallConnectionId(),
            removeParticipantSucceeded.getOperationContext(),
            RemoveParticipantSucceeded.class);
        assertEquals("serverCallId", eventFromProcessor.getServerCallId());
        assertEquals("callConnectionId", eventFromProcessor.getCallConnectionId());
        assertEquals("rawId", eventFromProcessor.getParticipant().getRawId());
    }

    @Test
    public void parseAndProcessRemoveParticipantFailedEvent() {
        String receivedEvent = "[{\n"
                + "\"id\": \"c3220fa3-79bd-473e-96a2-3ecb5be7d71f\",\n"
                + "\"source\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\",\n"
                + "\"type\": \"Microsoft.Communication.RemoveParticipantFailed\",\n"
                + "\"data\": {\n"
                + "\"operationContext\": \"context\",\n"
                + "\"participant\": {\n"
                + "\"rawId\": \"rawId\",\n"
                + "\"phoneNumber\": {\n"
                + "\"value\": \"value\"\n"
                + "}\n"
                + "},\n"
                + "\"callConnectionId\": \"callConnectionId\",\n"
                + "\"serverCallId\": \"serverCallId\",\n"
                + "\"correlationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\"\n"
                + "},\n"
                + "\"time\": \"2023-03-22T16:57:09.287755+00:00\",\n"
                + "\"specversion\": \"1.0\",\n"
                + "\"datacontenttype\": \"application/json\",\n"
                + "\"subject\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\"\n"
                + "}]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        RemoveParticipantFailed removeParticipantFailed = (RemoveParticipantFailed) event;

        assertNotNull(removeParticipantFailed);
        assertEquals("serverCallId", removeParticipantFailed.getServerCallId());
        assertEquals("callConnectionId", removeParticipantFailed.getCallConnectionId());
        assertEquals("rawId", removeParticipantFailed.getParticipant().getRawId());

        CallAutomationEventProcessor callAutomationEventProcessor = new CallAutomationEventProcessor();
        callAutomationEventProcessor.waitForEventProcessorAsync(removeParticipantFailed.getCallConnectionId(),
            removeParticipantFailed.getOperationContext(), RemoveParticipantFailed.class)
                .subscribe(eventFromProcessor -> {
                    assertEquals("serverCallId", eventFromProcessor.getServerCallId());
                    assertEquals("callConnectionId", eventFromProcessor.getCallConnectionId());
                    assertEquals("rawId", eventFromProcessor.getParticipant().getRawId());
                });
        callAutomationEventProcessor.processEvents(receivedEvent);
    }

    @Test
    public void parseContinuousDtmfRecognitionToneReceivedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneReceived\",\n"
            + "      \"type\":\"Microsoft.Communication.ContinuousDtmfRecognitionToneReceived\",\n"
            + "      \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneReceived\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"DTMF tone received successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"ContinuousDtmfRecognitionToneReceived\",\n"
            + "         \"sequenceId\":1,\n"
            + "         \"tone\":\"eight\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"operationContext\":\"context\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneReceived\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        ContinuousDtmfRecognitionToneReceived continuousDtmfRecognitionToneReceived = (ContinuousDtmfRecognitionToneReceived) event;

        assertNotNull(continuousDtmfRecognitionToneReceived);
        assertEquals("serverCallId", continuousDtmfRecognitionToneReceived.getServerCallId());
        assertEquals("callConnectionId", continuousDtmfRecognitionToneReceived.getCallConnectionId());
        assertEquals("eight", continuousDtmfRecognitionToneReceived.getTone().toString());
        assertEquals(1, continuousDtmfRecognitionToneReceived.getSequenceId());
        assertEquals("correlationId", continuousDtmfRecognitionToneReceived.getCorrelationId());
        assertEquals(200, continuousDtmfRecognitionToneReceived.getResultInformation().getCode());
        assertEquals(0, continuousDtmfRecognitionToneReceived.getResultInformation().getSubCode());
        assertEquals("context", continuousDtmfRecognitionToneReceived.getOperationContext());
        assertEquals("DTMF tone received successfully.", continuousDtmfRecognitionToneReceived.getResultInformation().getMessage());
    }

    @Test
    public void parseContinuousDtmfRecognitionToneFailedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneFailed\",\n"
            + "      \"type\":\"Microsoft.Communication.ContinuousDtmfRecognitionToneFailed\",\n"
            + "      \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneFailed\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":400,\n"
            + "            \"subCode\":12323,\n"
            + "            \"message\":\"Continuous DTMF tone Couldn't be received successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"ContinuousDtmfRecognitionToneFailed\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"operationContext\":\"context\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneFailed\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        ContinuousDtmfRecognitionToneFailed continuousDtmfRecognitionToneFailed = (ContinuousDtmfRecognitionToneFailed) event;

        assertNotNull(continuousDtmfRecognitionToneFailed);
        assertEquals("serverCallId", continuousDtmfRecognitionToneFailed.getServerCallId());
        assertEquals("callConnectionId", continuousDtmfRecognitionToneFailed.getCallConnectionId());
        assertEquals("correlationId", continuousDtmfRecognitionToneFailed.getCorrelationId());
        assertEquals(400, continuousDtmfRecognitionToneFailed.getResultInformation().getCode());
        assertEquals(12323, continuousDtmfRecognitionToneFailed.getResultInformation().getSubCode());
        assertEquals("context", continuousDtmfRecognitionToneFailed.getOperationContext());
        assertEquals("Continuous DTMF tone Couldn't be received successfully.", continuousDtmfRecognitionToneFailed.getResultInformation().getMessage());
    }

    @Test
    public void parseContinuousDtmfRecognitionStoppedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionStopped\",\n"
            + "      \"type\":\"Microsoft.Communication.ContinuousDtmfRecognitionStopped\",\n"
            + "      \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionStopped\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"Continuous DTMF Recognition stopped successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"ContinuousDtmfRecognitionStopped\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"operationContext\":\"context\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionStopped\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        ContinuousDtmfRecognitionStopped continuousDtmfRecognitionStopped = (ContinuousDtmfRecognitionStopped) event;

        assertNotNull(continuousDtmfRecognitionStopped);
        assertEquals("serverCallId", continuousDtmfRecognitionStopped.getServerCallId());
        assertEquals("callConnectionId", continuousDtmfRecognitionStopped.getCallConnectionId());
        assertEquals("correlationId", continuousDtmfRecognitionStopped.getCorrelationId());
        assertEquals(200, continuousDtmfRecognitionStopped.getResultInformation().getCode());
        assertEquals(0, continuousDtmfRecognitionStopped.getResultInformation().getSubCode());
        assertEquals("context", continuousDtmfRecognitionStopped.getOperationContext());
        assertEquals("Continuous DTMF Recognition stopped successfully.", continuousDtmfRecognitionStopped.getResultInformation().getMessage());
    }

    @Test
    public void parseSendDtmfTonesCompletedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/SendDtmfTonesCompleted\",\n"
            + "      \"type\":\"Microsoft.Communication.SendDtmfTonesCompleted\",\n"
            + "      \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/SendDtmfTonesCompleted\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"Send DTMF completed successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"SendDtmfTonesCompleted\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"operationContext\":\"context\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/SendDtmfTonesCompleted\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        SendDtmfTonesCompleted sendDtmfTonesCompleted = (SendDtmfTonesCompleted) event;

        assertNotNull(sendDtmfTonesCompleted);
        assertEquals("serverCallId", sendDtmfTonesCompleted.getServerCallId());
        assertEquals("callConnectionId", sendDtmfTonesCompleted.getCallConnectionId());
        assertEquals("correlationId", sendDtmfTonesCompleted.getCorrelationId());
        assertEquals(200, sendDtmfTonesCompleted.getResultInformation().getCode());
        assertEquals(0, sendDtmfTonesCompleted.getResultInformation().getSubCode());
        assertEquals("context", sendDtmfTonesCompleted.getOperationContext());
        assertEquals("Send DTMF completed successfully.", sendDtmfTonesCompleted.getResultInformation().getMessage());
    }

    @Test
    public void parseSendDtmfTonesFailedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/SendDtmfTonesFailed\",\n"
            + "      \"type\":\"Microsoft.Communication.SendDtmfTonesFailed\",\n"
            + "      \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/SendDtmfTonesFailed\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"Send DTMF couldn't be completed successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"SendDtmfTonesFailed\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"operationContext\":\"context\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/SendDtmfTonesFailed\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        SendDtmfTonesFailed sendDtmfTonesFailed = (SendDtmfTonesFailed) event;

        assertNotNull(sendDtmfTonesFailed);
        assertEquals("serverCallId", sendDtmfTonesFailed.getServerCallId());
        assertEquals("callConnectionId", sendDtmfTonesFailed.getCallConnectionId());
        assertEquals("correlationId", sendDtmfTonesFailed.getCorrelationId());
        assertEquals(200, sendDtmfTonesFailed.getResultInformation().getCode());
        assertEquals(0, sendDtmfTonesFailed.getResultInformation().getSubCode());
        assertEquals("context", sendDtmfTonesFailed.getOperationContext());
        assertEquals("Send DTMF couldn't be completed successfully.", sendDtmfTonesFailed.getResultInformation().getMessage());
    }

    @Test
    public void parseTransferAccptedEvent() {
        String receivedEvent = "[\n"
                + "    {\n"
                + "        \"id\": \"91f8b34b-383c-431d-9fa5-79d39fad9300\",\n"
                + "        \"source\": \"calling/callConnections/411f0b00-dc73-4528-a9e6-968ba983d2a1\",\n"
                + "        \"type\": \"Microsoft.Communication.CallTransferAccepted\",\n"
                + "        \"data\": {\n"
                + "            \"resultInformation\": {\n"
                + "                \"code\": 200,\n"
                + "                \"subCode\": 7015,\n"
                + "                \"message\": \"The transfer operation completed successfully.\"\n"
                + "            },\n"
                + "            \"transferTarget\": {\n"
                + "                \"rawId\": \"8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2f-2234-655d-573a0d00443e\",\n"
                + "                \"kind\": \"communicationUser\",\n"
                + "                \"communicationUser\": {\n"
                + "                    \"id\": \"8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2f-2234-655d-573a0d00443e\"\n"
                + "                }\n"
                + "            },\n"
                + "            \"transferee\": {\n"
                + "                \"rawId\": \"8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2e-e2b4-655d-573a0d004434\",\n"
                + "                \"kind\": \"communicationUser\",\n"
                + "                \"communicationUser\": {\n"
                + "                    \"id\": \"8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2e-e2b4-655d-573a0d004434\"\n"
                + "                }\n"
                + "            },\n"
                + "            \"callConnectionId\": \"411f0b00-dc73-4528-a9e6-968ba983d2a1\",\n"
                + "            \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDEuY29udi5za3lwZS5jb20vY29udi8yZWtNYmJRN3VVbUY1RDJERFdITWJnP2k9MTUmZT02MzgyNTMwMzY2ODQ5NzkwMDI=\",\n"
                + "            \"correlationId\": \"be43dd55-38e9-4de8-9d75-e20b6b32744f\"\n"
                + "        },\n"
                + "        \"time\": \"2023-07-19T18:31:16.6795146+00:00\",\n"
                + "        \"specversion\": \"1.0\",\n"
                + "        \"datacontenttype\": \"application/json\",\n"
                + "        \"subject\": \"calling/callConnections/411f0b00-dc73-4528-a9e6-968ba983d2a1\"\n"
                + "    }\n"
                + "]";
        CallTransferAccepted event = (CallTransferAccepted) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDEuY29udi5za3lwZS5jb20vY29udi8yZWtNYmJRN3VVbUY1RDJERFdITWJnP2k9MTUmZT02MzgyNTMwMzY2ODQ5NzkwMDI=", event.getServerCallId());
        assertEquals("411f0b00-dc73-4528-a9e6-968ba983d2a1", event.getCallConnectionId());
        assertEquals("be43dd55-38e9-4de8-9d75-e20b6b32744f", event.getCorrelationId());
        assertEquals("The transfer operation completed successfully.", event.getResultInformation().getMessage());
        assertEquals("8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2f-2234-655d-573a0d00443e", event.getTransferTarget().getRawId());
        assertEquals("8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2e-e2b4-655d-573a0d004434", event.getTransferee().getRawId());

        CallAutomationEventProcessor callAutomationEventProcessor = new CallAutomationEventProcessor();
        callAutomationEventProcessor.processEvents(receivedEvent);
        CallAutomationEventBase eventComing = callAutomationEventProcessor.waitForEventProcessor(eventFromProcessor -> Objects.equals(event.getServerCallId(), eventFromProcessor.getServerCallId())
            && Objects.equals(event.getCallConnectionId(), eventFromProcessor.getCallConnectionId()));

        assertNotNull(eventComing);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDEuY29udi5za3lwZS5jb20vY29udi8yZWtNYmJRN3VVbUY1RDJERFdITWJnP2k9MTUmZT02MzgyNTMwMzY2ODQ5NzkwMDI=", eventComing.getServerCallId());
        assertEquals("411f0b00-dc73-4528-a9e6-968ba983d2a1", eventComing.getCallConnectionId());
        assertEquals("be43dd55-38e9-4de8-9d75-e20b6b32744f", eventComing.getCorrelationId());
        assertEquals("The transfer operation completed successfully.", ((CallTransferAccepted) eventComing).getResultInformation().getMessage());
        assertEquals("8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2f-2234-655d-573a0d00443e", ((CallTransferAccepted) eventComing).getTransferTarget().getRawId());
        assertEquals("8:acs:3afbe310-c6d9-4b6f-a11e-c2aeb352f207_0000001a-0f2e-e2b4-655d-573a0d004434", ((CallTransferAccepted) eventComing).getTransferee().getRawId());
    }

    @Test
    public void parseCancelAddParticipantSucceededEvent() {
        String receivedEvent = "[{\n"
                + "\"id\": \"c3220fa3-79bd-473e-96a2-3ecb5be7d71f\",\n"
                + "\"source\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\",\n"
                + "\"type\": \"Microsoft.Communication.CancelAddParticipantSucceeded\",\n"
                + "\"data\": {\n"
                + "\"operationContext\": \"context\",\n"
                + "\"callConnectionId\": \"callConnectionId\",\n"
                + "\"serverCallId\": \"serverCallId\",\n"
                + "\"invitationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\",\n"
                + "\"correlationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\"\n"
                + "},\n"
                + "\"time\": \"2023-03-22T16:57:09.287755+00:00\",\n"
                + "\"specversion\": \"1.0\",\n"
                + "\"datacontenttype\": \"application/json\",\n"
                + "\"subject\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\"\n"
                + "}]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);

        CancelAddParticipantSucceeded cancelAddParticipantSucceeded = (CancelAddParticipantSucceeded) event;

        assertNotNull(cancelAddParticipantSucceeded);
        assertEquals("serverCallId", cancelAddParticipantSucceeded.getServerCallId());
        assertEquals("callConnectionId", cancelAddParticipantSucceeded.getCallConnectionId());
        assertEquals("b880bd5a-1916-470a-b43d-aabf3caff91c", cancelAddParticipantSucceeded.getInvitationId());
    }

    @Test
    public void parseCancelAddParticipantFailedEvent() {
        String receivedEvent = "[{\n"
                + "\"id\": \"c3220fa3-79bd-473e-96a2-3ecb5be7d71f\",\n"
                + "\"source\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\",\n"
                + "\"type\": \"Microsoft.Communication.CancelAddParticipantFailed\",\n"
                + "\"data\": {\n"
                + "\"operationContext\": \"context\",\n"
                + "\"callConnectionId\": \"callConnectionId\",\n"
                + "\"serverCallId\": \"serverCallId\",\n"
                + "\"invitationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\",\n"
                + "\"correlationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\"\n"
                + "},\n"
                + "\"time\": \"2023-03-22T16:57:09.287755+00:00\",\n"
                + "\"specversion\": \"1.0\",\n"
                + "\"datacontenttype\": \"application/json\",\n"
                + "\"subject\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\"\n"
                + "}]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        CancelAddParticipantFailed cancelAddParticipantFailed = (CancelAddParticipantFailed) event;

        assertNotNull(cancelAddParticipantFailed);
        assertEquals("serverCallId", cancelAddParticipantFailed.getServerCallId());
        assertEquals("callConnectionId", cancelAddParticipantFailed.getCallConnectionId());
        assertEquals("b880bd5a-1916-470a-b43d-aabf3caff91c", cancelAddParticipantFailed.getInvitationId());

        CallAutomationEventProcessor callAutomationEventProcessor = new CallAutomationEventProcessor();
        callAutomationEventProcessor.attachOngoingEventProcessor(cancelAddParticipantFailed.getCallConnectionId(),
            eventToHandle -> {
                assertEquals("serverCallId", eventToHandle.getServerCallId());
                assertEquals("callConnectionId", eventToHandle.getCallConnectionId());
                assertEquals("b880bd5a-1916-470a-b43d-aabf3caff91c", eventToHandle.getInvitationId());
            }, CancelAddParticipantFailed.class);
        callAutomationEventProcessor.processEvents(receivedEvent);
        callAutomationEventProcessor.detachOngoingEventProcessor(cancelAddParticipantFailed.getCallConnectionId(), CancelAddParticipantFailed.class);
    }

    @Test
    public void parseDialogCompletedEvent() {
        String receivedEvent = "\n"
            + "  {\n"
            + "    \"id\": \"91f8b34b-383c-431d-9fa5-79d39fad9300\",\n"
            + "    \"source\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogCompleted\",\n"
            + "    \"data\": {\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==\",\n"
            + "        \"correlationId\": \"fb2d738b-d9cb-496c-8be6-416aeecd4d96\"\n"
            + "    },\n"
            + "    \"time\": \"2023-07-27T07:48:56.0766803+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\"\n"
            + "  }]";
        DialogCompleted event = (DialogCompleted) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==", event.getServerCallId());
        assertEquals("441fbf00-5b21-4beb-bd4d-fd665ca4b65f", event.getCallConnectionId());
        assertEquals("fb2d738b-d9cb-496c-8be6-416aeecd4d96", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseDialogStartedEvent() {
        String receivedEvent = "[{\n"
            + "    \"id\": \"91f8b34b-383c-431d-9fa5-79d39fad9300\",\n"
            + "    \"source\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogStarted\",\n"
            + "    \"data\": {\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==\",\n"
            + "        \"correlationId\": \"fb2d738b-d9cb-496c-8be6-416aeecd4d96\"\n"
            + "    },\n"
            + "    \"time\": \"2023-07-27T07:48:55.2765079+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\"\n"
            + "}]";
        DialogStarted event = (DialogStarted) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==", event.getServerCallId());
        assertEquals("441fbf00-5b21-4beb-bd4d-fd665ca4b65f", event.getCallConnectionId());
        assertEquals("fb2d738b-d9cb-496c-8be6-416aeecd4d96", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseDialogFailedEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"91f8b34b-383c-431d-9fa5-79d39fad9300\",\n"
            + "    \"source\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogFailed\",\n"
            + "    \"data\": {\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Operation Failed\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==\",\n"
            + "        \"correlationId\": \"fb2d738b-d9cb-496c-8be6-416aeecd4d96\"\n"
            + "    },\n"
            + "    \"time\": \"2023-07-27T07:48:55.5109544+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\"\n"
            + "}]";
        DialogFailed event = (DialogFailed) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==", event.getServerCallId());
        assertEquals("441fbf00-5b21-4beb-bd4d-fd665ca4b65f", event.getCallConnectionId());
        assertEquals("fb2d738b-d9cb-496c-8be6-416aeecd4d96", event.getCorrelationId());
        assertEquals("Operation Failed", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseDialogLanguageChangeEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogLanguageChange\",\n"
            + "    \"data\": {\n"
            + "        \"selectedLanguage\": \"eng-USA\",\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"ivrContext\": \"Sanitized\",\n"
            + "        \"callConnectionId\": \"441fbf00-5b21-4beb-bd4d-fd665ca4b65f\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==\",\n"
            + "        \"correlationId\": \"fb2d738b-d9cb-496c-8be6-416aeecd4d96\"\n"
            + "    },\n"
            + "    \"time\": \"2023-07-27T07:48:55.5109544+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/441fbf00-5b21-4beb-bd4d-fd665ca4b65f\"\n"
            + "}]";

        DialogLanguageChange event = (DialogLanguageChange) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzc2MtMDkuY29udi5za3lwZS5jb20vY29udi9ubUpvbG9wMUhrdWhDVllYNUlxTTZnP2k9OCZlPTYzODI1NjEzMTYxNDIwOTA2MQ==", event.getServerCallId());
        assertEquals("441fbf00-5b21-4beb-bd4d-fd665ca4b65f", event.getCallConnectionId());
        assertEquals("fb2d738b-d9cb-496c-8be6-416aeecd4d96", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
        assertEquals("Sanitized", event.getIvrContext());
    }

    @Test
    public void parseDialogSensitivityUpdateEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogSensitivityUpdate\",\n"
            + "    \"data\": {\n"
            + "        \"sensitiveMask\": false,\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";
        DialogSensitivityUpdate event = (DialogSensitivityUpdate) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseDialogConsentEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogConsent\",\n"
            + "    \"data\": {\n"
            + "        \"sensitiveMask\": false,\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";
        DialogConsent event = (DialogConsent) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseDialogHangupEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogHangup\",\n"
            + "    \"data\": {\n"
            + "        \"sensitiveMask\": false,\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";
        DialogHangup event = (DialogHangup) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseDialogTransferEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.DialogTransfer\",\n"
            + "    \"data\": {\n"
            + "        \"sensitiveMask\": false,\n"
            + "        \"operationContext\": \"context\",\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Success Result\"\n"
            + "        },\n"
            + "        \"dialogInputType\": \"powerVirtualAgents\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";
        DialogTransfer event = (DialogTransfer) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Success Result", event.getResultInformation().getMessage());
        assertEquals("92e08834-b6ee-4ede-8956-9fefa27a691c", event.getDialogId());
        assertEquals("powerVirtualAgents", event.getDialogInputType().toString());
    }

    @Test
    public void parseTranscriptionStartedEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.TranscriptionStarted\",\n"
            + "    \"data\": {\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Transcription started successfully.\"\n"
            + "        },\n"
            + "        \"transcriptionUpdate\": {\n"
            + "            \"transcriptionStatus\": \"transcriptionStarted\",\n"
            + "            \"transcriptionStatusDetails\": \"subscriptionStarted\"\n"
            + "        },\n"
            + "        \"version\": \"2023-01-15-preview\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\",\n"
            + "        \"publicEventType\": \"Microsoft.Communication.TranscriptionStarted\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";


        TranscriptionStarted event = (TranscriptionStarted) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Transcription started successfully.", event.getResultInformation().getMessage());

        assertNotNull(event.getTranscriptionUpdateResult());
        assertEquals(TranscriptionStatus.TRANSCRIPTION_STARTED, event.getTranscriptionUpdateResult().getTranscriptionStatus());
        assertEquals(TranscriptionStatusDetails.SUBSCRIPTION_STARTED, event.getTranscriptionUpdateResult().getTranscriptionStatusDetails());
    }
    @Test
    public void parseTranscriptionResumedEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.TranscriptionResumed\",\n"
            + "    \"data\": {\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Transcription resumed.\"\n"
            + "        },\n"
            + "        \"transcriptionUpdate\": {\n"
            + "            \"transcriptionStatus\": \"transcriptionResumed\",\n"
            + "            \"transcriptionStatusDetails\": \"streamConnectionReestablished\"\n"
            + "        },\n"
            + "        \"version\": \"2023-01-15-preview\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\",\n"
            + "        \"publicEventType\": \"icrosoft.Communication.TranscriptionResumed\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";


        TranscriptionResumed event = (TranscriptionResumed) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Transcription resumed.", event.getResultInformation().getMessage());

        assertNotNull(event.getTranscriptionUpdateResult());
        assertEquals(TranscriptionStatus.TRANSCRIPTION_RESUMED, event.getTranscriptionUpdateResult().getTranscriptionStatus());
        assertEquals(TranscriptionStatusDetails.STREAM_CONNECTION_REESTABLISHED, event.getTranscriptionUpdateResult().getTranscriptionStatusDetails());
    }
    @Test
    public void parseTranscriptionStoppedEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.TranscriptionStopped\",\n"
            + "    \"data\": {\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Transcription stopped.\"\n"
            + "        },\n"
            + "        \"transcriptionUpdate\": {\n"
            + "            \"transcriptionStatus\": \"transcriptionStopped\",\n"
            + "            \"transcriptionStatusDetails\": \"subscriptionStopped\"\n"
            + "        },\n"
            + "        \"version\": \"2023-01-15-preview\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\",\n"
            + "        \"publicEventType\": \"icrosoft.Communication.TranscriptionStopped\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";


        TranscriptionStopped event = (TranscriptionStopped) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Transcription stopped.", event.getResultInformation().getMessage());

        assertNotNull(event.getTranscriptionUpdateResult());
        assertEquals(TranscriptionStatus.TRANSCRIPTION_STOPPED, event.getTranscriptionUpdateResult().getTranscriptionStatus());
        assertEquals(TranscriptionStatusDetails.SUBSCRIPTION_STOPPED, event.getTranscriptionUpdateResult().getTranscriptionStatusDetails());
    }
    @Test
    public void parseTranscriptionFailedEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.TranscriptionFailed\",\n"
            + "    \"data\": {\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 500,\n"
            + "            \"subCode\": 9999,\n"
            + "            \"message\": \"Unknown internal server error.\"\n"
            + "        },\n"
            + "        \"transcriptionUpdate\": {\n"
            + "            \"transcriptionStatus\": \"transcriptionFailed\",\n"
            + "            \"transcriptionStatusDetails\": \"unspecifiedError\"\n"
            + "        },\n"
            + "        \"version\": \"2023-01-15-preview\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\",\n"
            + "        \"publicEventType\": \"icrosoft.Communication.TranscriptionFailed\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";


        TranscriptionFailed event = (TranscriptionFailed) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Unknown internal server error.", event.getResultInformation().getMessage());

        assertNotNull(event.getTranscriptionUpdateResult());
        assertEquals(TranscriptionStatus.TRANSCRIPTION_FAILED, event.getTranscriptionUpdateResult().getTranscriptionStatus());
        assertEquals(TranscriptionStatusDetails.UNSPECIFIED_ERROR, event.getTranscriptionUpdateResult().getTranscriptionStatusDetails());
    }


    @Test
    public void parseTranscriptionUpdatedEvent() {
        String receivedEvent = "["
            + " {\n"
            + "    \"id\": \"Sanitized\",\n"
            + "    \"source\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "    \"type\": \"Microsoft.Communication.TranscriptionUpdated\",\n"
            + "    \"data\": {\n"
            + "        \"resultInformation\": {\n"
            + "            \"code\": 200,\n"
            + "            \"subCode\": 0,\n"
            + "            \"message\": \"Transcription updated.\"\n"
            + "        },\n"
            + "        \"transcriptionUpdate\": {\n"
            + "            \"transcriptionStatus\": \"transcriptionLocaleUpdated\",\n"
            + "            \"transcriptionStatusDetails\": \"subscriptionStarted\"\n"
            + "        },\n"
            + "        \"version\": \"2023-01-15-preview\",\n"
            + "        \"dialogId\": \"92e08834-b6ee-4ede-8956-9fefa27a691c\",\n"
            + "        \"callConnectionId\": \"491f1300-0c70-4f8c-97b4-94474f2a371b\",\n"
            + "        \"serverCallId\": \"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==\",\n"
            + "        \"correlationId\": \"ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5\",\n"
            + "        \"publicEventType\": \"Microsoft.Communication.TranscriptionUpdated\"\n"
            + "    },\n"
            + "    \"time\": \"2023-08-02T08:44:58.9826643+00:00\",\n"
            + "    \"specversion\": \"1.0\",\n"
            + "    \"datacontenttype\": \"application/json\",\n"
            + "    \"subject\": \"calling/callConnections/491f1300-0c70-4f8c-97b4-94474f2a371b\"\n"
            + "}]";


        TranscriptionUpdated event = (TranscriptionUpdated) CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LXVzd2UtMDUtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9zaXBuZWVRSGIwYUN1cmNfbmF4SjB3P2k9MTAtNjAtMzAtMTI1JmU9NjM4MjY1MTkwMDQ1MzY2NjYx==", event.getServerCallId());
        assertEquals("491f1300-0c70-4f8c-97b4-94474f2a371b", event.getCallConnectionId());
        assertEquals("ccadc1b4-7ea5-4d74-aebe-2c37ddc742a5", event.getCorrelationId());
        assertEquals("Transcription updated.", event.getResultInformation().getMessage());

        assertNotNull(event.getTranscriptionUpdateResult());
        assertEquals(TranscriptionStatus.TRANSCRIPTION_LOCALE_UPDATED, event.getTranscriptionUpdateResult().getTranscriptionStatus());
        assertEquals(TranscriptionStatusDetails.SUBSCRIPTION_STARTED, event.getTranscriptionUpdateResult().getTranscriptionStatusDetails());
    }

    @Test
    public void parseAnswerFailedEvent() {
        String receivedEvent = "[{\n"
                + "\"id\": \"c3220fa3-79bd-473e-96a2-3ecb5be7d71f\",\n"
                + "\"source\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\",\n"
                + "\"type\": \"Microsoft.Communication.AnswerFailed\",\n"
                + "\"data\": {\n"
                + "\"operationContext\": \"context\",\n"
                + "\"callConnectionId\": \"callConnectionId\",\n"
                + "\"serverCallId\": \"serverCallId\",\n"
                + "\"correlationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\"\n"
                + "},\n"
                + "\"time\": \"2023-03-22T16:57:09.287755+00:00\",\n"
                + "\"specversion\": \"1.0\",\n"
                + "\"datacontenttype\": \"application/json\",\n"
                + "\"subject\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\"\n"
                + "}]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        AnswerFailed answerFailed = (AnswerFailed) event;

        assertNotNull(answerFailed);
        assertEquals("serverCallId", answerFailed.getServerCallId());
        assertEquals("callConnectionId", answerFailed.getCallConnectionId());

        CallAutomationEventProcessor callAutomationEventProcessor = new CallAutomationEventProcessor();
        callAutomationEventProcessor.attachOngoingEventProcessor(answerFailed.getCallConnectionId(),
            eventToHandle -> {
                assertEquals("serverCallId", eventToHandle.getServerCallId());
                assertEquals("callConnectionId", eventToHandle.getCallConnectionId());
            }, AnswerFailed.class);
        callAutomationEventProcessor.processEvents(receivedEvent);
        callAutomationEventProcessor.detachOngoingEventProcessor(answerFailed.getCallConnectionId(), AnswerFailed.class);
    }

    @Test
    public void parseCreateCallFailedEvent() {
        String receivedEvent = "[{\n"
                + "\"id\": \"c3220fa3-79bd-473e-96a2-3ecb5be7d71f\",\n"
                + "\"source\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\",\n"
                + "\"type\": \"Microsoft.Communication.CreateCallFailed\",\n"
                + "\"data\": {\n"
                + "\"operationContext\": \"context\",\n"
                + "\"callConnectionId\": \"callConnectionId\",\n"
                + "\"serverCallId\": \"serverCallId\",\n"
                + "\"correlationId\": \"b880bd5a-1916-470a-b43d-aabf3caff91c\"\n"
                + "},\n"
                + "\"time\": \"2023-03-22T16:57:09.287755+00:00\",\n"
                + "\"specversion\": \"1.0\",\n"
                + "\"datacontenttype\": \"application/json\",\n"
                + "\"subject\": \"calling/callConnections/421f3500-f5de-4c12-bf61-9e2641433687\"\n"
                + "}]";

        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        CreateCallFailed createCallFailed = (CreateCallFailed) event;

        assertNotNull(createCallFailed);
        assertEquals("serverCallId", createCallFailed.getServerCallId());
        assertEquals("callConnectionId", createCallFailed.getCallConnectionId());

        CallAutomationEventProcessor callAutomationEventProcessor = new CallAutomationEventProcessor();
        callAutomationEventProcessor.attachOngoingEventProcessor(createCallFailed.getCallConnectionId(),
            eventToHandle -> {
                assertEquals("serverCallId", eventToHandle.getServerCallId());
                assertEquals("callConnectionId", eventToHandle.getCallConnectionId());
            }, CreateCallFailed.class);
        callAutomationEventProcessor.processEvents(receivedEvent);
        callAutomationEventProcessor.detachOngoingEventProcessor(createCallFailed.getCallConnectionId(), CreateCallFailed.class);
    }

    @Test
    public void parseHoldFailedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\": \"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "\"source\": \"calling/callConnections/callConnectionId/HoldFailed\",\n"
            + "\"type\": \"Microsoft.Communication.HoldFailed\",\n"
            + "\"data\": {\n"
            + "\"resultInformation\": {\n"
            + "\"code\": 400,\n"
            + "\"subCode\": 8536,\n"
            + "\"message\": \"Action failed, file could not be downloaded.\"\n"
            + "},\n"
            + "\"type\": \"HoldFailed\",\n"
            + "\"callConnectionId\": \"callConnectionId\",\n"
            + "\"serverCallId\": \"serverCallId\",\n"
            + "\"correlationId\": \"correlationId\"\n"
            + "},\n"
            + "\"time\": \"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "\"specversion\": \"1.0\",\n"
            + "\"datacontenttype\": \"application/json\",\n"
            + "\"subject\": \"calling/callConnections/callConnectionId\"\n"
            + "}]";
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        HoldFailed holdFailed = (HoldFailed) event;
        assertNotNull(holdFailed);
        assertEquals("correlationId", holdFailed.getCorrelationId());
        assertEquals("serverCallId", holdFailed.getServerCallId());
        assertEquals(400, holdFailed.getResultInformation().getCode());
        assertEquals(ReasonCode.Play.DOWNLOAD_FAILED, holdFailed.getReasonCode());
    }

    @Test
    public void parseMediaStreamingStartedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\":\"d13c62c5-721e-44b9-a680-9866c33db7e7\",\n"
            + "\"source\":\"calling/callConnections/4c1f5600-a9c6-4343-8979-b638a98de98f\",\n"
            + "\"type\":\"Microsoft.Communication.MediaStreamingStarted\",\n"
            + "\"data\":{\"eventSource\":\"calling/callConnections/4c1f5600-a9c6-4343-8979-b638a98de98f\",\n"
            + "\"operationContext\":\"startMediaStreamingContext\",\n"
            + "\"resultInformation\":{\"code\":200,\"subCode\":0,\"message\":\"Action completed successfully.\"},\n"
            + "\"mediaStreamingUpdate\":{\"contentType\":\"Audio\",\n"
            + "\"mediaStreamingStatus\":\"mediaStreamingStarted\",\n"
            + "\"mediaStreamingStatusDetails\":\"subscriptionStarted\"},\n"
            + "\"version\":\"2024-06-15-preview\",\n"
            + "\"callConnectionId\":\"4c1f5600-a9c6-4343-8979-b638a98de98f\",\n"
            + "\"serverCallId\":\"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LW1hc28tMDEtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9LTTQteUZBUmhVYXN3T1RqbklPSXZnP2k9MTAtMTI4LTk1LTUyJmU9NjM4NTAwMTgzOTYwNzY2MzQ0\",\n"
            + "\"correlationId\":\"30f0ad34-d615-4bf3-8476-5630ae7fc3db\",\n"
            + "\"publicEventType\":\"Microsoft.Communication.MediaStreamingStarted\"},\n"
            + "\"time\":\"2024-05-02T11:20:42.9110236+00:00\",\n"
            + "\"specversion\":\"1.0\",\n"
            + "\"datacontenttype\":\"application/json\",\n"
            + "\"subject\":\"calling/callConnections/4c1f5600-a9c6-4343-8979-b638a98de98f\"}]";

        MediaStreamingStarted event = (MediaStreamingStarted) CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LW1hc28tMDEtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9LTTQteUZBUmhVYXN3T1RqbklPSXZnP2k9MTAtMTI4LTk1LTUyJmU9NjM4NTAwMTgzOTYwNzY2MzQ0", event.getServerCallId());
        assertEquals("4c1f5600-a9c6-4343-8979-b638a98de98f", event.getCallConnectionId());
        assertEquals("30f0ad34-d615-4bf3-8476-5630ae7fc3db", event.getCorrelationId());
        assertEquals("Action completed successfully.", event.getResultInformation().getMessage());

        assertNotNull(event.getMediaStreamingUpdateResult());
        assertEquals(MediaStreamingStatus.MEDIA_STREAMING_STARTED, event.getMediaStreamingUpdateResult().getMediaStreamingStatus());
        assertEquals(MediaStreamingStatusDetails.SUBSCRIPTION_STARTED, event.getMediaStreamingUpdateResult().getMediaStreamingStatusDetails());
    }

    @Test
    public void parseMediaStreamingStoppedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\":\"41039554-9475-491a-875b-08d23c5d0e75\",\n"
            + "\"source\":\"calling/callConnections/4c1f5600-a9c6-4343-8979-b638a98de98f\",\n"
            + "\"type\":\"Microsoft.Communication.MediaStreamingStopped\",\n"
            + "\"data\":{\"eventSource\":\"calling/callConnections/4c1f5600-a9c6-4343-8979-b638a98de98f\",\n"
            + "\"operationContext\":\"startMediaStreamingContext\",\n"
            + "\"resultInformation\":{\"code\":200,\"subCode\":0,\"message\":\"Action completed successfully.\"},\n"
            + "\"mediaStreamingUpdate\":{\"contentType\":\"Audio\",\"mediaStreamingStatus\":\"mediaStreamingStopped\",\n"
            + "\"mediaStreamingStatusDetails\":\"subscriptionStopped\"},\n"
            + "\"version\":\"2024-06-15-preview\",\"callConnectionId\":\"4c1f5600-a9c6-4343-8979-b638a98de98f\",\n"
            + "\"serverCallId\":\"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LW1hc28tMDEtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9LTTQteUZBUmhVYXN3T1RqbklPSXZnP2k9MTAtMTI4LTk1LTUyJmU9NjM4NTAwMTgzOTYwNzY2MzQ0\",\n"
            + "\"correlationId\":\"30f0ad34-d615-4bf3-8476-5630ae7fc3db\",\n"
            + "\"publicEventType\":\"Microsoft.Communication.MediaStreamingStopped\"},\n"
            + "\"time\":\"2024-05-02T11:21:10.0261068+00:00\",\"specversion\":\"1.0\",\n"
            + "\"datacontenttype\":\"application/json\",\n"
            + "\"subject\":\"calling/callConnections/4c1f5600-a9c6-4343-8979-b638a98de98f\"}]";

        MediaStreamingStopped event = (MediaStreamingStopped) CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LW1hc28tMDEtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi9LTTQteUZBUmhVYXN3T1RqbklPSXZnP2k9MTAtMTI4LTk1LTUyJmU9NjM4NTAwMTgzOTYwNzY2MzQ0", event.getServerCallId());
        assertEquals("4c1f5600-a9c6-4343-8979-b638a98de98f", event.getCallConnectionId());
        assertEquals("30f0ad34-d615-4bf3-8476-5630ae7fc3db", event.getCorrelationId());
        assertEquals("Action completed successfully.", event.getResultInformation().getMessage());

        assertNotNull(event.getMediaStreamingUpdateResult());
        assertEquals(MediaStreamingStatus.MEDIA_STREAMING_STOPPED, event.getMediaStreamingUpdateResult().getMediaStreamingStatus());
        assertEquals(MediaStreamingStatusDetails.SUBSCRIPTION_STOPPED, event.getMediaStreamingUpdateResult().getMediaStreamingStatusDetails());
    }
    
    @Test
    public void parseMediaStreamingFailedEvent() {
        String receivedEvent = "[{\n"
            + "\"id\":\"a9bb7545-8f87-42aa-85d0-d7120dbe2414\",\n"
            + "\"source\":\"calling/callConnections/761f5600-43ab-48a0-bbad-ecc5ad5b15bb\",\n"
            + "\"type\":\"Microsoft.Communication.MediaStreamingFailed\",\n"
            + "\"data\":{\"eventSource\":\"calling/callConnections/761f5600-43ab-48a0-bbad-ecc5ad5b15bb\",\n"
            + "\"operationContext\":\"startMediaStreamingContext\",\n"
            + "\"resultInformation\":{\"code\":500,\"subCode\":8603,\n"
            + "\"message\":\"Action failed, not able to establish websocket connection.\"},\n"
            + "\"mediaStreamingUpdate\":{\"contentType\":\"Audio\",\n"
            + "\"mediaStreamingStatus\":\"mediaStreamingFailed\",\n"
            + "\"mediaStreamingStatusDetails\":\"streamConnectionUnsuccessful\"},\n"
            + "\"version\":\"2024-06-15-preview\",\"callConnectionId\":\"761f5600-43ab-48a0-bbad-ecc5ad5b15bb\",\n"
            + "\"serverCallId\":\"aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LW1hc28tMDQtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi94MVdMX0p3NnlVaW1aOEkzVi1MN3hnP2k9MTAtMTI4LTg0LTE3MSZlPTYzODQ5NzU2ODQ3MzUxNzU3Mg==\",\n"
            + "\"correlationId\":\"6032c474-201d-4ad1-8900-f92a595a6d94\",\n"
            + "\"publicEventType\":\"Microsoft.Communication.MediaStreamingFailed\"},\n"
            + "\"time\":\"2024-05-02T12:38:31.242039+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\n"
            + "\"subject\":\"calling/callConnections/761f5600-43ab-48a0-bbad-ecc5ad5b15bb\"}]";

        MediaStreamingFailed event = (MediaStreamingFailed) CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        assertEquals("aHR0cHM6Ly9hcGkuZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252LW1hc28tMDQtcHJvZC1ha3MuY29udi5za3lwZS5jb20vY29udi94MVdMX0p3NnlVaW1aOEkzVi1MN3hnP2k9MTAtMTI4LTg0LTE3MSZlPTYzODQ5NzU2ODQ3MzUxNzU3Mg==", event.getServerCallId());
        assertEquals("761f5600-43ab-48a0-bbad-ecc5ad5b15bb", event.getCallConnectionId());
        assertEquals("6032c474-201d-4ad1-8900-f92a595a6d94", event.getCorrelationId());
        assertEquals("Action failed, not able to establish websocket connection.", event.getResultInformation().getMessage());

        assertNotNull(event.getMediaStreamingUpdateResult());
        assertEquals(MediaStreamingStatus.MEDIA_STREAMING_FAILED, event.getMediaStreamingUpdateResult().getMediaStreamingStatus());
        assertEquals(MediaStreamingStatusDetails.STREAM_CONNECTION_UNSUCCESSFUL, event.getMediaStreamingUpdateResult().getMediaStreamingStatusDetails());
    }
}
