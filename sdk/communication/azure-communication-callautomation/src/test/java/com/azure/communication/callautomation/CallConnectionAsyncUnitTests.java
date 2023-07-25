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
import com.azure.communication.callautomation.models.MuteParticipantsOptions;
import com.azure.communication.callautomation.models.MuteParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferCallToParticipantOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsResult;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void transferToParticipantCallWithResponseWithTrasferee() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        TransferCallToParticipantOptions transferCallToParticipantOptions = new TransferCallToParticipantOptions(new CommunicationUserIdentifier(CALL_TARGET_ID))
            .setOperationContext(CALL_OPERATION_CONTEXT)
            .setTransferee(new CommunicationUserIdentifier(CALL_TRANSFEREE_ID));
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

    @Test
    public void muteParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new MuteParticipantsResponseInternal()), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        MuteParticipantsResult muteParticipantsResult = callConnectionAsync.muteParticipantsAsync(
            new CommunicationUserIdentifier(CALL_TARGET_ID)).block();

        assertNotNull(muteParticipantsResult);
        assertNull(muteParticipantsResult.getOperationContext());
    }

    @Test
    public void muteParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new MuteParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        MuteParticipantsOptions muteParticipantsOptions = new MuteParticipantsOptions(
            Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        Response<MuteParticipantsResult> muteParticipantsResultResponse =
            callConnectionAsync.muteParticipantsWithResponse(muteParticipantsOptions).block();

        assertNotNull(muteParticipantsResultResponse);
        assertEquals(202, muteParticipantsResultResponse.getStatusCode());
        assertNotNull(muteParticipantsResultResponse.getValue());
    }

    @Test
    public void muteParticipantNotFound() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 404)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        assertThrows(HttpResponseException.class, () ->  callConnectionAsync.muteParticipantsAsync(
            new CommunicationUserIdentifier(CALL_TARGET_ID)).block());
    }

    @Test
    public void muteNotAcsParticipant() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 400)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        assertThrows(HttpResponseException.class, () ->  callConnectionAsync.muteParticipantsAsync(
            new PhoneNumberIdentifier("+11234567890")).block());
    }

    @Test
    public void muteMoreThanOneParticipantWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 400)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        MuteParticipantsOptions muteParticipantOptions = new MuteParticipantsOptions(
            Arrays.asList(
                new CommunicationUserIdentifier(CALL_TARGET_ID),
                new CommunicationUserIdentifier(CALL_TARGET_ID)
            ))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        assertThrows(HttpResponseException.class,
            () ->  callConnectionAsync.muteParticipantsWithResponse(muteParticipantOptions).block());
    }

    @Test
    public void unmuteParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new UnmuteParticipantsResponseInternal()), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        UnmuteParticipantsResult unmuteParticipantsResult = callConnectionAsync.unmuteParticipantsAsync(
            new CommunicationUserIdentifier(CALL_TARGET_ID)
        ).block();

        assertNotNull(unmuteParticipantsResult);
        assertNull(unmuteParticipantsResult.getOperationContext());
    }

    @Test
    public void unmuteParticipantsNotFound() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 404)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        assertThrows(HttpResponseException.class, () ->  callConnectionAsync.unmuteParticipantsAsync(
            new CommunicationUserIdentifier(CALL_TARGET_ID)).block());
    }

    @Test
    public void unmuteNotAcsParticipant() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 400)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        assertThrows(HttpResponseException.class, () ->  callConnectionAsync.unmuteParticipantsAsync(
            new PhoneNumberIdentifier("+11234567890")).block());
    }

    @Test
    public void unmuteParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>(serializeObject(new UnmuteParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        UnmuteParticipantsOptions muteParticipantOptions = new UnmuteParticipantsOptions(
            Collections.singletonList(new CommunicationUserIdentifier(CALL_TARGET_ID)))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        Response<UnmuteParticipantsResult> unmuteParticipantsResultResponse =
            callConnectionAsync.unmuteParticipantsWithResponse(muteParticipantOptions).block();

        assertNotNull(unmuteParticipantsResultResponse);
        assertEquals(202, unmuteParticipantsResultResponse.getStatusCode());
        assertNotNull(unmuteParticipantsResultResponse.getValue());
    }

    @Test
    public void unmuteMoreThanOneParticipantWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<>(
            Collections.singletonList(
                new SimpleEntry<>("", 400)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        UnmuteParticipantsOptions muteParticipantOptions = new UnmuteParticipantsOptions(
            Arrays.asList(
                new CommunicationUserIdentifier(CALL_TARGET_ID),
                new CommunicationUserIdentifier(CALL_TARGET_ID)
            ))
            .setOperationContext(CALL_OPERATION_CONTEXT);

        assertThrows(HttpResponseException.class,
            () ->  callConnectionAsync.unmuteParticipantsWithResponse(muteParticipantOptions).block());
    }
}
