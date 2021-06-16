// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Assertions;

public class CallingServerTestUtils {
    protected static void validateCallConnectionResponse(Response<CallConnection> callConnectionResponse) {
        assertNotNull(callConnectionResponse);
        assertEquals(201, callConnectionResponse.getStatusCode());
        assertNotNull(callConnectionResponse.getValue());
        validateCallConnection(callConnectionResponse.getValue());
    }

    protected static void validateJoinCallConnectionResponse(Response<CallConnection> callConnectionResponse) {
        assertNotNull(callConnectionResponse);
        assertEquals(202, callConnectionResponse.getStatusCode());
        assertNotNull(callConnectionResponse.getValue());
        validateCallConnection(callConnectionResponse.getValue());
    }

    protected static void validateCallConnection(CallConnection callConnection) {
        assertNotNull(callConnection);
        assertNotNull(callConnection.getCallConnectionId());
        assertFalse(callConnection.getCallConnectionId().isEmpty());
    }

    protected static void validateCallConnectionAsyncResponse(Response<CallConnectionAsync> response) {
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getValue());
        validateCallConnectionAsync(response.getValue());
    }

    protected static void validateJoinCallConnectionAsyncResponse(Response<CallConnectionAsync> response) {
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getValue());
        validateCallConnectionAsync(response.getValue());
    }

    protected static void validateCallConnectionAsync(CallConnectionAsync callConnectionAsync) {
        assertNotNull(callConnectionAsync);
        assertNotNull(callConnectionAsync.getCallConnectionId());
        assertFalse(callConnectionAsync.getCallConnectionId().isEmpty());
    }

    protected static void validatePlayAudioResponse(Response<PlayAudioResult> playAudioResponse) {
        assertNotNull(playAudioResponse);
        Assertions.assertEquals(202, playAudioResponse.getStatusCode());
        assertNotNull(playAudioResponse.getValue());
        validatePlayAudioResult(playAudioResponse.getValue());
    }

    protected static void validatePlayAudioResult(PlayAudioResult playAudioResponse) {
        assertNotNull(playAudioResponse);
        assertNotNull(playAudioResponse.getOperationId());
        assertFalse(playAudioResponse.getOperationId().isEmpty());
        assertNotNull(playAudioResponse.getStatus());
        assertSame(playAudioResponse.getStatus(), OperationStatus.RUNNING);
    }

    protected static void validateCancelAllMediaOperationsResult(Response<CancelAllMediaOperationsResult> result) {
        assertNotNull(result);
        Assertions.assertEquals(200, result.getStatusCode());
        assertNotNull(result.getValue());
        validateCancelAllMediaOperations(result.getValue());
    }

    protected static void validateCancelAllMediaOperations(CancelAllMediaOperationsResult result) {
        assertNotNull(result);
        assertNotNull(result.getOperationId());
        assertFalse(result.getOperationId().isEmpty());
        assertNotNull(result.getStatus());
        assertSame(result.getStatus(), OperationStatus.COMPLETED);
    }

    protected static void validateResponse(Response<Void> response) {
        assertNotNull(response);
        Assertions.assertEquals(202, response.getStatusCode());
    }

    protected static void validateAddParticipantResponse(Response<AddParticipantResult> response) {
        assertNotNull(response);
        Assertions.assertEquals(202, response.getStatusCode());
        assertNotNull(response.getValue());
        validateAddParticipantResult(response.getValue());
    }

    protected static void validateAddParticipantResult(AddParticipantResult result) {
        assertNotNull(result);
        assertNotNull(result.getParticipantId());
        assertFalse(result.getParticipantId().isEmpty());
    }
}
