// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.azure.communication.callingserver.CallingServerTestUtils.validateCallConnection;
import static com.azure.communication.callingserver.CallingServerTestUtils.validatePlayAudioResponse;
import static com.azure.communication.callingserver.CallingServerTestUtils.validatePlayAudioResult;
import static com.azure.communication.callingserver.CallingServerTestUtils.validateResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServerCallLiveTests extends CallingServerTestBase {

    private final String fromUser = getRandomUserId();
    private final String toUser = getRandomUserId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsForConnectiongStringClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient connectionStringClient = setupClient(builder, "runAllClientFunctionsForConnectiongStringClient");
        String groupId = getGroupId("runAllClientFunctionsForConnectiongStringClient");
        runAllClientFunctions(groupId, connectionStringClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsForTokenCredentialClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingTokenCredential(httpClient);
        CallingServerClient tokenClient = setupClient(builder, "runAllClientFunctionsForTokenCredentialClient");
        String groupId = getGroupId("runAllClientFunctionsForTokenCredentialClient");
        runAllClientFunctions(groupId, tokenClient);

    }

    private void runAllClientFunctions(String groupId, CallingServerClient client) {
        String recordingId = "";
        List<CallConnection> callConnections = new ArrayList<>();
        ServerCall serverCall = null;

        try {
            callConnections = createCall(client, groupId, fromUser, toUser, CALLBACK_URI);
            serverCall = client.initializeServerCall(groupId);

            StartCallRecordingResult startCallRecordingResult = serverCall.startRecording(CALLBACK_URI);
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
            if (serverCall != null) {
                try {
                    serverCall.stopRecording(recordingId);
                } catch (Exception e) {
                    System.out.println("Error stopping recording: " + e.getMessage());
                }
            }

            cleanUpConnections(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponse(HttpClient httpClient) {
        String groupId = getGroupId("runAllClientFunctionsWithResponse");
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctionsWithResponse");
        String recordingId = "";
        List<CallConnection> callConnections = new ArrayList<>();
        ServerCall serverCall = null;

        try {
            callConnections = createCall(callingServerClient, groupId, fromUser, toUser, CALLBACK_URI);
            serverCall = callingServerClient.initializeServerCall(groupId);

            Response<StartCallRecordingResult> startRecordingResponse =
                serverCall.startRecordingWithResponse(CALLBACK_URI, null);
            assertEquals(startRecordingResponse.getStatusCode(), 200);
            StartCallRecordingResult startCallRecordingResult = startRecordingResponse.getValue();
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.ACTIVE);

            Response<Void> pauseResponse = serverCall.pauseRecordingWithResponse(recordingId, Context.NONE);
            assertEquals(pauseResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.INACTIVE);

            Response<Void> resumeResponse = serverCall.resumeRecordingWithResponse(recordingId, Context.NONE);
            assertEquals(resumeResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (serverCall != null) {
                try {
                    Response<Void> stopResponse = serverCall.stopRecordingWithResponse(recordingId, Context.NONE);
                    assertEquals(stopResponse.getStatusCode(), 200);
                } catch (Exception e) {
                    System.out.println("Error stopping recording: " + e.getMessage());
                }
            }

            cleanUpConnections(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunction(HttpClient httpClient) {
        String groupId = getGroupId("runPlayAudioFunction");
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunction");
        ServerCall serverCall;

        List<CallConnection> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createCall(callingServerClient, groupId, fromUser, toUser, CALLBACK_URI);
            serverCall = callingServerClient.initializeServerCall(groupId);
            PlayAudioOptions options = new PlayAudioOptions();
            options.setAudioFileId(UUID.randomUUID().toString());
            options.setCallbackUri(CALLBACK_URI);
            options.setOperationContext(operationContext);

            PlayAudioResult playAudioResult =
                serverCall.playAudio(AUDIO_FILE_URI, options);
            validatePlayAudioResult(playAudioResult);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            cleanUpConnections(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponse(HttpClient httpClient) {
        String groupId = getGroupId("runPlayAudioFunctionWithResponse");
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunctionWithResponse");
        ServerCall serverCall;

        List<CallConnection> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createCall(callingServerClient, groupId, fromUser, toUser, CALLBACK_URI);
            PlayAudioOptions playAudioOptions =
                new PlayAudioOptions()
                    .setLoop(false)
                    .setAudioFileId(UUID.randomUUID().toString())
                    .setCallbackUri(CALLBACK_URI)
                    .setOperationContext(operationContext);
            serverCall = callingServerClient.initializeServerCall(groupId);

            Response<PlayAudioResult> playAudioResult =
                serverCall.playAudioWithResponse(AUDIO_FILE_URI, playAudioOptions, Context.NONE);
            validatePlayAudioResponse(playAudioResult);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            cleanUpConnections(callConnections);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFails(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "startRecordingFails");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        ServerCall serverCall = callingServerClient.initializeServerCall(invalidServerCallId);

        try {
            Response<StartCallRecordingResult> response =
                serverCall.startRecordingWithResponse(CALLBACK_URI, Context.NONE);
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
    public void runAddRemoveScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenario");
        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                CALLBACK_URI,
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options);

            validateCallConnection(callConnection);

            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L1VDRl9RMVVlUGsyb0Y1YlJSMXliVXc_aT0xJmU9NjM3NTg0MzkzMzg3ODg3MDI3";
            ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = UUID.randomUUID().toString();
            AddParticipantResult addParticipantResult = serverCall
                .addParticipant(
                    new CommunicationUserIdentifier(toUser),
                    null,
                    operationContext,
                    CALLBACK_URI);

            String participantId = addParticipantResult.getParticipantId();
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
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runAddRemoveScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                CALLBACK_URI,
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options);

            validateCallConnection(callConnection);

            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L0Z1MENEVF9lLWtPalRtdjlXMDFuSXc_aT0wJmU9NjM3NTg0MzkwMjcxMzg0MTc3";
            ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = UUID.randomUUID().toString();
            Response<AddParticipantResult> addParticipantResultResponse =
                serverCall.addParticipantWithResponse(
                    new CommunicationUserIdentifier(toUser),
                    null,
                    operationContext, CALLBACK_URI,
                    null);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResultResponse);

            String participantId = addParticipantResultResponse.getValue().getParticipantId();
            Response<Void> removeResponse = serverCall.removeParticipantWithResponse(participantId, null);
            validateResponse(removeResponse);

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

    private void validateCallRecordingState(
        ServerCall serverCall,
        String recordingId,
        CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCall);
        assertNotNull(serverCall.getServerCallId());
        assertNotNull(recordingId);

        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        CallRecordingProperties callRecordingStateResult = serverCall.getRecordingState(recordingId);
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }

    protected void validateCallRecordingStateWithResponse(
        ServerCall serverCall,
        String recordingId,
        CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCall);
        assertNotNull(serverCall.getServerCallId());
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        Response<CallRecordingProperties> response =
            serverCall.getRecordingStateWithResponse(recordingId, null);
        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertNotNull(response.getValue());
        assertEquals(response.getValue().getRecordingState(), expectedCallRecordingState);
    }

    protected void cleanUpConnections(List<CallConnection> connections) {
        if (connections == null) {
            return;
        }

        connections.forEach(c -> {
            if (c != null) {
                try {
                    c.hangup();
                } catch (Exception e) {
                    System.out.println("Error hanging up: " + e.getMessage());
                }
            }
        });
    }
}
