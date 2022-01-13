// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.communication.callingserver.CallingServerResponseMocker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.api.Test;

public class CallConnectionAsyncUnitTests {

    static final String CALL_CONNECTION_ID = "callConnectionId";
    static final String OPERATION_ID = "operationId";
    static final String NEW_PARTICIPANT_ID = "newParticipantId";

    @Test
    public void createConnectionAsync() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201)
            )));

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        targetUsers.add(new CommunicationUserIdentifier("id2"));

        CreateCallOptions options = new CreateCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        CallConnectionAsync callConnection = callingServerAsyncClient.createCallConnection(sourceUser, targetUsers, options).block();
        assertEquals(CALL_CONNECTION_ID, callConnection.getCallConnectionId());
    }

    @Test
    public void createConnectionAsyncWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201)
            )));

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        targetUsers.add(new CommunicationUserIdentifier("id2"));

        CreateCallOptions options = new CreateCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.createCallConnectionWithResponse(sourceUser, targetUsers, options).block();
        assertEquals(201, callConnectionAsyncResponse.getStatusCode());
        assertNotNull(callConnectionAsyncResponse.getValue());
    }

    @Test
    public void playAudioAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        PlayAudioResult playAudioResult = callConnectionAsync.playAudio(URI.create("https://audioFileUri.local"), playAudioOptions).block();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioWithResponseAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        Response<PlayAudioResult> playAudioResultResponse = callConnectionAsync.playAudioWithResponse(
            URI.create("https://audioFileUri.local"),
            playAudioOptions
        ).block();
        assertEquals(202, playAudioResultResponse.getStatusCode());
    }

    @Test
    public void hangupAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnectionAsync.hangup().block();
    }

    @Test
    public void hangupAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> hangupResponse = callConnectionAsync.hangupWithResponse().block();

        assertEquals(202, hangupResponse.getStatusCode());
    }

    @Test
    public void deleteAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnectionAsync.delete().block();
    }

    @Test
    public void deleteAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> deleteResponse = callConnectionAsync.deleteWithResponse().block();

        assertEquals(202, deleteResponse.getStatusCode());
    }

    @Test
    public void cancelAllMediaOperationsAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.cancelAllMediaOperations().block();
    }

    @Test
    public void cancelAllMediaOperationsAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelMediaOperationResponse = callConnectionAsync.cancelAllMediaOperationsWithResponse().block();

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void addParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateAddParticipantResult(), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        AddParticipantResult addParticipantResult = callConnectionAsync.addParticipant(
            user,
            ALTERNATE_CALLER_ID,
            OPERATION_CONTEXT
        ).block();
        assertNotNull(addParticipantResult.getOperationId());
        assertFalse(addParticipantResult.getOperationId().isEmpty());
        assertNotNull(addParticipantResult.getStatus());
        assertSame(addParticipantResult.getStatus(), CallingOperationStatus.RUNNING);
    }

    @Test
    public void addParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateAddParticipantResult(), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        Response<AddParticipantResult> addParticipantResultResponse = callConnectionAsync.addParticipantWithResponse(
            user,
            ALTERNATE_CALLER_ID,
            OPERATION_CONTEXT
        ).block();
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertNotNull(addParticipantResult.getOperationId());
        assertFalse(addParticipantResult.getOperationId().isEmpty());
        assertNotNull(addParticipantResult.getStatus());
        assertSame(addParticipantResult.getStatus(), CallingOperationStatus.RUNNING);
    }

    @Test
    public void removeParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnectionAsync.removeParticipant(new CommunicationUserIdentifier(NEW_PARTICIPANT_ID)).block();
    }

    @Test
    public void removeParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> removeParticipantResponse = callConnectionAsync.removeParticipantWithResponse(
            new CommunicationUserIdentifier(NEW_PARTICIPANT_ID)
        ).block();
        assertEquals(202, removeParticipantResponse.getStatusCode());
    }

    @Test
    public void transferToParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateTransferCallResult(), 202)
            )));

        TransferCallResult transferCallResult = callConnectionAsync.transferToParticipant(
            new CommunicationUserIdentifier(NEW_PARTICIPANT_ID),
            ALTERNATE_CALLER_ID,
            "",
            ""
        ).block();

        assertEquals(CallingOperationStatus.COMPLETED, transferCallResult.getStatus());
    }

    @Test
    public void transferToParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateTransferCallResult(), 202)
            )));

        Response<TransferCallResult> transferCallResponse = callConnectionAsync.transferToParticipantWithResponse(
            new CommunicationUserIdentifier(NEW_PARTICIPANT_ID),
            ALTERNATE_CALLER_ID,
            "",
            ""
        ).block();

        assertEquals(202, transferCallResponse.getStatusCode());
        TransferCallResult transferCallResult = transferCallResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, transferCallResult.getStatus());
    }

    @Test
    public void transferToCallAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateTransferCallResult(), 202)
            )));

        TransferCallResult transferCallResult = callConnectionAsync.transferToCall(
            CALL_CONNECTION_ID,
            "",
            ""
        ).block();

        assertEquals(CallingOperationStatus.COMPLETED, transferCallResult.getStatus());
    }

    @Test
    public void transferToCallAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateTransferCallResult(), 202)
            )));

        Response<TransferCallResult> transferCallResponse = callConnectionAsync.transferToCallWithResponse(
            CALL_CONNECTION_ID,
            "",
            ""
        ).block();

        assertEquals(202, transferCallResponse.getStatusCode());
        TransferCallResult transferCallResult = transferCallResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, transferCallResult.getStatus());
    }

    @Test
    public void getCallAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetCallResult(), 200)
            )));

        CallConnectionProperties getCallResult = callConnectionAsync.getCall().block();

        assertEquals(CALLBACK_URI.toString(), getCallResult.getCallbackUri());
        assertEquals(CALL_CONNECTION_ID, getCallResult.getCallConnectionId());
        assertEquals(SERVERCALL_LOCATOR.getServerCallId(), ((ServerCallLocator) getCallResult.getCallLocator()).getServerCallId());
        assertEquals(CallConnectionState.CONNECTED, getCallResult.getCallConnectionState());
        assertEquals(Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED), getCallResult.getRequestedCallEvents());
        assertEquals(Collections.singletonList(CallMediaType.AUDIO), getCallResult.getRequestedMediaTypes());
        assertEquals(Collections.singletonList(COMMUNICATION_USER), getCallResult.getTargets());
        assertEquals(COMMUNICATION_USER_1, getCallResult.getSource());
    }

    @Test
    public void getCallAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetCallResult(), 200)
            )));

        Response<CallConnectionProperties> getCallResultResponse = callConnectionAsync.getCallWithResponse().block();
        assertEquals(200, getCallResultResponse.getStatusCode());
        CallConnectionProperties getCallResult = getCallResultResponse.getValue();

        assertEquals(CALLBACK_URI.toString(), getCallResult.getCallbackUri());
        assertEquals(CALL_CONNECTION_ID, getCallResult.getCallConnectionId());
        assertEquals(SERVERCALL_LOCATOR.getServerCallId(), ((ServerCallLocator) getCallResult.getCallLocator()).getServerCallId());
        assertEquals(CallConnectionState.CONNECTED, getCallResult.getCallConnectionState());
        assertEquals(Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED), getCallResult.getRequestedCallEvents());
        assertEquals(Collections.singletonList(CallMediaType.AUDIO), getCallResult.getRequestedMediaTypes());
        assertEquals(Collections.singletonList(COMMUNICATION_USER), getCallResult.getTargets());
        assertEquals(COMMUNICATION_USER_1, getCallResult.getSource());
    }

    @Test
    public void getParticipantsAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantsResult(), 200)
            )));

        List<CallParticipant> getParticipantsResult = callConnectionAsync.getParticipants().block();

        assertEquals(2, getParticipantsResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantsResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantsResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantsResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantsResult.get(1).getIdentifier());
    }

    @Test
    public void getParticipantsAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantsResult(), 200)
            )));

        Response<List<CallParticipant>> getParticipantsResultResponse = callConnectionAsync.getParticipantsWithResponse(
        ).block();

        assertEquals(200, getParticipantsResultResponse.getStatusCode());
        List<CallParticipant> getParticipantsResult = getParticipantsResultResponse.getValue();
        assertEquals(2, getParticipantsResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantsResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantsResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantsResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantsResult.get(1).getIdentifier());
    }

    @Test
    public void getParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantResult(), 200)
            )));

        CallParticipant getParticipantResult = callConnectionAsync.getParticipant(COMMUNICATION_USER).block();

        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.getIdentifier());
        assertEquals(true, getParticipantResult.isMuted());
    }

    @Test
    public void getParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantResult(), 200)
            )));

        Response<CallParticipant> getParticipantResultResponse = callConnectionAsync.getParticipantWithResponse(
            COMMUNICATION_USER
        ).block();

        assertEquals(200, getParticipantResultResponse.getStatusCode());
        CallParticipant getParticipantResult = getParticipantResultResponse.getValue();
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.getIdentifier());
        assertEquals(true, getParticipantResult.isMuted());
    }

    @Test
    public void keepAliveAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.keepAlive().block();
    }

    @Test
    public void keepAliveAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelMediaOperationResponse = callConnectionAsync.keepAliveWithResponse().block();

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void playAudioToParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioToParticipantResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        PlayAudioResult playAudioResult = callConnectionAsync.playAudioToParticipant(
            COMMUNICATION_USER,
            URI.create("https://audioFileUri"),
            playAudioOptions
        ).block();

        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioToParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioToParticipantResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        Response<PlayAudioResult> playAudioResultResponse = callConnectionAsync.playAudioToParticipantWithResponse(
            COMMUNICATION_USER,
            URI.create("https://audioFileUri"),
            playAudioOptions
        ).block();

        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void cancelParticipantMediaOperationAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.cancelParticipantMediaOperation(
            COMMUNICATION_USER,
            OPERATION_ID
        ).block();
    }

    @Test
    public void cancelParticipantMediaOperationAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelParticipantMediaOperationResponse = callConnectionAsync.cancelParticipantMediaOperationWithResponse(
            COMMUNICATION_USER,
            OPERATION_ID
        ).block();

        assertEquals(200, cancelParticipantMediaOperationResponse.getStatusCode());
    }

    @Test
    public void muteParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.muteParticipant(
            COMMUNICATION_USER
        ).block();
    }

    @Test
    public void muteParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelMediaOperationResponse = callConnectionAsync.muteParticipantWithResponse(
            COMMUNICATION_USER
        ).block();

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void unmuteParticipantAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.unmuteParticipant(
            COMMUNICATION_USER
        ).block();
    }

    @Test
    public void unmuteParticipantAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> unmuteParticipantResponse = callConnectionAsync.unmuteParticipantWithResponse(
            COMMUNICATION_USER
        ).block();

        assertEquals(200, unmuteParticipantResponse.getStatusCode());
    }

    @Test
    public void removeParticipantFromDefaultAudioGroupAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.removeParticipantFromDefaultAudioGroup(
            COMMUNICATION_USER
        ).block();
    }

    @Test
    public void removeParticipantFromDefaultAudioGroupAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> removeParticipantFromDefaultAudioGroupResponse = callConnectionAsync.removeParticipantFromDefaultAudioGroupWithResponse(
            COMMUNICATION_USER
        ).block();

        assertEquals(200, removeParticipantFromDefaultAudioGroupResponse.getStatusCode());
    }

    @Test
    public void addParticipantToDefaultAudioGroupAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.addParticipantToDefaultAudioGroup(
            COMMUNICATION_USER
        ).block();
    }

    @Test
    public void addParticipantToDefaultAudioGroupAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> addParticipantToDefaultAudioGroupResponse = callConnectionAsync.addParticipantToDefaultAudioGroupWithResponse(
            COMMUNICATION_USER
        ).block();

        assertEquals(200, addParticipantToDefaultAudioGroupResponse.getStatusCode());
    }

    @Test
    public void createAudioGroupAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateCreateAudioGroupResult(), 201)
            )));

        CreateAudioGroupResult createAudioGroupResult = callConnectionAsync.createAudioGroup(
            AudioRoutingMode.ONE_TO_ONE,
            Collections.singletonList(COMMUNICATION_USER)
        ).block();

        assertEquals(AUDIO_GROUPID, createAudioGroupResult.getAudioGroupId());
    }

    @Test
    public void createAudioGroupAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateCreateAudioGroupResult(), 201)
            )));

        Response<CreateAudioGroupResult> createAudioGroupResponse = callConnectionAsync.createAudioGroupWithResponse(
            AudioRoutingMode.ONE_TO_ONE,
            Collections.singletonList(COMMUNICATION_USER)
        ).block();

        assertEquals(201, createAudioGroupResponse.getStatusCode());
        CreateAudioGroupResult createAudioGroupResult = createAudioGroupResponse.getValue();
        assertEquals(AUDIO_GROUPID, createAudioGroupResult.getAudioGroupId());
    }

    @Test
    public void updateAudioGroupAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.updateAudioGroup(
            AUDIO_GROUPID,
            Arrays.asList(
                COMMUNICATION_USER,
                COMMUNICATION_USER_1
            )
        ).block();
    }

    @Test
    public void updateAudioGroupAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> updateAudioGroupResponse = callConnectionAsync.updateAudioGroupWithResponse(
            AUDIO_GROUPID,
            Arrays.asList(
                COMMUNICATION_USER,
                COMMUNICATION_USER_1
            )
        ).block();

        assertEquals(200, updateAudioGroupResponse.getStatusCode());
    }

    @Test
    public void getAudioGroupsAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetAudioGroupsResult(), 200)
            )));

        AudioGroupResult getAudioGroupsResult = callConnectionAsync.getAudioGroups(
            AUDIO_GROUPID
        ).block();

        assertEquals(AudioRoutingMode.ONE_TO_ONE, getAudioGroupsResult.getAudioRoutingMode());
        assertEquals(COMMUNICATION_USER, getAudioGroupsResult.getTargets().get(0));
    }

    @Test
    public void getAudioGroupsAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetAudioGroupsResult(), 200)
            )));

        Response<AudioGroupResult> getAudioGroupsResponse = callConnectionAsync.getAudioGroupsWithResponse(
            AUDIO_GROUPID
        ).block();

        assertEquals(200, getAudioGroupsResponse.getStatusCode());
        AudioGroupResult getAudioGroupsResult = getAudioGroupsResponse.getValue();
        assertEquals(AudioRoutingMode.ONE_TO_ONE, getAudioGroupsResult.getAudioRoutingMode());
        assertEquals(COMMUNICATION_USER, getAudioGroupsResult.getTargets().get(0));
    }

    @Test
    public void deleteAudioGroupAsync() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnectionAsync.deleteAudioGroup(
            AUDIO_GROUPID
        ).block();
    }

    @Test
    public void deleteAudioGroupAsyncWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> updateAudioGroupResponse = callConnectionAsync.deleteAudioGroupWithResponse(
            AUDIO_GROUPID
        ).block();

        assertEquals(200, updateAudioGroupResponse.getStatusCode());
    }
}
