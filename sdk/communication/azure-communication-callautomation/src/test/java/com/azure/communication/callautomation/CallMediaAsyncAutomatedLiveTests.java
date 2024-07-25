// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallIntelligenceOptions;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionStopped;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.SendDtmfTonesCompleted;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.communication.phonenumbers.PhoneNumbersClient;
import com.azure.communication.phonenumbers.PhoneNumbersClientBuilder;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.MediaStreamingAudioChannel;
import com.azure.communication.callautomation.models.MediaStreamingContentType;
import com.azure.communication.callautomation.models.MediaStreamingOptions;
import com.azure.communication.callautomation.models.MediaStreamingTransport;
import com.azure.communication.callautomation.models.TranscriptionOptions;
import com.azure.communication.callautomation.models.StartMediaStreamingOptions;
import com.azure.communication.callautomation.models.StopMediaStreamingOptions;
import com.azure.communication.callautomation.models.StartTranscriptionOptions;
import com.azure.communication.callautomation.models.StopTranscriptionOptions;
import com.azure.communication.callautomation.models.TranscriptionTransport;
import com.azure.communication.callautomation.models.events.MediaStreamingStarted;
import com.azure.communication.callautomation.models.events.MediaStreamingStopped;
import com.azure.communication.callautomation.models.events.TranscriptionStarted;
import com.azure.communication.callautomation.models.events.TranscriptionStopped;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.TextSource;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.test.annotation.DoNotRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CallMediaAsyncAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMediaInACallAutomatedTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to all participants
         * 5. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("playMediaInACallAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMediaInACallAutomatedTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMediaInACallAutomatedTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // play media to all participants
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.playToAll(new FileSource().setUrl(MEDIA_SOURCE)).block();
            PlayCompleted playCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playCompleted);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void dtmfActionsInACallAutomatedTest(HttpClient httpClient) {
        /* Test case:  Continuous Dtmf start, Stop and Send Dtmf in an ACS to ACS PSTN call
         * 1. Create a CallAutomationClient.
         * 2. Create a call from source to one ACS target.
         * 3. Get updated call properties and check for the connected state.
         * 4. Start continuous Dtmf detection on source, expect no failure(exception)
         * 5. Send Dtmf tones to target, expect SendDtmfCompleted event and no failure(exception)
         * 6. Stop continuous Dtmf detection on source, expect ContinuousDtmfDetectionStopped event and no failure(exception)
         * 7. Hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("dtmfActionsInACallAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            CommunicationIdentifier caller;
            CommunicationIdentifier receiver;
            // when in playback, use Sanitized values
            if (getTestMode() == TestMode.PLAYBACK) {
                caller = new PhoneNumberIdentifier("Sanitized");
                receiver = new PhoneNumberIdentifier("Sanitized");
            } else {
                PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
                    .connectionString(CONNECTION_STRING)
                    .httpClient(getHttpClientOrUsePlayback(httpClient))
                    .buildClient();
                List<String> phoneNumbers = phoneNumberClient.listPurchasedPhoneNumbers().stream().map(PurchasedPhoneNumber::getPhoneNumber).collect(Collectors.toList());
                receiver = new PhoneNumberIdentifier(phoneNumbers.get(1));
                caller = new PhoneNumberIdentifier(phoneNumbers.get(0));
            }

            CallAutomationAsyncClient client = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("dtmfActionsInACallAutomatedTest", next))
                .sourceIdentity(identityAsyncClient.createUser().block())
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            CallInvite invite = new CallInvite((PhoneNumberIdentifier) receiver, (PhoneNumberIdentifier) caller);
            CreateCallResult createCallResult = client.createCall(invite, DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId)).block();

            // validate the call
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());

            // get call connection id
            String callerConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callerConnectionId);

            // wait for the incomingCallContext
            String incomingCallContext = waitForIncomingCallContext(uniqueId, Duration.ofSeconds(20));
            assertNotNull(incomingCallContext);

            // answer the call
            AnswerCallOptions answerCallOptions = new AnswerCallOptions(incomingCallContext,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            AnswerCallResult answerCallResult = Objects.requireNonNull(client.answerCallWithResponse(answerCallOptions).block()).getValue();
            assertNotNull(answerCallResult);
            assertNotNull(answerCallResult.getCallConnectionAsync());
            assertNotNull(answerCallResult.getCallConnectionProperties());
            String targetConnectionId = answerCallResult.getCallConnectionProperties().getCallConnectionId();
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(callConnected);

            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();

            // start continuous dtmf detection on target
            callMediaAsync.startContinuousDtmfRecognition(receiver).block();

            // send Dtmf tones to target
            callMediaAsync.sendDtmfTones(Stream.of(DtmfTone.A, DtmfTone.B).collect(Collectors.toList()), receiver).block();

            // validate SendDtmfCompleted
            SendDtmfTonesCompleted sendDtmfCompleted = waitForEvent(SendDtmfTonesCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(sendDtmfCompleted);

            // stop continuous dtmf
            callMediaAsync.stopContinuousDtmfRecognition(receiver).block();

            // validate ContinuousDtmfRecognitionStopped
            ContinuousDtmfRecognitionStopped continuousDtmfRecognitionStopped = waitForEvent(
                ContinuousDtmfRecognitionStopped.class, callerConnectionId, Duration.ofSeconds(10)
            );
            assertNotNull(continuousDtmfRecognitionStopped);

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
    public void holdUnholdParticipantInACallTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. hold the participant
         * 5. unhold the participant
         * 6. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("holdUnholdParticipantInACallTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationUserIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("HoldUnholdParticipantInACallTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("HoldUnholdParticipantInACallTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);
        
            // hold the participant
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.hold(receiver).block();
            
            sleepIfRunningAgainstService(3000);
            CallConnectionAsync callConnectionAsync = callerAsyncClient.getCallConnectionAsync(callerConnectionId);

            CallParticipant participantResult = callConnectionAsync.getParticipant(receiver).block();
            assertNotNull(participantResult);
            assertTrue(participantResult.isOnHold());

            // unhold the participant
            callMediaAsync.unhold(receiver).block();

            sleepIfRunningAgainstService(3000);
            participantResult = callConnectionAsync.getParticipant(receiver).block();
            assertNotNull(participantResult);
            assertFalse(participantResult.isOnHold());
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

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

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

            // create options
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            MediaStreamingOptions mediaStreamingOptions = new MediaStreamingOptions(TRANSPORT_URL, MediaStreamingTransport.WEBSOCKET, MediaStreamingContentType.AUDIO, MediaStreamingAudioChannel.MIXED, false);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));

            createCallOptions.setMediaStreamingOptions(mediaStreamingOptions);

            // create a call
            Response<CreateCallResult> createCallResultResponse = callerAsyncClient.createGroupCallWithResponse(createCallOptions).block();
            assertNotNull(createCallResultResponse);

             // validate the call
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());

            // get call connection id
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

             // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);
             
            // Start Media Streaming
            StartMediaStreamingOptions startMediaStreamingOptions = new StartMediaStreamingOptions();
            // startMediaStreamingOptions.setOperationCallbackUrl(DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            CallMediaAsync callMedia = callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync();

            System.out.println("TRANSPORT_URL: " + TRANSPORT_URL);
            callMedia.startMediaStreamingWithResponse(startMediaStreamingOptions).block();

            MediaStreamingStarted mediaStreamingStarted = waitForEvent(MediaStreamingStarted.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(mediaStreamingStarted);

            // Stop Media Streaming
            StopMediaStreamingOptions stopMediaStreamingOptions = new StopMediaStreamingOptions();
           // stopMediaStreamingOptions.setOperationCallbackUrl(DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));

            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().stopMediaStreamingWithResponse(stopMediaStreamingOptions).block();
            MediaStreamingStopped mediaStreamingStopped = waitForEvent(MediaStreamingStopped.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(mediaStreamingStopped);
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

        List<CallConnectionAsync> callDestructors = new ArrayList<>();    

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
            TranscriptionOptions transcriptionOptions = new TranscriptionOptions(TRANSPORT_URL, TranscriptionTransport.WEBSOCKET, "en-US", false);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                    DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setTranscriptionOptions(transcriptionOptions);
            createCallOptions.setCallIntelligenceOptions(new CallIntelligenceOptions().setCognitiveServicesEndpoint(COGNITIVE_SERVICE_ENDPOINT));
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // Start Transcription
            StartTranscriptionOptions startTranscriptionOptions = new StartTranscriptionOptions();
            startTranscriptionOptions.setLocale("en-US");    

            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().startTranscriptionWithResponse(startTranscriptionOptions).block();
            TranscriptionStarted transcriptionStarted = waitForEvent(TranscriptionStarted.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(transcriptionStarted);

            // Stop Transcription
            StopTranscriptionOptions stopTranscriptionOptions = new StopTranscriptionOptions();
            callerAsyncClient.getCallConnectionAsync(callerConnectionId).getCallMediaAsync().stopTranscriptionWithResponse(stopTranscriptionOptions).block();
            TranscriptionStopped transcriptionStopped = waitForEvent(TranscriptionStopped.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(transcriptionStopped);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMultipleFileSourcesWithPlayMediaTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to target participant with mutiple file prompts
         * 5. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleFileSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);
            // Assert multiple File Sources
            List<PlaySource> playFileSources = new ArrayList<PlaySource>();
            playFileSources.add(new FileSource().setUrl(MEDIA_SOURCE));
            playFileSources.add(new FileSource().setUrl(MEDIA_SOURCE));

            // Play multiple files sources
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.play(playFileSources, targets).block();

            PlayCompleted playCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playCompleted);

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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMultipleFileSourcesWithPlayMediaAllTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to all participants with mutiple file prompts
         * 6. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleFileSourcesWithPlayMediaAllTest", next))
                .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);
            // Assert multiple File Sources
            List<PlaySource> playFileSources = new ArrayList<PlaySource>();
            playFileSources.add(new FileSource().setUrl(MEDIA_SOURCE));
            playFileSources.add(new FileSource().setUrl(MEDIA_SOURCE));

            // Play multiple files sources
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.playToAll(playFileSources).block();

            PlayCompleted playCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playCompleted);

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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMultipleTextSourcesWithPlayMediaAllTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to target participant with mutiple file prompts
         * 5. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleTextSourcesWithPlayMediaAllTest", next))
                .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setCallIntelligenceOptions(new CallIntelligenceOptions().setCognitiveServicesEndpoint(COGNITIVE_SERVICE_ENDPOINT));
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);
        
            // Assert multiple Text Sources
            String speechToTextVoice = "en-US-NancyNeural";
            List<PlaySource> playTextSources = new ArrayList<PlaySource>();
            playTextSources.add(new TextSource().setText("Test prompt1").setVoiceName(speechToTextVoice));
            playTextSources.add(new TextSource().setText("Test prompt2").setVoiceName(speechToTextVoice));

            // Play multiple files sources
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.play(playTextSources, targets).block();

            PlayCompleted playCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playCompleted);

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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMultipleTextSourcesWithPlayMediaTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to all participants with mutiple prompts
         * 5. play a media to target participant with mutiple prompts
         * 6. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleTextSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setCallIntelligenceOptions(new CallIntelligenceOptions().setCognitiveServicesEndpoint(COGNITIVE_SERVICE_ENDPOINT));
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // Assert multiple Text Sources
            String speechToTextVoice = "en-US-NancyNeural";
            List<PlaySource> playTextSources = new ArrayList<PlaySource>();
            playTextSources.add(new TextSource().setText("Test prompt1").setVoiceName(speechToTextVoice));
            playTextSources.add(new TextSource().setText("Test prompt2").setVoiceName(speechToTextVoice));

            // Play multiple files sources
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.play(playTextSources, targets).block();

            PlayCompleted playCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playCompleted);

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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMultipleCombinedSourcesWithPlayMediaTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to all participants with mutiple prompts
         * 5. play a media to target participant with mutiple prompts
         * 6. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("playMultipleCombinedSourcesWithPlayMediaTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setCallIntelligenceOptions(new CallIntelligenceOptions().setCognitiveServicesEndpoint(COGNITIVE_SERVICE_ENDPOINT));
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // Assert multiple Text and File Sources
            String speechToTextVoice = "en-US-NancyNeural";
            List<PlaySource> playTextAndFileSources = new ArrayList<PlaySource>();
            playTextAndFileSources.add(new TextSource().setText("Test prompt1").setVoiceName(speechToTextVoice));
            playTextAndFileSources.add(new FileSource().setUrl(MEDIA_SOURCE));
          
            // play File media to Target participant
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.play(playTextAndFileSources, targets).block();
            
            PlayCompleted playFileCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playFileCompleted);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void playMultipleCombinedSourcesWithPlayMediaAllTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. play a media to all participants with mutiple prompts
         * 5. play a media to target participant with mutiple prompts
         * 6. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("playMultipleCombinedSourcesWithPlayMediaAllTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("playMultipleSourcesWithPlayMediaTest", next))
                .buildAsyncClient();

            String uniqueId = serviceBusWithNewCall(caller, receiver);

            // create a call
            List<CommunicationIdentifier> targets = Collections.singletonList(receiver);
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            createCallOptions.setCallIntelligenceOptions(new CallIntelligenceOptions().setCognitiveServicesEndpoint(COGNITIVE_SERVICE_ENDPOINT));
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
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // Assert multiple Text and File Sources
            String speechToTextVoice = "en-US-NancyNeural";
            List<PlaySource> playTextAndFileSources = new ArrayList<PlaySource>();
            playTextAndFileSources.add(new TextSource().setText("Test prompt1").setVoiceName(speechToTextVoice));
            playTextAndFileSources.add(new FileSource().setUrl(MEDIA_SOURCE));
          
            // play File media to Target participant
            CallMediaAsync callMediaAsync = createCallResult.getCallConnectionAsync().getCallMediaAsync();
            callMediaAsync.playToAll(playTextAndFileSources).block();
            
            PlayCompleted playFileCompleted = waitForEvent(PlayCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(playFileCompleted);
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
}
