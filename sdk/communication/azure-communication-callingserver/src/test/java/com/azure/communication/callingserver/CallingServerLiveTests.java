// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.azure.communication.callingserver.CallingServerTestUtils.validateCallConnection;
import static com.azure.communication.callingserver.CallingServerTestUtils.validatePlayAudioResponse;
import static com.azure.communication.callingserver.CallingServerTestUtils.validatePlayAudioResult;
import static com.azure.communication.callingserver.CallingServerTestUtils.validateResponse;
import static com.azure.communication.callingserver.CallingServerTestUtils.validateApiResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;;

public class CallingServerLiveTests extends CallingServerTestBase {

    private final String fromUser = getRandomUserId();
    private final String toUser = getRandomUserId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsForConnectionStringClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient connectionStringClient = setupClient(builder, "runAllClientFunctionsForConnectionStringClient");
        String groupId = getGroupId("runAllClientFunctionsForConnectionStringClient");
        runAllClientFunctions(groupId, connectionStringClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsForTokenCredentialClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallingServerClient connectionStringClient = setupClient(builder, "runAllClientFunctionsForTokenCredentialClient");
        String groupId = getGroupId("runAllClientFunctionsForTokenCredentialClient");
        runAllClientFunctions(groupId, connectionStringClient);
    }

    private void runAllClientFunctions(String groupId, CallingServerClient callingServerClient) {
        String recordingId = "";
        List<CallConnection> callConnections = new ArrayList<>();
        GroupCallLocator groupCallLocator = new GroupCallLocator(groupId);

        try {
            callConnections = createCall(callingServerClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));
            StartCallRecordingResult startCallRecordingResult = callingServerClient.startRecording(groupCallLocator, URI.create(CALLBACK_URI));
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingState(callingServerClient, recordingId, CallRecordingState.ACTIVE);

            callingServerClient.pauseRecording(recordingId);
            validateCallRecordingState(callingServerClient, recordingId, CallRecordingState.INACTIVE);

            callingServerClient.resumeRecording(recordingId);
            validateCallRecordingState(callingServerClient, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (callingServerClient != null) {
                try {
                    callingServerClient.stopRecording(recordingId);
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
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAllClientFunctionsWithResponse");
        String recordingId = "";
        List<CallConnection> callConnections = new ArrayList<>();
        GroupCallLocator groupCallLocator = new GroupCallLocator(groupId);

        try {
            callConnections = createCall(callingServerClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));

            Response<StartCallRecordingResult> startRecordingResponse =
                callingServerClient.startRecordingWithResponse(groupCallLocator, URI.create(CALLBACK_URI), null, null);
            assertEquals(startRecordingResponse.getStatusCode(), 200);
            StartCallRecordingResult startCallRecordingResult = startRecordingResponse.getValue();
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingStateWithResponse(callingServerClient, recordingId, CallRecordingState.ACTIVE);

            Response<Void> pauseResponse = callingServerClient.pauseRecordingWithResponse(recordingId, Context.NONE);
            assertEquals(pauseResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(callingServerClient, recordingId, CallRecordingState.INACTIVE);

            Response<Void> resumeResponse = callingServerClient.resumeRecordingWithResponse(recordingId, Context.NONE);
            assertEquals(resumeResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(callingServerClient, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (callingServerClient != null) {
                try {
                    Response<Void> stopResponse = callingServerClient.stopRecordingWithResponse(recordingId, Context.NONE);
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
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunction");
        GroupCallLocator groupCallLocator = new GroupCallLocator(groupId);

        List<CallConnection> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createCall(callingServerClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));
            PlayAudioOptions options = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setOperationContext(operationContext)
                .setLoop(true);

            PlayAudioResult playAudioResult =
                callingServerClient.playAudio(groupCallLocator, URI.create(AUDIO_FILE_URI), options);
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
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runPlayAudioFunctionWithResponse");
        GroupCallLocator groupCallLocator = new GroupCallLocator(groupId);

        List<CallConnection> callConnections = new ArrayList<>();
        String operationContext = UUID.randomUUID().toString();

        try {
            callConnections = createCall(callingServerClient, groupCallLocator, fromUser, toUser, URI.create(CALLBACK_URI));
            PlayAudioOptions playAudioOptions =
                new PlayAudioOptions()
                    .setLoop(false)
                    .setAudioFileId(UUID.randomUUID().toString())
                    .setCallbackUri(URI.create(CALLBACK_URI))
                    .setOperationContext(operationContext);

            Response<PlayAudioResult> playAudioResult =
                callingServerClient.playAudioWithResponse(groupCallLocator, URI.create(AUDIO_FILE_URI), playAudioOptions, Context.NONE);
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
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "startRecordingFails");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        ServerCallLocator serverCallLocator = new ServerCallLocator(invalidServerCallId);

        try {
            Response<StartCallRecordingResult> response =
                callingServerClient.startRecordingWithResponse(serverCallLocator, URI.create(CALLBACK_URI), null, Context.NONE);
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
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        validateCallConnection(callConnection);

        try {
            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            // serverCallId looks like this: "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3VodHNzZEZ3NFVHX1J4d1lHYWlLRmc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4"
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            String operationContext = UUID.randomUUID().toString();
            AddParticipantResult addParticipantResult = callingServerClient
                .addParticipant(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext);
            assert addParticipantResult != null;

            // Remove User
            callingServerClient.removeParticipant(serverCallLocator, addedUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hangup
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runAddRemoveScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runAddRemoveScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        validateCallConnection(callConnection);

        try {
            // Get Server Call
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            // serverCallId looks like this: "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3VodHNzZEZ3NFVHX1J4d1lHYWlLRmc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4"
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            String operationContext = UUID.randomUUID().toString();
            Response<AddParticipantResult> addParticipantResultResponse =
                callingServerClient.addParticipantWithResponse(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext,
                    null);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResultResponse);

            // Remove User
            Response<Void> removeResponse = callingServerClient.removeParticipantWithResponse(serverCallLocator, addedUser, null);
            validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreatePlayCancelMediaHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreatePlayCancelMediaHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);
        validateCallConnection(callConnection);

        try {
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Play Prompt Audio
            String operationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setOperationContext(operationContext)
                .setLoop(true);
            PlayAudioResult playAudioResult =
                callingServerClient.playAudio(serverCallLocator, URI.create(AUDIO_FILE_URI), playAudioOptions);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult);

            // Cancel Prompt Audio
            callingServerClient.cancelMediaOperation(serverCallLocator, playAudioResult.getOperationId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hangup
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreatePlayCancelMediaHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreatePlayCancelMediaHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options, Context.NONE);
        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);

        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Play Prompt Audio
            String operationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setOperationContext(operationContext)
                .setLoop(true);

            Response<PlayAudioResult> playAudioResponse = callingServerClient.playAudioWithResponse(serverCallLocator, URI.create(AUDIO_FILE_URI), playAudioOptions, null);
            validatePlayAudioResponse(playAudioResponse);

            // Cancel Prompt Audio
            Response<Void> cancelMediaOperationResponse = callingServerClient.cancelMediaOperationWithResponse(serverCallLocator, playAudioResponse.getValue().getOperationId(), null);
            validateApiResponse(cancelMediaOperationResponse);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddPlayAudioToParticipantCancelRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddPlayAudioToParticipantCancelRemoveHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);
        validateCallConnection(callConnection);

        try {
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);
            
            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            String operationContext = UUID.randomUUID().toString();
            AddParticipantResult addParticipantResult = callingServerClient
                .addParticipant(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext);
            assert addParticipantResult != null;

            // Play Prompt Audio
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setOperationContext(operationContext)
                .setLoop(true);
            PlayAudioResult playAudioToParticipantResult = callingServerClient.playAudioToParticipant(serverCallLocator, addedUser, URI.create(AUDIO_FILE_URI), playAudioOptions);
            validatePlayAudioResult(playAudioToParticipantResult);

            // Cancel Prompt Audio
            callingServerClient.cancelParticipantMediaOperation(serverCallLocator, addedUser, playAudioToParticipantResult.getOperationId());

            // Remove User
            callingServerClient.removeParticipant(serverCallLocator, addedUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hangup
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddPlayAudioToParticipantCancelRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddPlayAudioToParticipantCancelRemoveHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options, Context.NONE);
        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);

        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            String operationContext = UUID.randomUUID().toString();
            Response<AddParticipantResult> addParticipantResultResponse =
                callingServerClient.addParticipantWithResponse(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext,
                    null);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResultResponse);

            // Play Prompt Audio
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setOperationContext(operationContext)
                .setLoop(true);

            Response<PlayAudioResult> playAudioToParticipantResponse = callingServerClient.playAudioToParticipantWithResponse(serverCallLocator, addedUser, URI.create(AUDIO_FILE_URI), playAudioOptions, null);
            validatePlayAudioResponse(playAudioToParticipantResponse);

            // Cancel Prompt Audio
            Response<Void> cancelParticipantMediaOperationResponse = callingServerClient.cancelParticipantMediaOperationWithResponse(serverCallLocator, addedUser, playAudioToParticipantResponse.getValue().getOperationId(), null);
            validateApiResponse(cancelParticipantMediaOperationResponse);

            // Remove User
            Response<Void> removeResponse = callingServerClient.removeParticipantWithResponse(serverCallLocator, addedUser, null);
            validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
            named = "SKIP_LIVE_TEST",
            matches = "(?i)(true)",
            disabledReason = "Requires human intervention")
    public void runCreateAddGetParticipantRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddGetParticipantRemoveHangupScenario");
        
        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
                    new CommunicationUserIdentifier(fromUser),
                    Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                    options);
        validateCallConnection(callConnection);

        try {
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);

            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            String operationContext = UUID.randomUUID().toString();
            AddParticipantResult addParticipantResult = callingServerClient
                .addParticipant(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext);
            assert addParticipantResult != null;

            // Get Participant
            CallParticipant getParticipantResult = callingServerClient.getParticipant(serverCallLocator, addedUser);
            assert getParticipantResult != null;

            // Get Participants
            List <CallParticipant> getParticipantsResult = callingServerClient.getParticipants(serverCallLocator);
            assertTrue(getParticipantsResult.size() > 2);

            // Remove User
            callingServerClient.removeParticipant(serverCallLocator, addedUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hangup
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
            named = "SKIP_LIVE_TEST",
            matches = "(?i)(true)",
            disabledReason = "Requires human intervention")
    public void runCreateAddGetParticipantRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddGetParticipantRemoveHangupScenarioWithResponse");
        
        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options, Context.NONE);
        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);

        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);
            
            // Add User
            CommunicationUserIdentifier addedUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            String operationContext = UUID.randomUUID().toString();
            Response<AddParticipantResult> addParticipantResultResponse =
                callingServerClient.addParticipantWithResponse(
                    serverCallLocator,
                    addedUser,
                    URI.create(CALLBACK_URI),
                    null,
                    operationContext,
                    null);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResultResponse);
                       
            // Get User
            Response<CallParticipant> getParticipantResponse =
                callingServerClient.getParticipantWithResponse(serverCallLocator, addedUser, null);
            CallingServerTestUtils.validateGetParticipantResponse(getParticipantResponse);

            // Get Users
            Response<List<CallParticipant>> getParticipantsResponse = 
                callingServerClient.getParticipantsWithResponse(serverCallLocator, null);
            assertTrue(getParticipantsResponse.getValue().size() > 2);
            CallingServerTestUtils.validateGetParticipantsResponse(getParticipantsResponse);
            
            // Remove User
            Response<Void> removeResponse = callingServerClient.removeParticipantWithResponse(serverCallLocator, addedUser, null);
            validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }
             
    private CallingServerClient setupClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private void validateCallRecordingState(
        CallingServerClient callingServerClient,
        String recordingId,
        CallRecordingState expectedCallRecordingState) {
        assertNotNull(callingServerClient);
        assertNotNull(recordingId);

        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        CallRecordingProperties callRecordingStateResult = callingServerClient.getRecordingState(recordingId);
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }

    protected void validateCallRecordingStateWithResponse(
        CallingServerClient callingServerClient,
        String recordingId,
        CallRecordingState expectedCallRecordingState) {
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        Response<CallRecordingProperties> response =
            callingServerClient.getRecordingStateWithResponse(recordingId, null);
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

