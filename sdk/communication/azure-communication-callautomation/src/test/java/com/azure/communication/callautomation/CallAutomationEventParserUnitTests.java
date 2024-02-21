// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.ChoiceResult;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CallTransferAccepted;
import com.azure.communication.callautomation.models.events.CancelAddParticipantFailed;
import com.azure.communication.callautomation.models.events.CancelAddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionStopped;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneFailed;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneReceived;
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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallAutomationEventParserUnitTests {
    static final String EVENT_PARTICIPANT_UPDATED = "{\"id\":\"61069ef9-5ca9-457f-ac36-e2bb5e8400ca\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\",\"type\":\"Microsoft.Communication.ParticipantsUpdated\",\"data\":{\"participants\":[{\"identifier\": {\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\"}}, \"isMuted\": false},{\"identifier\": {\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\"}}, \"isMuted\": false}],\"type\":\"participantsUpdated\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.9129474+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\"}";
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
        Optional<RecognizeResult> recognizeResult = recognizeCompleted.getRecognizeResult();
        assertInstanceOf(ChoiceResult.class, recognizeResult.get());

        ChoiceResult choiceResult = (ChoiceResult) recognizeResult.get();
        assertEquals("Support", choiceResult.getLabel());
        assertEquals("customer help", choiceResult.getRecognizedPhrase());
        assertEquals("serverCallId", recognizeCompleted.getServerCallId());
        assertEquals(200, recognizeCompleted.getResultInformation().getCode());
        assertEquals(Recognize.SPEECH_OPTION_MATCHED, recognizeCompleted.getReasonCode());
    }

    @Test
    public void parseRecognizeCompletedWithDtmfEvent() {
        CallAutomationEventBase event = CallAutomationEventParser.parseEvents(EVENT_RECOGNIZE_DTMF).get(0);
        assertNotNull(event);
        RecognizeCompleted recognizeCompleted = (RecognizeCompleted) event;
        Optional<RecognizeResult> recognizeResult = recognizeCompleted.getRecognizeResult();
        assertNotNull(recognizeCompleted);
        assertEquals("serverCallId", recognizeCompleted.getServerCallId());
        assertEquals(200, recognizeCompleted.getResultInformation().getCode());
        assertEquals(Recognize.DMTF_OPTION_MATCHED, recognizeCompleted.getReasonCode());
        assertInstanceOf(DtmfResult.class, recognizeResult.get());
        DtmfResult dtmfResult = (DtmfResult) recognizeResult.get();
        List<DtmfTone> dtmfToneList = dtmfResult.getTones();
        assertEquals(3, dtmfToneList.size());
        assertEquals("five", dtmfToneList.get(0).toString());
        assertEquals("six", dtmfToneList.get(1).toString());
        assertEquals("pound", dtmfToneList.get(2).toString());
        String tonesInString = dtmfResult.convertToString();
        assertEquals(tonesInString, "56#");
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
    public void parseRemoveParticipantSucceededEvent() {
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
    }

    @Test
    public void parseRemoveParticipantFailedEvent() {
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
    }

}
