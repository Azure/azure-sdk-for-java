// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.callingserver.models.StartHoldMusicResult;
import com.azure.communication.callingserver.models.StopHoldMusicResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Synchronous Client that supports server call operations.
 */
public final class ServerCall {
    private final ServerCallAsync serverCallAsync;

    ServerCall(ServerCallAsync serverCallAsync) {
        this.serverCallAsync = serverCallAsync;
    }

    /**
     * Get server call id property.
     *
     * @return Server call id value.
     */
    public String getServerCallId() {
        return serverCallAsync.getServerCallId();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param callBackUri CallBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
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
     * @param callBackUri CallBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
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
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(String participantId, final Context context) {
        return serverCallAsync.removeParticipantWithResponse(participantId, context).block();
    }

    /**
     * Start recording of the call.
     *
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartCallRecordingResult startRecording(String recordingStateCallbackUri) {
        return serverCallAsync.startRecording(recordingStateCallbackUri).block();
    }

    /**
     * Start recording of the call.
     *
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResult> startRecordingWithResponse(
        String recordingStateCallbackUri,
        final Context context) {
        return serverCallAsync.startRecordingWithResponse(recordingStateCallbackUri, context).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopRecording(String recordingId) {
        serverCallAsync.stopRecording(recordingId).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String recordingId, final Context context) {
        return serverCallAsync.stopRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void pauseRecording(String recordingId) {
        serverCallAsync.pauseRecording(recordingId).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String recordingId, final Context context) {
        return serverCallAsync.pauseRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Resume recording of the call.
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
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String recordingId, final Context context) {
        return serverCallAsync.resumeRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingProperties getRecordingState(String recordingId) {
        return serverCallAsync.getRecordingState(recordingId).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallRecordingProperties> getRecordingStateWithResponse(String recordingId, final Context context) {
        return serverCallAsync.getRecordingStateWithResponse(recordingId, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri Media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId Id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri Callback Uri to receive PlayAudio status notifications.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
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
     * @param audioFileUri Media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String audioFileUri, PlayAudioOptions playAudioOptions) {
        return serverCallAsync.playAudioInternal(audioFileUri, playAudioOptions).block();
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri Media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(
        String audioFileUri,
        PlayAudioOptions playAudioOptions,
        final Context context) {
        return serverCallAsync.playAudioWithResponseInternal(audioFileUri, playAudioOptions, context).block();
    }
    
    /**
     * Remove participant from the call using identifier.
     *
     * @param participant The identifier of the participant to be removed from the call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipantById(CommunicationIdentifier participant) {
        serverCallAsync.removeParticipantById(participant).block();
    }

    /**
     * Remove participant from the call using identifier.
     *
     * @param participant The identifier of the participant to be removed from the call.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantByIdWithResponse(CommunicationIdentifier participant, final Context context) {
        return serverCallAsync.removeParticipantByIdWithResponse(participant, context).block();
    }

    /**
     * Get participants of the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public List<CallParticipant> getParticipants() {
        return serverCallAsync.getParticipants().block();
    }

    /**
     * Get participants of the call.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<List<CallParticipant>> getParticipantsWithResponse(final Context context) {
        return serverCallAsync.getParticipantsWithResponse(context).block();
    }

    /**
     * Get participant of the call using participant id.
     *
     * @param participantId The participant id.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallParticipant getParticipant(String participantId) {
        return serverCallAsync.getParticipant(participantId).block();
    }

    /**
     * Get participant of the call using participant id.
     *
     * @param participantId The participant id.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallParticipant> getParticipantWithResponse(String participantId, final Context context) {
        return serverCallAsync.getParticipantWithResponse(participantId, context).block();
    }

    /**
     * Hold the participant and play default music.
     *
     * @param participantId The participant id.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for start hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartHoldMusicResult startHoldMusic(String participantId) {  
        return serverCallAsync.startHoldMusic(participantId).block();
    }

    /**
     * Hold the participant and play default music.
     *
     * @param participantId The participant id.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for start hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartHoldMusicResult> startHoldMusicWithResponse(String participantId, final Context context) {  
        return serverCallAsync.startHoldMusicWithResponse(participantId, context).block();
    }

    /**
     * Hold the participant and play custom audio.
     *
     * @param participantId The participant id.
     * @param audioFileUri The uri of the audio file.
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for start hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartHoldMusicResult startHoldMusic(String participantId, String audioFileUri, String audioFileId, String callbackUri) {  
        return serverCallAsync.startHoldMusic(participantId, audioFileUri, audioFileId, callbackUri).block();
    }

    /**
     * Hold the participant and play custom audio.
     *
     * @param participantId The participant id.
     * @param audioFileUri The uri of the audio file.
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for start hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartHoldMusicResult> startHoldMusicWithResponse(String participantId, String audioFileUri, String audioFileId, String callbackUri, final Context context) {  
        return serverCallAsync.startHoldMusicWithResponse(participantId, audioFileUri, audioFileId, callbackUri, context).block();
    }

    /**
     * Remove participant from the hold and stop playing audio.
     *
     * @param participantId The participant id.
     * @param operationId The id of the start hold music operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for stop hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StopHoldMusicResult stopHoldMusic(String participantId, String operationId) {  
        return serverCallAsync.stopHoldMusic(participantId, operationId).block();
    }

    /**
     * Remove participant from the hold and stop playing audio.
     *
     * @param participantId The participant id.
     * @param operationId The id of the start hold music operation.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for stop hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StopHoldMusicResult> stopHoldMusicWithResponse(String participantId, String operationId, final Context context) {  
        return serverCallAsync.stopHoldMusicWithResponse(participantId, operationId, context).block();
    }
}
