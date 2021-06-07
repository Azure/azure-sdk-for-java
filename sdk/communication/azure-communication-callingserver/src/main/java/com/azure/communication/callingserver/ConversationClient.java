// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.JoinCallResponse;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Sync Client that supports server call operations.
 */
@ServiceClient(builder = CallClientBuilder.class)
public final class ConversationClient {
    private final ConversationAsyncClient conversationAsyncClient;

    ConversationClient(ConversationAsyncClient conversationAsyncClient) {
        this.conversationAsyncClient = conversationAsyncClient;
    }

    /**
     * Join a call
     *
     * @param conversationId The conversation id.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @return JoinCallResponse for a successful JoinCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JoinCallResponse joinCall(String conversationId, CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        return conversationAsyncClient.joinCall(conversationId, source, joinCallOptions).block();
    }

    /**
     * Join a call
     *
     * @param conversationId The conversation id.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful JoinCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JoinCallResponse> joinCallWithResponse(String conversationId, CommunicationIdentifier source, JoinCallOptions joinCallOptions, Context context) {
        return conversationAsyncClient.joinCallWithResponse(conversationId, source, joinCallOptions, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void addParticipant(String conversationId,
                               CommunicationIdentifier participant,
                               String callBackUri,
                               String alternateCallerId,
                               String operationContext) {
        return conversationAsyncClient.addParticipant(conversationId, participant, alternateCallerId, operationContext, callBackUri).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantWithResponse(String conversationId,
                                                     CommunicationIdentifier participant,
                                                     String callBackUri,
                                                     String alternateCallerId,
                                                     String operationContext,
                                                     Context context) {
        return conversationAsyncClient
            .addParticipantWithResponse(conversationId,
                participant,
                callBackUri,
                alternateCallerId,
                operationContext,
                context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void removeParticipant(String conversationId, String participantId) {
        return conversationAsyncClient.removeParticipant(conversationId, participantId).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(String conversationId, String participantId, Context context) {
        return conversationAsyncClient.removeParticipantWithResponse(conversationId, participantId, context).block();
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartCallRecordingResponse startRecording(String conversationId, String recordingStateCallbackUri) {
        return conversationAsyncClient.startRecording(conversationId, recordingStateCallbackUri).block();
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResponse> startRecordingWithResponse(String conversationId,
            String recordingStateCallbackUri, Context context) {
        return conversationAsyncClient.startRecordingWithResponse(conversationId, recordingStateCallbackUri, context).block();
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void stopRecording(String conversationId, String recordingId) {
        return conversationAsyncClient.stopRecording(conversationId, recordingId).block();
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String conversationId, String recordingId, Context context) {
        return conversationAsyncClient.stopRecordingWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void pauseRecording(String conversationId, String recordingId) {
        return conversationAsyncClient.pauseRecording(conversationId, recordingId).block();
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String conversationId, String recordingId, Context context) {
        return conversationAsyncClient.pauseRecordingWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void resumeRecording(String conversationId, String recordingId) {
        return conversationAsyncClient.resumeRecording(conversationId, recordingId).block();
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String conversationId, String recordingId, Context context) {
        return conversationAsyncClient.resumeRecordingWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingStateResponse getRecordingState(String conversationId, String recordingId) {
        return conversationAsyncClient.getRecordingState(conversationId, recordingId).block();
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallRecordingStateResponse> getRecordingStateWithResponse(String conversationId,
            String recordingId, Context context) {
        return conversationAsyncClient.getRecordingStateWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResponse playAudio(String conversationId,
                                       String audioFileUri,
                                       String audioFileId,
                                       String callbackUri,
                                       String operationContext) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest =
            PlayAudioConverter.convert(audioFileUri, false, audioFileId, callbackUri, operationContext);
        return conversationAsyncClient.playAudio(conversationId, playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
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
    public Response<PlayAudioResponse> playAudioWithResponse(String conversationId,
                                                             String audioFileUri,
                                                             String audioFileId,
                                                             String callbackUri,
                                                             String operationContext,
                                                             Context context) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest =
            PlayAudioConverter.convert(audioFileUri, false, audioFileId, callbackUri, operationContext);
        return conversationAsyncClient.playAudioWithResponse(conversationId, playAudioRequest, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResponse playAudio(String conversationId,
                                       String audioFileUri,
                                       PlayAudioOptions playAudioOptions) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return conversationAsyncClient.playAudio(conversationId, playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResponse> playAudioWithResponse(String conversationId,
                                                             String audioFileUri,
                                                             PlayAudioOptions playAudioOptions,
                                                             Context context) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return conversationAsyncClient.playAudioWithResponse(conversationId, playAudioRequest, context).block();
    }
}
