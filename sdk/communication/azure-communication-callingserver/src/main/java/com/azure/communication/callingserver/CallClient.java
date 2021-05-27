// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;

/**
 * Sync Client that supports server call operations.
 */
@ServiceClient(builder = CallClientBuilder.class)
public final class CallClient {
    private final CallAsyncClient callAsyncClient;

    CallClient(CallAsyncClient callAsyncClient) { this.callAsyncClient = callAsyncClient; }

    /**
     * Create a Call Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param CreateCallOptions The call Options.
     * @return response for a successful CreateCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CreateCallResult createCall(CommunicationIdentifier source, Iterable<CommunicationIdentifier> targets, CreateCallOptions CreateCallOptions) {
        return callAsyncClient.createCall(source, targets, CreateCallOptions).block();
    }

    /**
     * Create a Call Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param CreateCallOptions The call Options.
     * @return response for a successful CreateCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CreateCallResult> createCallWithResponse(CommunicationIdentifier source, Iterable<CommunicationIdentifier> targets, CreateCallOptions CreateCallOptions) {
        return callAsyncClient.createCallWithResponse(source, targets, CreateCallOptions).block();
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String callId, String audioFileUri, boolean loop, String audioFileId, String operationContext) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest().
            setAudioFileUri(audioFileUri).setLoop(loop).setAudioFileId(audioFileId).setOperationContext(operationContext);
        return callAsyncClient.playAudio(callId, playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(String callId, String audioFileUri, boolean loop, String audioFileId, String operationContext) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest().
            setAudioFileUri(audioFileUri).setLoop(loop).setAudioFileId(audioFileId).setOperationContext(operationContext);
        return callAsyncClient.playAudioWithResponse(callId, playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param playAudioRequest Play audio request.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String callId, PlayAudioRequest playAudioRequest) {
        return callAsyncClient.playAudio(callId, playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param playAudioRequest Play audio request.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(String callId, PlayAudioRequest playAudioRequest) {
        return callAsyncClient.playAudioWithResponse(callId, playAudioRequest).block();
    }

    /**
     * Disconnect the current caller in a Group-call or end a p2p-call.
     *
     * @param callId Call id to to hang up.
     * @return response for a successful HangupCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void hangupCall(String callId) {
        return callAsyncClient.hangupCall(callId).block();
    }

    /**
     * Disconnect the current caller in a Group-call or end a p2p-call.
     *
     * @param callId Call id to to hang up.
     * @return response for a successful HangupCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> hangupCallWithResponse(String callId) {
        return callAsyncClient.hangupCallWithResponse(callId).block();
    }

    /**
     * Deletes a call.
     *
     * @param callId Call id to delete.
     * @return response for a successful DeleteCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void deleteCall(String callId) {
        return callAsyncClient.deleteCall(callId).block();
    }

    /**
     * Deletes a call.
     *
     * @param callId Call id to delete.
     * @return response for a successful DeleteCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteCallWithResponse(String callId) {
        return callAsyncClient.deleteCallWithResponse(callId).block();
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId Call id to to cancel media processing.
     * @param request Cancel Media Processing request.
     * @return response for a successful CancelMediaProcessing request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CancelMediaProcessingResult cancelMediaProcessing(String callId, CancelMediaProcessingRequest request) {
        return callAsyncClient.cancelMediaProcessing(callId, request).block();
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId Call id to to cancel media processing.
     * @param request Cancel Media Processing request.
     * @return response for a successful CancelMediaProcessing request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CancelMediaProcessingResult> cancelMediaProcessingWithResponse(String callId, CancelMediaProcessingRequest request) {
        return callAsyncClient.cancelMediaProcessingWithResponse(callId, request).block();
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId Call id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void inviteParticipants(String callId, InviteParticipantsRequest request) {
        return callAsyncClient.inviteParticipants(callId, request).block();
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId Call id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> inviteParticipantsWithResponse(String callId, InviteParticipantsRequest request) {
        return callAsyncClient.inviteParticipantsWithResponse(callId, request).block();
    }

    /**
     * Remove participant from the call.
     *
     * @param callId Call id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void removeParticipant(String callId, String participantId) {
        return callAsyncClient.removeParticipant(callId, participantId).block();
    }

    /**
     * Remove participant from the call.
     *
     * @param callId Call id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(String callId, String participantId) {
        return callAsyncClient.removeParticipantWithResponse(callId, participantId).block();
    }
}
