// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode. The runAllClientFunctions and runAllClientFunctionsWithResponse
 * test will not run in LIVE or RECORD as they cannot get their own conversationId.
 */
public class ServerCallAsyncTests extends CallingServerTestBase {
    private String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsAsync");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        try {
            StartCallRecordingResponse startCallRecordingResponse = serverCallAsync.startRecording(recordingStateCallbackUri).block();
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(serverCallAsync, recordingId, CallRecordingState.ACTIVE);

            serverCallAsync.pauseRecording(recordingId).block();
            validateCallRecordingState(serverCallAsync, recordingId, CallRecordingState.INACTIVE);

            serverCallAsync.resumeRecording(recordingId).block();
            validateCallRecordingState(serverCallAsync, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            serverCallAsync.stopRecording(recordingId).block();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsWithResponseAsync");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);

        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        try {
            Response<StartCallRecordingResponse> response = serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri).block();
            assertEquals(response.getStatusCode(), 200);
            StartCallRecordingResponse startCallRecordingResponse = response.getValue();
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(serverCallAsync, recordingId, CallRecordingState.ACTIVE);

            serverCallAsync.pauseRecording(recordingId).block();
            validateCallRecordingState(serverCallAsync, recordingId, CallRecordingState.INACTIVE);

            serverCallAsync.resumeRecording(recordingId).block();
            validateCallRecordingState(serverCallAsync, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            serverCallAsync.stopRecording(recordingId).block();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionAsync");

        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);

        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);
        try {
            PlayAudioResponse playAudioResponse = serverCallAsync.playAudio(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResponse, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionWithResponseAsync");

        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            Response<PlayAudioResponse> playAudioResponse = serverCallAsync.playAudioWithResponse(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFailsAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "startRecordingFailsAsync");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(invalidServerCallId);

        try {
            Response<StartCallRecordingResponse> response = serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri).block();
        } catch (CallingServerErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioAsync");
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);
        try {
            // Add User
            String participant = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-756c-41ce-ac00-343a0d001b58";
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            String callBackUri = "https://host.app/api/callback/calling";
            serverCallAsync.addParticipant(new CommunicationUserIdentifier(participant), null, operationContext, callBackUri).block();

            // Remove User
            String participantId = "2bbea6fd-e898-4296-ba38-7a6a1a5f697f";
            serverCallAsync.removeParticipant(participantId).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioWithResponseAsync");
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);
        try {
            // Add User
            String participant = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-756c-41ce-ac00-343a0d001b58";
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            String callBackUri = "https://host.app/api/callback/calling";
            Response<Void> addResponse = serverCallAsync.addParticipantWithResponse(new CommunicationUserIdentifier(participant), null, operationContext, callBackUri).block();
            CallingServerTestUtils.validateResponse(addResponse);

            // Remove User
            String participantId = "d5f93caf-2e3f-4c82-a395-8323fb9e08c6";
            Response<Void> removeResponse = serverCallAsync.removeParticipantWithResponse(participantId).block();
            CallingServerTestUtils.validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    private CallingServerAsyncClient setupAsyncClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private void validateCallRecordingState(ServerCallAsync serverCallAsync, String recordingId, CallRecordingState expectedCallRecordingState) throws InterruptedException {
        assertNotNull(serverCallAsync);
        assertNotNull(serverCallAsync.getServerCallId());
        assertNotNull(recordingId);

        /**
         * There is a delay bewteen the action and when the state is available.
         * Waiting to make sure we get the updated state, when we are running
         * against a live service.
         */
        sleepIfRunningAgainstService(6000);

        CallRecordingStateResponse callRecordingStateResult = serverCallAsync.getRecordingState(recordingId).block();
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }
}
