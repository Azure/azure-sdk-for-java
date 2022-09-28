// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.events.CallConnectedEvent;
import com.azure.communication.callautomation.models.events.CallDisconnectedEvent;
import com.azure.communication.callautomation.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

        try {
            // create caller and receiver
            CommunicationIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier target = identityAsyncClient.createUser().block();

            String uniqueId = serviceBusWithNewCall(caller, target).block();

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(target));
            CreateCallOptions createCallOptions = new CreateCallOptions(caller, targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            CreateCallResult createCallResult = callAsyncClient.createCall(createCallOptions).block();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnectionProperties());
            String callerConnectionId = createCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(callerConnectionId);

            // wait for the incomingCallContext
            System.out.println("Waiting for incomingCallContext...");
            waitForOperationCompletion(8000);

            // answer the call
            String incomingCallContext = incomingCallContextStore.get(uniqueId);
            assertNotNull(incomingCallContext);
            AnswerCallResult answerCallResult = callAsyncClient.answerCall(incomingCallContext,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId)).block();
            assertNotNull(answerCallResult);
            assertNotNull(answerCallResult.getCallConnectionProperties());
            String receiverConnectionId = answerCallResult.getCallConnectionProperties().getCallConnectionId();

            // wait for callback events.
            waitForOperationCompletion(5000);
            assertNotNull(eventStore.get(receiverConnectionId));

            // check if the callee(receiver) receives events.
            assertNotNull(eventStore.get(receiverConnectionId).get(CallConnectedEvent.class));
            assertNotNull(eventStore.get(receiverConnectionId).get(ParticipantsUpdatedEvent.class));
            // check if the caller receives same events as well.
            assertNotNull(eventStore.get(callerConnectionId).get(CallConnectedEvent.class));
            assertNotNull(eventStore.get(callerConnectionId).get(ParticipantsUpdatedEvent.class));

            // hang up the call.
            answerCallResult.getCallConnectionAsync().hangUp(true).block();
            waitForOperationCompletion(5000);

            // check if both parties had the call terminated.
            assertNotNull(eventStore.get(receiverConnectionId).get(CallDisconnectedEvent.class));
            assertNotNull(eventStore.get(callerConnectionId).get(CallDisconnectedEvent.class));
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
