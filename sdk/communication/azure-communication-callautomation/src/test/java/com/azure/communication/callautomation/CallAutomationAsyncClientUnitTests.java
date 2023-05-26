// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallRejectReason;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.RedirectCallOptions;
import com.azure.communication.callautomation.models.RejectCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallAutomationAsyncClientUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void createGroupCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null), 201)
            )));
        CallInvite callInvite = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));

        CreateCallResult createCallResult = callAutomationAsyncClient.createCall(callInvite, CALL_CALLBACK_URL).block();
        assertNotNull(createCallResult);
    }

    @Test
    public void createCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null), 201)
            )));
        List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)));

        CreateCallResult createCallResult = callAutomationAsyncClient.createGroupCall(targets, CALL_CALLBACK_URL).block();
        assertNotNull(createCallResult);
    }

    @Test
    public void createGroupCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 201)
            )));
        List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)));
        CreateGroupCallOptions callOptions = new CreateGroupCallOptions(targets, CALL_CALLBACK_URL);
        callOptions.setOperationContext(CALL_SUBJECT);

        Response<CreateCallResult> createCallResult = callAutomationAsyncClient.createGroupCallWithResponse(callOptions).block();

        assertNotNull(createCallResult);
        assertEquals(201, createCallResult.getStatusCode());
        assertNotNull(createCallResult.getValue());
    }

    @Test
    public void createCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 201)
            )));

        CallInvite callInvite = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));
        CreateCallOptions callOptions = new CreateCallOptions(callInvite, CALL_CALLBACK_URL);
        callOptions.setOperationContext(CALL_SUBJECT);

        Response<CreateCallResult> createCallResult = callAutomationAsyncClient.createCallWithResponse(callOptions).block();

        assertNotNull(createCallResult);
        assertEquals(201, createCallResult.getStatusCode());
        assertNotNull(createCallResult.getValue());
    }

    @Test
    public void answerCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL,  null), 200)
            )));

        AnswerCallResult answerCallResult = callAutomationAsyncClient.answerCall(CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URL).block();

        assertNotNull(answerCallResult);
    }

    @Test
    public void answerCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )));

        AnswerCallOptions answerCallOptions = new AnswerCallOptions(CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URL);
        Response<AnswerCallResult> answerCallResult = callAutomationAsyncClient.answerCallWithResponse(
            answerCallOptions).block();

        assertNotNull(answerCallResult);
        assertEquals(200, answerCallResult.getStatusCode());
        assertNotNull(answerCallResult.getValue());
    }

    @Test
    public void redirectCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>("", 204)
            ))
        );

        CallInvite target = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));

        callAutomationAsyncClient.redirectCall(CALL_INCOMING_CALL_CONTEXT, target);
    }

    @Test
    public void redirectCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>("", 204)
            ))
        );
        CallInvite target = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));
        RedirectCallOptions redirectCallOptions = new RedirectCallOptions(CALL_INCOMING_CALL_CONTEXT, target);
        Response<Void> redirectCallResponse = callAutomationAsyncClient.redirectCallWithResponse(redirectCallOptions).block();

        assertNotNull(redirectCallResponse);
        assertEquals(204, redirectCallResponse.getStatusCode());
    }

    @Test
    public void rejectCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>("", 204)
            ))
        );

        callAutomationAsyncClient.rejectCall(CALL_INCOMING_CALL_CONTEXT);
    }

    @Test
    public void rejectCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new AbstractMap.SimpleEntry<>("", 204)
            ))
        );

        RejectCallOptions rejectCallOptions = new RejectCallOptions(CALL_INCOMING_CALL_CONTEXT)
            .setCallRejectReason(CallRejectReason.BUSY);
        Response<Void> rejectCallResponse = callAutomationAsyncClient.rejectCallWithResponse(rejectCallOptions).block();

        assertNotNull(rejectCallResponse);
        assertEquals(204, rejectCallResponse.getStatusCode());
    }
}
