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
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode. The runAllClientFunctions and runAllClientFunctionsWithResponse
 * test will not run in LIVE or RECORD as they cannot get their own conversationId.
 */
public class ServerCallTests extends CallingServerTestBase {
    private String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctions(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctions");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        try {

            StartCallRecordingResponse startCallRecordingResponse = serverCall.startRecording(recordingStateCallbackUri);
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.ACTIVE);

            serverCall.pauseRecording(recordingId);
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.INACTIVE);

            serverCall.resumeRecording(recordingId);
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            serverCall.stopRecording(recordingId);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctionsWithResponse");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        try {
            Response<StartCallRecordingResponse> response = serverCall.startRecordingWithResponse(recordingStateCallbackUri, Context.NONE);
            assertEquals(response.getStatusCode(), 200);
            StartCallRecordingResponse startCallRecordingResponse = response.getValue();
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.ACTIVE);

            serverCall.pauseRecording(recordingId);
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.INACTIVE);

            serverCall.resumeRecording(recordingId);
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            serverCall.stopRecording(recordingId);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunction(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunction");
        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            PlayAudioResponse playAudioResponse = serverCall.playAudio(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResponse, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunctionWithResponse");
        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            Response<PlayAudioResponse> playAudioResponse = serverCall.playAudioWithResponse(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext, Context.NONE);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFails(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "startRecordingFails");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCall serverCall = callingServerClient.initializeServerCall(invalidServerCallId);

        try {
            Response<StartCallRecordingResponse> response = serverCall.startRecordingWithResponse(recordingStateCallbackUri, Context.NONE);
        } catch (CallingServerErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenario(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddScenario");
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        try {
            // Add User
            String conversationId = "aHR0cHM6Ly9jb252LXVzd2UtMDItc2RmLWFrcy5jb252LnNreXBlLmNvbS9jb252L3VEWHc4M1FsdUVtcG03TlVybElaTVE_aT0xMC02MC0zLTIwNyZlPTYzNzU4MjU1MTI1OTkzMzg5Ng";
            String participant = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-756c-41ce-ac00-343a0d001b58";
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            String callBackUri = "https://host.app/api/callback/calling";
            serverCall.addParticipant(new CommunicationUserIdentifier(participant), null, operationContext, callBackUri);

            // Remove User
            String participantId = "ebe62cd6-2085-4faf-8ceb-38a9a4482de8";
            serverCall.removeParticipant(participantId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenarioWithResponse");
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        try {
            // Add User
            String conversationId = "aHR0cHM6Ly9jb252LXVzd2UtMDItc2RmLWFrcy5jb252LnNreXBlLmNvbS9jb252L3VEWHc4M1FsdUVtcG03TlVybElaTVE_aT0xMC02MC0zLTIwNyZlPTYzNzU4MjU1MTI1OTkzMzg5Ng";
            String participant = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-756c-41ce-ac00-343a0d001b58";
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            String callBackUri = "https://host.app/api/callback/calling";
            Response<Void> addResponse = serverCall.addParticipantWithResponse(new CommunicationUserIdentifier(participant), null, operationContext, callBackUri, Context.NONE);
            CallingServerTestUtils.validateResponse(addResponse);

            // Remove User
            String participantId = "2a7155ef-a580-49cd-bcee-2ae4e8cdc602";
            Response<Void> removeResponse = serverCall.removeParticipantWithResponse(participantId, Context.NONE);
            CallingServerTestUtils.validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    private CallingServerClient setupClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private void validateCallRecordingState(ServerCall serverCall, String recordingId, CallRecordingState expectedCallRecordingState) throws InterruptedException {
        assertNotNull(recordingId);
        assertNotNull(serverCall.getServerCallId());

        /**
         * There is a delay bewteen the action and when the state is available.
         * Waiting to make sure we get the updated state, when we are running
         * against a live service.
         */
        sleepIfRunningAgainstService(6000);

        CallRecordingStateResponse callRecordingStateResult = serverCall.getRecordingState(recordingId);
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }
}
