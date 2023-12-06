// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallConnectionState;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.RecordingChannel;
import com.azure.communication.callautomation.models.RecordingContent;
import com.azure.communication.callautomation.models.RecordingFormat;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CallDisconnected;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
public class CallRecordingAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {
    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createACSCallAndUnmixedAudioTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. start recording the call without channel affinity
         * 5. stop recording the call
         * 6. hang up the call.
         * 7. once call is hung up, verify disconnected event
         */
        CommunicationIdentityClient communicationIdentityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .buildClient();

        String callConnectionId = "";
        try {
            // Create caller and receiver
            CommunicationUserIdentifier source = communicationIdentityClient.createUser();
            CommunicationUserIdentifier target = communicationIdentityClient.createUser();

            // Create call automation client and use source as the caller.
            CallAutomationClient callerClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("recordingOperations", next))
                .sourceIdentity(source)
                .buildClient();

            // Create call automation client for receivers.
            CallAutomationClient receiverClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("recordingOperations", next))
                .buildClient();

            // setup service bus
            String uniqueId = serviceBusWithNewCall(source, target);

            // create call and assert response
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(Arrays.asList(target), String.format("%s?q=%s", DISPATCHER_CALLBACK, uniqueId));
            CreateCallResult createCallResult = callerClient.createGroupCallWithResponse(createCallOptions, null).getValue();
            callConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callConnectionId);

            // wait for incoming call context
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // answer the call
            AnswerCallOptions answerCallOptions = new AnswerCallOptions(incomingCallContext, DISPATCHER_CALLBACK);
            AnswerCallResult answerCallResult = receiverClient.answerCallWithResponse(answerCallOptions, null).getValue();
            assertNotNull(answerCallResult);

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // get properties
            CallConnectionProperties callConnectionProperties = createCallResult.getCallConnection().getCallProperties();
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            // start recording
            RecordingStateResult recordingStateResult = callerClient.getCallRecording().start(
                new StartRecordingOptions(new ServerCallLocator(callConnectionProperties.getServerCallId()))
                    .setRecordingChannel(RecordingChannel.UNMIXED)
                    .setRecordingContent(RecordingContent.AUDIO)
                    .setRecordingFormat(RecordingFormat.WAV)
                    .setRecordingStateCallbackUrl(DISPATCHER_CALLBACK)
            );

            assertNotNull(recordingStateResult.getRecordingId());

            // stop recording
            callerClient.getCallRecording().stop(recordingStateResult.getRecordingId());

            // hangup
            if (!callConnectionId.isEmpty()) {
                CallConnection callConnection = callerClient.getCallConnection(callConnectionId);
                callConnection.hangUp(true);
                CallDisconnected callDisconnected = waitForEvent(CallDisconnected.class, callConnectionId, Duration.ofSeconds(10));
                assertNotNull(callDisconnected);
            }
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createACSCallUnmixedAudioAffinityTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. start recording the call with channel affinity
         * 5. stop recording the call
         * 6. hang up the call.
         * 7. once call is hung up, verify disconnected event
         */
        CommunicationIdentityClient communicationIdentityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .buildClient();

        String callConnectionId = "";
        try {
            // Create caller and receiver
            CommunicationUserIdentifier source = communicationIdentityClient.createUser();
            CommunicationUserIdentifier target = communicationIdentityClient.createUser();

            // Create call automation client and use source as the caller.
            CallAutomationClient callerClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("recordingOperations", next))
                .sourceIdentity(source)
                .buildClient();

            // Create call automation client for receivers.
            CallAutomationClient receiverClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("recordingOperations", next))
                .buildClient();

            // setup service bus
            String uniqueId = serviceBusWithNewCall(source, target);

            // create call and assert response
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(Arrays.asList(target), String.format("%s?q=%s", DISPATCHER_CALLBACK, uniqueId));
            CreateCallResult createCallResult = callerClient.createGroupCallWithResponse(createCallOptions, null).getValue();
            callConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callConnectionId);

            // wait for incoming call context
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // answer the call
            AnswerCallOptions answerCallOptions = new AnswerCallOptions(incomingCallContext, DISPATCHER_CALLBACK);
            AnswerCallResult answerCallResult = receiverClient.answerCallWithResponse(answerCallOptions, null).getValue();
            assertNotNull(answerCallResult);

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // get properties
            CallConnectionProperties callConnectionProperties = createCallResult.getCallConnection().getCallProperties();
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            // start recording
            RecordingStateResult recordingStateResult = callerClient.getCallRecording().start(
                new StartRecordingOptions(new ServerCallLocator(callConnectionProperties.getServerCallId()))
                    .setRecordingChannel(RecordingChannel.UNMIXED)
                    .setRecordingContent(RecordingContent.AUDIO)
                    .setRecordingFormat(RecordingFormat.WAV)
                    .setRecordingStateCallbackUrl(DISPATCHER_CALLBACK)
                    .setAudioChannelParticipantOrdering(new ArrayList<CommunicationIdentifier>() {
                        {
                            add(source);
                            add(target);
                        }
                    })
            );

            assertNotNull(recordingStateResult.getRecordingId());

            // stop recording
            callerClient.getCallRecording().stop(recordingStateResult.getRecordingId());

            // hangup
            if (!callConnectionId.isEmpty()) {
                CallConnection callConnection = callerClient.getCallConnection(callConnectionId);
                callConnection.hangUpWithResponse(true, null);
                CallDisconnected callDisconnected = waitForEvent(CallDisconnected.class, callConnectionId, Duration.ofSeconds(10));
                assertNotNull(callDisconnected);
            }
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
