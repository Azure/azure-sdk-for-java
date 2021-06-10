// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.JoinCallResult;
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

    protected static void validateCallConnection(CallConnection callConnection) {
        assertNotNull(callConnection);
        assertNotNull(callConnection.getCallConnectionId());
        assertFalse(callConnection.getCallConnectionId().isEmpty());
    }

    protected static void validateCallConnectionAsyncResponse(Response<CallConnectionAsync> callConnectionResponseAsync) {
        assertNotNull(callConnectionResponseAsync);
        assertEquals(201, callConnectionResponseAsync.getStatusCode());
        assertNotNull(callConnectionResponseAsync.getValue());
        validateCallConnectionAsync(callConnectionResponseAsync.getValue());
    }

    protected static void validateCallConnectionAsync(CallConnectionAsync callConnectionAsync) {
        assertNotNull(callConnectionAsync);
        assertNotNull(callConnectionAsync.getCallConnectionId());
        assertFalse(callConnectionAsync.getCallConnectionId().isEmpty());
    }

    protected static void validateJoinCall(JoinCallResult joinCallResult) {
        assertNotNull(joinCallResult);
        assertNotNull(joinCallResult.getCallConnectionId());
        assertFalse(joinCallResult.getCallConnectionId().isEmpty());
    }

    protected static void validatePlayAudioResponse(Response<PlayAudioResult> playAudioResponse, String operationContext) {
        assertNotNull(playAudioResponse);
        Assertions.assertEquals(202, playAudioResponse.getStatusCode());
        assertNotNull(playAudioResponse.getValue());
        validatePlayAudioResult(playAudioResponse.getValue(), operationContext);
    }

    protected static void validatePlayAudioResult(PlayAudioResult playAudioResponse, String operationContext) {
        assertNotNull(playAudioResponse);
        assertNotNull(playAudioResponse.getId());
        assertFalse(playAudioResponse.getId().isEmpty());
        assertNotNull(playAudioResponse.getOperationContext());
        assertTrue(playAudioResponse.getOperationContext().equalsIgnoreCase(operationContext));
        assertNotNull(playAudioResponse.getStatus());
        assertSame(playAudioResponse.getStatus(), OperationStatus.RUNNING);
    }

    protected static void validateCancelAllMediaOperationsResponse(Response<CancelAllMediaOperationsResult> cancelAllMediaOperationsResult, String cancelMediaOperationContext) {
        assertNotNull(cancelAllMediaOperationsResult);
        Assertions.assertEquals(200, cancelAllMediaOperationsResult.getStatusCode());
        assertNotNull(cancelAllMediaOperationsResult.getValue());
        validateCancelAllMediaOperations(cancelAllMediaOperationsResult.getValue(), cancelMediaOperationContext);
    }

    protected static void validateCancelAllMediaOperations(CancelAllMediaOperationsResult cancelAllMediaOperationsResponse, String cancelMediaOperationContext) {
        assertNotNull(cancelAllMediaOperationsResponse);
        assertNotNull(cancelAllMediaOperationsResponse.getId());
        assertFalse(cancelAllMediaOperationsResponse.getId().isEmpty());
        assertNotNull(cancelAllMediaOperationsResponse.getOperationContext());
        assertTrue(cancelAllMediaOperationsResponse.getOperationContext().equalsIgnoreCase(cancelMediaOperationContext));
        assertNotNull(cancelAllMediaOperationsResponse.getStatus());
        assertSame(cancelAllMediaOperationsResponse.getStatus(), OperationStatus.COMPLETED);
    }

    protected static void validateResponse(Response<Void> response) {
        assertNotNull(response);
        Assertions.assertEquals(202, response.getStatusCode());
    }
}
