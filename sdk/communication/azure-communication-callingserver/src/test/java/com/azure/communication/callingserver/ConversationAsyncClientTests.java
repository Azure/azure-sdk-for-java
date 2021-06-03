// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.models.GetCallRecordingStateResponse;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode. The runAllClientFunctions and runAllClientFunctionsWithResponse
 * test will not run in LIVE or RECORD as they cannot get their own conversationId.
 */
public class ConversationAsyncClientTests extends CallingServerTestBase {
    private String conversationId = "aHR0cHM6Ly9jb252LXVzZWEtMDcuY29udi5za3lwZS5jb20vY29udi85M0FnUnVMbGdVdUU2MWdxa1pnaHVBP2k9NTEmZT02Mzc1NzY1NzUwOTIzMTQ3OTU";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsAsync");
        String recordingId = "";        
        URI recordingStateCallbackUri = new URI("https://dev.skype.net:6448");

        try {
            StartCallRecordingResponse startCallRecordingResponse = conversationAsyncClient.startRecording(conversationId, recordingStateCallbackUri).block();
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(conversationAsyncClient, conversationId, recordingId, CallRecordingState.ACTIVE);

            conversationAsyncClient.pauseRecording(conversationId, recordingId).block();
            validateCallRecordingState(conversationAsyncClient, conversationId, recordingId, CallRecordingState.INACTIVE);

            conversationAsyncClient.resumeRecording(conversationId, recordingId).block();
            validateCallRecordingState(conversationAsyncClient, conversationId, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            conversationAsyncClient.stopRecording(conversationId, recordingId).block();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsWithResponseAsync");
        String recordingId = "";        
        URI recordingStateCallbackUri = new URI("https://dev.skype.net:6448");
        System.out.println("conversationId: " + conversationId);

        try {
            Response<StartCallRecordingResponse> response = conversationAsyncClient.startRecordingWithResponse(conversationId, recordingStateCallbackUri).block();
            assertEquals(response.getStatusCode(), 200);
            StartCallRecordingResponse startCallRecordingResponse = response.getValue();
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(conversationAsyncClient, conversationId, recordingId, CallRecordingState.ACTIVE);

            conversationAsyncClient.pauseRecording(conversationId, recordingId).block();
            validateCallRecordingState(conversationAsyncClient, conversationId, recordingId, CallRecordingState.INACTIVE);

            conversationAsyncClient.resumeRecording(conversationId, recordingId).block();
            validateCallRecordingState(conversationAsyncClient, conversationId, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            conversationAsyncClient.stopRecording(conversationId, recordingId).block();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionAsync");
        
        var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";      
        String callbackUri = "https://dev.skype.net:6448";
        
        System.out.println("conversationId: " + conversationId);
        try {
            PlayAudioResponse playAudioResponse = conversationAsyncClient.playAudio(conversationId, audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResponse, operationContext);
           
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionWithResponseAsync");
        
        var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";      
        String callbackUri = "https://dev.skype.net:6448";
        
        System.out.println("conversationId: " + conversationId);
        try {
            Response<PlayAudioResponse> playAudioResponse = conversationAsyncClient.playAudioWithResponse(conversationId, audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);
           
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFailsAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "startRecordingFailsAsync");
        var invalidConversationId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";       
        URI recordingStateCallbackUri = new URI("https://dev.skype.net:6448");
        System.out.println("conversationId: " + invalidConversationId);

        try {
            Response<StartCallRecordingResponse> response = conversationAsyncClient.startRecordingWithResponse(invalidConversationId, recordingStateCallbackUri).block();
            assertEquals(response.getStatusCode(), 400);
        } catch (CommunicationErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    private ConversationAsyncClient setupAsyncClient(ConversationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
    
    protected ConversationClientBuilder addLoggingPolicy(ConversationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
    
    private void validateCallRecordingState(ConversationAsyncClient conversationAsyncClient, String conversationId, String recordingId, CallRecordingState expectedCallRecordingState) throws InterruptedException {
        assertNotNull(recordingId);
        assertNotNull(conversationId);

        /** 
         * There is a delay bewteen the action and when the state is available.
         * Waiting to make sure we get the updated state, when we are running
         * against a live service. 
         */
        sleepIfRunningAgainstService(6000);

        GetCallRecordingStateResponse callRecordingStateResult = conversationAsyncClient.getRecordingState(conversationId, recordingId).block();
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }
}
