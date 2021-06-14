// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.communication.callingserver.CallingServerTestUtils.validateCallConnection;
import static com.azure.communication.callingserver.CallingServerTestUtils.validatePlayAudioResponse;
import static com.azure.communication.callingserver.CallingServerTestUtils.validatePlayAudioResult;
import static com.azure.communication.callingserver.CallingServerTestUtils.validateResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServerCallLiveTests extends CallingServerTestBase {

    private final String groupId = getGroupId();
    private final String fromUser = getNewUserId();
    private final String toUser = getNewUserId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctions(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctions");
        String recordingId = "";
        List<CallConnection> callConnections = new ArrayList<>();
        ServerCall serverCall = null;

        try {
            callConnections = createCall(callingServerClient, groupId, fromUser, toUser, CALLBACK_URI);
            serverCall = callingServerClient.initializeServerCall(groupId);

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

            Response<Void> pauseResponse = serverCall.pauseRecordingWithResponse(recordingId, null);
            assertEquals(pauseResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.INACTIVE);

            Response<Void> resumeResponse = serverCall.resumeRecordingWithResponse(recordingId, null);
            assertEquals(resumeResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCall, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (serverCall != null) {
                try {
                    Response<Void> stopResponse = serverCall.stopRecordingWithResponse(recordingId, null);
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
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunction");
        ServerCall serverCall;

        List<CallConnection> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createCall(callingServerClient, groupId, fromUser, toUser, CALLBACK_URI);
            serverCall = callingServerClient.initializeServerCall(groupId);

            PlayAudioResult playAudioResult =
                serverCall.playAudio(AUDIO_FILE_URI, UUID.randomUUID().toString(), CALLBACK_URI, operationContext);
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
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunctionWithResponse");
        ServerCall serverCall;

        List<CallConnection> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createCall(callingServerClient, groupId, fromUser, toUser, CALLBACK_URI);
            serverCall = callingServerClient.initializeServerCall(groupId);

            Response<PlayAudioResult> playAudioResult =
                serverCall.playAudioWithResponse(
                    AUDIO_FILE_URI, operationContext,
                    CALLBACK_URI, operationContext,
                    null);
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
                serverCall.startRecordingWithResponse(CALLBACK_URI, null);
            assertEquals(response.getStatusCode(), 400);
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
                CALLBACK_URI,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(TO_PHONE_NUMBER) },
                options);

            validateCallConnection(callConnection);

            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L1ktWjZ5dzFzWVVTUUdWX2xPQWk1X2c_aT0xJmU9NjM3NTg0MzkzMzg3ODg3MDI3";
            ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = UUID.randomUUID().toString();
            serverCall
                .addParticipant(
                    new CommunicationUserIdentifier(toUser),
                    null,
                    operationContext,
                    CALLBACK_URI);

            // Remove User
            /*
              There is an update that we require to be able to get
              the participantId from the service when a user is
              added to a call. Until that is fixed this recorded
              value needs to be used.
             */
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
                CALLBACK_URI,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(TO_PHONE_NUMBER) },
                options);

            validateCallConnection(callConnection);

            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L1lXS2R2TTNRc0Vpc0VNYVUtNlhvSlE_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = UUID.randomUUID().toString();
            Response<Void> addResponse =
                serverCall.addParticipantWithResponse(
                    new CommunicationUserIdentifier(toUser),
                    null,
                    operationContext, CALLBACK_URI,
                    null);
            validateResponse(addResponse);

            // Remove User
            /*
              There is an update that we require to be able to get
              the participantId from the service when a user is
              added to a call. Until that is fixed this recorded
              values needs to be used.
             */
            String participantId = "76b33acb-5097-4af0-a646-e07ccee48957";
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

        CallRecordingStateResult callRecordingStateResult = serverCall.getRecordingState(recordingId);
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

        Response<CallRecordingStateResult> response =
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
