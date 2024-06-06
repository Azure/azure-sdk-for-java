// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.RejectCallOptions;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CallDisconnected;
import com.azure.communication.callautomation.models.events.ParticipantsUpdated;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.DoNotRecord;

import src.main.java.com.azure.communication.callautomation.CallAutomationAsyncClient;
import src.main.java.com.azure.communication.callautomation.CallConnectionAsync;
import src.main.java.com.azure.communication.callautomation.models.MediaStreamingAudioChannel;
import src.main.java.com.azure.communication.callautomation.models.MediaStreamingTransport;
import src.main.java.com.azure.communication.callautomation.models.StartTranscriptionOptions;
import src.main.java.com.azure.communication.callautomation.models.StopTranscriptionOptions;
import src.main.java.com.azure.communication.callautomation.models.TranscriptionOptions;
import src.main.java.com.azure.communication.callautomation.models.events.MediaStreamingStarted;
import src.main.java.com.azure.communication.callautomation.models.events.MediaStreamingStopped;
import src.main.java.com.azure.communication.callautomation.models.events.TranscriptionStarted;
import src.main.java.com.azure.communication.callautomation.models.events.TranscriptionStopped;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.azure.communication.callautomation.models.MediaStreamingAudioChannel;
import com.azure.communication.callautomation.models.MediaStreamingContentType;
import com.azure.communication.callautomation.models.MediaStreamingOptions;
import com.azure.communication.callautomation.models.MediaStreamingTransport;
import com.azure.communication.callautomation.models.StartMediaStreamingOptions;
import com.azure.communication.callautomation.models.StopMediaStreamingOptions;
import com.azure.communication.callautomation.models.TranscriptionTransportType;
import com.azure.communication.callautomation.models.events.MediaStreamingStarted;
import com.azure.communication.callautomation.models.events.MediaStreamingStopped;
import com.azure.communication.callautomation.models.events.TranscriptionStarted;
import com.azure.communication.callautomation.models.events.TranscriptionStopped;

public class CallAutomationAsyncClientAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
            named = "SKIP_LIVE_TEST",
            matches = "(?i)(true)",
            disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndAnswerThenHangupAutomatedTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. hang up the call.
         * 5. once call is hung up, verify disconnected event
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenHangupAutomatedTest", next))
                .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            // Create call automation client and use source as the caller.
            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenHangupAutomatedTest", next))
                    .sourceIdentity(caller)
                    .buildAsyncClient();
            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenHangupAutomatedTest", next))
                    .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, target);

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            Response<CreateCallResult> createCallResultResponse = callerAsyncClient.createGroupCallWithResponse(createCallOptions).block();

            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());
            String callerConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callerConnectionId);

            // wait for the incomingCallContext
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // answer the call
            AnswerCallOptions answerCallOptions = new AnswerCallOptions(incomingCallContext,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            AnswerCallResult answerCallResult = Objects.requireNonNull(receiverAsyncClient.answerCallWithResponse(answerCallOptions).block()).getValue();
            assertNotNull(answerCallResult);
            assertNotNull(answerCallResult.getCallConnectionAsync());
            assertNotNull(answerCallResult.getCallConnectionProperties());
            String receiverConnectionId = answerCallResult.getCallConnectionProperties().getCallConnectionId();
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // check events to caller side
            CallConnected callerCallConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            ParticipantsUpdated callerParticipantUpdatedEvent = waitForEvent(ParticipantsUpdated.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callerCallConnected);
            assertNotNull(callerParticipantUpdatedEvent);

            // check events to receiver side
            CallConnected receiverCallConnected = waitForEvent(CallConnected.class, receiverConnectionId, Duration.ofSeconds(10));
            ParticipantsUpdated receiverParticipantUpdatedEvent = waitForEvent(ParticipantsUpdated.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(receiverCallConnected);
            assertNotNull(receiverParticipantUpdatedEvent);

            // hang up the call.
            answerCallResult.getCallConnectionAsync().hangUp(true).block();

            // check if both parties had the call terminated.
            CallDisconnected callerCallDisconnected = waitForEvent(CallDisconnected.class, receiverConnectionId, Duration.ofSeconds(10));
            CallDisconnected receiverCallDisconnected = waitForEvent(CallDisconnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callerCallDisconnected);
            assertNotNull(receiverCallDisconnected);

        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        } finally {
            if (!callDestructors.isEmpty()) {
                try {
                    callDestructors.forEach(callConnection -> callConnection.hangUpWithResponse(true).block());
                } catch (Exception ignored) {
                    // Some call might have been terminated during the test, and it will cause exceptions here.
                    // Do nothing and iterate to next call connection.
                }
            }
        }
    }

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
            named = "SKIP_LIVE_TEST",
            matches = "(?i)(true)",
            disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndRejectAutomatedTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call but rejected
         * 1. create a CallAutomationClient.
         * 2. Reject
         * 3. See if call is not established
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndRejectAutomatedTest", next))
                .buildAsyncClient();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            // Create call automation client and use source as the caller.
            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndRejectAutomatedTest", next))
                    .sourceIdentity(caller)
                    .buildAsyncClient();
            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndRejectAutomatedTest", next))
                    .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, target);

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            Response<CreateCallResult> createCallResultResponse = callerAsyncClient.createGroupCallWithResponse(createCallOptions).block();

            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());
            String callerConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callerConnectionId);

            // wait for the incomingCallContext
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // rejet the call
            RejectCallOptions rejectCallOptions = new RejectCallOptions(incomingCallContext);
            receiverAsyncClient.rejectCallWithResponse(rejectCallOptions).block();

            // check events
            CallDisconnected callDisconnected = waitForEvent(CallDisconnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callDisconnected);
            assertThrows(RuntimeException.class, () -> createCallResult.getCallConnection().getCallProperties());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
            named = "SKIP_LIVE_TEST",
            matches = "(?i)(true)",
            disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndMediaStreamingTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call and Media Streaming
     * 1. create a CallAutomationClient.
     * 2. Start Media Streaming and Stop Media Streaming
     * 3. See Media Streaming sterted and stoped in call
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndMediaStreamingTest", next))
                .buildAsyncClient();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            // Create call automation client and use source as the caller.
            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndRejectAutomatedTest", next))
                    .sourceIdentity(caller)
                    .buildAsyncClient();
            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndRejectAutomatedTest", next))
                    .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, target);

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            MediaStreamingOptions mediaStreamingOptions = new MediaStreamingOptions("wss://localhost", MediaStreamingTransport.WEBSOCKET, MediaStreamingContentType.AUDIO, MediaStreamingAudioChannel.MIXED, false);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setMediaStreamingOptions(mediaStreamingOptions);
            Response<CreateCallResult> createCallResultResponse = callerAsyncClient.createGroupCallWithResponse(createCallOptions).block();

            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());
            String callerConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callerConnectionId);

            // wait for the incomingCallContext
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // Start Media Streaming
            StartMediaStreamingOptions startMediaStreamingOptions = new StartMediaStreamingOptions();
            startMediaStreamingOptions.setOperationCallbackUrl(DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));

            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().startMediaStreamingWithResponse(startMediaStreamingOptions, Context.NONE);
            MediaStreamingStarted mediaStreamingStarted = waitForEvent("MediaStreamingStarted", callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(mediaStreamingStarted);

            // Stop Media Streaming
            StopMediaStreamingOptions stopMediaStreamingOptions = new StopMediaStreamingOptions();
            stopMediaStreamingOptions.setOperationCallbackUrl(DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));

            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().stopMediaStreamingWithResponse(stopMediaStreamingOptions, Context.NONE);
            MediaStreamingStopped mediaStreamingStopped = waitForEvent("MediaStreamingStopped", callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(mediaStreamingStopped);

            // hang up the call.
            callerAsyncClient.getCallConnectionAsync().hangUp(true).block();

            // check events
            CallDisconnected callDisconnected = waitForEvent(CallDisconnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callDisconnected);
            assertThrows(RuntimeException.class, () -> createCallResult.getCallConnection().getCallProperties());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
            named = "SKIP_LIVE_TEST",
            matches = "(?i)(true)",
            disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndTranscriptionTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call and Media Streaming
     * 1. create a CallAutomationClient.
     * 2. Start Transcription and Stop Transcription
     * 3. See Transcription sterted and stoped in call
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndTranscriptionTest", next))
                .buildAsyncClient();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            // Create call automation client and use source as the caller.
            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndTranscriptionTest", next))
                    .sourceIdentity(caller)
                    .buildAsyncClient();
            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndTranscriptionTest", next))
                    .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, target);

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            TranscriptionOptions transcriptionOptions = new TranscriptionOptions("wss://localhost", TranscriptionTransportType.WEBSOCKET, "en-US", false);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setTranscriptionOptions(transcriptionOptions);
            Response<CreateCallResult> createCallResultResponse = callerAsyncClient.createGroupCallWithResponse(createCallOptions).block();

            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());
            String callerConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callerConnectionId);

            // wait for the incomingCallContext
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(10));
            assertNotNull(incomingCallContext);

            // Start Transcription
            StartTranscriptionOptions startTranscriptionOptions = new StartTranscriptionOptions();
            startTranscriptionOptions.setLocale("en-US");    

            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().startTranscriptionWithResponse(startTranscriptionOptions, Context.NONE);
            TranscriptionStarted transcriptionStarted = waitForEvent("TranscriptionStarted", callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(transcriptionStarted);
            
            // Stop Transcription
            StopTranscriptionOptions stopTranscriptionOptions = new StopTranscriptionOptions();
            stopTranscriptionOptions.setLocale("en-US");    
            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().stopTranscriptionWithResponse(stopTranscriptionOptions, Context.NONE);
            TranscriptionStopped transcriptionStopped = waitForEvent("TranscriptionStopped", callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(transcriptionStopped);

            // hang up the call.
            callerAsyncClient.getCallConnectionAsync().hangUp(true).block();

            // check events
            CallDisconnected callDisconnected = waitForEvent(CallDisconnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callDisconnected);
            assertThrows(RuntimeException.class, () -> createCallResult.getCallConnection().getCallProperties());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
