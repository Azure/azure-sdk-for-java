// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.MuteParticipantsResponseInternal;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantResponseInternal;
import com.azure.communication.callautomation.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callautomation.implementation.models.UnmuteParticipantsResponseInternal;
import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.HangUpOptions;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.MuteParticipantsOptions;
import com.azure.communication.callautomation.models.MuteParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsResult;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CallConnectionUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void getCallProperties() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();

        assertNotNull(callConnectionProperties);
    }

    @Test
    public void getCallPropertiesWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<CallConnectionProperties> callConnectionProperties = callConnection.getCallPropertiesWithResponse(Context.NONE);

        assertNotNull(callConnectionProperties);
        assertEquals(200, callConnectionProperties.getStatusCode());
        assertNotNull(callConnectionProperties.getValue());
    }

    @Test
    public void hangUp() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        callConnection.hangUp(false);
    }

    @Test
    public void hangUpWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnection.hangUpWithResponse(new HangUpOptions(false), Context.NONE);

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void hangUpWithResponseForEveryone() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 204)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnection.hangUpWithResponse(new HangUpOptions(true), Context.NONE);

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void getParticipant() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CallParticipant callParticipant = callConnection.getParticipant(CALL_CALLER_ID);

        assertNotNull(callParticipant);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipant.getIdentifier()).getId());
    }

    @Test
    public void getParticipantWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
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
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateListParticipantsResponse(), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        ListParticipantsResult listParticipants = callConnection.listParticipants();

        assertNotNull(listParticipants);
        assertNotNull(listParticipants.getValues());
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) listParticipants.getValues().get(0).getIdentifier()).getId());
    }

    @Test
    public void listParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateListParticipantsResponse(), 200)
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
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);
        CallInvite callInvite = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));
        TransferCallResult transferCallResult = callConnection.transferToParticipantCall(callInvite);

        assertNotNull(transferCallResult);
        assertEquals(CALL_OPERATION_CONTEXT, transferCallResult.getOperationContext());
    }

    @Test
    public void transferToParticipantCallWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CallInvite callInvite = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));
        Response<TransferCallResult> transferCallResultResponse = callConnection.transferToParticipantCallWithResponse(
            new TransferToParticipantCallOptions(callInvite)
                .setOperationContext(CALL_OPERATION_CONTEXT), Context.NONE);

        assertNotNull(transferCallResultResponse);
        assertEquals(202, transferCallResultResponse.getStatusCode());
        assertNotNull(transferCallResultResponse.getValue());
    }

    @Test
    public void addParticipants() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnection(CALL_CONNECTION_ID);

        AddParticipantResult addParticipantsResult = callConnection.addParticipant(
            new CallInvite(
                new CommunicationUserIdentifier(CALL_TARGET_ID)));
        assertNotNull(addParticipantsResult);
        assertEquals(CALL_TARGET_ID, ((CommunicationUserIdentifier) addParticipantsResult
            .getParticipant()
            .getIdentifier())
            .getId());
    }

    @Test
    public void addParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnection(CALL_CONNECTION_ID);

        CallInvite callInvite = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID), new HashMap<String, String>());
        Response<AddParticipantResult> addParticipantsResultResponse = callConnection.addParticipantWithResponse(
            new AddParticipantOptions(callInvite)
                .setOperationContext(CALL_OPERATION_CONTEXT),
                Context.NONE);

        assertNotNull(addParticipantsResultResponse);
        assertEquals(202, addParticipantsResultResponse.getStatusCode());
        assertNotNull(addParticipantsResultResponse.getValue());
    }

    @Test
    public void removeParticipants() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new RemoveParticipantResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        RemoveParticipantResult removeParticipantsResult = callConnection.removeParticipant(
            new CommunicationUserIdentifier(CALL_TARGET_ID));

        assertNotNull(removeParticipantsResult);
        assertEquals(CALL_OPERATION_CONTEXT, removeParticipantsResult.getOperationContext());
    }

    @Test
    public void removeParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new RemoveParticipantResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        RemoveParticipantOptions removeParticipantsOptions = new RemoveParticipantOptions(
            new CommunicationUserIdentifier(CALL_TARGET_ID))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<RemoveParticipantResult> removeParticipantsResultResponse = callConnection.removeParticipantWithResponse(
            removeParticipantsOptions, Context.NONE);


        assertNotNull(removeParticipantsResultResponse);
        assertEquals(202, removeParticipantsResultResponse.getStatusCode());
        assertNotNull(removeParticipantsResultResponse.getValue());
    }

    @Test
    public void muteParticipants() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new MuteParticipantsResponseInternal()), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        MuteParticipantsResult muteParticipantsResult =
            callConnection.muteParticipants(new CommunicationUserIdentifier(CALL_TARGET_ID));

        assertNotNull(muteParticipantsResult);
        assertNull(muteParticipantsResult.getOperationContext());
    }

    @Test
    public void muteParticipantsWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new MuteParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        MuteParticipantsOptions muteParticipantsOptions = new MuteParticipantsOptions(
            Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        Response<MuteParticipantsResult> muteParticipantsResultResponse =
            callConnection.muteParticipantsWithResponse(muteParticipantsOptions, Context.NONE);

        assertNotNull(muteParticipantsResultResponse);
        assertEquals(202, muteParticipantsResultResponse.getStatusCode());
        assertNotNull(muteParticipantsResultResponse.getValue());
    }

    @Test
    public void unmuteParticipant() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new UnmuteParticipantsResponseInternal()), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        UnmuteParticipantsResult unmuteParticipantsResult =
            callConnection.unmuteParticipants(new CommunicationUserIdentifier(CALL_TARGET_ID));

        assertNotNull(unmuteParticipantsResult);
        assertNull(unmuteParticipantsResult.getOperationContext());
    }

    @Test
    public void unmuteParticipantWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new UnmuteParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        UnmuteParticipantsOptions muteParticipantOptions = new UnmuteParticipantsOptions(
            Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        Response<UnmuteParticipantsResult> unmuteParticipantsResultResponse =
            callConnection.unmuteParticipantsWithResponse(muteParticipantOptions, Context.NONE);

        assertNotNull(unmuteParticipantsResultResponse);
        assertEquals(202, unmuteParticipantsResultResponse.getStatusCode());
        assertNotNull(unmuteParticipantsResultResponse.getValue());
    }
}
