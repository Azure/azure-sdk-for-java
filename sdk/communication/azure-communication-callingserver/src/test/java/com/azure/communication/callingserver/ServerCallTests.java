// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallRecordingStateResult;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
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

    // Calling Tests
    private String fromUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-93fa-bc5c-28c5-593a0d000f2c";
    private String alternateId =   "+11111111111";
    private String to =   "+11111111111";
    private String callBackUri = "https://host.app/api/callback/calling";



    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctions(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctions");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        try {

            StartCallRecordingResult startCallRecordingResult = serverCall.startRecording(recordingStateCallbackUri);
            recordingId = startCallRecordingResult.getRecordingId();
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
    public void runAllClientFunctionsWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctionsWithResponse");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        try {
            Response<StartCallRecordingResult> response = serverCall.startRecordingWithResponse(recordingStateCallbackUri, Context.NONE);
            assertEquals(response.getStatusCode(), 200);
            StartCallRecordingResult startCallRecordingResult = response.getValue();
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.ACTIVE);

            Response<Void> pauseResponse = serverCall.pauseRecordingWithResponse(recordingId, null);
            assertEquals(pauseResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.INACTIVE);

            Response<Void> resumeResponse = serverCall.resumeRecordingWithResponse(recordingId, Context.NONE);
            assertEquals(resumeResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            Response<Void> stopResponse = serverCall.stopRecordingWithResponse(recordingId, Context.NONE);
            assertEquals(stopResponse.getStatusCode(), 200);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunction(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunction");
        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            PlayAudioResult playAudioResult = serverCall.playAudio(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunctionWithResponse");
        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            Response<PlayAudioResult> playAudioResult = serverCall.playAudioWithResponse(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext, null);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFails(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "startRecordingFails");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCall serverCall = callingServerClient.initializeServerCall(invalidServerCallId);

        try {
            Response<StartCallRecordingResult> response = serverCall.startRecordingWithResponse(recordingStateCallbackUri, Context.NONE);
        } catch (CallingServerErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenario");
        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Get Server Call
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L1ktWjZ5dzFzWVVTUUdWX2xPQWk1X2c_aT0xJmU9NjM3NTg0MzkzMzg3ODg3MDI3";
            ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            serverCall.addParticipant(new CommunicationUserIdentifier(invitedUser), null, operationContext, callBackUri);

            // Remove User
            String participantId = "72647661-033a-4d1a-b858-465375977be0";
            serverCall.removeParticipant(participantId);

            // Hangup
            callConnection.hangup();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Get Server Call
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L1lXS2R2TTNRc0Vpc0VNYVUtNlhvSlE_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> addResponse = serverCall.addParticipantWithResponse(new CommunicationUserIdentifier(invitedUser), null, operationContext, callBackUri, Context.NONE);
            CallingServerTestUtils.validateResponse(addResponse);

            // Remove User
            String participantId = "76b33acb-5097-4af0-a646-e07ccee48957";
            Response<Void> removeResponse = serverCall.removeParticipantWithResponse(participantId, Context.NONE);
            CallingServerTestUtils.validateResponse(removeResponse);

            // Hangup
            callConnection.hangup();
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

    private void validateCallRecordingState(ServerCall serverCall,
            String recordingId,
            CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCall);
        assertNotNull(serverCall.getServerCallId());
        assertNotNull(recordingId);

        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        CallRecordingStateResult callRecordingStateResult = serverCall.getRecordingState(recordingId);
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }

    protected void validateCallRecordingStateWithResponse(ServerCall serverCall,
            String recordingId,
            CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCall);
        assertNotNull(serverCall.getServerCallId());
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        Response<CallRecordingStateResult> response = serverCall.getRecordingStateWithResponse(recordingId, Context.NONE);
        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertNotNull(response.getValue());
        assertEquals(response.getValue().getRecordingState(), expectedCallRecordingState);
    }
}
