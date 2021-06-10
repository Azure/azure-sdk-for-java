// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CallRecordingStateResult;
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

    ServerCall(String serverCallId, ServerCallAsync serverCallAsync) {
        this.serverCallAsync = serverCallAsync;
    }

    /**
     * Get the server call id property
     *
     * @return the id value.
     */
    public String getServerCallId() {
        return this.serverCallAsync.getServerCallId();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void addParticipant(CommunicationIdentifier participant,
                               String callBackUri,
                               String alternateCallerId,
                               String operationContext) {
        return serverCallAsync.addParticipant(participant, alternateCallerId, operationContext, callBackUri).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantWithResponse(CommunicationIdentifier participant,
                                                     String callBackUri,
                                                     String alternateCallerId,
                                                     String operationContext,
                                                     Context context) {
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
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void removeParticipant(String participantId) {
        return serverCallAsync.removeParticipant(participantId).block();
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
        return serverCallAsync.removeParticipantWithResponse(participantId, context).block();
    }

    /**
     * Start recording
     *
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @return result for a successful startRecording request.
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
     * @return result for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResult> startRecordingWithResponse(
            String recordingStateCallbackUri, Context context) {
        return serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri, context).block();
    }

    /**
     * Stop recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void stopRecording(String recordingId) {
        return serverCallAsync.stopRecording(recordingId).block();
    }

    /**
     * Stop recording
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String recordingId, Context context) {
        return serverCallAsync.stopRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Pause recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void pauseRecording(String recordingId) {
        return serverCallAsync.pauseRecording(recordingId).block();
    }

    /**
     * Pause recording
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String recordingId, Context context) {
        return serverCallAsync.pauseRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Resume recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void resumeRecording(String recordingId) {
        return serverCallAsync.resumeRecording(recordingId).block();
    }

    /**
     * Resume recording
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String recordingId, Context context) {
        return serverCallAsync.resumeRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Get recording state
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingStateResult getRecordingState(String recordingId) {
        return serverCallAsync.getRecordingState(recordingId).block();
    }

    /**
     * Get recording state
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallRecordingStateResult> getRecordingStateWithResponse(String recordingId, Context context) {
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
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String audioFileUri,
                                     String audioFileId,
                                     String callbackUri,
                                     String operationContext) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(false);
        playAudioRequest.setAudioFileId(audioFileId);
        playAudioRequest.setOperationContext(operationContext);
        playAudioRequest.setCallbackUri(callbackUri);
        return serverCallAsync.playAudio(playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation.
     * @param context A {@link Context} representing the request context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(String audioFileUri,
                                                             String audioFileId,
                                                             String callbackUri,
                                                             String operationContext,
                                                             Context context) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(false);
        playAudioRequest.setAudioFileId(audioFileId);
        playAudioRequest.setOperationContext(operationContext);
        playAudioRequest.setCallbackUri(callbackUri);
        return serverCallAsync.playAudioWithResponse(playAudioRequest, context).block();
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
    public PlayAudioResult playAudio(String audioFileUri,
                                       PlayAudioOptions playAudioOptions) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return serverCallAsync.playAudio(playAudioRequest).block();
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
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return serverCallAsync.playAudioWithResponse(playAudioRequest, context).block();
    }
}
