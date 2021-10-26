// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.communication.callingserver.CallingServerResponseMocker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

public class CallConnectionUnitTests {

    @Test
    public void createConnection() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
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

        CallConnection callConnection = callingServerClient.createCallConnection(sourceUser, targetUsers, options);
        assertEquals(CALL_CONNECTION_ID, callConnection.getCallConnectionId());
    }

    @Test
    public void createConnectionWithResponse() {
        CallingServerClient callingServerClient = getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
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

        Response<CallConnection> callConnectionAsyncResponse = callingServerClient.createCallConnectionWithResponse(
            sourceUser,
            targetUsers,
            options,
            Context.NONE);
        assertEquals(201, callConnectionAsyncResponse.getStatusCode());
        assertNotNull(callConnectionAsyncResponse.getValue());
    }

    @Test
    public void playAudio() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        PlayAudioResult playAudioResult = callConnection.playAudio(URI.create("https://audioFileUri"), playAudioOptions);
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        Response<PlayAudioResult> playAudioResultResponse = callConnection.playAudioWithResponse(URI.create("https://audioFileUri"), playAudioOptions, Context.NONE);
        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void hangup() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnection.hangup();
    }

    @Test
    public void hangupWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> hangupResponse = callConnection.hangupWithResponse(Context.NONE);

        assertEquals(202, hangupResponse.getStatusCode());
    }

    @Test
    public void cancelAllMediaOperations() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.cancelAllMediaOperations();
    }

    @Test
    public void cancelAllMediaOperationsWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelMediaOperationResponse = callConnection.cancelAllMediaOperationsWithResponse(Context.NONE);

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void addParticipant() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateAddParticipantResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        AddParticipantResult addParticipantResult = callConnection.addParticipant(
            user,
            "alternateCallerId",
            OPERATION_CONTEXT,
            CALLBACK_URI
        );
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void addParticipantWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateAddParticipantResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        Response<AddParticipantResult> addParticipantResultResponse = callConnection.addParticipantWithResponse(
            user,
            "alternateCallerId",
            OPERATION_CONTEXT,
            CALLBACK_URI,
            Context.NONE
        );
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void removeParticipant() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnection.removeParticipant(new CommunicationUserIdentifier(NEW_PARTICIPANT_ID));
    }

    @Test
    public void removeParticipantWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> removeParticipantResponse = callConnection.removeParticipantWithResponse(
            new CommunicationUserIdentifier(NEW_PARTICIPANT_ID),
            Context.NONE
        );
        assertEquals(202, removeParticipantResponse.getStatusCode());
    }

    @Test
    public void transferCall() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnection.transferCall(
            new CommunicationUserIdentifier(NEW_PARTICIPANT_ID),
            CALL_CONNECTION_ID,
            ""
        );
    }

    @Test
    public void transferCallWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> transferCallResponse = callConnection.transferCallWithResponse(
            new CommunicationUserIdentifier(NEW_PARTICIPANT_ID),
            CALL_CONNECTION_ID,
            "",
            Context.NONE
        );
        assertEquals(202, transferCallResponse.getStatusCode());
    }

    @Test
    public void getCall() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetCallResult(), 200)
            )));

        CallConnectionProperties getCallResult = callConnection.getCall();

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
    public void getCallWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetCallResult(), 200)
            )));

        Response<CallConnectionProperties> getCallResultResponse = callConnection.getCallWithResponse(Context.NONE);
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
    public void getParticipants() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantsResult(), 200)
            )));

        List<CallParticipant> getParticipantsResult = callConnection.getParticipants();

        assertEquals(2, getParticipantsResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantsResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantsResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantsResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantsResult.get(1).getIdentifier());
    }

    @Test
    public void getParticipantsWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantsResult(), 200)
            )));

        Response<List<CallParticipant>> getParticipantsResultResponse = callConnection.getParticipantsWithResponse(
            Context.NONE
        );

        assertEquals(200, getParticipantsResultResponse.getStatusCode());
        List<CallParticipant> getParticipantsResult = getParticipantsResultResponse.getValue();
        assertEquals(2, getParticipantsResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantsResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantsResult.get(0).getIdentifier());
        assertEquals(NEW_PARTICIPANT_ID_1, getParticipantsResult.get(1).getParticipantId());
        assertEquals(COMMUNICATION_USER_1, getParticipantsResult.get(1).getIdentifier());
    }

    @Test
    public void getParticipant() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantResult(), 200)
            )));

        List<CallParticipant> getParticipantResult = callConnection.getParticipant(COMMUNICATION_USER);

        assertEquals(1, getParticipantResult.size());
        assertEquals(NEW_PARTICIPANT_ID, getParticipantResult.get(0).getParticipantId());
        assertEquals(COMMUNICATION_USER, getParticipantResult.get(0).getIdentifier());
        assertEquals(true, getParticipantResult.get(0).isMuted());
    }

    @Test
    public void getParticipantWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateGetParticipantResult(), 200)
            )));

        Response<List<CallParticipant>> getParticipantResultResponse = callConnection.getParticipantWithResponse(
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
    public void keepAlive() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.keepAlive();
    }

    @Test
    public void keepAliveWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelMediaOperationResponse = callConnection.keepAliveWithResponse(Context.NONE);

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void playAudioToParticipant() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioToParticipantResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        PlayAudioResult playAudioResult = callConnection.playAudioToParticipant(
            COMMUNICATION_USER,
            URI.create("https://audioFileUri"),
            playAudioOptions
        );

        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioToParticipantWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generatePlayAudioToParticipantResult(), 202)
            )));

        PlayAudioOptions playAudioOptions = new PlayAudioOptions()
            .setAudioFileId("audioFileId")
            .setCallbackUri(CALLBACK_URI)
            .setLoop(true);
        Response<PlayAudioResult> playAudioResultResponse = callConnection.playAudioToParticipantWithResponse(
            COMMUNICATION_USER,
            URI.create("https://audioFileUri"),
            playAudioOptions,
            Context.NONE
        );

        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void cancelParticipantMediaOperation() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.cancelParticipantMediaOperation(
            COMMUNICATION_USER,
            OPERATION_ID
        );
    }

    @Test
    public void cancelParticipantMediaOperationWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelParticipantMediaOperationResponse = callConnection.cancelParticipantMediaOperationWithResponse(
            COMMUNICATION_USER,
            OPERATION_ID,
            Context.NONE
        );

        assertEquals(200, cancelParticipantMediaOperationResponse.getStatusCode());
    }

    @Test
    public void muteParticipant() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.muteParticipant(
            COMMUNICATION_USER
        );
    }

    @Test
    public void muteParticipantWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> cancelMediaOperationResponse = callConnection.muteParticipantWithResponse(
            COMMUNICATION_USER,
            Context.NONE
        );

        assertEquals(200, cancelMediaOperationResponse.getStatusCode());
    }

    @Test
    public void unmuteParticipant() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.unmuteParticipant(
            COMMUNICATION_USER
        );
    }

    @Test
    public void unmuteParticipantWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> unmuteParticipantResponse = callConnection.unmuteParticipantWithResponse(
            COMMUNICATION_USER,
            Context.NONE
        );

        assertEquals(200, unmuteParticipantResponse.getStatusCode());
    }

    @Test
    public void holdParticipantMeetingAudio() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.holdParticipantMeetingAudio(
            COMMUNICATION_USER
        );
    }

    @Test
    public void holdParticipantMeetingAudioWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> holdParticipantMeetingAudioResponse = callConnection.holdParticipantMeetingAudioWithResponse(
            COMMUNICATION_USER,
            Context.NONE
        );

        assertEquals(200, holdParticipantMeetingAudioResponse.getStatusCode());
    }

    @Test
    public void resumeParticipantMeetingAudio() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.resumeParticipantMeetingAudio(
            COMMUNICATION_USER
        );
    }

    @Test
    public void resumeParticipantMeetingAudioWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> resumeParticipantMeetingAudioResponse = callConnection.resumeParticipantMeetingAudioWithResponse(
            COMMUNICATION_USER,
            Context.NONE
        );

        assertEquals(200, resumeParticipantMeetingAudioResponse.getStatusCode());
    }

    @Test
    public void createAudioRoutingGroup() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateCreateAudioRoutingGroupResult(), 201)
            )));

        CreateAudioRoutingGroupResult createAudioRoutingGroupResult = callConnection.createAudioRoutingGroup(
            AudioRoutingMode.ONE_TO_ONE,
            Collections.singletonList(COMMUNICATION_USER)
        );

        assertEquals(AUDIOROUTING_GROUPID, createAudioRoutingGroupResult.getAudioRoutingGroupId());
    }

    @Test
    public void createAudioRoutingGroupWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(generateCreateAudioRoutingGroupResult(), 201)
            )));

        Response<CreateAudioRoutingGroupResult> createAudioRoutingGroupResponse = callConnection.createAudioRoutingGroupWithResponse(
            AudioRoutingMode.ONE_TO_ONE,
            Collections.singletonList(COMMUNICATION_USER),
            Context.NONE
        );

        assertEquals(201, createAudioRoutingGroupResponse.getStatusCode());
        CreateAudioRoutingGroupResult createAudioRoutingGroupResult = createAudioRoutingGroupResponse.getValue();
        assertEquals(AUDIOROUTING_GROUPID, createAudioRoutingGroupResult.getAudioRoutingGroupId());
    }

    @Test
    public void updateAudioRoutingGroup() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        callConnection.updateAudioRoutingGroup(
            AUDIOROUTING_GROUPID,
            Arrays.asList(
                COMMUNICATION_USER,
                COMMUNICATION_USER_1
            )
        );
    }

    @Test
    public void updateAudioRoutingGroupWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 200)
            )));

        Response<Void> updateAudioRoutingGroupResponse = callConnection.updateAudioRoutingGroupWithResponse(
            AUDIOROUTING_GROUPID,
            Arrays.asList(
                COMMUNICATION_USER,
                COMMUNICATION_USER_1
            ),
            Context.NONE
        );

        assertEquals(200, updateAudioRoutingGroupResponse.getStatusCode());
    }

    @Test
    public void deleteAudioRoutingGroup() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        callConnection.deleteAudioRoutingGroup(
            AUDIOROUTING_GROUPID
        );
    }

    @Test
    public void deleteAudioRoutingGroupWithResponse() {
        CallConnection callConnection = getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>("", 202)
            )));

        Response<Void> deleteAudioRoutingGroupResponse = callConnection.deleteAudioRoutingGroupWithResponse(
            AUDIOROUTING_GROUPID,
            Context.NONE
        );

        assertEquals(202, deleteAudioRoutingGroupResponse.getStatusCode());
    }
}
