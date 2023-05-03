// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.events.CallAutomationEventData;
import com.azure.communication.callautomation.models.events.CallConnectedEventData;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionStoppedEventData;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneFailedEventData;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneReceivedEventData;
import com.azure.communication.callautomation.models.events.ParticipantsUpdatedEventData;
import com.azure.communication.callautomation.models.events.PlayCanceledEventData;
import com.azure.communication.callautomation.models.events.PlayCompletedEventData;
import com.azure.communication.callautomation.models.events.PlayFailedEventData;
import com.azure.communication.callautomation.models.events.ReasonCode;
import com.azure.communication.callautomation.models.events.RecognizeCanceledEventData;
import com.azure.communication.callautomation.models.events.RecognizeCompletedEventData;
import com.azure.communication.callautomation.models.ChoiceResult;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.events.RecognizeFailedEventData;
import com.azure.communication.callautomation.models.events.RecordingStateChangedEventData;
import com.azure.communication.callautomation.models.events.RemoveParticipantFailedEventData;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceededEventData;
import com.azure.communication.callautomation.models.events.ReasonCode.Recognize;

import com.azure.communication.callautomation.models.events.SendDtmfCompletedEventData;
import com.azure.communication.callautomation.models.events.SendDtmfFailedEventData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class CallAutomationEventParserUnitTests {
    static final String EVENT_PARTICIPANT_UPDATED = "{\"id\":\"61069ef9-5ca9-457f-ac36-e2bb5e8400ca\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\",\"type\":\"Microsoft.Communication.ParticipantsUpdated\",\"data\":{\"participants\":[{\"identifier\": {\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff6-dd51-54b7-a43a0d001998\"}}, \"isMuted\": false},{\"identifier\": {\"rawId\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:816df1ca-971b-44d7-b8b1-8fba90748500_00000013-2ff7-1579-99bf-a43a0d0010bc\"}}, \"isMuted\": false}],\"type\":\"participantsUpdated\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.9129474+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/ParticipantsUpdated\"}";
    static final String EVENT_CALL_CONNECTED = "{\"id\":\"46116fb7-27e0-4a99-9478-a659c8fd4815\",\"source\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/CallConnected\",\"type\":\"Microsoft.Communication.CallConnected\",\"data\":{\"type\":\"callConnected\",\"callConnectionId\":\"401f3500-62bd-46a9-8c09-9e1b06caca01\",\"correlationId\":\"ebd8bf1f-0794-494f-bdda-913042c06ef7\"},\"time\":\"2022-08-12T03:35:07.8174402+00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/401f3500-62bd-46a9-8c09-9e1b06caca01/CallConnected\"}";
    static final String EVENT_RECOGNIZE_DTMF = "[{\"id\":\"ac2cb537-2d62-48bf-909e-cc93534c4258\",\"source\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"type\":\"Microsoft.Communication.RecognizeCompleted\",\"data\":{\"eventSource\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"operationContext\":\"OperationalContextValue-1118-1049\",\"resultInformation\":{\"code\":200,\"subCode\":8533,\"message\":\"Action completed, DTMF option matched.\"},\"recognitionType\":\"dtmf\",\"dtmfResult\":{\"tones\":[\"five\", \"six\", \"pound\"]},\"choiceResult\":{\"label\":\"Marketing\"},\"callConnectionId\":\"401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"serverCallId\":\"serverCallId\",\"correlationId\":\"d4f4c1be-59d8-4850-b9bf-ee564c15839d\"},\"time\":\"2022-11-22T01:41:44.5582769+00:00\",\"specversion\":\"1.0\",\"subject\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\"}]";
    static final String EVENT_RECOGNIZE_CHOICE = "[{\"id\":\"e25b99ef-3632-45bb-96d1-d9191547ff33\",\"source\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"type\":\"Microsoft.Communication.RecognizeCompleted\",\"data\":{\"eventSource\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"operationContext\":\"OperationalContextValue-1118-1049\",\"resultInformation\":{\"code\":200,\"subCode\":8545,\"message\":\"Action completed, Recognized phrase matches a valid option.\"},\"recognitionType\":\"choices\",\"choiceResult\":{\"label\":\"Support\",\"recognizedPhrase\":\"customer help\"},\"callConnectionId\":\"401f7000-c1c0-41e2-962d-85d0dc1d6f01\",\"serverCallId\":\"serverCallId\",\"correlationId\":\"d4f4c1be-59d8-4850-b9bf-ee564c15839d\"},\"time\":\"2022-11-22T01:41:00.1967145+00:00\",\"specversion\":\"1.0\",\"subject\":\"calling/callConnections/401f7000-c1c0-41e2-962d-85d0dc1d6f01\"}]";

    @Test
    public void parseEvent() {
        CallAutomationEventData callAutomationEventData = CallAutomationEventParser.parseEvents(EVENT_PARTICIPANT_UPDATED).get(0);

        assertNotNull(callAutomationEventData);
        assertEquals(callAutomationEventData.getClass(), ParticipantsUpdatedEventData.class);
        ParticipantsUpdatedEventData participantsUpdatedEventData = (ParticipantsUpdatedEventData) callAutomationEventData;
        assertNotNull((participantsUpdatedEventData).getParticipants());
        participantsUpdatedEventData.getParticipants().forEach(participant -> {
            assertNotNull(participant);
            assertNotNull(participant.getIdentifier());
            assertNotNull(participant.isMuted());
        });
    }

    @Test
    public void parseEventList() {
        List<CallAutomationEventData> callAutomationEventDataList = CallAutomationEventParser.parseEvents("["
            + EVENT_CALL_CONNECTED + "," + EVENT_PARTICIPANT_UPDATED + "]");

        assertNotNull(callAutomationEventDataList);
        assertEquals(callAutomationEventDataList.get(0).getClass(), CallConnectedEventData.class);
        assertEquals(callAutomationEventDataList.get(1).getClass(), ParticipantsUpdatedEventData.class);
        assertNotNull(callAutomationEventDataList.get(0).getCallConnectionId());
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
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        RecordingStateChangedEventData recordingEvent = (RecordingStateChangedEventData) event;
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
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        PlayCompletedEventData playCompletedEventData = (PlayCompletedEventData) event;
        assertNotNull(playCompletedEventData);
        assertEquals("serverCallId", playCompletedEventData.getServerCallId());
        assertEquals(200, playCompletedEventData.getResultInformation().getCode());
        assertEquals(ReasonCode.COMPLETED_SUCCESSFULLY, playCompletedEventData.getReasonCode());
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
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        PlayFailedEventData playFailedEventData = (PlayFailedEventData) event;
        assertNotNull(playFailedEventData);
        assertEquals("serverCallId", playFailedEventData.getServerCallId());
        assertEquals(400, playFailedEventData.getResultInformation().getCode());
        assertEquals(ReasonCode.Play.DOWNLOAD_FAILED, playFailedEventData.getReasonCode());
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
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        PlayCanceledEventData playCanceledEventData = (PlayCanceledEventData) event;
        assertNotNull(playCanceledEventData);
        assertEquals("serverCallId", playCanceledEventData.getServerCallId());
    }
    @Test
    public void parseRecognizeCompletedWithChoiceEvent() {
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(EVENT_RECOGNIZE_CHOICE).get(0);
        assertNotNull(event);
        RecognizeCompletedEventData recognizeCompletedEventData = (RecognizeCompletedEventData) event;
        assertNotNull(recognizeCompletedEventData);
        Optional<RecognizeResult> choiceResult = recognizeCompletedEventData.getRecognizeResult();
        assertInstanceOf(ChoiceResult.class, choiceResult.get());
        assertEquals("serverCallId", recognizeCompletedEventData.getServerCallId());
        assertEquals(200, recognizeCompletedEventData.getResultInformation().getCode());
        assertEquals(Recognize.SPEECH_OPTION_MATCHED, recognizeCompletedEventData.getReasonCode());
    }

    @Test
    public void parseRecognizeCompletedWithDtmfEvent() {
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(EVENT_RECOGNIZE_DTMF).get(0);
        assertNotNull(event);
        RecognizeCompletedEventData recognizeCompletedEventData = (RecognizeCompletedEventData) event;
        Optional<RecognizeResult> dtmfResult = recognizeCompletedEventData.getRecognizeResult();
        DtmfResult tonesResult = (DtmfResult) dtmfResult.get();
        assertInstanceOf(DtmfResult.class, dtmfResult.get());
        String tonesInString = tonesResult.convertToString();
        assertEquals(tonesInString, "56#");
        assertNotNull(recognizeCompletedEventData);
        assertEquals("serverCallId", recognizeCompletedEventData.getServerCallId());
        assertEquals(200, recognizeCompletedEventData.getResultInformation().getCode());
        assertEquals(Recognize.DMTF_OPTION_MATCHED, recognizeCompletedEventData.getReasonCode());
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
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        RecognizeFailedEventData recognizeFailedEventData = (RecognizeFailedEventData) event;
        assertNotNull(recognizeFailedEventData);
        assertEquals("serverCallId", recognizeFailedEventData.getServerCallId());
        assertEquals(400, recognizeFailedEventData.getResultInformation().getCode());
        assertEquals(ReasonCode.Recognize.INITIAL_SILENCE_TIMEOUT, recognizeFailedEventData.getReasonCode());
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
        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);
        RecognizeCanceledEventData recognizeCanceledEventData = (RecognizeCanceledEventData) event;
        assertNotNull(recognizeCanceledEventData);
        assertEquals("serverCallId", recognizeCanceledEventData.getServerCallId());
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

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);
        assertNotNull(event);

        RemoveParticipantSucceededEventData removeParticipantSucceededEventData = (RemoveParticipantSucceededEventData) event;

        assertNotNull(removeParticipantSucceededEventData);
        assertEquals("serverCallId", removeParticipantSucceededEventData.getServerCallId());
        assertEquals("callConnectionId", removeParticipantSucceededEventData.getCallConnectionId());
        assertEquals("rawId", removeParticipantSucceededEventData.getParticipant().getRawId());
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

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        RemoveParticipantFailedEventData removeParticipantFailedEventData = (RemoveParticipantFailedEventData) event;

        assertNotNull(removeParticipantFailedEventData);
        assertEquals("serverCallId", removeParticipantFailedEventData.getServerCallId());
        assertEquals("callConnectionId", removeParticipantFailedEventData.getCallConnectionId());
        assertEquals("rawId", removeParticipantFailedEventData.getParticipant().getRawId());
    }

    @Test
    public void parseContinuousDtmfRecognitionToneReceivedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneReceived\",\n"
            + "      \"type\":\"Microsoft.Communication.ContinuousDtmfRecognitionToneReceived\",\n"
            + "\t  \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneReceived\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"DTMF tone received successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"ContinuousDtmfRecognitionToneReceived\",\n"
            + "         \"toneInfo\":{\n"
            + "            \"sequenceId\":1,\n"
            + "            \"tone\":\"eight\",\n"
            + "            \"participantId\":\"267e33a9-c28e-4ecf-a33e-b3abd9526e32\"\n"
            + "         },\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneReceived\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        ContinuousDtmfRecognitionToneReceivedEventData continuousDtmfRecognitionToneReceived = (ContinuousDtmfRecognitionToneReceivedEventData) event;

        assertNotNull(continuousDtmfRecognitionToneReceived);
        assertEquals("serverCallId", continuousDtmfRecognitionToneReceived.getServerCallId());
        assertEquals("callConnectionId", continuousDtmfRecognitionToneReceived.getCallConnectionId());
        assertEquals("eight", continuousDtmfRecognitionToneReceived.getToneInfo().getTone().toString());
        assertEquals(1, continuousDtmfRecognitionToneReceived.getToneInfo().getSequenceId());
        assertEquals("correlationId", continuousDtmfRecognitionToneReceived.getCorrelationId());
        assertEquals(200, continuousDtmfRecognitionToneReceived.getResultInformation().getCode());
        assertEquals(0, continuousDtmfRecognitionToneReceived.getResultInformation().getSubCode());
        assertEquals("DTMF tone received successfully.", continuousDtmfRecognitionToneReceived.getResultInformation().getMessage());
    }

    @Test
    public void parseContinuousDtmfRecognitionToneFailedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneFailed\",\n"
            + "      \"type\":\"Microsoft.Communication.ContinuousDtmfRecognitionToneFailed\",\n"
            + "\t  \"specversion\":\"1.0\",\n"
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
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionToneFailed\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        ContinuousDtmfRecognitionToneFailedEventData continuousDtmfRecognitionToneFailed = (ContinuousDtmfRecognitionToneFailedEventData) event;

        assertNotNull(continuousDtmfRecognitionToneFailed);
        assertEquals("serverCallId", continuousDtmfRecognitionToneFailed.getServerCallId());
        assertEquals("callConnectionId", continuousDtmfRecognitionToneFailed.getCallConnectionId());
        assertEquals("correlationId", continuousDtmfRecognitionToneFailed.getCorrelationId());
        assertEquals(400, continuousDtmfRecognitionToneFailed.getResultInformation().getCode());
        assertEquals(12323, continuousDtmfRecognitionToneFailed.getResultInformation().getSubCode());
        assertEquals("Continuous DTMF tone Couldn't be received successfully.", continuousDtmfRecognitionToneFailed.getResultInformation().getMessage());
    }

    @Test
    public void parseContinuousDtmfRecognitionStoppedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionStopped\",\n"
            + "      \"type\":\"Microsoft.Communication.ContinuousDtmfRecognitionStopped\",\n"
            + "\t  \"specversion\":\"1.0\",\n"
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
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/ContinuousDtmfRecognitionStopped\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        ContinuousDtmfRecognitionStoppedEventData continuousDtmfRecognitionStopped = (ContinuousDtmfRecognitionStoppedEventData) event;

        assertNotNull(continuousDtmfRecognitionStopped);
        assertEquals("serverCallId", continuousDtmfRecognitionStopped.getServerCallId());
        assertEquals("callConnectionId", continuousDtmfRecognitionStopped.getCallConnectionId());
        assertEquals("correlationId", continuousDtmfRecognitionStopped.getCorrelationId());
        assertEquals(200, continuousDtmfRecognitionStopped.getResultInformation().getCode());
        assertEquals(0, continuousDtmfRecognitionStopped.getResultInformation().getSubCode());
        assertEquals("Continuous DTMF Recognition stopped successfully.", continuousDtmfRecognitionStopped.getResultInformation().getMessage());
    }

    @Test
    public void parseSendDtmfCompletedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/SendDtmfCompleted\",\n"
            + "      \"type\":\"Microsoft.Communication.SendDtmfCompleted\",\n"
            + "\t  \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/SendDtmfCompleted\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"Send DTMF completed successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"SendDtmfCompleted\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/SendDtmfCompleted\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        SendDtmfCompletedEventData sendDtmfCompleted = (SendDtmfCompletedEventData) event;

        assertNotNull(sendDtmfCompleted);
        assertEquals("serverCallId", sendDtmfCompleted.getServerCallId());
        assertEquals("callConnectionId", sendDtmfCompleted.getCallConnectionId());
        assertEquals("correlationId", sendDtmfCompleted.getCorrelationId());
        assertEquals(200, sendDtmfCompleted.getResultInformation().getCode());
        assertEquals(0, sendDtmfCompleted.getResultInformation().getSubCode());
        assertEquals("Send DTMF completed successfully.", sendDtmfCompleted.getResultInformation().getMessage());
    }

    @Test
    public void parseSendDtmfFailedEvent() {

        String receivedEvent = "[\n"
            + "   {\n"
            + "      \"id\":\"704a7a96-4d74-4ebe-9cd0-b7cc39c3d7b1\",\n"
            + "      \"source\":\"calling/callConnections/callConnectionId/SendDtmfFailed\",\n"
            + "      \"type\":\"Microsoft.Communication.SendDtmfFailed\",\n"
            + "\t  \"specversion\":\"1.0\",\n"
            + "      \"data\":{\n"
            + "         \"eventSource\":\"calling/callConnections/callConnectionId/SendDtmfFailed\",\n"
            + "         \"resultInformation\":{\n"
            + "            \"code\":200,\n"
            + "            \"subCode\":0,\n"
            + "            \"message\":\"Send DTMF couldn't be completed successfully.\"\n"
            + "         },\n"
            + "         \"type\":\"SendDtmfFailed\",\n"
            + "         \"callConnectionId\":\"callConnectionId\",\n"
            + "         \"serverCallId\":\"serverCallId\",\n"
            + "         \"correlationId\":\"correlationId\",\n"
            + "         \"time\":\"2022-08-12T03:13:25.0252763+00:00\",\n"
            + "         \"specversion\":\"1.0\",\n"
            + "         \"datacontenttype\":\"application/json\",\n"
            + "         \"subject\":\"calling/callConnections/callConnectionId/SendDtmfFailed\"\n"
            + "      }\n"
            + "   }\n"
            + "]";

        CallAutomationEventData event = CallAutomationEventParser.parseEvents(receivedEvent).get(0);

        assertNotNull(event);

        SendDtmfFailedEventData sendDtmfFailed = (SendDtmfFailedEventData) event;

        assertNotNull(sendDtmfFailed);
        assertEquals("serverCallId", sendDtmfFailed.getServerCallId());
        assertEquals("callConnectionId", sendDtmfFailed.getCallConnectionId());
        assertEquals("correlationId", sendDtmfFailed.getCorrelationId());
        assertEquals(200, sendDtmfFailed.getResultInformation().getCode());
        assertEquals(0, sendDtmfFailed.getResultInformation().getSubCode());
        assertEquals("Send DTMF couldn't be completed successfully.", sendDtmfFailed.getResultInformation().getMessage());
    }
}
