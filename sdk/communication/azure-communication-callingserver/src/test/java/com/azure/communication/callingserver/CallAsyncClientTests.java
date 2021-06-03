// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import com.azure.communication.callingserver.implementation.models.CallModality;
import com.azure.communication.callingserver.implementation.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResponse;
import com.azure.communication.callingserver.models.PlayAudioResponse;
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
 * live. By default, tests are run in playback mode.
 */
public class CallAsyncClientTests extends CallingServerTestBase {
    private String from = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String alternateId =   "+18445764430";
    private String to =   "+15125189815";     
    private String callBackUri = "https://host.app/api/callback/calling";
    private String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6d91-5555-b5bb-a43a0d00068c";;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayAudioHangupScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callAsyncClient = setupAsyncClient(builder, "runCreatePlayAudioHangupScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            CreateCallResponse createCallResult = callAsyncClient.createCall(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options).block();
            
            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            var callId = createCallResult.getCallLegId();            

            // Play Audio
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            PlayAudioResponse playAudioResult = callAsyncClient.playAudio(
                callId, 
                audioFileUri, 
                false, 
                UUID.randomUUID().toString(), 
                operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

            // Hang up
            callAsyncClient.hangupCall(callId).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }   

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayAudioHangupScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callAsyncClient = setupAsyncClient(builder, "runCreatePlayAudioHangupScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            Response<CreateCallResponse> createCallResponse = callAsyncClient.createCallWithResponse(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options, 
                Context.NONE).block();
            
            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            var callId = createCallResponse.getValue().getCallLegId();            

            // Play Audio
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<PlayAudioResponse> playAudioResponse = callAsyncClient.playAudioWithResponse(
                callId, 
                audioFileUri, 
                false, 
                UUID.randomUUID().toString(), 
                operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);

            // Hang up
            Response<Void> hangupResponse = callAsyncClient.hangupCallWithResponse(callId, Context.NONE).block();
            CallingServerTestUtils.validateHangupResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    } 
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void inviteUserRemoveParticipantScenarioAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callAsyncClient = setupAsyncClient(builder, "inviteUserRemoveParticipantScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            CreateCallResponse createCallResult = callAsyncClient.createCall(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options).block();
            
            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            var callId = createCallResult.getCallLegId();            

            // Invite User
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            callAsyncClient.addParticipant(callId, new CommunicationUserIdentifier(invitedUser), null, operationContext).block();
            
            // Remove Participant
            var participantId = "845156dc-15ad-4dec-883c-ee2e27cdb99e";
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
    public void inviteUserRemoveParticipantScenarioWithResponseAsync(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callAsyncClient = setupAsyncClient(builder, "inviteUserRemoveParticipantScenarioWithResponseAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            Response<CreateCallResponse> createCallResponse = callAsyncClient.createCallWithResponse(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options, 
                Context.NONE).block();
            
            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            var callId = createCallResponse.getValue().getCallLegId();            

            // Invite User
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> inviteParticipantResponse = callAsyncClient.addParticipantWithResponse(callId, new CommunicationUserIdentifier(invitedUser), null, operationContext, Context.NONE).block();
            CallingServerTestUtils.validateInviteParticipantResponse(inviteParticipantResponse);
             
            // Remove Participant
            var participantId = "8465d43d-3cf2-4e7f-96f6-e9824348c894";
            Response<Void> removeParticipantResponse = callAsyncClient.removeParticipantWithResponse(callId, participantId, Context.NONE).block();
            CallingServerTestUtils.validateRemoveParticipantResponse(removeParticipantResponse);

            // Hang up
            Response<Void> hangupResponse = callAsyncClient.hangupCallWithResponse(callId, Context.NONE).block();
            CallingServerTestUtils.validateHangupResponse(hangupResponse);
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

