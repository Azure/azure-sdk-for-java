// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServerCallAsyncLiveTests extends CallingServerTestBase {

    private final String fromUser = getRandomUserId();
    private final String toUser = getRandomUserId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsForConnectionStringClient(HttpClient httpClient) {
        GroupCallLocator groupCallLocator = new GroupCallLocator(getGroupId("runAllClientFunctionsForConnectionStringClient"));
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsForConnectionStringClient");
        runAllClientFunctionsAsync(groupCallLocator, callingServerAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsForTokenCredentialClient(HttpClient httpClient) {
        GroupCallLocator groupCallLocator = new GroupCallLocator(getGroupId("runAllClientFunctionsForTokenCredentialClient"));
        CallingServerClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsForTokenCredentialClient");
        runAllClientFunctionsAsync(groupCallLocator, callingServerAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    private  void runAllClientFunctionsAsync(CallLocator callLocator, CallingServerAsyncClient callingServerAsyncClient) {
        GroupCallLocator groupCallLocator = new GroupCallLocator(getGroupId("runAllClientFunctionsAsync"));
        String recordingId = "";
        List<CallConnectionAsync> callConnections = new ArrayList<>();

        try {
            callConnections = createAsyncCall(callingServerAsyncClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));

            StartCallRecordingResult startCallRecordingResult = callingServerAsyncClient.startRecording(groupCallLocator, URI.create(CALLBACK_URI)).block();
            assert startCallRecordingResult != null;
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingState(callingServerAsyncClient, recordingId, CallRecordingState.ACTIVE);

            callingServerAsyncClient.pauseRecording(recordingId).block();
            validateCallRecordingState(callingServerAsyncClient, recordingId, CallRecordingState.INACTIVE);

            callingServerAsyncClient.resumeRecording(recordingId).block();
            validateCallRecordingState(callingServerAsyncClient, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (callingServerAsyncClient != null) {
                try {
                    callingServerAsyncClient.stopRecording(recordingId).block();
                } catch (Exception e) {
                    System.out.println("Error stopping recording: " + e.getMessage());
                }
            }

            cleanUpConnectionsAsync(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponseAsync(HttpClient httpClient) {
        GroupCallLocator groupCallLocator = new GroupCallLocator(getGroupId("runAllClientFunctionsWithResponseAsync"));
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient =
            setupAsyncClient(builder, "runAllClientFunctionsWithResponseAsync");
        String recordingId = "";
        List<CallConnectionAsync> callConnections = new ArrayList<>();

        try {
            callConnections = createAsyncCall(callingServerAsyncClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));

            Response<StartCallRecordingResult> startRecordingResponse =
                callingServerAsyncClient.startRecordingWithResponse(groupCallLocator, URI.create(CALLBACK_URI), null, null).block();
            assert startRecordingResponse != null;
            assertEquals(startRecordingResponse.getStatusCode(), 200);
            StartCallRecordingResult startCallRecordingResult = startRecordingResponse.getValue();
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingStateWithResponse(callingServerAsyncClient, recordingId, CallRecordingState.ACTIVE);

            Response<Void> pauseResponse = callingServerAsyncClient.pauseRecordingWithResponse(recordingId).block();
            assert pauseResponse != null;
            assertEquals(pauseResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(callingServerAsyncClient, recordingId, CallRecordingState.INACTIVE);

            Response<Void> resumeResponse = callingServerAsyncClient.resumeRecordingWithResponse(recordingId).block();
            assert resumeResponse != null;
            assertEquals(resumeResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(callingServerAsyncClient, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (callingServerAsyncClient != null) {
                try {
                    Response<Void> stopResponse = callingServerAsyncClient.stopRecordingWithResponse(recordingId).block();
                    assert stopResponse != null;
                    assertEquals(stopResponse.getStatusCode(), 200);
                } catch (Exception e) {
                    System.out.println("Error stopping recording: " + e.getMessage());
                }
            }

            cleanUpConnectionsAsync(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionAsync(HttpClient httpClient) {
        GroupCallLocator groupCallLocator = new GroupCallLocator(getGroupId("runPlayAudioFunctionAsync"));
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient =
            setupAsyncClient(builder, "runPlayAudioFunctionAsync");

        List<CallConnectionAsync> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createAsyncCall(callingServerAsyncClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));
            PlayAudioOptions options = new PlayAudioOptions()
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setOperationContext(operationContext)
                .setLoop(false);

            PlayAudioResult playAudioResult =
                callingServerAsyncClient.playAudio(groupCallLocator, URI.create(AUDIO_FILE_URI), options).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            cleanUpConnectionsAsync(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponseAsync(HttpClient httpClient) {
        GroupCallLocator groupCallLocator = new GroupCallLocator(getGroupId("runPlayAudioFunctionWithResponseAsync"));
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient =
            setupAsyncClient(builder, "runPlayAudioFunctionWithResponseAsync");

        List<CallConnectionAsync> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createAsyncCall(callingServerAsyncClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));

            PlayAudioOptions options = new PlayAudioOptions();
            options.setAudioFileId(UUID.randomUUID().toString());
            options.setCallbackUri(URI.create(CALLBACK_URI));
            options.setOperationContext(operationContext);
            options.setLoop(false);

            Response<PlayAudioResult> playAudioResult =
                callingServerAsyncClient.playAudioWithResponse(
                    groupCallLocator,
                    URI.create(AUDIO_FILE_URI),
                    options).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            cleanUpConnectionsAsync(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFailsAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "startRecordingFailsAsync");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        ServerCallLocator serverCallLocator = new ServerCallLocator(invalidServerCallId);

        try {
            Response<StartCallRecordingResult> response =
                callingServerAsyncClient.startRecordingWithResponse(serverCallLocator, URI.create(CALLBACK_URI), null, null).block();
            assert response != null;
            assertEquals(response.getStatusCode(), 400);
        } catch (CallingServerErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runAddRemoveScenarioAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient =
            setupAsyncClient(builder, "runAddRemoveScenarioAsync");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options).block();

        CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

        try {
            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            // serverCallId looks like this: "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3VodHNzZEZ3NFVHX1J4d1lHYWlLRmc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4"
            String serverCallId = CallLocatorConverter.convert(callConnectionAsync.getCall().block().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier("8:acs:" + AZURE_TENANT_ID + "_" + "0000000d-4093-ebc3-d6d4-4448220005fd");

            String operationContext = UUID.randomUUID().toString();
            AddParticipantResult addParticipantResult = callingServerAsyncClient
                .addParticipant(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext)
                .block();

            assert addParticipantResult != null;

            // Remove User
            callingServerAsyncClient.removeParticipant(serverCallLocator, addedUser).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            assert callConnectionAsync != null;
            callConnectionAsync.hangup().block();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runAddRemoveScenarioWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioWithResponseAsync");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options).block();

        CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

        try {
            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            // serverCallId looks like this: "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3VodHNzZEZ3NFVHX1J4d1lHYWlLRmc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4"
            String serverCallId = CallLocatorConverter.convert(callConnectionAsync.getCall().block().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier("8:acs:" + AZURE_TENANT_ID + "_" + "0000000d-4093-ebc3-d6d4-4448220005fd");

            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(toUser);
            Response<AddParticipantResult> addParticipantResultResponse =
                callingServerAsyncClient
                    .addParticipantWithResponse(
                        serverCallLocator,
                        addedUser,
                        URI.create(CALLBACK_URI),
                        null,
                        operationContext)
                    .block();
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResultResponse);

            // Remove User
            Response<Void> removeResponse = callingServerAsyncClient.removeParticipantWithResponse(serverCallLocator, addedUser).block();
            CallingServerTestUtils.validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            assert callConnectionAsync != null;
            callConnectionAsync.hangup().block();
        }
    }

    private CallingServerAsyncClient setupAsyncClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private void validateCallRecordingState(CallingServerAsyncClient callingServerAsyncClient,
            String recordingId,
            CallRecordingState expectedCallRecordingState) {
        assertNotNull(callingServerAsyncClient);
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        CallRecordingProperties callRecordingStateResult = callingServerAsyncClient.getRecordingState(recordingId).block();
        assert callRecordingStateResult != null;
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }

    protected void validateCallRecordingStateWithResponse(
        CallingServerAsyncClient callingServerAsyncClient,
        String recordingId,
        CallRecordingState expectedCallRecordingState) {
        assertNotNull(callingServerAsyncClient);
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        Response<CallRecordingProperties> response =
            callingServerAsyncClient.getRecordingStateWithResponse(recordingId).block();
        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertNotNull(response.getValue());
        assertEquals(response.getValue().getRecordingState(), expectedCallRecordingState);
    }

    protected void cleanUpConnectionsAsync(List<CallConnectionAsync> connections) {
        if (connections == null) {
            return;
        }

        connections.forEach(c -> {
            if (c != null) {
                try {
                    c.hangup().block();
                } catch (Exception e) {
                    System.out.println("Error hanging up: " + e.getMessage());
                }
            }
        });
    }
}
