// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

public class CallConnectionAsyncUnitTests {

    static final String CALL_CONNECTION_ID = "callConnectionId";
    static final String OPERATION_ID = "operationId";
    static final String NEW_PARTICIPANT_ID = "newParticipantId";

    @Test
    public void createConnectionWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateCreateCallResult(CallingServerResponseMocker.CALL_CONNECTION_ID), 201)
            )));

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        targetUsers.add(new CommunicationUserIdentifier("id2"));

        CreateCallOptions options = new CreateCallOptions(
            URI.create("https://callbackUri.local"),
            Collections.singletonList(MediaType.AUDIO),
            Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

        Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.createCallConnectionWithResponse(sourceUser, targetUsers, options).block();
        assertEquals(201, callConnectionAsyncResponse.getStatusCode());
        assertNotNull(callConnectionAsyncResponse.getValue());
    }

    @Test
    public void getCallConnectionCallingServerAsyncClient() {
        CallingServerAsyncClient callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList()));

        CallConnectionAsync callConnection = callingServerAsyncClient.getCallConnection(CallingServerResponseMocker.CALL_CONNECTION_ID);
        assertNotNull(callConnection);
    }

    @Test
    public void playAudioWithResponse() {
        CallConnectionAsync callConnectionAsync = getPlayAudioCallConnection();

        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri.local"));
        Response<PlayAudioResult> playAudioResultResponse = callConnectionAsync.playAudioWithResponse(URI.create("https://audioFileUri.local"), playAudioOptions).block();
        assertEquals(202, playAudioResultResponse.getStatusCode());
    }

    @Test
    public void playAudio() {
        CallConnectionAsync callConnectionAsync = getPlayAudioCallConnection();

        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri.local"));
        PlayAudioResult playAudioResult = callConnectionAsync.playAudio(URI.create("https://audioFileUri.local"), playAudioOptions).block();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioWithoutPlayAudioOptions202() {
        CallConnectionAsync callConnectionAsync = getPlayAudioCallConnection();
        PlayAudioOptions options = new PlayAudioOptions()
            .setAudioFileId("audioFieldId")
            .setCallbackUri(URI.create("https://callbackUri.local"))
            .setLoop(false)
            .setOperationContext("operationContext");

        PlayAudioResult playAudioResult = callConnectionAsync.playAudio(URI.create("https://audioFileUri.local"), options).block();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void appParticipantWithResponse() {
        CallConnectionAsync callConnectionAsync = getAddParticipantCallConnection();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        Response<AddParticipantResult> addParticipantResultResponse = callConnectionAsync.addParticipantWithResponse(user, "alternateCallerId", "operationContext", URI.create("https://callbackUri.local"), Context.NONE).block();
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipant() {
        CallConnectionAsync callConnectionAsync = getAddParticipantCallConnection();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        AddParticipantResult addParticipantResult = callConnectionAsync.addParticipant(user, "alternateCallerId", "operationContext", URI.create("https://callbackUri.local")).block();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void joinCallWithResponse() {
        CallingServerAsyncClient callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CallingServerResponseMocker.URI_CALLBACK,
            Collections.singletonList(MediaType.VIDEO),
            Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));
        Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.joinCallWithResponse(new ServerCallLocator(CallingServerResponseMocker.SERVER_CALL_ID), (CommunicationIdentifier) user, options).block();
        assertEquals(202, callConnectionAsyncResponse.getStatusCode());
        assertNotNull(callConnectionAsyncResponse.getValue());
    }

    @Test
    public void joinCall() {
        CallingServerAsyncClient callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CallingServerResponseMocker.URI_CALLBACK,
            Collections.singletonList(MediaType.VIDEO),
            Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));
        CallConnectionAsync callConnectionAsync = callingServerAsyncClient.joinCall(new ServerCallLocator(CallingServerResponseMocker.SERVER_CALL_ID), (CommunicationIdentifier) user, options).block();
        assertNotNull(callConnectionAsync);
    }

    private CallConnectionAsync getPlayAudioCallConnection() {
        return CallingServerResponseMocker.getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generatePlayAudioResult(
                    OPERATION_ID,
                    OperationStatus.COMPLETED,
                    new ResultInfoInternal().setCode(202).setSubcode(0).setMessage("message")),
                    202)
            )));
    }

    private CallConnectionAsync getAddParticipantCallConnection() {
        return CallingServerResponseMocker.getCallConnectionAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(NEW_PARTICIPANT_ID), 202)
            )));
    }
}
