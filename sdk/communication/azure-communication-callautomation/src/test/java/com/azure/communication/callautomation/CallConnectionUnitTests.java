// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.CancelAddParticipantResponse;
import com.azure.communication.callautomation.implementation.models.MuteParticipantsResultInternal;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantResponseInternal;
import com.azure.communication.callautomation.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.CancelAddParticipantOperationOptions;
import com.azure.communication.callautomation.models.CancelAddParticipantOperationResult;
import com.azure.communication.callautomation.models.MuteParticipantOptions;
import com.azure.communication.callautomation.models.MuteParticipantResult;
import com.azure.communication.callautomation.models.RemoveParticipantOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferCallToParticipantOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallConnectionUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void getCallProperties() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null, null), 200)
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
                    CALL_CALLER_ID, CALL_CALLER_DISPLAY_NAME, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, null, null), 200)
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

        Response<Void> hangUpResponse = callConnection.hangUpWithResponse(false, Context.NONE);

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

        Response<Void> hangUpResponse = callConnection.hangUpWithResponse(true, Context.NONE);

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void getParticipant() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateGetParticipantResponse(CALL_CALLER_ID, false, false), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CallParticipant callParticipant = callConnection.getParticipant(USER_1);

        assertNotNull(callParticipant);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipant.getIdentifier()).getId());
    }

    @Test
    public void getParticipantWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(generateGetParticipantResponse(CALL_CALLER_ID, false, false), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        Response<CallParticipant> callParticipantResponse = callConnection.getParticipantWithResponse(USER_1, Context.NONE);

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

        List<CallParticipant> listParticipants = callConnection.listParticipants().stream().collect(Collectors.toList());

        assertNotNull(listParticipants);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) listParticipants.get(0).getIdentifier()).getId());
    }

    @Test
    public void transferToParticipantCall() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);
        TransferCallResult transferCallResult = callConnection.transferCallToParticipant(new CommunicationUserIdentifier(CALL_TARGET_ID));

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
        Response<TransferCallResult> transferCallResultResponse = callConnection.transferCallToParticipantWithResponse(
            new TransferCallToParticipantOptions(new CommunicationUserIdentifier(CALL_TARGET_ID))
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

        CallInvite callInvite = new CallInvite(new CommunicationUserIdentifier(CALL_TARGET_ID));
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
    public void muteParticipant() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new MuteParticipantsResultInternal()), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        MuteParticipantResult muteParticipantResult =
            callConnection.muteParticipant(new CommunicationUserIdentifier(CALL_TARGET_ID));

        assertNotNull(muteParticipantResult);
    }

    @Test
    public void muteParticipantWithResponse() {
        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new MuteParticipantsResultInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 200)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        MuteParticipantOptions muteParticipantOptions = new MuteParticipantOptions(new CommunicationUserIdentifier(CALL_TARGET_ID))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        Response<MuteParticipantResult> muteParticipantResultResponse =
            callConnection.muteParticipantWithResponse(muteParticipantOptions, Context.NONE);

        assertNotNull(muteParticipantResultResponse);
        assertEquals(200, muteParticipantResultResponse.getStatusCode());
        assertNotNull(muteParticipantResultResponse.getValue());
    }

    @Test
    public void cancelAddParticipant() {
        String invitationId = "invitationId";

        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new CancelAddParticipantResponse()
                    .setInvitationId(invitationId)
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CancelAddParticipantOperationResult result = callConnection.cancelAddParticipantOperation(invitationId);

        assertNotNull(result);
        assertEquals(CALL_OPERATION_CONTEXT, result.getOperationContext());
        assertEquals(invitationId, result.getInvitationId());
    }

    @Test
    public void cancelAddParticipantWithResponse() {
        String invitationId = "invitationId";

        CallConnection callConnection = getCallAutomationClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new CancelAddParticipantResponse()
                    .setInvitationId(invitationId)
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnection(CALL_CONNECTION_ID);

        CancelAddParticipantOperationOptions options = new CancelAddParticipantOperationOptions(invitationId)
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<CancelAddParticipantOperationResult> response = callConnection.cancelAddParticipantOperationWithResponse(
            options, Context.NONE);


        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getValue());
    }
}
