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
import com.azure.communication.callautomation.models.events.CreateCallFailed;
import com.azure.communication.callautomation.models.events.ParticipantsUpdated;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
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

import com.azure.communication.callautomation.models.ConnectCallOptions;
import com.azure.communication.callautomation.models.ConnectCallResult;
import com.azure.communication.callautomation.models.ServerCallLocator;

public class CallAutomationAsyncClientAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndRejectCallDisconnectedAutomatedTest(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndRejectCreateCallFailedAutomatedTest(HttpClient httpClient) {
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
            CreateCallFailed createCallFailed = waitForEvent(CreateCallFailed.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(createCallFailed);
            assertThrows(RuntimeException.class, () -> createCallResult.getCallConnection().getCallProperties());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndConnectCallTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. get Connect call with server call id
         * 5. hang up the call with Connect call result.
         * 6. once call is hung up, verify disconnected event
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndConnectCallTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            // Create call automation client and use source as the caller.
            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                    .addPolicy((context, next) -> logHeaders("createVOIPCallAndConnectCallTest", next))
                    .sourceIdentity(caller)
                    .buildAsyncClient();
            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndConnectCallTest", next))
                .buildAsyncClient();

            // Create call automation client and use source as the caller.
            CallAutomationAsyncClient connectAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndConnectCallTest", next))
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

            //Connect call
            String serverCallId = answerCallResult.getCallConnectionProperties().getServerCallId();
            ServerCallLocator serverCallLocator = new ServerCallLocator(serverCallId);
            ConnectCallOptions connectCallOptions = new ConnectCallOptions(serverCallLocator, DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            ConnectCallResult connectCallResult = Objects.requireNonNull(connectAsyncClient.connectCallWithResponse(connectCallOptions).block()).getValue();
            String callConnectionId = connectCallResult.getCallConnectionProperties().getCallConnectionId();
            CallConnected connectCallConnectedEvent = waitForEvent(CallConnected.class, callConnectionId, Duration.ofSeconds(10));
            assertNotNull(connectCallConnectedEvent);

            // hang up the call.
            connectCallResult.getCallConnectionAsync().hangUp(true).block();

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
}
