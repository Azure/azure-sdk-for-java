// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode. The runAllClientFunctions and runAllClientFunctionsWithResponse
 * test will not run in LIVE or RECORD as they cannot get their own conversationId.
 */
public class ConversationClientTests extends CallingServerTestBase {
    private String conversationId = "aHR0cHM6Ly9jb252LXVzZWEtMDcuY29udi5za3lwZS5jb20vY29udi85M0FnUnVMbGdVdUU2MWdxa1pnaHVBP2k9NTEmZT02Mzc1NzY1NzUwOTIzMTQ3OTU";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctions(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "runAllClientFunctions");
        String recordingId = "";        
        String recordingStateCallbackUri = "https://dev.skype.net:6448";

        try {
            StartCallRecordingResponse startCallRecordingResponse = conversationClient.startRecording(conversationId, recordingStateCallbackUri);
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(conversationClient, conversationId, recordingId, CallRecordingState.ACTIVE);

            conversationClient.pauseRecording(conversationId, recordingId);
            validateCallRecordingState(conversationClient, conversationId, recordingId, CallRecordingState.INACTIVE);

            conversationClient.resumeRecording(conversationId, recordingId);
            validateCallRecordingState(conversationClient, conversationId, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            conversationClient.stopRecording(conversationId, recordingId);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "runAllClientFunctionsWithResponse");
        String recordingId = "";        
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("conversationId: " + conversationId);

        try {
            Response<StartCallRecordingResponse> response = conversationClient.startRecordingWithResponse(conversationId, recordingStateCallbackUri, Context.NONE);
            assertEquals(response.getStatusCode(), 200);
            StartCallRecordingResponse startCallRecordingResponse = response.getValue();
            recordingId = startCallRecordingResponse.getRecordingId();
            validateCallRecordingState(conversationClient, conversationId, recordingId, CallRecordingState.ACTIVE);

            conversationClient.pauseRecording(conversationId, recordingId);
            validateCallRecordingState(conversationClient, conversationId, recordingId, CallRecordingState.INACTIVE);

            conversationClient.resumeRecording(conversationId, recordingId);
            validateCallRecordingState(conversationClient, conversationId, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            conversationClient.stopRecording(conversationId, recordingId);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunction(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "runPlayAudioFunction");
        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";      
        String callbackUri = "https://dev.skype.net:6448";
        
        System.out.println("conversationId: " + conversationId);
        try {
            PlayAudioResponse playAudioResponse = conversationClient.playAudio(conversationId, audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResponse, operationContext);
           
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "runPlayAudioFunctionWithResponse");
        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri = "https://host.app/audio/bot-callcenter-intro.wav";      
        String callbackUri = "https://dev.skype.net:6448";
        
        System.out.println("conversationId: " + conversationId);
        try {
            Response<PlayAudioResponse> playAudioResponse = conversationClient.playAudioWithResponse(conversationId, audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext, Context.NONE);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResponse, operationContext);
           
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFails(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "startRecordingFails");
        String invalidConversationId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";       
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("conversationId: " + invalidConversationId);

        try {
            Response<StartCallRecordingResponse> response = conversationClient.startRecordingWithResponse(invalidConversationId, recordingStateCallbackUri, Context.NONE);
            assertEquals(response.getStatusCode(), 400);
        } catch (CommunicationErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    // @ParameterizedTest
    // @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    // public void runJoinScenario(HttpClient httpClient) throws URISyntaxException, InterruptedException {
    //     ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
    //     ConversationClient conversationClient = setupClient(builder, "runJoinScenario");
    //     String from = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-74ee-b6ea-6a0b-343a0d0012ce";
    //     String callBackUri = "https://84612e0a8d55.ngrok.io/api/callback/calling"; 
    //     CommunicationIdentifier source = new CommunicationUserIdentifier(from);
    //     JoinCallOptions joinCallOptions = new JoinCallOptions().
    //         setCallbackUri(callBackUri).setRequestedModalities(new LinkedList<CallModality>(Arrays.asList(CallModality.AUDIO))).
    //         setRequestedCallEvents(new LinkedList<EventSubscriptionType>(Arrays.asList(EventSubscriptionType.PARTICIPANTS_UPDATED)));

    //     String conversationId = "";
    //     try {
    //         JoinCallResponse joinCallResponse = conversationClient.joinCall(conversationId, source, joinCallOptions);
    //         CallingServerTestUtils.validateJoinCall(joinCallResponse);
    //     } catch (Exception e) {
    //         System.out.println("Error: " + e.getMessage());
    //         throw e;
    //     }
    // }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenario(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "runAddScenario");        
        try {
            // Add User
            String conversationId = "aHR0cHM6Ly9jb252LXVzd2UtMDItc2RmLWFrcy5jb252LnNreXBlLmNvbS9jb252L3VEWHc4M1FsdUVtcG03TlVybElaTVE_aT0xMC02MC0zLTIwNyZlPTYzNzU4MjU1MTI1OTkzMzg5Ng";
            String participant = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-756c-41ce-ac00-343a0d001b58";
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            String callBackUri = "https://host.app/api/callback/calling";
            conversationClient.addParticipant(conversationId, new CommunicationUserIdentifier(participant), null, operationContext, callBackUri);

            // Remove User
            String participantId = "ebe62cd6-2085-4faf-8ceb-38a9a4482de8"; 
            conversationClient.removeParticipant(conversationId, participantId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioWithResponse(HttpClient httpClient) throws URISyntaxException, InterruptedException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "runAddRemoveScenarioWithResponse");        
        try {
            // Add User
            String conversationId = "aHR0cHM6Ly9jb252LXVzd2UtMDItc2RmLWFrcy5jb252LnNreXBlLmNvbS9jb252L3VEWHc4M1FsdUVtcG03TlVybElaTVE_aT0xMC02MC0zLTIwNyZlPTYzNzU4MjU1MTI1OTkzMzg5Ng";
            String participant = "8:acs:016a7064-0581-40b9-be73-6dde64d69d72_0000000a-756c-41ce-ac00-343a0d001b58";
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            String callBackUri = "https://host.app/api/callback/calling";
            Response<Void> addResponse = conversationClient.addParticipantWithResponse(conversationId, new CommunicationUserIdentifier(participant), null, operationContext, callBackUri, Context.NONE);
            CallingServerTestUtils.validateResponse(addResponse);

            // Remove User
            String participantId = "2a7155ef-a580-49cd-bcee-2ae4e8cdc602"; 
            Response<Void> removeResponse = conversationClient.removeParticipantWithResponse(conversationId, participantId, Context.NONE);
            CallingServerTestUtils.validateResponse(removeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    private ConversationClient setupClient(ConversationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected ConversationClientBuilder addLoggingPolicy(ConversationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
    
    private void validateCallRecordingState(ConversationClient conversationClient, String conversationId, String recordingId, CallRecordingState expectedCallRecordingState) throws InterruptedException {
        assertNotNull(recordingId);
        assertNotNull(conversationId);

        /** 
         * There is a delay bewteen the action and when the state is available.
         * Waiting to make sure we get the updated state, when we are running
         * against a live service. 
         */
        sleepIfRunningAgainstService(6000);

        CallRecordingStateResponse callRecordingStateResult = conversationClient.getRecordingState(conversationId, recordingId);
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }
}
