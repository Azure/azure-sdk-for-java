// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.callingserver.models.CancelAllMediaOperationsResponse;
import com.azure.communication.callingserver.models.CreateCallResponse;
import com.azure.communication.callingserver.models.JoinCallResponse;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.core.http.rest.Response;

public class CallingServerTestUtils {
    protected static void validateCreateCallResponse(Response<CreateCallResponse> createCallResponse) {
        assertNotNull(createCallResponse);
        assertTrue(createCallResponse.getStatusCode() == 201);
        assertNotNull(createCallResponse.getValue());
        validateCreateCallResult(createCallResponse.getValue());
    }

    protected static void validateCreateCallResult(CreateCallResponse createCallResult) {
        assertNotNull(createCallResult);
        assertNotNull(createCallResult.getCallLegId());
        assertTrue(!createCallResult.getCallLegId().isEmpty());
    }

    protected static void validateJoinCall(JoinCallResponse joinCallResponse) {
        assertNotNull(joinCallResponse);
        assertNotNull(joinCallResponse.getCallLegId());
        assertTrue(!joinCallResponse.getCallLegId().isEmpty());
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

    protected static void validateCancelAllMediaOperationsResponse(Response<CancelAllMediaOperationsResponse> cancelAllMediaOperationsResponse, String cancelMediaOperationContext) {   
        assertNotNull(cancelAllMediaOperationsResponse);
        assertTrue(cancelAllMediaOperationsResponse.getStatusCode() == 200);
        assertNotNull(cancelAllMediaOperationsResponse.getValue());
        validateCancelAllMediaOperations(cancelAllMediaOperationsResponse.getValue(), cancelMediaOperationContext);
    }

    protected static void validateCancelAllMediaOperations(CancelAllMediaOperationsResponse cancelAllMediaOperationsResponse, String cancelMediaOperationContext) {
        assertNotNull(cancelAllMediaOperationsResponse);
        assertNotNull(cancelAllMediaOperationsResponse.getId());
        assertTrue(!cancelAllMediaOperationsResponse.getId().isEmpty());
        assertNotNull(cancelAllMediaOperationsResponse.getOperationContext());
        assertTrue(cancelAllMediaOperationsResponse.getOperationContext().equalsIgnoreCase(cancelMediaOperationContext));
        assertNotNull(cancelAllMediaOperationsResponse.getStatus());
        assertTrue(cancelAllMediaOperationsResponse.getStatus() == OperationStatus.COMPLETED);
    }

    protected static void validateResponse(Response<Void> response) {
        assertNotNull(response);
        assertTrue(response.getStatusCode() == 202);
    }
}
