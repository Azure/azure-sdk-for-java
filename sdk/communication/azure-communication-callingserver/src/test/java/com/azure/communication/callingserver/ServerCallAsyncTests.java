// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallRecordingStateResult;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ServerCallAsyncTests extends CallingServerTestBase {
    private String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    // Calling Tests
    private String fromUser = getRandomUserId();
    private String toUser = getRandomUserId();
    private String alternateId =   "+11111111111";
    private String to =   "+11111111111";
    private String callBackUri = "https://host.app/api/callback/calling";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsAsync");
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        String groupId = "c400789f-e11b-4ceb-88cb-bc8df2a01568"; // This needs to match the recording
        List<CallConnectionAsync> callConnections = new ArrayList<CallConnectionAsync>();
        ServerCallAsync serverCall = null;

        try {
            callConnections = createCall(callingServerAsyncClient, groupId, fromUser, toUser);
            serverCall = callingServerAsyncClient.initializeServerCall(groupId);

            StartCallRecordingResult startCallRecordingResult = serverCall.startRecording(recordingStateCallbackUri).block();     
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.ACTIVE);

            serverCall.pauseRecording(recordingId).block();
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.INACTIVE);

            serverCall.resumeRecording(recordingId).block();         
            validateCallRecordingState(serverCall, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (serverCall != null) {
                try {
                    serverCall.stopRecording(recordingId).block();
                } catch (Exception e) {
                    System.out.println("Error stopping recording: " + e.getMessage());
                }
            }

            cleanUpConnectionsAsync(callConnections); 
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAllClientFunctionsWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAllClientFunctionsWithResponseAsync");  
        String recordingId = "";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        String groupId = "7936319e-4317-475d-919d-ada32213b700"; // This needs to match the recording
        List<CallConnectionAsync> callConnections = new ArrayList<CallConnectionAsync>();
        ServerCallAsync serverCallAsync = null;

        try {
            callConnections = createCall(callingServerAsyncClient, groupId, fromUser, toUser);
            serverCallAsync = callingServerAsyncClient.initializeServerCall(groupId);

            Response<StartCallRecordingResult> startRecordingResponse = serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri).block();
            assertEquals(startRecordingResponse.getStatusCode(), 200);
            StartCallRecordingResult startCallRecordingResult = startRecordingResponse.getValue();     
            recordingId = startCallRecordingResult.getRecordingId();
            validateCallRecordingStateWithResponse(serverCallAsync, recordingId, CallRecordingState.ACTIVE);

            Response<Void> pauseResponse = serverCallAsync.pauseRecordingWithResponse(recordingId).block();
            assert pauseResponse != null;
            assertEquals(pauseResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCallAsync, recordingId, CallRecordingState.INACTIVE);

            Response<Void> resumeResponse = serverCallAsync.resumeRecordingWithResponse(recordingId).block();
            assert resumeResponse != null;
            assertEquals(resumeResponse.getStatusCode(), 200);
            validateCallRecordingStateWithResponse(serverCallAsync, recordingId, CallRecordingState.ACTIVE);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            if (serverCallAsync != null) {
                try {
                    Response<Void> stopResponse = serverCallAsync.stopRecordingWithResponse(recordingId).block();
                    assertEquals(stopResponse.getStatusCode(), 200);                     
                } catch (Exception e) {
                    System.out.println("Error stopping recording: " + e.getMessage());
                }
            }

            cleanUpConnectionsAsync(callConnections); 
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionAsync");

        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);

        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);
        try {
            PlayAudioResult playAudioResult = serverCallAsync.playAudio(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runPlayAudioFunctionWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runPlayAudioFunctionWithResponseAsync");

        String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
        String audioFileUri =  "https://host.app/audio/bot-callcenter-intro.wav";
        String callbackUri = "https://dev.skype.net:6448";
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

        System.out.println("serverCallId: " + serverCallId);
        try {
            Response<PlayAudioResult> playAudioResult = serverCallAsync.playAudioWithResponse(audioFileUri, UUID.randomUUID().toString(), callbackUri, operationContext).block();
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult, operationContext);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void startRecordingFailsAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "startRecordingFailsAsync");
        String invalidServerCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDkuY29udi5za3lwZS5jb20vY29udi9EZVF2WEJGVVlFV1NNZkFXYno2azN3P2k9MTEmZT02Mzc1NzIyMjk0Mjc0NTI4Nzk=";
        String recordingStateCallbackUri = "https://dev.skype.net:6448";
        System.out.println("serverCallId: " + serverCallId);
        ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(invalidServerCallId);

        try {
            Response<StartCallRecordingResult> response = serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri).block();
            assert response != null;
            assertEquals(response.getStatusCode(), 400);
        } catch (CallingServerErrorException e) {
            assertEquals(e.getResponse().getStatusCode(), 400);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioAsync");

        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Get Server Call
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3NXdWxkazBmMEVpdnAxWjhiU2NuUHc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            serverCallAsync.addParticipant(new CommunicationUserIdentifier(toUser), null, operationContext, callBackUri).block();

            // Remove User
            String participantId = "206ac04a-1aae-4d82-9015-9c30cb174888";
            serverCallAsync.removeParticipant(participantId).block();

            // Hang up
            assert callConnectionAsync != null;
            callConnectionAsync.hangup().block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void runAddRemoveScenarioWithResponseAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "runAddRemoveScenarioWithResponseAsync");
        try {
            // Establish a call
            CreateCallOptions options = new CreateCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });

            options.setAlternateCallerId(new PhoneNumberIdentifier(alternateId));

            CallConnectionAsync callConnectionAsync = callingServerAsyncClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                new CommunicationIdentifier[] { new PhoneNumberIdentifier(to) },
                options).block();

            CallingServerTestUtils.validateCallConnectionAsync(callConnectionAsync);

            // Get Server Call
            String serverCallId = "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L0NUT014YmNIRmttZ1BqbE5kYjExNlE_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4";
            ServerCallAsync serverCallAsync = callingServerAsyncClient.initializeServerCall(serverCallId);

            // Add User
            String operationContext = "ac794123-3820-4979-8e2d-50c7d3e07b12";
            Response<Void> addResponse = serverCallAsync.addParticipantWithResponse(new CommunicationUserIdentifier(toUser), null, operationContext, callBackUri).block();
            CallingServerTestUtils.validateResponse(addResponse);

            // Remove User
            String participantId = "b133b1f3-4a11-49e4-abe0-ac9fdd660634";
            Response<Void> removeResponse = serverCallAsync.removeParticipantWithResponse(participantId).block();
            CallingServerTestUtils.validateResponse(removeResponse);

            // Hang up
            assert callConnectionAsync != null;
            callConnectionAsync.hangup().block();
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

    private void validateCallRecordingState(ServerCallAsync serverCallAsync,
            String recordingId,
            CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCallAsync);
        assertNotNull(serverCallAsync.getServerCallId());
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        CallRecordingStateResult callRecordingStateResult = serverCallAsync.getRecordingState(recordingId).block();
        assert callRecordingStateResult != null;
        assertEquals(callRecordingStateResult.getRecordingState(), expectedCallRecordingState);
    }

    protected void validateCallRecordingStateWithResponse(ServerCallAsync serverCallAsync,
            String recordingId,
            CallRecordingState expectedCallRecordingState) {
        assertNotNull(serverCallAsync);
        assertNotNull(serverCallAsync.getServerCallId());
        assertNotNull(recordingId);


        // There is a delay between the action and when the state is available.
        // Waiting to make sure we get the updated state, when we are running
        // against a live service.
        sleepIfRunningAgainstService(6000);

        Response<CallRecordingStateResult> response = serverCallAsync.getRecordingStateWithResponse(recordingId).block();
        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertNotNull(response.getValue());
        assertEquals(response.getValue().getRecordingState(), expectedCallRecordingState);
    }
    
    protected void cleanUpConnectionsAsync(List<CallConnectionAsync> connections) {
        if (connections == null) {
            return;
        }
        
        connections.forEach(c -> {
            if (c != null) {
                try {
                    c.hangup().block();  
                } catch (Exception e) {
                    System.out.println("Error hanging up: " + e.getMessage());
                }
            }
        }); 
    }

    protected List<CallConnectionAsync> createCall(CallingServerAsyncClient callingServerClient, String groupId, String from, String to) {
        CallConnectionAsync fromCallConnection =  null;
        CallConnectionAsync toCallConnection = null;

        try {          
            CommunicationIdentifier fromParticipant = new CommunicationUserIdentifier(from);
            CommunicationIdentifier toParticipant = new CommunicationUserIdentifier(to);

            JoinCallOptions fromCallOptions = new JoinCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });
            fromCallConnection = callingServerClient.join(groupId, fromParticipant, fromCallOptions).block();
            sleepIfRunningAgainstService(1000);
            CallingServerTestUtils.validateCallConnectionAsync(fromCallConnection);
            
            JoinCallOptions joinCallOptions = new JoinCallOptions(
                callBackUri,
                new CallModality[] { CallModality.AUDIO },
                new EventSubscriptionType[] { EventSubscriptionType.PARTICIPANTS_UPDATED });
            
            toCallConnection = callingServerClient.join(groupId, toParticipant, joinCallOptions).block();
            sleepIfRunningAgainstService(1000);
            CallingServerTestUtils.validateCallConnectionAsync(toCallConnection);

            return Arrays.asList(fromCallConnection, toCallConnection);
        } catch (Exception e) {
            System.out.println("Error creating call: " + e.getMessage());

            if (fromCallConnection != null) {
                fromCallConnection.hangup();
            }

            if (toCallConnection != null) {
                toCallConnection.hangup();
            }

            throw e;
        }        
    }    
}
