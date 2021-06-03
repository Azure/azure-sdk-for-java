// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.JoinCallResponse;
import com.azure.communication.callingserver.models.GetCallRecordingStateResult;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.models.JoinCallRequest;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.net.URI;

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
     * @param request Join Call request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JoinCallResponse joinCall(String conversationId, JoinCallRequest request) {
        return conversationAsyncClient.joinCall(conversationId, request).block();
    }

    /**
     * Join a call
     *
     * @param conversationId The conversation id.
     * @param request Join Call request.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JoinCallResponse> inviteParticipantsWithResponse(String conversationId, JoinCallRequest request, Context context) {
        return conversationAsyncClient.joinCallWithResponse(conversationId, request, context).block();
    }

    /**
     * Invite participants to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void inviteParticipants(String conversationId, InviteParticipantsRequest request) {
        return conversationAsyncClient.inviteParticipants(conversationId, request).block();
    }

    /**
     * Invite participants to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param request Invite participant request.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> inviteParticipantsWithResponse(String conversationId, InviteParticipantsRequest request, Context context) {
        return conversationAsyncClient.inviteParticipantsWithResponse(conversationId, request, context).block();
    }

    /**
     * Remove participant from the Conversation.
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
     * Remove participant from the Conversation.
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
    public StartCallRecordingResult startRecording(String conversationId, URI recordingStateCallbackUri) {
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
    public Response<StartCallRecordingResult> startRecordingWithResponse(String conversationId,
            URI recordingStateCallbackUri, Context context) {
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
    public GetCallRecordingStateResult getRecordingState(String conversationId, String recordingId) {
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
    public Response<GetCallRecordingStateResult> getRecordingStateWithResponse(String conversationId,
            String recordingId, Context context) {
        return conversationAsyncClient.getRecordingStateWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(String conversationId, URI audioFileUri, String audioFileId, URI callbackUri, String operationContext) {
        return conversationAsyncClient.playAudio(conversationId, audioFileUri, audioFileId, callbackUri, operationContext).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation.
     * @param context A {@link Context} representing the request context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(String conversationId, URI audioFileUri, String audioFileId, URI callbackUri, String operationContext, Context context) {
        return conversationAsyncClient.playAudioWithResponse(conversationId, audioFileUri, audioFileId, callbackUri, operationContext, context).block();
    }
}
