// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.RepeatabilityHeaders;
import com.azure.communication.callautomation.models.events.CallConnectedEvent;
import com.azure.communication.callautomation.models.events.CallDisconnectedEvent;
import com.azure.communication.callautomation.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.common.CommunicationIdentifier;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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
        CallAutomationAsyncClient callAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenHangupAutomatedTest", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenHangupAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            String uniqueId = serviceBusWithNewCall(caller, target);

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            CreateCallOptions createCallOptions = new CreateCallOptions(caller, targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            Response<CreateCallResult> createCallResultResponse = callAsyncClient.createCallWithResponse(createCallOptions).block();
            RepeatabilityHeaders repeatabilityHeaders = createCallOptions.getRepeatabilityHeaders();
            assertNotNull(repeatabilityHeaders);
            assertNotNull(repeatabilityHeaders.getRepeatabilityRequestId());
            assertNotNull(repeatabilityHeaders.getRepeatabilityFirstSent());

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
            AnswerCallResult answerCallResult = callAsyncClient.answerCall(incomingCallContext,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId)).block();
            assertNotNull(answerCallResult);
            assertNotNull(answerCallResult.getCallConnectionAsync());
            assertNotNull(answerCallResult.getCallConnectionProperties());
            String receiverConnectionId = answerCallResult.getCallConnectionProperties().getCallConnectionId();
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // check events to caller side
            CallConnectedEvent callerCallConnectedEvent = waitForEvent(CallConnectedEvent.class, callerConnectionId, Duration.ofSeconds(10));
            ParticipantsUpdatedEvent callerParticipantUpdatedEvent = waitForEvent(ParticipantsUpdatedEvent.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callerCallConnectedEvent);
            assertNotNull(callerParticipantUpdatedEvent);

            // check events to receiver side
            CallConnectedEvent receiverCallConnectedEvent = waitForEvent(CallConnectedEvent.class, receiverConnectionId, Duration.ofSeconds(10));
            ParticipantsUpdatedEvent receiverParticipantUpdatedEvent = waitForEvent(ParticipantsUpdatedEvent.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(receiverCallConnectedEvent);
            assertNotNull(receiverParticipantUpdatedEvent);

            // hang up the call.
            answerCallResult.getCallConnectionAsync().hangUp(true).block();

            // check if both parties had the call terminated.
            CallDisconnectedEvent callerCallDisconnectedEvent = waitForEvent(CallDisconnectedEvent.class, receiverConnectionId, Duration.ofSeconds(10));
            CallDisconnectedEvent receiverCallDisconnectedEvent = waitForEvent(CallDisconnectedEvent.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callerCallDisconnectedEvent);
            assertNotNull(receiverCallDisconnectedEvent);

        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        } finally {
            if (!callDestructors.isEmpty()) {
                try {
                    callDestructors.forEach(callConnection -> callConnection.hangUp(true).block());
                } catch (Exception ignored) {
                    // Some call might have been terminated during the test, and it will cause exceptions here.
                    // Do nothing and iterate to next call connection.
                }
            }
        }
    }
}
