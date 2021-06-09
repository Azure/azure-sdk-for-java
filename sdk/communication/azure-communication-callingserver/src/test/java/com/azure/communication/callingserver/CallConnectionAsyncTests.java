// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResponse;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class CallConnectionAsyncTests extends CallingServerTestBase {
    private String from = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String alternateId =   "+11111111111";
    private String to =   "+11111111111";
    private String callBackUri = "https://host.app/api/callback/calling";
    private String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-74ee-b6ea-6a0b-343a0d0012ce";;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayCancelHangupScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runCreatePlayCancelHangupScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Play Audio
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            PlayAudioResponse playAudioResult = callConnectionAsync.playAudio(
                audioFileUri,
                false,
                UUID.randomUUID().toString(),
                null,
                operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

            // Cancel All Media Operations
            String cancelMediaOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b13";
            CancelAllMediaOperationsResponse cancelAllMediaOperationsResponse = callConnectionAsync.cancelAllMediaOperations(cancelMediaOperationContext).block();
            CallingServerTestUtils.validateCancelAllMediaOperations(cancelAllMediaOperationsResponse, cancelMediaOperationContext);

            // Hang up
            callConnectionAsync.hangup().block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayCancelHangupScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runCreatePlayCancelHangupScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsyncResponse(callConnectionAsyncResponse);
            CallConnectionAsync callConnectionAsync = callConnectionAsyncResponse.getValue();

            // Play Audio
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<PlayAudioResponse> playAudioResponse =
                callConnectionAsync.playAudioWithResponse(
                    audioFileUri,
                    false,
                    UUID.randomUUID().toString(),
                    null,
                    operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);

            // Cancel All Media Operations
            String cancelMediaOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b13";
            Response<CancelAllMediaOperationsResponse> cancelAllMediaOperationsResponse = callConnectionAsync.cancelAllMediaOperationsWithResponse(cancelMediaOperationContext).block();
            CallingServerTestUtils.validateCancelAllMediaOperationsResponse(cancelAllMediaOperationsResponse, cancelMediaOperationContext);

            // Hang up
            Response<Void> hangupResponse = callConnectionAsync.hangupWithResponse().block();
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateAddRemoveHangupScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runCreateAddRemoveHangupScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Invite User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            callConnectionAsync.addParticipant(new CommunicationUserIdentifier(invitedUser), null, operationContext).block();

            // Remove Participant
            String participantId = "9d265602-aee7-4553-ac75-a7e167e0b083";
            callConnectionAsync.removeParticipant(participantId).block();

            // Hang up
            callConnectionAsync.hangup().block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateAddRemoveHangupScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runCreateAddRemoveHangupScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsyncResponse(callConnectionAsyncResponse);
            CallConnectionAsync callConnectionAsync = callConnectionAsyncResponse.getValue();

            // Invite User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> inviteParticipantResponse = callConnectionAsync.addParticipantWithResponse(new CommunicationUserIdentifier(invitedUser), null, operationContext).block();
            CallingServerTestUtils.validateResponse(inviteParticipantResponse);

            // Remove Participant
            String participantId = "4c100bf4-304c-48e0-87a8-03597ec75464";
            Response<Void> removeParticipantResponse = callConnectionAsync.removeParticipantWithResponse(participantId).block();
            CallingServerTestUtils.validateResponse(removeParticipantResponse);

            // Hang up
            Response<Void> hangupResponse = callConnectionAsync.hangupWithResponse().block();
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateDeleteScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runCreateDeleteScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Delete Call
            callConnectionAsync.hangup().block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateDeleteScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallingServerClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runCreateDeleteScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CallConnectionAsync> callConnectionAsyncResponse = callingServerAsyncClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsyncResponse(callConnectionAsyncResponse);
            CallConnectionAsync callConnectionAsync = callConnectionAsyncResponse.getValue();

            // Delete Call
            Response<Void> hangupResponse = callConnectionAsync.hangupWithResponse().block();
            CallingServerTestUtils.validateResponse(hangupResponse);
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
}

