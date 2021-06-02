package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.callingserver.models.CreateCallResult;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioResult;

import com.azure.core.http.rest.Response;

public class CallingServerTestUtils {
    protected static void ValidateCreateCallResponse(Response<CreateCallResult> createCallResponse)
    {
        assertNotNull(createCallResponse);
        assertTrue(createCallResponse.getStatusCode() == 201);
        assertNotNull(createCallResponse.getValue());
        ValidateCreateCallResult(createCallResponse.getValue());
    }

    protected static void ValidateCreateCallResult(CreateCallResult createCallResult)
    {
        assertNotNull(createCallResult);
        assertNotNull(createCallResult.getCallLegId());
        assertTrue(!createCallResult.getCallLegId().isEmpty());
    }

    protected static void ValidatePlayAudioResponse(Response<PlayAudioResult> playAudioResponse, String operationContext)
    {   
        assertNotNull(playAudioResponse);
        assertTrue(playAudioResponse.getStatusCode() == 202);
        assertNotNull(playAudioResponse.getValue());
        ValidatePlayAudioResult(playAudioResponse.getValue(), operationContext);
    }

    protected static void ValidatePlayAudioResult(PlayAudioResult playAudioResult, String operationContext)
    {
        assertNotNull(playAudioResult);
        assertNotNull(playAudioResult.getId());
        assertTrue(!playAudioResult.getId().isEmpty());
        assertNotNull(playAudioResult.getOperationContext());
        assertTrue(playAudioResult.getOperationContext().equalsIgnoreCase(operationContext));
        assertNotNull(playAudioResult.getStatus());
        assertTrue(playAudioResult.getStatus() == OperationStatus.RUNNING);
    }

    protected static void ValidateHangupResponse(Response<Void> hangupResponse)
    {
        assertNotNull(hangupResponse);
        assertTrue(hangupResponse.getStatusCode() == 202);
    }

    protected static void ValidateInviteParticipantResponse(Response<Void> inviteParticipantResponse)
    {
        assertNotNull(inviteParticipantResponse);
        assertTrue(inviteParticipantResponse.getStatusCode() == 202);
    }

    protected static void ValidateRemoveParticipantResponse(Response<Void> removeParticipantResponse)
    {
        assertNotNull(removeParticipantResponse);
        assertTrue(removeParticipantResponse.getStatusCode() == 202);
    }
}
