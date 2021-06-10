// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Sync Client that supports call connection operations.
 */
public final class CallConnection {
    private final CallConnectionAsync callConnectionAsync;
    private final ClientLogger logger = new ClientLogger(CallConnection.class);

    CallConnection(CallConnectionAsync callConnectionAsync) {
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Get the call connection id property
     *
     * @return the id value.
     */
    public String getCallConnectionId() {
        return this.callConnectionAsync.getCallConnectionId();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri call back uri to receive notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String audioFileUri,
                                       boolean loop,
                                       String audioFileId,
                                       String callbackUri,
                                       String operationContext) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(loop);
        playAudioRequest.setAudioFileId(audioFileId);
        playAudioRequest.setOperationContext(operationContext);
        playAudioRequest.setCallbackUri(callbackUri);
        return callConnectionAsync.playAudio(playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri call back uri to receive notifications.
     * @param operationContext The value to identify context of the operation.
     * @param context A {@link Context} representing the request context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(String audioFileUri,
                                                           boolean loop,
                                                           String audioFileId,
                                                           String callbackUri,
                                                           String operationContext,
                                                           Context context) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(loop);
        playAudioRequest.setAudioFileId(audioFileId);
        playAudioRequest.setOperationContext(operationContext);
        playAudioRequest.setCallbackUri(callbackUri);
        return callConnectionAsync.playAudioWithResponse(playAudioRequest, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String audioFileUri, PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return callConnectionAsync.playAudio(playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(String audioFileUri,
                                                           PlayAudioOptions playAudioOptions,
                                                           Context context) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return callConnectionAsync.playAudioWithResponse(playAudioRequest, context).block();
    }

    /**
     * Disconnect the current caller in a Group-call or end a p2p-call.
     *
     * @return response for a successful Hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void hangup() {
        return callConnectionAsync.hangup().block();
    }

    /**
     * Disconnect the current caller in a Group-call or end a p2p-call.
     *
     * @param context A {@link Context} representing the request context.
     * @return response for a successful HangupCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> hangupWithResponse(Context context) {
        return callConnectionAsync.hangupWithResponse(context).block();
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext operationContext.
     * @return response for a successful CancelMediaOperations request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CancelAllMediaOperationsResult cancelAllMediaOperations(String operationContext) {
        return callConnectionAsync.cancelAllMediaOperations(operationContext).block();
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext operationContext.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful CancelMediaOperations request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CancelAllMediaOperationsResult> cancelAllMediaOperationsWithResponse(String operationContext,
                                                                                           Context context) {
        return callConnectionAsync.cancelAllMediaOperationsWithResponse(operationContext, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void addParticipant(CommunicationIdentifier participant,
                               String alternateCallerId,
                               String operationContext) {
        return callConnectionAsync.addParticipant(participant, alternateCallerId, operationContext).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantWithResponse(CommunicationIdentifier participant,
                                                     String alternateCallerId,
                                                     String operationContext,
                                                     Context context) {
        return callConnectionAsync
            .addParticipantWithResponse(participant, alternateCallerId, operationContext, context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void removeParticipant(String participantId) {
        return callConnectionAsync.removeParticipant(participantId).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(String participantId, Context context) {
        return callConnectionAsync.removeParticipantWithResponse(participantId, context).block();
    }
}
