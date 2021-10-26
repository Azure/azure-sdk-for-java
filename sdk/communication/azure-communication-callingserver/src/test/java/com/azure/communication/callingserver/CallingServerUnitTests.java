// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.communication.callingserver.CallingServerResponseMocker.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;

import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;



public class CallingServerUnitTests {
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";
    static final String CALL_CONNECTION_ID = "callConnectionId";

    @Test
    public void startRecordingFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerClient(new ArrayList<>()).startRecording(
                SERVERCALL_LOCATOR,
                URI.create("/not/absolute/uri")
            ));
    }

    @Test
    public void startRecordingWithFullParamsFails() {
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions();
        startRecordingOptions.setRecordingChannel(RecordingChannel.MIXED);
        startRecordingOptions.setRecordingContent(RecordingContent.AUDIO_VIDEO);
        startRecordingOptions.setRecordingFormat(RecordingFormat.MP4);

        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerClient(new ArrayList<>()).startRecordingWithResponse(
                SERVERCALL_LOCATOR,
                URI.create("/not/absolute/uri"),
                startRecordingOptions,
                null
            ));
    }

    @Test
    public void answerCall() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAnswerCallResult(CALL_CONNECTION_ID), 201)
            )));

        AnswerCallOptions answerCallOptions = new AnswerCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        CallConnection callConnection = callingServerClient.answerCall(
            INCOMING_CALL_CONTEXT,
            answerCallOptions
        );

        assertNotNull(callConnection);
    }

    @Test
    public void answerCallWithResponse() {
        CallingServerClient callingServerAsyncClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAnswerCallResult(CALL_CONNECTION_ID), 201)
            )));

        AnswerCallOptions options = new AnswerCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        Response<CallConnection> callConnectionResponse = callingServerAsyncClient.answerCallWithResponse(
            INCOMING_CALL_CONTEXT,
            options,
            Context.NONE
        );

        assertEquals(201, callConnectionResponse.getStatusCode());
        assertNotNull(callConnectionResponse.getValue());
    }

    @Test
    public void joinCall() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        CallConnection callConnection = callingServerClient.joinCall(
            SERVERCALL_LOCATOR,
            (CommunicationIdentifier) user,
            options
        );

        assertNotNull(callConnection);
    }

    @Test
    public void joinCallWithResponse() {
        CallingServerClient callingServerAsyncClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        Response<CallConnection> callConnectionResponse = callingServerAsyncClient.joinCallWithResponse(
            SERVERCALL_LOCATOR,
            (CommunicationIdentifier) user,
            options,
            Context.NONE
        );

        assertEquals(202, callConnectionResponse.getStatusCode());
        assertNotNull(callConnectionResponse.getValue());
    }

    @Test
    public void getCallConnection() {
        CallingServerClient callingServerAsyncClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList()));

        CallConnection callConnection = callingServerAsyncClient.getCallConnection(CALL_CONNECTION_ID);
        assertNotNull(callConnection);
    }

    @Test
    public void addParticipantNullParticipantFails() {
        assertThrows(
            NullPointerException.class,
            () -> getCallingServerClient(new ArrayList<>()).addParticipant(
                SERVERCALL_LOCATOR,
                null,
                null,
                null,
                null
            ));
    }

    @Test
    public void addParticipant() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantResult(COMMUNICATION_USER.getId()), 202)
            ))
        );

        AddParticipantResult addParticipantResult = callingServerClient.addParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            CALLBACK_URI,
            "alternateCallerId",
            "operationContext"
        );

        assertEquals(COMMUNICATION_USER.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void addParticipantWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantResult(COMMUNICATION_USER.getId()), 202)
            ))
        );

        Response<AddParticipantResult> addParticipantResultResponse = callingServerClient.addParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            CALLBACK_URI,
            "alternateCallerId",
            "operationContext",
            Context.NONE
        );

        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(COMMUNICATION_USER.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void removeParticipant() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerClient.removeParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER
        );
    }

    @Test
    public void removeParticipantWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> removeParticipantResultResponse = callingServerClient.removeParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            Context.NONE
        );

        assertEquals(202, removeParticipantResultResponse.getStatusCode());
    }

    @Test
    public void getParticipant() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetParticipantResult(),
                    200)
            ))
        );

        List<CallParticipant> getParticipantResult = callingServerClient.getParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER
        );

        assertEquals(1, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(true, getParticipantResult.get(0).isMuted());
    }

    @Test
    public void getParticipantWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetParticipantResult(),
                    200)
            ))
        );

        Response<List<CallParticipant>> getParticipantResultResponse = callingServerClient.getParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            Context.NONE
        );

        assertEquals(200, getParticipantResultResponse.getStatusCode());
        List<CallParticipant> getParticipantResult = getParticipantResultResponse.getValue();
        assertEquals(1, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(true, getParticipantResult.get(0).isMuted());
    }

    @Test
    public void getAllParticipants() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetAllParticipantsResult(),
                    200)
            ))
        );

        List<CallParticipant> getParticipantResult = callingServerClient.getAllParticipants(
            SERVERCALL_LOCATOR
        );

        assertEquals(2, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantResult.get(1).getIdentifier());
    }

    @Test
    public void getAllParticipantsWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetAllParticipantsResult(),
                    200)
            ))
        );

        Response<List<CallParticipant>> getParticipantResultResponse = callingServerClient.getAllParticipantsWithResponse(
            SERVERCALL_LOCATOR,
            Context.NONE
        );

        assertEquals(200, getParticipantResultResponse.getStatusCode());
        List<CallParticipant> getParticipantResult = getParticipantResultResponse.getValue();
        assertEquals(2, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantResult.get(1).getIdentifier());
    }

    @Test
    public void playAudio() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generatePlayAudioResult(),
                    202)
            ))
        );

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId").
            setCallbackUri(URI.create("callbackUri")).
            setOperationContext("operationContext").
            setLoop(true);

        PlayAudioResult playAudioResult = callingServerClient.playAudio(
            SERVERCALL_LOCATOR,
            URI.create("audioFileUri"),
            playAudioOptions
        );

        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generatePlayAudioResult(),
                    202)
            ))
        );

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(URI.create("https://callbackUri"))
            .setLoop(true);

        Response<PlayAudioResult> playAudioResultResponse = callingServerClient.playAudioWithResponse(
            SERVERCALL_LOCATOR,
            URI.create("https://audioFileUri"),
            playAudioOptions,
            Context.NONE
        );

        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void cancelMediaOperation() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        callingServerClient.cancelMediaOperation(
            SERVERCALL_LOCATOR,
            OPERATION_ID
        );
    }

    @Test
    public void cancelMediaOperationWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        Response<Void> cancelMediaOperationResponse = callingServerClient.cancelMediaOperationWithResponse(
            SERVERCALL_LOCATOR,
            OPERATION_ID,
            Context.NONE
        );

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void cancelParticipantMediaOperation() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        callingServerClient.cancelParticipantMediaOperation(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            OPERATION_ID
        );
    }

    @Test
    public void cancelParticipantMediaOperationWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        Response<Void> cancelParticipantMediaOperationResponse = callingServerClient.cancelParticipantMediaOperationWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            OPERATION_ID,
            Context.NONE
        );

        assertEquals(200, cancelParticipantMediaOperationResponse.getStatusCode());
    }

    @Test
    public void playAudioToParticipant() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generatePlayAudioResult(),
                    202)
            ))
        );

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId").
            setCallbackUri(URI.create("callbackUri")).
            setOperationContext("operationContext").
            setLoop(true);

        PlayAudioResult playAudioToParticipantResult = callingServerClient.playAudioToParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            URI.create("audioFileUri"),
            playAudioOptions
        );

        assertEquals(CallingOperationStatus.COMPLETED, playAudioToParticipantResult.getStatus());
    }

    @Test
    public void playAudioToParticipantWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generatePlayAudioResult(),
                    202)
            ))
        );

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(URI.create("https://callbackUri"))
            .setLoop(true);

        Response<PlayAudioResult> playAudioToParticipantResultResponse = callingServerClient.playAudioToParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            URI.create("https://audioFileUri"),
            playAudioOptions,
            Context.NONE
        );

        assertEquals(202, playAudioToParticipantResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioToParticipantResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void redirectCall() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerClient.redirectCall(
            INCOMING_CALL_CONTEXT,
            Arrays.asList(COMMUNICATION_USER),
            URI.create("audioFileUri"),
            TIMEOUT
        );
    }

    @Test
    public void redirectCallWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> redirectCallResponse = callingServerClient.redirectCallWithResponse(
            INCOMING_CALL_CONTEXT,
            Arrays.asList(COMMUNICATION_USER),
            URI.create("audioFileUri"),
            TIMEOUT,
            Context.NONE
        );

        assertEquals(202, redirectCallResponse.getStatusCode());
    }

    @Test
    public void rejectCall() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerClient.rejectCall(
            INCOMING_CALL_CONTEXT,
            URI.create("audioFileUri"),
            CallRejectReason.BUSY
        );
    }

    @Test
    public void rejectCallWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> rejectCallResponse = callingServerClient.rejectCallWithResponse(
            INCOMING_CALL_CONTEXT,
            URI.create("audioFileUri"),
            CallRejectReason.BUSY,
            Context.NONE
        );

        assertEquals(202, rejectCallResponse.getStatusCode());
    }
}
