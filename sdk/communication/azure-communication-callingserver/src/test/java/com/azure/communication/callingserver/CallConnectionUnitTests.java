// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.implementation.models.CallingOperationResultDetailsInternal;
import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

public class CallConnectionUnitTests {

    static final String CALL_CONNECTION_ID = "callConnectionId";
    static final String OPERATION_ID = "operationId";
    static final String NEW_PARTICIPANT_ID = "newParticipantId";

    @Test
    public void createConnectionWithResponse() {
        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateCreateCallResult(CallingServerResponseMocker.CALL_CONNECTION_ID), 201)
            )));

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        targetUsers.add(new CommunicationUserIdentifier("id2"));

        CreateCallOptions options = new CreateCallOptions(
            URI.create("https://callbackUri"),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        Response<CallConnection> callConnectionAsyncResponse = callingServerClient.createCallConnectionWithResponse(sourceUser, targetUsers, options, Context.NONE);
        assertEquals(201, callConnectionAsyncResponse.getStatusCode());
        assertNotNull(callConnectionAsyncResponse.getValue());
    }

    @Test
    public void getCallConnectionCallingServerClient() {
        CallingServerClient callingServerAsyncClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList()));

        CallConnection callConnection = callingServerAsyncClient.getCallConnection(CallingServerResponseMocker.CALL_CONNECTION_ID);
        assertNotNull(callConnection);
    }

    @Test
    public void playAudioWithResponse() {
        CallConnection callConnection = getCallConnection();

        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri")).setLoop(true);
        Response<PlayAudioResult> playAudioResultResponse = callConnection.playAudioWithResponse(URI.create("https://audioFileUri"), playAudioOptions, Context.NONE);
        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudio() {
        CallConnection callConnection = getCallConnection();

        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri")).setLoop(true);
        PlayAudioResult playAudioResult = callConnection.playAudio(URI.create("https://audioFileUri"), playAudioOptions);
        assertEquals(CallingOperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void appParticipantWithResponse() {
        CallConnection callConnection = getAddParticipantCallConnection();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        Response<AddParticipantResult> addParticipantResultResponse = callConnection.addParticipantWithResponse(user, "alternateCallerId", "operationContext", URI.create("https://callbackUri"), Context.NONE);
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipant() {
        CallConnection callConnection = getAddParticipantCallConnection();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        AddParticipantResult addParticipantResult = callConnection.addParticipant(user, "alternateCallerId", "operationContext", URI.create("https://callbackUri"));
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void joinCall() {
        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CallingServerResponseMocker.URI_CALLBACK,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        CallConnection callConnection = callingServerClient.joinCall(new ServerCallLocator(CallingServerResponseMocker.SERVER_CALL_ID), (CommunicationIdentifier) user, options);
        assertNotNull(callConnection);
    }

    @Test
    public void joinCallWithResponse() {
        CallingServerClient callingServerAsyncClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateJoinCallResult(NEW_PARTICIPANT_ID), 202)
            )));

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(NEW_PARTICIPANT_ID);
        JoinCallOptions options = new JoinCallOptions(
            CallingServerResponseMocker.URI_CALLBACK,
            Collections.singletonList(CallMediaType.VIDEO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        Response<CallConnection> callConnectionResponse = callingServerAsyncClient.joinCallWithResponse(new ServerCallLocator(CallingServerResponseMocker.SERVER_CALL_ID), (CommunicationIdentifier) user, options, Context.NONE);
        assertEquals(202, callConnectionResponse.getStatusCode());
        assertNotNull(callConnectionResponse.getValue());
    }

    private CallConnection getCallConnection() {
        return CallingServerResponseMocker.getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generatePlayAudioResult(
                    OPERATION_ID,
                    CallingOperationStatus.COMPLETED,
                    new CallingOperationResultDetailsInternal().setCode(202).setSubcode(0).setMessage("message")),
                    202)
            )));
    }

    private CallConnection getAddParticipantCallConnection() {
        return CallingServerResponseMocker.getCallConnection(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateCreateCallResult(CALL_CONNECTION_ID), 201),
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(NEW_PARTICIPANT_ID), 202)
            )));
    }
}
