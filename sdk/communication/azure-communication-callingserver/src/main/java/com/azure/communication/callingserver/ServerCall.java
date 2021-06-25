// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Sync Client that supports server call operations.
 */
public final class ServerCall {
    private final ServerCallAsync serverCallAsync;

    ServerCall(ServerCallAsync serverCallAsync) {
        this.serverCallAsync = serverCallAsync;
    }

    /**
     * Get the server call id property
     *
     * @return the id value.
     */
    public String getServerCallId() {
        return serverCallAsync.getServerCallId();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantResult addParticipant(
        CommunicationIdentifier participant,
        String callBackUri,
        String alternateCallerId,
        String operationContext) {
        return serverCallAsync.addParticipant(participant, callBackUri, alternateCallerId, operationContext).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantResult> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String callBackUri,
        String alternateCallerId,
        String operationContext,
        final Context context) {
        return serverCallAsync.addParticipantWithResponse(
                participant,
                callBackUri,
                alternateCallerId,
                operationContext,
                context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipant(String participantId) {
        serverCallAsync.removeParticipant(participantId).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(String participantId, final Context context) {
        return serverCallAsync.removeParticipantWithResponse(participantId, context).block();
    }

    /**
     * Start recording
     *
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartCallRecordingResult startRecording(String recordingStateCallbackUri) {
        return serverCallAsync.startRecording(recordingStateCallbackUri).block();
    }

    /**
     * Start recording
     *
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResult> startRecordingWithResponse(
        String recordingStateCallbackUri,
        final Context context) {
        return serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri, context).block();
    }

    /**
     * Stop recording
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopRecording(String recordingId) {
        serverCallAsync.stopRecording(recordingId).block();
    }

    /**
     * Stop recording
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String recordingId, final Context context) {
        return serverCallAsync.stopRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Pause recording
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void pauseRecording(String recordingId) {
        serverCallAsync.pauseRecording(recordingId).block();
    }

    /**
     * Pause recording
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String recordingId, final Context context) {
        return serverCallAsync.pauseRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Resume recording
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resumeRecording(String recordingId) {
        serverCallAsync.resumeRecording(recordingId).block();
    }

    /**
     * Resume recording
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String recordingId, final Context context) {
        return serverCallAsync.resumeRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Get recording state
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingProperties getRecordingState(String recordingId) {
        return serverCallAsync.getRecordingState(recordingId).block();
    }

    /**
     * Get recording state
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallRecordingProperties> getRecordingStateWithResponse(String recordingId, final Context context) {
        return serverCallAsync.getRecordingStateWithResponse(recordingId, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(
        String audioFileUri,
        String audioFileId,
        String callbackUri,
        String operationContext) {
        return serverCallAsync.playAudioInternal(audioFileUri, audioFileId, callbackUri, operationContext).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String audioFileUri, PlayAudioOptions playAudioOptions) {
        return serverCallAsync.playAudioInternal(audioFileUri, playAudioOptions).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(
        String audioFileUri,
        PlayAudioOptions playAudioOptions,
        final Context context) {
        return serverCallAsync.playAudioWithResponseInternal(audioFileUri, playAudioOptions, context).block();
    }
}
