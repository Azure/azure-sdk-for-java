// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.GetCallRecordingStateResult;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.JoinCallResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
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
     * Invite participants to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param alternateCallerId alternateCallerId of Invited participant.
     * @param operationContext operationContext.
     * @param callBackUri callBackUri to get notifications.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void addParticipant(String conversationId, CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri) {
        return conversationAsyncClient.addParticipant(conversationId, participant, alternateCallerId, operationContext, callBackUri).block();
    }

    /**
     * Invite participants to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param alternateCallerId alternateCallerId of Invited participant.
     * @param operationContext operationContext.
     * @param callBackUri callBackUri to get notifications.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantWithResponse(String conversationId, CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri, Context context) {
        return conversationAsyncClient.addParticipantWithResponse(conversationId, participant, alternateCallerId, operationContext, callBackUri, context).block();
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
}
