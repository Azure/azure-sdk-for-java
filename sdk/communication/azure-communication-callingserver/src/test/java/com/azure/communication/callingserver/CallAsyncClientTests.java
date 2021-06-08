// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResponse;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResponse;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class CallAsyncClientTests extends CallingServerTestBase {
    private String from = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String alternateId =   "+11111111111";
    private String to =   "+11111111111";
    private String callBackUri = "https://host.app/api/callback/calling";
    private String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-74ee-b6ea-6a0b-343a0d0012ce";;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayCancelHangupScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallAsyncClient callAsyncClient = setupAsyncClient(builder, "runCreatePlayCancelHangupScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CreateCallResponse createCallResult = callAsyncClient.createCall(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            String callId = createCallResult.getCallLegId();

            // Play Audio
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            PlayAudioResponse playAudioResult = callAsyncClient.playAudio(
                callId,
                audioFileUri,
                false,
                UUID.randomUUID().toString(),
                null,
                operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

            // Cancel All Media Operations
            String cancelMediaOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b13";
            CancelAllMediaOperationsResponse cancelAllMediaOperationsResponse = callAsyncClient.cancelAllMediaOperations(callId, cancelMediaOperationContext).block();
            CallingServerTestUtils.validateCancelAllMediaOperations(cancelAllMediaOperationsResponse, cancelMediaOperationContext);

            // Hang up
            callAsyncClient.hangupCall(callId).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayCancelHangupScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallAsyncClient callAsyncClient = setupAsyncClient(builder, "runCreatePlayCancelHangupScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CreateCallResponse> createCallResponse = callAsyncClient.createCallWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            String callId = createCallResponse.getValue().getCallLegId();

            // Play Audio
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<PlayAudioResponse> playAudioResponse = callAsyncClient.playAudioWithResponse(
                callId,
                audioFileUri,
                false,
                UUID.randomUUID().toString(),
                null,
                operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);

            // Cancel All Media Operations
            String cancelMediaOperationContext = "ac794123-3820-4979-8e2d-50c7d3e07b13";
            Response<CancelAllMediaOperationsResponse> cancelAllMediaOperationsResponse = callAsyncClient.cancelAllMediaOperationsWithResponse(callId, cancelMediaOperationContext).block();
            CallingServerTestUtils.validateCancelAllMediaOperationsResponse(cancelAllMediaOperationsResponse, cancelMediaOperationContext);

            // Hang up
            Response<Void> hangupResponse = callAsyncClient.hangupCallWithResponse(callId).block();
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateAddRemoveHangupScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallAsyncClient callAsyncClient = setupAsyncClient(builder, "runCreateAddRemoveHangupScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CreateCallResponse createCallResult = callAsyncClient.createCall(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            String callId = createCallResult.getCallLegId();

            // Invite User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            callAsyncClient.addParticipant(callId, new CommunicationUserIdentifier(invitedUser), null, operationContext).block();

            // Remove Participant
            String participantId = "9d265602-aee7-4553-ac75-a7e167e0b083";
            callAsyncClient.removeParticipant(callId, participantId).block();

            // Hang up
            callAsyncClient.hangupCall(callId).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateAddRemoveHangupScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallAsyncClient callAsyncClient = setupAsyncClient(builder, "runCreateAddRemoveHangupScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CreateCallResponse> createCallResponse = callAsyncClient.createCallWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            String callId = createCallResponse.getValue().getCallLegId();

            // Invite User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> inviteParticipantResponse = callAsyncClient.addParticipantWithResponse(callId, new CommunicationUserIdentifier(invitedUser), null, operationContext).block();
            CallingServerTestUtils.validateResponse(inviteParticipantResponse);

            // Remove Participant
            String participantId = "4c100bf4-304c-48e0-87a8-03597ec75464";
            Response<Void> removeParticipantResponse = callAsyncClient.removeParticipantWithResponse(callId, participantId).block();
            CallingServerTestUtils.validateResponse(removeParticipantResponse);

            // Hang up
            Response<Void> hangupResponse = callAsyncClient.hangupCallWithResponse(callId).block();
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateDeleteScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallAsyncClient callAsyncClient = setupAsyncClient(builder, "runCreateDeleteScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CreateCallResponse createCallResult = callAsyncClient.createCall(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            String callId = createCallResult.getCallLegId();

            // Delete Call
            callAsyncClient.deleteCall(callId).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreateDeleteScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        CallClientBuilder builder = getCallClientUsingConnectionString(httpClient);
        CallAsyncClient callAsyncClient = setupAsyncClient(builder, "runCreateDeleteScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            Response<CreateCallResponse> createCallResponse = callAsyncClient.createCallWithResponse(
                new CommunicationUserIdentifier(from),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            String callId = createCallResponse.getValue().getCallLegId();

            // Delete Call
            Response<Void> hangupResponse = callAsyncClient.deleteCallWithResponse(callId).block();
            CallingServerTestUtils.validateResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    private CallAsyncClient setupAsyncClient(CallClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallClientBuilder addLoggingPolicy(CallClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}

