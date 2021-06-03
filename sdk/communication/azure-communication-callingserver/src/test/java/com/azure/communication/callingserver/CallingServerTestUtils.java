// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.callingserver.implementation.models.OperationStatus;
import com.azure.communication.callingserver.models.CreateCallResult;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.core.http.rest.Response;

public class CallingServerTestUtils {
    protected static void validateCreateCallResponse(Response<CreateCallResult> createCallResponse) {
        assertNotNull(createCallResponse);
        assertTrue(createCallResponse.getStatusCode() == 201);
        assertNotNull(createCallResponse.getValue());
        validateCreateCallResult(createCallResponse.getValue());
    }

    protected static void validateCreateCallResult(CreateCallResult createCallResult) {
        assertNotNull(createCallResult);
        assertNotNull(createCallResult.getCallLegId());
        assertTrue(!createCallResult.getCallLegId().isEmpty());
    }

    protected static void validatePlayAudioResponse(Response<PlayAudioResponse> playAudioResponse, String operationContext) {   
        assertNotNull(playAudioResponse);
        assertTrue(playAudioResponse.getStatusCode() == 202);
        assertNotNull(playAudioResponse.getValue());
        validatePlayAudioResult(playAudioResponse.getValue(), operationContext);
    }

    protected static void validatePlayAudioResult(PlayAudioResponse playAudioResponse, String operationContext) {
        assertNotNull(playAudioResponse);
        assertNotNull(playAudioResponse.getId());
        assertTrue(!playAudioResponse.getId().isEmpty());
        assertNotNull(playAudioResponse.getOperationContext());
        assertTrue(playAudioResponse.getOperationContext().equalsIgnoreCase(operationContext));
        assertNotNull(playAudioResponse.getStatus());
        assertTrue(playAudioResponse.getStatus() == OperationStatus.RUNNING);
    }

    protected static void validateHangupResponse(Response<Void> hangupResponse) {
        assertNotNull(hangupResponse);
        assertTrue(hangupResponse.getStatusCode() == 202);
    }

    protected static void validateInviteParticipantResponse(Response<Void> inviteParticipantResponse) {
        assertNotNull(inviteParticipantResponse);
        assertTrue(inviteParticipantResponse.getStatusCode() == 202);
    }

    protected static void validateRemoveParticipantResponse(Response<Void> removeParticipantResponse) {
        assertNotNull(removeParticipantResponse);
        assertTrue(removeParticipantResponse.getStatusCode() == 202);
    }
}
