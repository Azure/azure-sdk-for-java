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
import com.azure.core.util.Context;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CallAutomationClientUnitTests extends CallAutomationUnitTestBase {

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void createCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL), 201)
            )));
        CommunicationUserIdentifier caller = new CommunicationUserIdentifier(CALL_CALLER_ID);
        List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)));
        CreateCallOptions callOptions = new CreateCallOptions(caller, targets, CALL_CALLBACK_URL);
        callOptions.setSubject(CALL_SUBJECT);

        CreateCallResult createCallResult = callAutomationClient.createCall(callOptions);

        assertNotNull(createCallResult);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void createCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL), 201)
            )));
        CommunicationUserIdentifier caller = new CommunicationUserIdentifier(CALL_CALLER_ID);
        List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)));
        CreateCallOptions callOptions = new CreateCallOptions(caller, targets, CALL_CALLBACK_URL);
        callOptions.setSubject(CALL_SUBJECT);

        Response<CreateCallResult> createCallResult = callAutomationClient.createCallWithResponse(callOptions, Context.NONE);

        assertNotNull(createCallResult);
        assertEquals(201, createCallResult.getStatusCode());
        assertNotNull(createCallResult.getValue());
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void answerCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL), 200)
            )));

        AnswerCallResult answerCallResult = callAutomationClient.answerCall(CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URL);

        assertNotNull(answerCallResult);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void answerCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL), 200)
            )));

        Response<AnswerCallResult> answerCallResult = callAutomationClient.answerCallWithResponse(
            CALL_INCOMING_CALL_CONTEXT, CALL_CALLBACK_URL, MEDIA_STREAMING_CONFIGURATION, Context.NONE);

        assertNotNull(answerCallResult);
        assertEquals(200, answerCallResult.getStatusCode());
        assertNotNull(answerCallResult.getValue());
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void redirectCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );
        CommunicationUserIdentifier target = new CommunicationUserIdentifier(CALL_TARGET_ID);

        callAutomationClient.redirectCall(CALL_INCOMING_CALL_CONTEXT, target);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void redirectCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );
        CommunicationUserIdentifier target = new CommunicationUserIdentifier(CALL_TARGET_ID);

        Response<Void> redirectCallResponse = callAutomationClient.redirectCallWithResponse(
            CALL_INCOMING_CALL_CONTEXT, target, Context.NONE);

        assertEquals(204, redirectCallResponse.getStatusCode());
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void rejectCall() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );

        callAutomationClient.rejectCall(CALL_INCOMING_CALL_CONTEXT, CallRejectReason.BUSY);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void rejectCallWithResponse() {
        CallAutomationClient callAutomationClient = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            ))
        );

        Response<Void> rejectCallResponse = callAutomationClient.rejectCallWithResponse(CALL_INCOMING_CALL_CONTEXT,
            CallRejectReason.BUSY, Context.NONE);

        assertEquals(204, rejectCallResponse.getStatusCode());
    }
}
