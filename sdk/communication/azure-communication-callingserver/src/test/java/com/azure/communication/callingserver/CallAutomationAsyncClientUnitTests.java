// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AnswerCallResult;
import com.azure.communication.callingserver.models.CallRejectReason;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallAutomationAsyncClientUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void createCall() throws URISyntaxException {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URI), 201)
            )));
        CommunicationUserIdentifier caller = new CommunicationUserIdentifier(CALL_CALLER_ID);
        List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID)));
        CreateCallOptions callOptions = new CreateCallOptions(caller, targets, new URI(CALL_CALLBACK_URI));
        callOptions.setSubject(CALL_SUBJECT);

        CreateCallResult createCallResult = callAutomationAsyncClient.createCall(callOptions).block();

        assertNotNull(createCallResult);
    }

    @Test
    public void createCallWithResponse() throws URISyntaxException {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URI), 201)
            )));
        CommunicationUserIdentifier caller = new CommunicationUserIdentifier(CALL_CALLER_ID);
        List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID)));
        CreateCallOptions callOptions = new CreateCallOptions(caller, targets, new URI(CALL_CALLBACK_URI));
        callOptions.setSubject(CALL_SUBJECT);

        Response<CreateCallResult> createCallResult = callAutomationAsyncClient.createCallWithResponse(callOptions).block();

        assertNotNull(createCallResult);
        assertEquals(201, createCallResult.getStatusCode());
        assertNotNull(createCallResult.getValue());
    }

    @Test
    public void answerCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URI), 200)
            )));

        AnswerCallResult answerCallResult = callAutomationAsyncClient.answerCall(CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URI).block();

        assertNotNull(answerCallResult);
    }

    @Test
    public void answerCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URI), 200)
            )));

        Response<AnswerCallResult> answerCallResult = callAutomationAsyncClient.answerCallWithResponse(
            CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URI).block();

        assertNotNull(answerCallResult);
        assertEquals(200, answerCallResult.getStatusCode());
        assertNotNull(answerCallResult.getValue());
    }

    @Test
    public void redirectCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>("", 204)
            ))
        );
        CommunicationUserIdentifier target = new CommunicationUserIdentifier(CALL_TARGET_ID);

        callAutomationAsyncClient.redirectCall(CALL_INCOMING_CALL_CONTEXT, target);
    }

    @Test
    public void redirectCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>("", 204)
            ))
        );
        CommunicationUserIdentifier target = new CommunicationUserIdentifier(CALL_TARGET_ID);

        Response<Void> redirectCallResponse = callAutomationAsyncClient.redirectCallWithResponse(
            CALL_INCOMING_CALL_CONTEXT, target).block();

        assertNotNull(redirectCallResponse);
        assertEquals(204, redirectCallResponse.getStatusCode());
    }

    @Test
    public void rejectCall() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>("", 204)
            ))
        );

        callAutomationAsyncClient.rejectCall(CALL_INCOMING_CALL_CONTEXT, CallRejectReason.BUSY);
    }

    @Test
    public void rejectCallWithResponse() {
        CallAutomationAsyncClient callAutomationAsyncClient = getCallAutomationAsyncClient(new ArrayList<AbstractMap.SimpleEntry<String, Integer>>(
            Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>("", 204)
            ))
        );

        Response<Void> rejectCallResponse = callAutomationAsyncClient.rejectCallWithResponse(CALL_INCOMING_CALL_CONTEXT,
            CallRejectReason.BUSY).block();

        assertNotNull(rejectCallResponse);
        assertEquals(204, rejectCallResponse.getStatusCode());
    }
}
