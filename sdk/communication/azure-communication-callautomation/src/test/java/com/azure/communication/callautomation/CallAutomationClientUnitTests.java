// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallRejectReason;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.RedirectCallOptions;
import com.azure.communication.callautomation.models.RejectCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CallAutomationClientUnitTests extends CallAutomationUnitTestBase {

    @Test
    public void createCall() {
        CommunicationUserIdentifier caller = new CommunicationUserIdentifier(CALL_CALLER_ID);

        CallAutomationClient callAutomationClient = getCallAutomationClientWithSourceIdentity(caller, new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null, null), 201)
            )));
        List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)));

        CreateCallResult createCallResult = callAutomationClient.createGroupCall(targets, CALL_CALLBACK_URL);

        assertNotNull(createCallResult);
    }

    @Test
    public void createCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null, null), 201)
            )));
        CommunicationUserIdentifier caller = new CommunicationUserIdentifier(CALL_CALLER_ID);
        List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)));
        CreateGroupCallOptions callOptions = new CreateGroupCallOptions(targets, CALL_CALLBACK_URL);
        callOptions.setOperationContext(CALL_SUBJECT);

        Response<CreateCallResult> createCallResult = callAutomationClient.createGroupCallWithResponse(callOptions, Context.NONE);

        assertNotNull(createCallResult);
        assertEquals(201, createCallResult.getStatusCode());
        assertNotNull(createCallResult.getValue());
    }

    @Test
    public void answerCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null, null), 200)
            )));

        AnswerCallResult answerCallResult = callAutomationClient.answerCall(CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URL);

        assertNotNull(answerCallResult);
    }

    @Test
    public void answerCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null, null), 200)
            )));

        AnswerCallOptions answerCallOptions = new AnswerCallOptions(CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URL)
            .setMediaStreamingOptions(MEDIA_STREAMING_CONFIGURATION)
            .setTranscriptionOptions(TRANSCRIPTION_CONFIGURATION);

        Response<AnswerCallResult> answerCallResult = callAutomationClient.answerCallWithResponse(
            answerCallOptions, Context.NONE);

        assertNotNull(answerCallResult);
        assertEquals(200, answerCallResult.getStatusCode());
        assertNotNull(answerCallResult.getValue());
        // assertEquals(MEDIA_SUBSCRIPTION_ID, answerCallResult.getValue().getCallConnectionProperties().getMediaSubscriptionId());
        // assertEquals(DATA_SUBSCRIPTION_ID, answerCallResult.getValue().getCallConnectionProperties().getDataSubscriptionId());
    }

    @Test
    public void redirectCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );
        CallInvite target = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));

        callAutomationClient.redirectCall(CALL_INCOMING_CALL_CONTEXT, target);
    }

    @Test
    public void redirectCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );
        CallInvite target = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));
        RedirectCallOptions redirectCallOptions = new RedirectCallOptions(CALL_INCOMING_CALL_CONTEXT, target);

        Response<Void> redirectCallResponse = callAutomationClient.redirectCallWithResponse(
            redirectCallOptions, Context.NONE);

        assertEquals(204, redirectCallResponse.getStatusCode());
    }

    @Test
    public void rejectCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );

        callAutomationClient.rejectCall(CALL_INCOMING_CALL_CONTEXT);
    }

    @Test
    public void rejectCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );

        RejectCallOptions rejectCallOptions = new RejectCallOptions(CALL_INCOMING_CALL_CONTEXT)
            .setCallRejectReason(CallRejectReason.BUSY);
        Response<Void> rejectCallResponse = callAutomationClient.rejectCallWithResponse(rejectCallOptions, Context.NONE);

        assertEquals(204, rejectCallResponse.getStatusCode());
    }
}
