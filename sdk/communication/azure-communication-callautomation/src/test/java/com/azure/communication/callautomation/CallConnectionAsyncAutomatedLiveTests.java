// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.events.AddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceeded;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenAddParticipantFinallyRemoveParticipantAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationUserIdentifier receiver = identityAsyncClient.createUser().block();
            CommunicationUserIdentifier anotherReceiver = identityAsyncClient.createUser().block();

            String uniqueId = serviceBusWithNewCall(caller, receiver);
            String anotherUniqueId = serviceBusWithNewCall(caller, anotherReceiver);

            // Create call automation client for caller.
            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenAddParticipantFinallyRemoveParticipantAutomatedTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("createVOIPCallAndAnswerThenAddParticipantFinallyRemoveParticipantAutomatedTest", next))
                .buildAsyncClient();

            // create a call
            List<CommunicationIdentifier> targetParticipants = new ArrayList<>(Arrays.asList(receiver));
            CreateGroupCallOptions createCallOptions = new CreateGroupCallOptions(targetParticipants,
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

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, receiverConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // add another receiver to the call
            AddParticipantOptions addParticipantsOptions = new AddParticipantOptions(new CallInvite(anotherReceiver));
            Response<AddParticipantResult> addParticipantsResultResponse = createCallResult.getCallConnectionAsync().addParticipantWithResponse(addParticipantsOptions).block();
            assertNotNull(addParticipantsResultResponse);

            // wait for the incomingCallContext on another receiver
            String anotherIncomingCallContext = waitForIncomingCallContext(anotherUniqueId, Duration.ofSeconds(10));
            assertNotNull(anotherIncomingCallContext);

            // answer the call
            answerCallOptions = new AnswerCallOptions(anotherIncomingCallContext,
                DISPATCHER_CALLBACK + String.format("?q=%s", anotherUniqueId));
            AnswerCallResult anotherAnswerCallResult = Objects.requireNonNull(receiverAsyncClient.answerCallWithResponse(answerCallOptions).block()).getValue();
            assertNotNull(anotherAnswerCallResult);
            assertNotNull(anotherAnswerCallResult.getCallConnectionAsync());
            assertNotNull(anotherAnswerCallResult.getCallConnectionProperties());
            callDestructors.add(anotherAnswerCallResult.getCallConnectionAsync());

            // wait for addParticipantSucceed
            AddParticipantSucceeded addParticipantSucceeded = waitForEvent(AddParticipantSucceeded.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(addParticipantSucceeded);

            // check participant number in the call
            List<CallParticipant> listParticipantsResult = createCallResult.getCallConnectionAsync().listParticipants().log().collectList().block();
            assertNotNull(listParticipantsResult);
            assertEquals(3, listParticipantsResult.size());

            // remove a participant from the call
            RemoveParticipantResult removeParticipantResult = createCallResult.getCallConnectionAsync().removeParticipant(receiver).block();
            assertNotNull(removeParticipantResult);

            // wait for removeParticipantSucceed
            RemoveParticipantSucceeded removeParticipantSucceeded = waitForEvent(RemoveParticipantSucceeded.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(removeParticipantSucceeded);
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
