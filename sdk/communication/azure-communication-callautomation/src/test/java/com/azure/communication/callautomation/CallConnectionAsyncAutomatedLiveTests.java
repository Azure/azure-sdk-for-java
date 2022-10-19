// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AddParticipantsOptions;
import com.azure.communication.callautomation.models.AddParticipantsResult;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantsResult;
import com.azure.communication.callautomation.models.RepeatabilityHeaders;
import com.azure.communication.callautomation.models.events.CallConnectedEvent;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class CallConnectionAsyncAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    public void createVOIPCallAndAnswerThenAddParticipantFinallyRemoveParticipantAutomatedTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. add one more ACS target to the call.
         * 5. remove the newly added target from the call.
         */
        CallAutomationAsyncClient callAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenAddParticipantFinallyRemoveParticipantAutomatedTest", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenAddParticipantFinallyRemoveParticipantAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationIdentifier receiver = identityAsyncClient.createUser().block();
            CommunicationIdentifier anotherReceiver = identityAsyncClient.createUser().block();

            String uniqueId = serviceBusWithNewCall(caller, receiver);
            String anotherUniqueId = serviceBusWithNewCall(caller, anotherReceiver);

            // create a call
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(receiver));
            CreateCallOptions createCallOptions = new CreateCallOptions(caller, targets,
                DISPATCHER_CALLBACK + String.format("?q=%s", uniqueId));
            Response<CreateCallResult> createCallResultResponse = callAsyncClient.createCallWithResponse(createCallOptions).block();
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

            // wait for callConnected
            CallConnectedEvent callConnectedEvent = waitForEvent(CallConnectedEvent.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnectedEvent);

            // add another receiver to the call
            targets.clear();
            targets.add(anotherReceiver);
            AddParticipantsOptions addParticipantsOptions = new AddParticipantsOptions(targets);
            Response<AddParticipantsResult> addParticipantsResultResponse = createCallResult.getCallConnectionAsync().addParticipantsWithResponse(addParticipantsOptions).block();
            assertNotNull(addParticipantsResultResponse);

            // check repeatabilityHeaders
            RepeatabilityHeaders repeatabilityHeaders = addParticipantsOptions.getRepeatabilityHeaders();
            assertNotNull(repeatabilityHeaders);
            assertNotNull(repeatabilityHeaders.getRepeatabilityFirstSent());
            assertInstanceOf(String.class, repeatabilityHeaders.getRepeatabilityFirstSentInHttpDateFormat());
            assertNotNull(repeatabilityHeaders.getRepeatabilityRequestId());

            // wait for the incomingCallContext on another receiver
            String anotherIncomingCallContext = waitForIncomingCallContext(anotherUniqueId, Duration.ofSeconds(10));
            assertNotNull(anotherIncomingCallContext);

            // answer the call
            AnswerCallResult anotherAnswerCallResult = callAsyncClient.answerCall(anotherIncomingCallContext,
                DISPATCHER_CALLBACK + String.format("?q=%s", anotherUniqueId)).block();
            assertNotNull(anotherAnswerCallResult);
            assertNotNull(anotherAnswerCallResult.getCallConnectionAsync());
            assertNotNull(anotherAnswerCallResult.getCallConnectionProperties());
            String anotherReceiverConnectionId = anotherAnswerCallResult.getCallConnectionProperties().getCallConnectionId();
            callDestructors.add(anotherAnswerCallResult.getCallConnectionAsync());

            // clear the slot for next callConnectedEvent from another receiver call connection establishment.
            if (eventStore.get(callerConnectionId) != null) {
                eventStore.get(callerConnectionId).remove(CallConnectedEvent.class);
            }

            // wait for callConnected
            CallConnectedEvent anotherCallConnectedEvent = waitForEvent(CallConnectedEvent.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnectedEvent);

            // check participant number in the call
            ListParticipantsResult listParticipantsResult = createCallResult.getCallConnectionAsync().listParticipants().block();
            assertNotNull(listParticipantsResult);
            assertEquals(3, listParticipantsResult.getValues().size());

            // remove a participant from the call
            RemoveParticipantsResult removeParticipantsResult = createCallResult.getCallConnectionAsync().removeParticipants(targets).block();

            waitForOperationCompletion(8000);

            // check participant number in the call
            listParticipantsResult = createCallResult.getCallConnectionAsync().listParticipants().block();
            assertNotNull(listParticipantsResult);
            assertEquals(2, listParticipantsResult.getValues().size());
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
