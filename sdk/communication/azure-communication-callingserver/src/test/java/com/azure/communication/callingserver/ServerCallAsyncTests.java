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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode. The runAllClientFunctions and runAllClientFunctionsWithResponse
 * test will not run in LIVE or RECORD as they cannot get their own conversationId.
 */
public class ServerCallAsyncTests extends CallingServerTestBase {
    private String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    // Calling Tests
    private String fromUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-93fa-bc5c-28c5-593a0d000f2c";
    private String alternateId =   "+11111111111";
    private String to =   "+11111111111";
    private String callBackUri = "https://host.app/api/callback/calling";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsAsync");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        try {
            StartCallRecordingResult startCallRecordingResponse = serverCallAsync.startRecording(recordingStateCallbackUri).block();
            assert startCallRecordingResponse != null;
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
    public void runAllClientFunctionsWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsWithResponseAsync");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);

        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        try {
            Response<StartCallRecordingResult> response = serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri).block();
            assert response != null;
            assertEquals(response.getStatusCode(), 200);
            StartCallRecordingResult startCallRecordingResponse = response.getValue();
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
    public void runPlayAudioFunctionAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionAsync");

        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);

        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);
        try {
            PlayAudioResult playAudioResult = serverCallAsync.playAudio(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionWithResponseAsync");

        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            Response<PlayAudioResult> playAudioResult = serverCallAsync.playAudioWithResponse(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFailsAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "startRecordingFailsAsync");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(invalidServerCallId);

        try {
            Response<StartCallRecordingResult> response = serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri).block();
        } catch (CallingServerErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Get Server Call
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3NXdWxkazBmMEVpdnAxWjhiU2NuUHc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            serverCallAsync.addParticipant(new CommunicationUserIdentifier(invitedUser), null, operationContext, callBackUri).block();

            // Remove User
            String participantId = "206ac04a-1aae-4d82-9015-9c30cb174888";
            serverCallAsync.removeParticipant(participantId).block();

            // Hang up
            assert callConnectionAsync != null;
            callConnectionAsync.hangup().block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioWithResponseAsync");
        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Get Server Call
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L0NUT014YmNIRmttZ1BqbE5kYjExNlE_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> addResponse = serverCallAsync.addParticipantWithResponse(new CommunicationUserIdentifier(invitedUser), null, operationContext, callBackUri).block();
            CallingServerTestUtils.validateResponse(addResponse);

            // Remove User
            String participantId = "b133b1f3-4a11-49e4-abe0-ac9fdd660634";
            Response<Void> removeResponse = serverCallAsync.removeParticipantWithResponse(participantId).block();
            CallingServerTestUtils.validateResponse(removeResponse);

            // Hang up
            assert callConnectionAsync != null;
            callConnectionAsync.hangup().block();
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

    private void validateCallRecordingState(ServerCallAsync serverCallAsync,
                                            String recordingId,
                                            CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCallAsync);
        assertNotNull(serverCallAsync.getServerCallId());
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        CallRecordingStateResult callRecordingStateResult = serverCallAsync.getRecordingState(recordingId).block();
        assert callRecordingStateResult != null;
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }
}
