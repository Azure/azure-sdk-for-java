// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallConnectionState;
import com.azure.communication.callautomation.models.ChannelAffinity;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.RecordingChannel;
import com.azure.communication.callautomation.models.RecordingContent;
import com.azure.communication.callautomation.models.RecordingFormat;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.callautomation.models.events.CallConnectedEvent;
import com.azure.communication.callautomation.models.events.CallDisconnectedEvent;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
public class CallRecordingAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {
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
        CallAutomationClient client = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recordingOperations", next))
            .buildClient();

        CommunicationIdentityClient communicationIdentityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .buildClient();

        String callConnectionId = "";
        try {
            // Create caller and receiver
            CommunicationUserIdentifier source = communicationIdentityClient.createUser();
            CommunicationUserIdentifier target = communicationIdentityClient.createUser();

            // setup service bus
            String uniqueId = serviceBusWithNewCall(source, target);

            // create call and assert response
            CreateCallResult createCallResult = client.createCall(
                source, Arrays.asList(target), String.format("%s?q=%s", DISPATCHER_CALLBACK, uniqueId)
            );
            callConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callConnectionId);

            // wait for incoming call context
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // answer the call
            AnswerCallResult answerCallResult = client.answerCall(incomingCallContext, DISPATCHER_CALLBACK);
            assertNotNull(answerCallResult);

            // wait for callConnected
            CallConnectedEvent callConnectedEvent = waitForEvent(CallConnectedEvent.class, callConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnectedEvent);

            // get properties
            CallConnectionProperties callConnectionProperties = createCallResult.getCallConnection().getCallProperties();
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            // start recording
            RecordingStateResult recordingStateResult = client.getCallRecording().startRecording(
                new StartRecordingOptions(new ServerCallLocator(callConnectionProperties.getServerCallId()))
                    .setRecordingChannel(RecordingChannel.UNMIXED)
                    .setRecordingContent(RecordingContent.AUDIO)
                    .setRecordingFormat(RecordingFormat.WAV)
                    .setRecordingStateCallbackUrl(DISPATCHER_CALLBACK)
            );

            assertNotNull(recordingStateResult.getRecordingId());

            // stop recording
            client.getCallRecording().stopRecording(recordingStateResult.getRecordingId());

            // hangup
            if (!callConnectionId.isEmpty()) {
                CallConnection callConnection = client.getCallConnection(callConnectionId);
                callConnection.hangUp(true);
                CallDisconnectedEvent callDisconnectedEvent = waitForEvent(CallDisconnectedEvent.class, callConnectionId, Duration.ofSeconds(10));
                assertNotNull(callDisconnectedEvent);
            }
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }

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
        CallAutomationClient client = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recordingOperations", next))
            .buildClient();

        CommunicationIdentityClient communicationIdentityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .buildClient();

        String callConnectionId = "";
        try {
            // Create caller and receiver
            CommunicationUserIdentifier source = communicationIdentityClient.createUser();
            CommunicationUserIdentifier target = communicationIdentityClient.createUser();

            // setup service bus
            String uniqueId = serviceBusWithNewCall(source, target);

            // create call and assert response
            CreateCallResult createCallResult = client.createCall(
                source, Arrays.asList(target), String.format("%s?q=%s", DISPATCHER_CALLBACK, uniqueId)
            );
            callConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callConnectionId);

            // wait for incoming call context
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // answer the call
            AnswerCallResult answerCallResult = client.answerCall(incomingCallContext, DISPATCHER_CALLBACK);
            assertNotNull(answerCallResult);

            // wait for callConnected
            CallConnectedEvent callConnectedEvent = waitForEvent(CallConnectedEvent.class, callConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnectedEvent);

            // get properties
            CallConnectionProperties callConnectionProperties = createCallResult.getCallConnection().getCallProperties();
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            // start recording
            RecordingStateResult recordingStateResult = client.getCallRecording().startRecording(
                new StartRecordingOptions(new ServerCallLocator(callConnectionProperties.getServerCallId()))
                    .setRecordingChannel(RecordingChannel.UNMIXED)
                    .setRecordingContent(RecordingContent.AUDIO)
                    .setRecordingFormat(RecordingFormat.WAV)
                    .setRecordingStateCallbackUrl(DISPATCHER_CALLBACK)
                    .setChannelAffinity(new ArrayList<ChannelAffinity>() {
                        {
                            add(new ChannelAffinity(0, source));
                            add(new ChannelAffinity(1, target));
                        }
                    })
            );

            assertNotNull(recordingStateResult.getRecordingId());

            // stop recording
            client.getCallRecording().stopRecording(recordingStateResult.getRecordingId());

            // hangup
            if (!callConnectionId.isEmpty()) {
                CallConnection callConnection = client.getCallConnection(callConnectionId);
                callConnection.hangUp(true);
                CallDisconnectedEvent callDisconnectedEvent = waitForEvent(CallDisconnectedEvent.class, callConnectionId, Duration.ofSeconds(10));
                assertNotNull(callDisconnectedEvent);
            }
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
