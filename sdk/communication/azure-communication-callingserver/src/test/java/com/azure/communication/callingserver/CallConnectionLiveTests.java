// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

public class CallConnectionLiveTests extends CallingServerTestBase {

    private final String fromUser = getNewUserId();
    private final String toUser = getNewUserId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreatePlayCancelHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreatePlayCancelHangupScenario");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Play Audio
            String playAudioOperationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setLoop(false)
                .setOperationContext(playAudioOperationContext);
            PlayAudioResult playAudioResult = callConnection.playAudio(
                URI.create(AUDIO_FILE_URI), playAudioOptions);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult);

            // Cancel All Media Operations
            String cancelMediaOperationContext = UUID.randomUUID().toString();
            CancelAllMediaOperationsResult cancelAllMediaOperationsResult =
                callConnection.cancelAllMediaOperations(cancelMediaOperationContext);
            CallingServerTestUtils.validateCancelAllMediaOperations(cancelAllMediaOperationsResult);

            // Hang up
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
    public void runCreatePlayCancelHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreatePlayCancelHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            Response<CallConnection> callConnectionResponse =
                callingServerClient.createCallConnectionWithResponse(
                    new CommunicationUserIdentifier(fromUser),
                    Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                    options,
                    null);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Play Audio
            String operationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions =
                new PlayAudioOptions()
                    .setLoop(false)
                    .setAudioFileId(UUID.randomUUID().toString())
                    .setCallbackUri(null)
                    .setOperationContext(operationContext);
            Response<PlayAudioResult> playAudioResult =
                callConnection.playAudioWithResponse(URI.create(AUDIO_FILE_URI), playAudioOptions, null);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult);

            // Cancel All Media Operations
            String cancelMediaOperationContext = UUID.randomUUID().toString();
            Response<CancelAllMediaOperationsResult> cancelAllMediaOperationsResult =
                callConnection.cancelAllMediaOperationsWithResponse(cancelMediaOperationContext, null);
            CallingServerTestUtils.validateCancelAllMediaOperationsResult(cancelAllMediaOperationsResult);

            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
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
    public void runCreateAddRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddRemoveHangupScenario");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(toUser);
            AddParticipantResult addParticipantResult = callConnection.addParticipant(addUser, null, operationContext, URI.create(CALLBACK_URI));

            String participantId = addParticipantResult.getParticipantId();
            callConnection.removeParticipant(addUser);

            // Hang up
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
    public void runCreateAddRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateAddRemoveHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            Response<CallConnection> callConnectionResponse =
                callingServerClient.createCallConnectionWithResponse(
                    new CommunicationUserIdentifier(fromUser),
                    Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                    options,
                    null);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(toUser);
            Response<AddParticipantResult> addParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addUser,
                    null,
                    operationContext,
                    URI.create(CALLBACK_URI),
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResponse);

            Response<Void> removeParticipantResponse =
                callConnection.removeParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);

            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
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
    public void runCreateJoinHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateJoinHangupScenario");

        try {
            // Establish a call
            CreateCallOptions createCallOptions = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            createCallOptions.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                createCallOptions);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Join
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            ServerCallLocator serverCallLocator = new ServerCallLocator("aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L2RUUjRPVGFxVzAyZ3cxVGpNSUNBdEE_aT0wJmU9NjM3NTg0MzkwMjcxMzg0MTc3");
            JoinCallOptions joinCallOptions = new JoinCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));
            CallConnection joinedCallConnection =
                callingServerClient.joinCall(serverCallLocator, new CommunicationUserIdentifier(toUser), joinCallOptions);
            CallingServerTestUtils.validateCallConnection(joinedCallConnection);

            //Hangup
            callConnection.hangup();
            joinedCallConnection.hangup();
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
    public void runCreateJoinHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateJoinHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions createCallOptions = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            createCallOptions.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                createCallOptions,
                null);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Join
            ServerCallLocator serverCallLocator = new ServerCallLocator("aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3dXZW9hNjAweGtPZ0d6eHE2eG1tQVE_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4");
            JoinCallOptions joinCallOptions = new JoinCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));
            Response<CallConnection> joinedCallConnectionResponse =
                callingServerClient.joinCallWithResponse(
                    serverCallLocator,
                    new CommunicationUserIdentifier(toUser),
                    joinCallOptions,
                    null);
            CallingServerTestUtils.validateJoinCallConnectionResponse(joinedCallConnectionResponse);
            CallConnection joinedCallConnection = joinedCallConnectionResponse.getValue();

            //Hangup
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
            Response<Void> joinCallHangupResponse = joinedCallConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(joinCallHangupResponse);
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
}
