// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;

import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.CallingServerAsyncClient;
import com.azure.communication.callingserver.CallingServerClient;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

public class ServerCallUnitTests {
    private final ServerCallLocator serverCallLocator = new ServerCallLocator("aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM");
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";

    @Test
    public void startRecordingRelativeUriFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerClient().startRecording(serverCallLocator, URI.create("/not/absolute/uri")));
    }

    @Test
    public void startRecordingWithResponseRelativeUriFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerClient().startRecordingWithResponse(serverCallLocator, URI.create("/not/absolute/uri"), null));
    }

    @Test
    public void addParticipantNullParticipantFails() {
        assertThrows(
            NullPointerException.class,
            () -> getCallingServerClient().addParticipant(serverCallLocator, null, null, null, null));
    }

    @Test
    public void startRecordingAsyncFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> getCallingServerClient().startRecording(serverCallLocator, URI.create("/not/absolute/uri")));
    }

    @Test
    public void playAudioWithResponse() {
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri"));
        Response<PlayAudioResult> playAudioResultResponse = getCallingServerClient().playAudioWithResponse(serverCallLocator, URI.create("https://audioFileUri"), playAudioOptions, Context.NONE);
        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudio() {
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("callbackUri")).setOperationContext("operationContext");
        PlayAudioResult playAudioResult = getCallingServerClient().playAudio(serverCallLocator, URI.create("audioFileUri"), playAudioOptions);
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioWithResponseAsync() {
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri"));
        Response<PlayAudioResult> playAudioResultResponse = getCallingServerAsyncClient().playAudioWithResponse(serverCallLocator, URI.create("https://audioFileUri"), playAudioOptions).block();
        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioAsync() {
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri")).setOperationContext("operationContext");
        PlayAudioResult playAudioResult = getCallingServerAsyncClient().playAudio(serverCallLocator, URI.create("https://audioFileUri"), playAudioOptions).block();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void playAudioAsyncUsingOptions() {
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri(URI.create("https://callbackUri"));
        PlayAudioResult playAudioResult = getCallingServerAsyncClient().playAudio(serverCallLocator, URI.create("audioFileUri"), playAudioOptions).block();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());
    }

    @Test
    public void appParticipantServerCall() {
        var callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT.getId()), 202)
            ))
        );

        AddParticipantResult addParticipantResult = callingServerClient.addParticipant(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext");
        assertEquals(CallingServerResponseMocker.NEW_PARTICIPANT.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipantServerCallWithResponse() {
        var callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT.getId()), 202)
            ))
        );

        Response<AddParticipantResult> addParticipantResultResponse = callingServerClient.addParticipantWithResponse(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext", Context.NONE);
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(CallingServerResponseMocker.NEW_PARTICIPANT.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipantServerCallAsync() {
        var callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT.getId()), 202)
            ))
        );

        AddParticipantResult addParticipantResult = callingServerAsyncClient.addParticipant(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext").block();
        assertEquals(CallingServerResponseMocker.NEW_PARTICIPANT.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipantServerCallAsyncWithResponse() {
        var callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT.getId()), 202)
            ))
        );

        Response<AddParticipantResult> addParticipantResultResponse = callingServerAsyncClient.addParticipantWithResponse(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext").block();
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(CallingServerResponseMocker.NEW_PARTICIPANT.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void removeParticipantServerCall() {
        var callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerClient.removeParticipant(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT);
    }

    @Test
    public void removeParticipantServerCallWithResponse() {
        var callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> removeParticipantResultResponse = callingServerClient.removeParticipantWithResponse(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT, Context.NONE);
        assertEquals(202, removeParticipantResultResponse.getStatusCode());
    }

    @Test
    public void removeParticipantServerCallAsync() {
        var callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        callingServerAsyncClient.removeParticipant(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT).block();
    }

    @Test
    public void removeParticipantServerCallAsyncWithResponse() {
        var callingServerAsyncClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> removeParticipantResultResponse = callingServerAsyncClient.removeParticipantWithResponse(serverCallLocator, CallingServerResponseMocker.NEW_PARTICIPANT, Context.NONE).block();
        assertEquals(202, removeParticipantResultResponse.getStatusCode());
    }

    private CallingServerClient getCallingServerClient() {
        return new CallingServerClientBuilder()
        .httpClient(new NoOpHttpClient())
        .connectionString(MOCK_CONNECTION_STRING)
        .buildClient();
    }

    private CallingServerAsyncClient getCallingServerAsyncClient() {
        return new CallingServerClientBuilder()
        .httpClient(new NoOpHttpClient())
        .connectionString(MOCK_CONNECTION_STRING)
        .buildAsyncClient();
    }
}
