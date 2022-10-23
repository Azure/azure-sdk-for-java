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
import com.azure.communication.callautomation.models.RepeatabilityHeaders;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallConnectionAsyncUnitTests extends CallAutomationUnitTestBase {
    @Test
    public void getCallProperties() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        CallConnectionProperties callConnectionProperties = callConnectionAsync.getCallProperties().block();

        assertNotNull(callConnectionProperties);
    }

    @Test
    public void getCallPropertiesWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateCallProperties(CALL_CONNECTION_ID, CALL_SERVER_CALL_ID,
                    CALL_CALLER_ID, CALL_TARGET_ID, CALL_CONNECTION_STATE, CALL_SUBJECT, CALL_CALLBACK_URL, MEDIA_SUBSCRIPTION_ID), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<CallConnectionProperties> callConnectionProperties = callConnectionAsync.getCallPropertiesWithResponse().block();

        assertNotNull(callConnectionProperties);
        assertEquals(200, callConnectionProperties.getStatusCode());
        assertNotNull(callConnectionProperties.getValue());
    }

    @Test
    public void hangUp() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 204)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        callConnectionAsync.hangUp(false);
    }

    @Test
    public void hangUpWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 204)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<Void> hangUpResponse = callConnectionAsync.hangUpWithResponse(new HangUpOptions(false)).block();

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());
    }

    @Test
    public void hangUpWithResponseForEveryone() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 204)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        HangUpOptions hangUpOptions = new HangUpOptions(true);
        Response<Void> hangUpResponse = callConnectionAsync.hangUpWithResponse(hangUpOptions).block();

        assertNotNull(hangUpResponse);
        assertEquals(204, hangUpResponse.getStatusCode());

        RepeatabilityHeaders repeatabilityHeaders = hangUpOptions.getRepeatabilityHeaders();
        assertNotNull(repeatabilityHeaders);
        assertNotNull(repeatabilityHeaders.getRepeatabilityFirstSentInHttpDateFormat());
        assertNotNull(repeatabilityHeaders.getRepeatabilityRequestId().toString());
    }

    @Test
    public void getParticipant() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        CallParticipant callParticipant = callConnectionAsync.getParticipant(CALL_CALLER_ID).block();

        assertNotNull(callParticipant);
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipant.getIdentifier()).getId());
    }

    @Test
    public void getParticipantWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateGetParticipantResponse(CALL_CALLER_ID, false), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<CallParticipant> callParticipantResponse = callConnectionAsync.getParticipantWithResponse(CALL_CALLER_ID).block();

        assertNotNull(callParticipantResponse);
        assertEquals(200, callParticipantResponse.getStatusCode());
        assertNotNull(callParticipantResponse.getValue());
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) callParticipantResponse.getValue().getIdentifier()).getId());
    }

    @Test
    public void listParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateListParticipantsResponse(), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        ListParticipantsResult listParticipants = callConnectionAsync.listParticipants().block();

        assertNotNull(listParticipants);
        assertNotNull(listParticipants.getValues());
        assertEquals(CALL_CALLER_ID, ((CommunicationUserIdentifier) listParticipants.getValues().get(0).getIdentifier()).getId());
    }

    @Test
    public void listParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateListParticipantsResponse(), 200)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        Response<ListParticipantsResult> listParticipantsResultResponse = callConnectionAsync.listParticipantsWithResponse().block();

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
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        TransferCallResult transferCallResult = callConnectionAsync.transferToParticipantCall(new CommunicationUserIdentifier(CALL_TARGET_ID)).block();
        assertNotNull(transferCallResult);
        assertEquals(CALL_OPERATION_CONTEXT, transferCallResult.getOperationContext());
    }

    @Test
    public void transferToParticipantCallWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new TransferCallResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        TransferToParticipantCallOptions transferToParticipantCallOptions = new TransferToParticipantCallOptions(new CommunicationUserIdentifier(CALL_TARGET_ID))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<TransferCallResult> transferCallResultResponse = callConnectionAsync.transferToParticipantCallWithResponse(transferToParticipantCallOptions).block();

        assertNotNull(transferCallResultResponse);
        assertEquals(202, transferCallResultResponse.getStatusCode());
        assertNotNull(transferCallResultResponse.getValue());

        RepeatabilityHeaders repeatabilityHeaders = transferToParticipantCallOptions.getRepeatabilityHeaders();
        assertNotNull(repeatabilityHeaders);
        assertNotNull(repeatabilityHeaders.getRepeatabilityFirstSentInHttpDateFormat());
        assertNotNull(repeatabilityHeaders.getRepeatabilityRequestId().toString());
    }

    @Test
    public void addParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnectionAsync(CALL_CONNECTION_ID);

        AddParticipantsResult addParticipantsResult = callConnectionAsync.addParticipants(
            new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID)))).block();

        assertNotNull(addParticipantsResult);
        assertEquals(CALL_TARGET_ID, ((CommunicationUserIdentifier) addParticipantsResult
            .getParticipants()
            .get(0)
            .getIdentifier())
            .getId());
    }

    @Test
    public void addParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(generateAddParticipantsResponse(), 202)
            )
        )).getCallConnectionAsync(CALL_CONNECTION_ID);

        AddParticipantsOptions addParticipantsOptions = new AddParticipantsOptions(new ArrayList<>(Arrays.asList(
            new CommunicationUserIdentifier(CALL_TARGET_ID))))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<AddParticipantsResult> addParticipantsResultResponse = callConnectionAsync.addParticipantsWithResponse(addParticipantsOptions).block();

        assertNotNull(addParticipantsResultResponse);
        assertEquals(202, addParticipantsResultResponse.getStatusCode());
        assertNotNull(addParticipantsResultResponse.getValue());

        RepeatabilityHeaders repeatabilityHeaders = addParticipantsOptions.getRepeatabilityHeaders();
        assertNotNull(repeatabilityHeaders);
        assertNotNull(repeatabilityHeaders.getRepeatabilityFirstSentInHttpDateFormat());
        assertNotNull(repeatabilityHeaders.getRepeatabilityRequestId().toString());
    }

    @Test
    public void removeParticipants() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new RemoveParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        RemoveParticipantsResult removeParticipantsResult = callConnectionAsync.removeParticipants(
            new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID)))).block();

        assertNotNull(removeParticipantsResult);
        assertEquals(CALL_OPERATION_CONTEXT, removeParticipantsResult.getOperationContext());
    }

    @Test
    public void removeParticipantsWithResponse() {
        CallConnectionAsync callConnectionAsync = getCallAutomationAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(serializeObject(new RemoveParticipantsResponseInternal()
                    .setOperationContext(CALL_OPERATION_CONTEXT)), 202)
            )))
            .getCallConnectionAsync(CALL_CONNECTION_ID);

        RemoveParticipantsOptions removeParticipantsOptions = new RemoveParticipantsOptions(
            new ArrayList<>(Arrays.asList(new CommunicationUserIdentifier(CALL_TARGET_ID))))
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<RemoveParticipantsResult> removeParticipantsResultResponse = callConnectionAsync.removeParticipantsWithResponse(
            removeParticipantsOptions).block();


        assertNotNull(removeParticipantsResultResponse);
        assertEquals(202, removeParticipantsResultResponse.getStatusCode());
        assertNotNull(removeParticipantsResultResponse.getValue());

        RepeatabilityHeaders repeatabilityHeaders = removeParticipantsOptions.getRepeatabilityHeaders();
        assertNotNull(repeatabilityHeaders);
        assertNotNull(repeatabilityHeaders.getRepeatabilityFirstSentInHttpDateFormat());
        assertNotNull(repeatabilityHeaders.getRepeatabilityRequestId().toString());
    }
}
