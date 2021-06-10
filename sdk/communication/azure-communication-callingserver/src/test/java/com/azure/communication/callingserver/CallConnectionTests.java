// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.util.UUID;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to setup if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class CallConnectionTests extends CallingServerTestBase {

    private String from = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-74ee-b6ea-6a0b-343a0d0012ce";
    private String joinedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-74ee-b6ea-6a0b-343a0d0012cf";
    private String alternateId =   "+11111111111";
    private String to =   "+11111111111";
    private String callBackUri = "https://host.app/api/callback/calling";
    private String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayCancelHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreatePlayCancelHangupScenario");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Play Audio
            String playAudioOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            PlayAudioResult playAudioResult = callConnection.playAudio(
                audioFileUri,
                false,
                UUID.randomUUID().toString(),
                null,
                playAudioOperationContext);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, playAudioOperationContext);

            // Cancel All Media Operations
            String cancelMediaOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b13";
            CancelAllMediaOperationsResult cancelAllMediaOperationsResult = callConnection.cancelAllMediaOperations(cancelMediaOperationContext);
            CallingServerTestUtils.validateCancelAllMediaOperations(cancelAllMediaOperationsResult, cancelMediaOperationContext);

            // Hang up
            callConnection.hangup();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayCancelHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreatePlayCancelHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options,
                Context.NONE);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Play Audio
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<PlayAudioResult> playAudioResult =
                callConnection.playAudioWithResponse(
                    audioFileUri,
                    false,
                    UUID.randomUUID().toString(),
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult, operationContext);

            // Cancel All Media Operations
            String cancelMediaOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b13";
            Response<CancelAllMediaOperationsResult> cancelAllMediaOperationsResult = callConnection.cancelAllMediaOperationsWithResponse(cancelMediaOperationContext, Context.NONE);
            CallingServerTestUtils.validateCancelAllMediaOperationsResponse(cancelAllMediaOperationsResult, cancelMediaOperationContext);

            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(Context.NONE);
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateAddRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddRemoveHangupScenario");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            callConnection.addParticipant(new CommunicationUserIdentifier(invitedUser), null, operationContext);

            // Remove Participant
            String participantId = "f29f70e3-1eaf-44c0-839c-b4e8a74ffec3";
            callConnection.removeParticipant(participantId);

            // Hang up
            callConnection.hangup();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateAddRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddRemoveHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options,
                Context.NONE);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> inviteParticipantResponse = callConnection
                .addParticipantWithResponse(
                    new CommunicationUserIdentifier(invitedUser),
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateResponse(inviteParticipantResponse);

            // Remove Participant
            String participantId = "71ed956b-366e-450c-9a61-3bbccf42baa5";
            Response<Void> removeParticipantResponse = callConnection.removeParticipantWithResponse(participantId, Context.NONE);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);

            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(Context.NONE);
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateJoinHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateJoinHangupScenario");

        try {
            // Establish a call
            CreateCallOptions createCallOptions = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            createCallOptions.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                createCallOptions);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Join
            var serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L2RUUjRPVGFxVzAyZ3cxVGpNSUNBdEE_aT0wJmU9NjM3NTg0MzkwMjcxMzg0MTc3";
            JoinCallOptions joinCallOptions = new JoinCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });
            CallConnection joinedCallConnection =
                callingServerClient.join(serverCallId, new CommunicationUserIdentifier(joinedUser), joinCallOptions);
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
    public void runCreateJoinHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateJoinHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions createCallOptions = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            createCallOptions.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                createCallOptions,
                Context.NONE);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Join
            var serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3dXZW9hNjAweGtPZ0d6eHE2eG1tQVE_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            JoinCallOptions joinCallOptions = new JoinCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });
            Response<CallConnection> joinedCallConnectionResponse =
                callingServerClient.joinWithResponse(
                    serverCallId,
                    new CommunicationUserIdentifier(joinedUser),
                    joinCallOptions,
                    Context.NONE);
            CallingServerTestUtils.validateJoinCallConnectionResponse(joinedCallConnectionResponse);
            CallConnection joinedCallConnection = joinedCallConnectionResponse.getValue();

            //Hangup
            Response<Void> hangupResponse = callConnection.hangupWithResponse(Context.NONE);
            CallingServerTestUtils.validateResponse(hangupResponse);
            Response<Void> joinCallHangupResponse = joinedCallConnection.hangupWithResponse(Context.NONE);
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
