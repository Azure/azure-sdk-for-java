// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.RemoveParticipantResponseInternal;
import com.azure.communication.callautomation.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.RemoveParticipantOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferCallToParticipantOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallConnectionAsyncUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void getCallProperties() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        CallConnectionProperties callConnectionProperties = callConnectionAsync.getCallProperties().block();

        assertNotNull(callConnectionProperties);
    }

    @Test
    public void getCallPropertiesWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<CallConnectionProperties> callConnectionProperties = callConnectionAsync.getCallPropertiesWithResponse().block();

        assertNotNull(callConnectionProperties);
        assertEquals(200, callConnectionProperties.getStatusCode());
        assertNotNull(callConnectionProperties.getValue());
    }

    @Test
    public void hangUp() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        callConnectionAsync.hangUp(false);
    }

    @Test
    public void hangUpWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnectionAsync.hangUpWithResponse(false).block();

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void hangUpWithResponseForEveryone() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnectionAsync.hangUpWithResponse(true).block();

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void getParticipant() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        CallParticipant callParticipant = callConnectionAsync.getParticipant(USER_1).block();

        assertNotNull(callParticipant);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipant.getIdentifier()).getId());
    }

    @Test
    public void getParticipantWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<CallParticipant> callParticipantResponse = callConnectionAsync.getParticipantWithResponse(USER_1).block();

        assertNotNull(callParticipantResponse);
        assertEquals(200, callParticipantResponse.getStatusCode());
        assertNotNull(callParticipantResponse.getValue());
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipantResponse.getValue().getIdentifier()).getId());
    }

    @Test
    public void listParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateListParticipantsResponse(), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        List<CallParticipant> listParticipants = callConnectionAsync.listParticipants().log().collectList().block();

        assertNotNull(listParticipants);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) listParticipants.get(0).getIdentifier()).getId());
    }

    @Test
    public void transferToParticipantCall() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        TransferCallResult transferCallResult = callConnectionAsync.transferCallToParticipant(new CommunicationUserIdentifier(CALL_TARGET_ID)).block();
        assertNotNull(transferCallResult);
        assertEquals(CALL_OPERATION_CONTEXT, transferCallResult.getOperationContext());
    }

    @Test
    public void transferToParticipantCallWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        TransferCallToParticipantOptions transferCallToParticipantOptions = new TransferCallToParticipantOptions(new CommunicationUserIdentifier(CALL_TARGET_ID))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<TransferCallResult> transferCallResultResponse = callConnectionAsync.transferCallToParticipantWithResponse(transferCallToParticipantOptions).block();

        assertNotNull(transferCallResultResponse);
        assertEquals(202, transferCallResultResponse.getStatusCode());
        assertNotNull(transferCallResultResponse.getValue());
    }

    @Test
    public void addParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnectionAsync(CALL_CONNECTION_ID);

        AddParticipantResult addParticipantsResult = callConnectionAsync.addParticipant(
            new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID))).block();

        assertNotNull(addParticipantsResult);
        assertEquals(CALL_TARGET_ID, ((CommunicationUserIdentifier) addParticipantsResult
            .getParticipant()
            .getIdentifier())
            .getId());
    }

    @Test
    public void addParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnectionAsync(CALL_CONNECTION_ID);

        AddParticipantOptions addParticipantsOptions = new AddParticipantOptions(new CallInvite(
            new CommunicationUserIdentifier(CALL_TARGET_ID)))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<AddParticipantResult> addParticipantsResultResponse = callConnectionAsync.addParticipantWithResponse(addParticipantsOptions).block();

        assertNotNull(addParticipantsResultResponse);
        assertEquals(202, addParticipantsResultResponse.getStatusCode());
        assertNotNull(addParticipantsResultResponse.getValue());
    }

    @Test
    public void removeParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new RemoveParticipantResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        RemoveParticipantResult removeParticipantsResult = callConnectionAsync.removeParticipant(
            new CommunicationUserIdentifier(CALL_TARGET_ID)).block();

        assertNotNull(removeParticipantsResult);
        assertEquals(CALL_OPERATION_CONTEXT, removeParticipantsResult.getOperationContext());
    }

    @Test
    public void removeParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new RemoveParticipantResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        RemoveParticipantOptions removeParticipantsOptions = new RemoveParticipantOptions(
            new CommunicationUserIdentifier(CALL_TARGET_ID))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<RemoveParticipantResult> removeParticipantsResultResponse = callConnectionAsync.removeParticipantWithResponse(
            removeParticipantsOptions).block();


        assertNotNull(removeParticipantsResultResponse);
        assertEquals(202, removeParticipantsResultResponse.getStatusCode());
        assertNotNull(removeParticipantsResultResponse.getValue());
    }
}
