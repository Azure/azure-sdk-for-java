// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.models.RecordingChannel;
import com.azure.communication.callingserver.models.RecordingContent;
import com.azure.communication.callingserver.models.RecordingFormat;
import com.azure.communication.callingserver.models.StartRecordingOptions;
import com.azure.communication.callingserver.implementation.models.ResultInfoInternal;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

public class ServerCallUnitTests {

    private final String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";

    @Test
    public void startRecordingRelativeUriFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecording("/not/absolute/uri"));
    }

    @Test
    public void startRecordingWithRecordingParamsRelativeUriFails() {
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions();
        startRecordingOptions.setRecordingChannel(RecordingChannel.MIXED);
        startRecordingOptions.setRecordingContent(RecordingContent.AUDIO_VIDEO);
        startRecordingOptions.setRecordingFormat(RecordingFormat.MP4);

        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecordingWithResponse("/not/absolute/uri", startRecordingOptions, null));
    }

    @Test
    public void startRecordingWithResponseRelativeUriFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecordingWithResponse("/not/absolute/uri", null, null));
    }

    @Test
    public void addParticipantNullParticipantFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            NullPointerException.class,
            () -> serverCall.addParticipant(null, null, null, null));
    }

    @Test
    public void startRecordingAsyncFails() {
        ServerCallAsync serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildAsyncClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecording("/not/absolute/uri")
                .block());
    }

    @Test
    public void startRecordingWithRecordingParamsAsyncFails() {
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions();
        startRecordingOptions.setRecordingChannel(RecordingChannel.MIXED);
        startRecordingOptions.setRecordingContent(RecordingContent.AUDIO_VIDEO);
        startRecordingOptions.setRecordingFormat(RecordingFormat.MP4);

        ServerCallAsync serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildAsyncClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecordingWithResponse("/not/absolute/uri", startRecordingOptions, null)
                .block());
    }

    @Test
    public void playAudioWithResponse() {
        ServerCall serverCall = getServerCall();
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri("callbackUri");  
        Response<PlayAudioResult> playAudioResultResponse = serverCall.playAudioWithResponse("audioFileUri", playAudioOptions, Context.NONE);
        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());        
    }

    @Test
    public void playAudio() {
        ServerCall serverCall = getServerCall();
       
        PlayAudioResult playAudioResult = serverCall.playAudio("audioFileUri", "audioFieldId", "callbackUri", "operationContext");
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());        
    }    
    
    @Test
    public void playAudioWithResponseAsync() {
        ServerCallAsync serverCall = getServerCallAsync();
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri("callbackUri");      
        Response<PlayAudioResult> playAudioResultResponse = serverCall.playAudioWithResponse("audioFileUri", playAudioOptions).block();
        assertEquals(202, playAudioResultResponse.getStatusCode());
        PlayAudioResult playAudioResult = playAudioResultResponse.getValue();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());        
    }

    @Test
    public void playAudioAsync() {
        ServerCallAsync serverCall = getServerCallAsync();
      
        PlayAudioResult playAudioResult = serverCall.playAudio("audioFileUri", "audioFieldId", "callbackUri", "operationContext").block();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());        
    }  

    @Test
    public void playAudioAsyncUsingOptions() {
        ServerCallAsync serverCall = getServerCallAsync();
      
        PlayAudioOptions playAudioOptions = new PlayAudioOptions().setAudioFileId("audioFileId").setCallbackUri("callbackUri");   
        PlayAudioResult playAudioResult = serverCall.playAudio("audioFileUri", playAudioOptions).block();
        assertEquals(OperationStatus.COMPLETED, playAudioResult.getStatus());        
    }  

    @Test
    public void appParticipantServerCall() {
        ServerCall serverCall = CallingServerResponseMocker.getServerCall(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT_ID), 202)
            ))
        );

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(CallingServerResponseMocker.NEW_PARTICIPANT_ID);
        AddParticipantResult addParticipantResult = serverCall.addParticipant(user, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext");
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipantServerCallWithResponse() {
        ServerCall serverCall = CallingServerResponseMocker.getServerCall(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT_ID), 202)
            ))
        );

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(CallingServerResponseMocker.NEW_PARTICIPANT_ID);
        Response<AddParticipantResult> addParticipantResultResponse = serverCall.addParticipantWithResponse(user, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext", Context.NONE);
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipantServerCallAsync() {
        ServerCallAsync serverCall = CallingServerResponseMocker.getServerCallAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT_ID), 202)
            ))
        );

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(CallingServerResponseMocker.NEW_PARTICIPANT_ID);
        AddParticipantResult addParticipantResult = serverCall.addParticipant(user, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext").block();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void appParticipantServerCallAsyncWithResponse() {
        ServerCallAsync serverCall = CallingServerResponseMocker.getServerCallAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateAddParticipantResult(CallingServerResponseMocker.NEW_PARTICIPANT_ID), 202)
            ))
        );

        CommunicationUserIdentifier user = new CommunicationUserIdentifier(CallingServerResponseMocker.NEW_PARTICIPANT_ID);
        Response<AddParticipantResult> addParticipantResultResponse = serverCall.addParticipantWithResponse(user, CallingServerResponseMocker.URI_CALLBACK, "alternateCallerId", "operationContext").block();
        assertEquals(202, addParticipantResultResponse.getStatusCode());
        AddParticipantResult addParticipantResult = addParticipantResultResponse.getValue();
        assertEquals(user.getId(), addParticipantResult.getParticipantId());
    }

    @Test
    public void removeParticipantServerCall() {
        ServerCall serverCall = CallingServerResponseMocker.getServerCall(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        serverCall.removeParticipant(CallingServerResponseMocker.NEW_PARTICIPANT_ID);
    }

    @Test
    public void removeParticipantServerCallWithResponse() {
        ServerCall serverCall = CallingServerResponseMocker.getServerCall(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> removeParticipantResultResponse = serverCall.removeParticipantWithResponse(CallingServerResponseMocker.NEW_PARTICIPANT_ID, Context.NONE);
        assertEquals(202, removeParticipantResultResponse.getStatusCode());
    }

    @Test
    public void removeParticipantServerCallAsync() {
        ServerCallAsync serverCall = CallingServerResponseMocker.getServerCallAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        serverCall.removeParticipant(CallingServerResponseMocker.NEW_PARTICIPANT_ID).block();
    }

    @Test
    public void removeParticipantServerCallAsyncWithResponse() {
        ServerCallAsync serverCall = CallingServerResponseMocker.getServerCallAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 202)
            ))
        );

        Response<Void> removeParticipantResultResponse = serverCall.removeParticipantWithResponse(CallingServerResponseMocker.NEW_PARTICIPANT_ID, Context.NONE).block();
        assertEquals(202, removeParticipantResultResponse.getStatusCode());
    }

    private ServerCall getServerCall() {
        return CallingServerResponseMocker.getServerCall(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generatePlayAudioResult(
                    CallingServerResponseMocker.OPERATION_ID, 
                    OperationStatus.COMPLETED, 
                    new ResultInfoInternal().setCode(202).setSubcode(0).setMessage("message")),
                    202)
            )));
    }

    private ServerCallAsync getServerCallAsync() {
        return CallingServerResponseMocker.getServerCallAsync(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generatePlayAudioResult(
                    CallingServerResponseMocker.OPERATION_ID, 
                    OperationStatus.COMPLETED, 
                    new ResultInfoInternal().setCode(202).setSubcode(0).setMessage("message")),
                    202)
            )));
    }     
}
