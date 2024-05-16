// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.*;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.DialogCompleted;
import com.azure.communication.callautomation.models.events.DialogStarted;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.DoNotRecord;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CallDialogAsyncAutomatedLiveTests extends CallAutomationAutomatedLiveTestBase {

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires environment to be set up")
    @Disabled("Disabling this for now as there is service issue with this test case")
    public void dialogActionInACallAutomatedTest(HttpClient httpClient) {
        /* Test case:Start and Stop Dialog on ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. Start dialog, expect no failure(exception)
         * 5. Stop dialog, expect DialogCompletedEvent
         * 6. hang up the call.
         */

        CommunicationIdentityAsyncClient identityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("dialogActionInACallAutomatedTest", next))
            .buildAsyncClient();

        List<CallConnectionAsync> callDestructors = new ArrayList<>();

        try {
            // create caller and receiver
            CommunicationUserIdentifier caller = identityAsyncClient.createUser().block();
            CommunicationUserIdentifier receiver = identityAsyncClient.createUser().block();

            CallAutomationAsyncClient callerAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("dialogActionInACallAutomatedTest", next))
                .sourceIdentity(caller)
                .buildAsyncClient();

            // Create call automation client for receivers.
            CallAutomationAsyncClient receiverAsyncClient = getCallAutomationClientUsingConnectionString(httpClient)
                .addPolicy((context, next) -> logHeaders("dialogActionInACallAutomatedTest", next))
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
            String receiverConnectionId = answerCallResult.getCallConnectionProperties().getCallConnectionId();
            assertNotNull(answerCallResult);
            assertNotNull(answerCallResult.getCallConnectionAsync());
            assertNotNull(answerCallResult.getCallConnectionProperties());
            callDestructors.add(answerCallResult.getCallConnectionAsync());

            // wait for callConnected
            CallConnected callConnected = waitForEvent(CallConnected.class, callerConnectionId, Duration.ofSeconds(10));
            assertNotNull(callConnected);

            // start dialog
            String dialogId = "92e08834-b6ee-4ede-8956-9fefa27a691c";
            Map<String, Object> dialogContext = new HashMap<>();
            StartDialogOptions options = new StartDialogOptions(dialogId, DialogInputType.POWER_VIRTUAL_AGENTS, dialogContext);
            options.setBotId(BOT_APP_ID);

            CallDialogAsync callDialogAsync = answerCallResult.getCallConnectionAsync().getCallDialogAsync();
            Response<DialogStateResult> dialogStateResultResponse = callDialogAsync.startDialogWithResponse(options).block();
            assertNotNull(dialogStateResultResponse);
            assertEquals(201, dialogStateResultResponse.getStatusCode());
            DialogStateResult dialogStateResult = dialogStateResultResponse.getValue();
            assertNotNull(dialogStateResult);
            assertEquals(dialogId, dialogStateResult.getDialogId());
            DialogStarted dialogStarted = waitForEvent(DialogStarted.class, receiverConnectionId, Duration.ofSeconds(20));
            assertNotNull(dialogStarted);

            // stop dialog
            callDialogAsync.stopDialog(dialogId).block();
            DialogCompleted dialogCompleted = waitForEvent(DialogCompleted.class, receiverConnectionId, Duration.ofSeconds(20));
            assertNotNull(dialogCompleted);
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
