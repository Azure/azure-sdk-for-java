// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.EventSubscriptionType;
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
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to setup if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class CallClientTests extends CallingServerTestBase {
    private String from = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6198-4a66-02c3-593a0d00560d";
    private String alternateId =   "+18445764430";
    private String to =   "+15125189815";      
    private String callBackUri = "https://host.app/api/callback/calling";
    private String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";
    private String invitedUser = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-6d91-5555-b5bb-a43a0d00068c";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayAudioHangupScenario(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callClient = setupClient(builder, "runCreatePlayAudioHangupScenario");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            CreateCallResponse createCallResult = callClient.createCall(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options);
            
            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            var callId = createCallResult.getCallLegId();            

            // Play Audio
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            PlayAudioResponse playAudioResult = callClient.playAudio(
                callId, 
                audioFileUri, 
                false, 
                UUID.randomUUID().toString(), 
                operationContext);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

            // Hang up
            callClient.hangupCall(callId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }   

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runCreatePlayAudioHangupScenarioWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callClient = setupClient(builder, "runCreatePlayAudioHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            Response<CreateCallResponse> createCallResponse = callClient.createCallWithResponse(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options, 
                Context.NONE);
            
            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            var callId = createCallResponse.getValue().getCallLegId();            

            // Play Audio
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<PlayAudioResponse> playAudioResponse = callClient.playAudioWithResponse(
                callId, 
                audioFileUri, 
                false, 
                UUID.randomUUID().toString(), 
                operationContext, 
                Context.NONE);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);

            // Hang up
            Response<Void> hangupResponse = callClient.hangupCallWithResponse(callId, Context.NONE);
            CallingServerTestUtils.validateHangupResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void inviteUserRemoveParticipantScenario(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callClient = setupClient(builder, "inviteUserRemoveParticipantScenario");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            CreateCallResponse createCallResult = callClient.createCall(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options);
            
            CallingServerTestUtils.validateCreateCallResult(createCallResult);
            var callId = createCallResult.getCallLegId();            

            // Invite User
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            callClient.addParticipant(callId, new CommunicationUserIdentifier(invitedUser), null, operationContext);
            
            // Remove Participant
            var participantId = "39659b2c-53a8-40a3-93d5-5da018967401";
            callClient.removeParticipant(callId, participantId);
           
            // Hang up
            callClient.hangupCall(callId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void inviteUserRemoveParticipantScenarioWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        var builder = getCallClientUsingConnectionString(httpClient);
        var callClient = setupClient(builder, "inviteUserRemoveParticipantScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri, 
                new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO)), 
                new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));
                
            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));
            
            Response<CreateCallResponse> createCallResponse = callClient.createCallWithResponse(
                new CommunicationUserIdentifier(from), 
                new LinkedList<CommunicationIdentifier>(Arrays.asList(new PhoneNumberIdentifier(to))), 
                options, 
                Context.NONE);
            
            CallingServerTestUtils.validateCreateCallResponse(createCallResponse);
            var callId = createCallResponse.getValue().getCallLegId();            

            // Invite User
            var operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> inviteParticipantResponse = callClient.addParticipantWithResponse(callId, new CommunicationUserIdentifier(invitedUser), null, operationContext, Context.NONE);
            CallingServerTestUtils.validateInviteParticipantResponse(inviteParticipantResponse);
            
            // Remove Participant
            var participantId = "d702fa95-a3ee-4e2d-8101-8e100e7aaf8a";
            Response<Void> removeParticipantResponse = callClient.removeParticipantWithResponse(callId, participantId, Context.NONE);
            CallingServerTestUtils.validateRemoveParticipantResponse(removeParticipantResponse);

            // Hang up
            Response<Void> hangupResponse = callClient.hangupCallWithResponse(callId, Context.NONE);
            CallingServerTestUtils.validateHangupResponse(hangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    private CallClient setupClient(CallClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallClientBuilder addLoggingPolicy(CallClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
