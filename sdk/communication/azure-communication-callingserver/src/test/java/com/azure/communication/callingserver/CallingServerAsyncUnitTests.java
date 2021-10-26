// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.communication.callingserver.CallingServerResponseMocker.*;

import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CallingServerAsyncUnitTests {

    @Test
    public void startRecordingAsyncFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerAsyncClient(new ArrayList<>()).startRecording(
                SERVERCALL_LOCATOR,
                URI.create("/not/absolute/uri")
            ).block());
    }

    @Test
    public void startRecordingAsyncWithFullParamsFails() {
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions();
        startRecordingOptions.setRecordingChannel(RecordingChannel.MIXED);
        startRecordingOptions.setRecordingContent(RecordingContent.AUDIO_VIDEO);
        startRecordingOptions.setRecordingFormat(RecordingFormat.MP4);
        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerAsyncClient(new ArrayList<>()).startRecordingWithResponse(
                SERVERCALL_LOCATOR,
                URI.create("/not/absolute/uri"),
                startRecordingOptions,
                null
            ).block());
    }

    @Test
    public void joinCallAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        CallConnectionAsync callConnectionAsync = callingServerAsyncClient.joinCall(
            SERVERCALL_LOCATOR,
            (CommunicationIdentifier) user,
            options
        ).block();

        assertNotNull(callConnectionAsync);
    }

    @Test
    public void joinCallAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.joinCallWithResponse(
            SERVERCALL_LOCATOR,
            (CommunicationIdentifier) user,
            options
        ).block();

        assertEquals(202, callConnectionAsyncResponse.getStatusCode());
        assertNotNull(callConnectionAsyncResponse.getValue());
    }

    @Test
    public void getCallConnectionAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList()));

        CallConnectionAsync callConnection = callingServerAsyncClient.getCallConnection(CALL_CONNECTION_ID);
        assertNotNull(callConnection);
    }

    @Test
    public void addParticipantAsyncNullParticipantFails() {
        assertThrows(
            NullPointerException.class,
            () -> getCallingServerAsyncClient(new ArrayList<>()).addParticipant(
                SERVERCALL_LOCATOR,
                null,
                null,
                null,
                null
            ).block());
    }

    @Test
    public void addParticipantAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantResult(COMMUNICATION_USER.getId()), 202)
            ))
        );

        AddParticipantResult addParticipantResult = callingServerAsyncClient.addParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            CALLBACK_URI,
            "alternateCallerId",
            "operationContext"
        ).block();
        assertEquals(COMMUNICATION_USER.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void addParticipantAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantResult(COMMUNICATION_USER.getId()), 202)
            ))
        );

        Response<AddParticipantResult> addParticipantResultResponse = callingServerAsyncClient.addParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            CALLBACK_URI,
            "alternateCallerId",
            "operationContext"
        ).block();
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(COMMUNICATION_USER.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void removeParticipantAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerAsyncClient.removeParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER
        ).block();
    }

    @Test
    public void removeParticipantAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> removeParticipantResultResponse = callingServerAsyncClient.removeParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            Context.NONE
        ).block();
        assertEquals(202, removeParticipantResultResponse.getStatusCode());
    }

    @Test
    public void getParticipantAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetParticipantResult(),
                    200)
            ))
        );

        List<CallParticipant> getParticipantResult = callingServerAsyncClient.getParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER
        ).block();

        assertEquals(1, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(true, getParticipantResult.get(0).isMuted());
    }

    @Test
    public void getParticipantAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetParticipantResult(),
                    200)
            ))
        );

        Response<List<CallParticipant>> getParticipantResultResponse = callingServerAsyncClient.getParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            Context.NONE
        ).block();

        assertEquals(200, getParticipantResultResponse.getStatusCode());
        List<CallParticipant> getParticipantResult = getParticipantResultResponse.getValue();
        assertEquals(1, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(true, getParticipantResult.get(0).isMuted());
    }

    @Test
    public void getAllParticipantsAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetAllParticipantsResult(),
                    200)
            ))
        );

        List<CallParticipant> getParticipantResult = callingServerAsyncClient.getAllParticipants(
            SERVERCALL_LOCATOR
        ).block();

        assertEquals(2, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantResult.get(1).getIdentifier());
    }

    @Test
    public void getAllParticipantsAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generateGetAllParticipantsResult(),
                    200)
            ))
        );

        Response<List<CallParticipant>> getParticipantResultResponse = callingServerAsyncClient.getAllParticipantsWithResponse(
            SERVERCALL_LOCATOR
        ).block();

        assertEquals(200, getParticipantResultResponse.getStatusCode());
        List<CallParticipant> getParticipantResult = getParticipantResultResponse.getValue();
        assertEquals(2, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantResult.get(1).getIdentifier());
    }

    @Test
    public void playAudioAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generatePlayAudioResult(),
                    202)
            ))
        );

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(URI.create("https://callbackUri"))
            .setLoop(true)
            .setOperationContext("operationContext");

        PlayAudioResult playAudioResult = callingServerAsyncClient.playAudio(
            SERVERCALL_LOCATOR,
            URI.create("https://audioFileUri"),
            playAudioOptions
        ).block();

        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(
                    generatePlayAudioResult(),
                    202)
            ))
        );

        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri")).setLoop(true);
        Response<PlayAudioResult> playAudioResultResponse = callingServerAsyncClient.playAudioWithResponse(
            SERVERCALL_LOCATOR,
            URI.create("https://audioFileUri"),
            playAudioOptions
        ).block();

        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void cancelMediaOperationAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        callingServerAsyncClient.cancelMediaOperation(
            SERVERCALL_LOCATOR,
            OPERATION_ID
        ).block();
    }

    @Test
    public void cancelMediaOperationAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        Response<Void> cancelMediaOperationResponse = callingServerAsyncClient.cancelMediaOperationWithResponse(
            SERVERCALL_LOCATOR,
            OPERATION_ID
        ).block();

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void cancelParticipantMediaOperationAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        callingServerAsyncClient.cancelParticipantMediaOperation(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            OPERATION_ID
        ).block();
    }

    @Test
    public void cancelParticipantMediaOperationAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 200)
            ))
        );

        Response<Void> cancelParticipantMediaOperationResponse = callingServerAsyncClient.cancelParticipantMediaOperationWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            OPERATION_ID
        ).block();

        assertEquals(200, cancelParticipantMediaOperationResponse.getStatusCode());
    }

    @Test
    public void playAudioToParticipantAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
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

        PlayAudioResult playAudioToParticipantResult = callingServerAsyncClient.playAudioToParticipant(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            URI.create("audioFileUri"),
            playAudioOptions
        ).block();

        assertEquals(CallingOperationStatus.COMPLETED, playAudioToParticipantResult.getStatus());
    }

    @Test
    public void playAudioToParticipantAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
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

        Response<PlayAudioResult> playAudioToParticipantResultResponse = callingServerAsyncClient.playAudioToParticipantWithResponse(
            SERVERCALL_LOCATOR,
            COMMUNICATION_USER,
            URI.create("https://audioFileUri"),
            playAudioOptions
        ).block();

        assertEquals(202, playAudioToParticipantResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioToParticipantResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void redirectCallAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerAsyncClient.redirectCall(
            INCOMINGCALL_CONTEXT,
            Arrays.asList(COMMUNICATION_USER),
            URI.create("audioFileUri"),
            TIMEOUT
        ).block();
    }

    @Test
    public void redirectCallAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> redirectCallResponse = callingServerAsyncClient.redirectCallWithResponse(
            INCOMINGCALL_CONTEXT,
            Arrays.asList(COMMUNICATION_USER),
            URI.create("audioFileUri"),
            TIMEOUT
        ).block();

        assertEquals(202, redirectCallResponse.getStatusCode());
    }

    @Test
    public void rejectCallAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerAsyncClient.rejectCall(
            INCOMINGCALL_CONTEXT,
            URI.create("audioFileUri"),
            CallRejectReason.BUSY
        ).block();
    }

    @Test
    public void rejectCallAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> rejectCallResponse = callingServerAsyncClient.rejectCallWithResponse(
            INCOMINGCALL_CONTEXT,
            URI.create("audioFileUri"),
            CallRejectReason.BUSY
        ).block();

        assertEquals(202, rejectCallResponse.getStatusCode());
    }
}
