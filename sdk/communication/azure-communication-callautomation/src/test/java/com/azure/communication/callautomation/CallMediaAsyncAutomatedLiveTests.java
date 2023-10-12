// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
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
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class CallMediaAsyncAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {
    @DoNotRecord(skipInPlayback = true)
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

    @DoNotRecord(skipInPlayback = true)
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
         * 5. Send Dtmf tones to target, expect SendDtmfTonesCompleted event and no failure(exception)
         * 6. Stop continuous Dtmf detection on source, expect ContinuousDtmfDetectionStopped event and no failure(exception)
         * 7. Hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("dtmfActionsInACallAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            PhoneNumberIdentifier caller;
            PhoneNumberIdentifier receiver;
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
            CallInvite invite = new CallInvite(receiver, caller);
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

            // validate SendDtmfTonesCompleted
            SendDtmfTonesCompleted sendDtmfTonesCompleted = waitForEvent(SendDtmfTonesCompleted.class, callerConnectionId, Duration.ofSeconds(20));
            assertNotNull(sendDtmfTonesCompleted);

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
}
