// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.communication.callautomation.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callautomation.models.AddParticipantsOptions;
import com.azure.communication.callautomation.models.AddParticipantsResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.HangUpOptions;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantsOptions;
import com.azure.communication.callautomation.models.RemoveParticipantsResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.AbstractMap.SimpleEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallConnectionUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void getCallProperties() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();

        assertNotNull(callConnectionProperties);
    }

    @Test
    public void getCallPropertiesWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<CallConnectionProperties> callConnectionProperties = callConnection.getCallPropertiesWithResponse(Context.NONE);

        assertNotNull(callConnectionProperties);
        assertEquals(200, callConnectionProperties.getStatusCode());
        assertNotNull(callConnectionProperties.getValue());
    }

    @Test
    public void hangUp() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 204)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        callConnection.hangUp(false);
    }

    @Test
    public void hangUpWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 204)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnection.hangUpWithResponse(new HangUpOptions(false), Context.NONE);

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void hangUpWithResponseForEveryone() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 204)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnection.hangUpWithResponse(new HangUpOptions(true), Context.NONE);

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void getParticipant() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CallParticipant callParticipant = callConnection.getParticipant(CALL_CALLER_ID);

        assertNotNull(callParticipant);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipant.getIdentifier()).getId());
    }

    @Test
    public void getParticipantWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<CallParticipant> callParticipantResponse = callConnection.getParticipantWithResponse(CALL_CALLER_ID, Context.NONE);

        assertNotNull(callParticipantResponse);
        assertEquals(200, callParticipantResponse.getStatusCode());
        assertNotNull(callParticipantResponse.getValue());
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipantResponse.getValue().getIdentifier()).getId());
    }

    @Test
    public void listParticipants() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateListParticipantsResponse(), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        ListParticipantsResult listParticipants = callConnection.listParticipants();

        assertNotNull(listParticipants);
        assertNotNull(listParticipants.getValues());
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) listParticipants.getValues().get(0).getIdentifier()).getId());
    }

    @Test
    public void listParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateListParticipantsResponse(), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<ListParticipantsResult> listParticipantsResultResponse = callConnection.listParticipantsWithResponse(Context.NONE);

        assertNotNull(listParticipantsResultResponse);
        assertEquals(200, listParticipantsResultResponse.getStatusCode());
        assertNotNull(listParticipantsResultResponse.getValue());
        assertEquals(CALL_TARGET_ID, ((CommunicationUserIdentifier) listParticipantsResultResponse
            .getValue()
            .getValues()
            .get(1)
            .getIdentifier())
            .getId());
    }

    @Test
    public void transferToParticipantCall() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        TransferCallResult transferCallResult = callConnection.transferToParticipantCall(new CommunicationUserIdentifier(CALL_TARGET_ID));

        assertNotNull(transferCallResult);
        assertEquals(CALL_OPERATION_CONTEXT, transferCallResult.getOperationContext());
    }

    @Test
    public void transferToParticipantCallWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<TransferCallResult> transferCallResultResponse = callConnection.transferToParticipantCallWithResponse(
            new TransferToParticipantCallOptions(new CommunicationUserIdentifier(CALL_TARGET_ID))
                .setOperationContext(CALL_OPERATION_CONTEXT), Context.NONE);

        assertNotNull(transferCallResultResponse);
        assertEquals(202, transferCallResultResponse.getStatusCode());
        assertNotNull(transferCallResultResponse.getValue());
    }

    @Test
    public void addParticipants() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnection(CALL_CONNECTION_ID);

        AddParticipantsResult addParticipantsResult = callConnection.addParticipants(
            new ArrayList<>(Arrays.asList(
                new CommunicationUserIdentifier(CALL_TARGET_ID))));
        assertNotNull(addParticipantsResult);
        assertEquals(CALL_TARGET_ID, ((CommunicationUserIdentifier) addParticipantsResult
            .getParticipants()
            .get(0)
            .getIdentifier())
            .getId());
    }

    @Test
    public void addParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnection(CALL_CONNECTION_ID);

        Response<AddParticipantsResult> addParticipantsResultResponse = callConnection.addParticipantsWithResponse(
            new AddParticipantsOptions(new ArrayList<>(Arrays.asList(
                new CommunicationUserIdentifier(CALL_TARGET_ID))))
                .setOperationContext(CALL_OPERATION_CONTEXT), Context.NONE);

        assertNotNull(addParticipantsResultResponse);
        assertEquals(202, addParticipantsResultResponse.getStatusCode());
        assertNotNull(addParticipantsResultResponse.getValue());
    }

    @Test
    public void removeParticipants() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new RemoveParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        RemoveParticipantsResult removeParticipantsResult = callConnection.removeParticipants(
            new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID))));

        assertNotNull(removeParticipantsResult);
        assertEquals(CALL_OPERATION_CONTEXT, removeParticipantsResult.getOperationContext());
    }

    @Test
    public void removeParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new RemoveParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        RemoveParticipantsOptions removeParticipantsOptions = new RemoveParticipantsOptions(
            new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID))))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<RemoveParticipantsResult> removeParticipantsResultResponse = callConnection.removeParticipantsWithResponse(
            removeParticipantsOptions, Context.NONE);


        assertNotNull(removeParticipantsResultResponse);
        assertEquals(202, removeParticipantsResultResponse.getStatusCode());
        assertNotNull(removeParticipantsResultResponse.getValue());
    }
}
