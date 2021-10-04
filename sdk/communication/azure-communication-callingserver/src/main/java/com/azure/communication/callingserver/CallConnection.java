// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import java.net.URI;
import java.util.List;

import com.azure.communication.callingserver.implementation.models.AudioRoutingMode;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartHoldMusicResult;
import com.azure.communication.callingserver.models.StopHoldMusicResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Synchronous Client that supports call connection operations.
 */
public final class CallConnection {
    private final CallConnectionAsync callConnectionAsync;
    private final ClientLogger logger = new ClientLogger(CallConnection.class);

    CallConnection(CallConnectionAsync callConnectionAsync) {
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Get the call connection id property.
     *
     * @return Call connection id value.
     */
    public String getCallConnectionId() {
        return callConnectionAsync.getCallConnectionId();
    }

    /**
     * Play audio in the call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(URI audioFileUri, PlayAudioOptions playAudioOptions) {
        return callConnectionAsync.playAudioInternal(audioFileUri, playAudioOptions).block();
    }

    /**
     * Play audio in the call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
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
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        return callConnectionAsync
            .playAudioWithResponseInternal(audioFileUri, playAudioOptions, context)
            .block();
    }

    /**
     * Disconnect from a call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void hangup() {
        callConnectionAsync.hangup().block();
    }

    /**
     * Disconnect from a call.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> hangupWithResponse(Context context) {
        return callConnectionAsync.hangupWithResponse(context).block();
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful cancel all media operations request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CancelAllMediaOperationsResult cancelAllMediaOperations(String operationContext) {
        return callConnectionAsync.cancelAllMediaOperations(operationContext).block();
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful cancel all media operations request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CancelAllMediaOperationsResult> cancelAllMediaOperationsWithResponse(
        String operationContext,
        Context context) {
        return callConnectionAsync.cancelAllMediaOperationsWithResponse(operationContext, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param callBackUri callBackUri to get notifications.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantResult addParticipant(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext,
        URI callbackUri) {
        return callConnectionAsync.addParticipant(participant, alternateCallerId, operationContext, callbackUri).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param callBackUri callBackUri to get notifications.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantResult> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext,
        URI callbackUri,
        Context context) {
        return callConnectionAsync
            .addParticipantWithResponse(participant, alternateCallerId, operationContext, callbackUri, context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipant(CommunicationIdentifier participant) {
        callConnectionAsync.removeParticipant(participant).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participant The identifier of the participant.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(CommunicationIdentifier participant, Context context) {
        return callConnectionAsync.removeParticipantWithResponse(participant, context).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant The identifier of the participant.
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void transferCall(CommunicationIdentifier targetParticipant, String targetCallConnectionId, String userToUserInformation) {
        callConnectionAsync.transferCall(targetParticipant, targetCallConnectionId, userToUserInformation).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant The identifier of the participant.
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> transferCallWithResponse(CommunicationIdentifier targetParticipant, String targetCallConnectionId, String userToUserInformation, Context context) {
        return callConnectionAsync.transferCallWithResponse(targetParticipant, targetCallConnectionId, userToUserInformation, context).block();
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnectionProperties get() {
        return callConnectionAsync.get().block();
    }

    /**
     * Get call connection properties.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnectionProperties> getWithResponse(Context context) {
        return callConnectionAsync.getWithResponse(context).block();
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
        return callConnectionAsync.getParticipants().block();
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
    public Response<List<CallParticipant>> getParticipantsWithResponse(Context context) {
        return callConnectionAsync.getParticipantsWithResponse(context).block();
    }

    /**
     * Get participant of the call using participant id.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<CallParticipant> getParticipant(CommunicationIdentifier participant) {
        return callConnectionAsync.getParticipant(participant).block();
    }

    /**
     * Get participant of the call using participant id.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<CallParticipant>> getParticipantWithResponse(CommunicationIdentifier participant, Context context) {
        return callConnectionAsync.getParticipantWithResponse(participant, context).block();
    }

    /**
     * Keep call connection alive.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful keep alive request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void keepAlive() {
        return callConnectionAsync.keepAlive().block();
    }

    /**
     * Keep call connection alive.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful keep alive request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> keepAliveWithResponse(Context context) {
        return callConnectionAsync.keepAliveWithResponse(context).block();
    }

    /**
     * Play audio to a participant.
     *
     * @param participant The identifier of the participant.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudioToParticipant(
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantWithResponse(participant, audioFileUri, playAudioOptions, Context.NONE).getValue();
    }

    /**
     * Play audio to a participant.
     *
     * @param participant The identifier of the participant.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioToParticipantWithResponse(
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context
    ) {
        return callConnectionAsync.playAudioToParticipantWithResponseInternal(participant, audioFileUri, playAudioOptions, context).block();
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param participant The identifier of the participant.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void CancelParticipantMediaOperation(
        CommunicationIdentifier participant,
        String mediaOperationId) {
        return cancelParticipantMediaOperationWithResponse(participant, mediaOperationId, Context.NONE).getValue();
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param participant The identifier of the participant.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelParticipantMediaOperationWithResponse(
        CommunicationIdentifier participant,
        String mediaOperationId,
        Context context) {
        return callConnectionAsync.cancelParticipantMediaOperationWithResponseInternal(participant, mediaOperationId, context).block();
    }

    /**
     * Mute Participant.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void muteParticipant(
        CommunicationIdentifier participant) {
        return muteParticipantWithResponse(participant, Context.NONE).getValue();
    }

    /**
     * Mute Participant.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> muteParticipantWithResponse(
        CommunicationIdentifier participant,
        Context context) {
        return callConnectionAsync.muteParticipantWithResponseInternal(participant, context).block();
    }

    /**
     * Unmute Participant.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void unmuteParticipant(
        CommunicationIdentifier participant) {
        return unmuteParticipantWithResponse(participant, Context.NONE).getValue();
    }

    /**
     * Unmute Participant.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> unmuteParticipantWithResponse(
        CommunicationIdentifier participant,
        Context context) {
        return callConnectionAsync.unmuteParticipantWithResponseInternal(participant, context).block();
    }

    /**
     * Hold Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void holdParticipantMeetingAudio(
        CommunicationIdentifier participant) {
        return holdParticipantMeetingAudioWithResponse(participant, Context.NONE).getValue();
    }

    /**
     * Hold Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> holdParticipantMeetingAudioWithResponse(
        CommunicationIdentifier participant,
        Context context) {
        return callConnectionAsync.holdParticipantMeetingAudioWithResponseInternal(participant, context).block();
    }

    /**
     * Resume Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void resumeParticipantMeetingAudio(
        CommunicationIdentifier participant) {
        return resumeParticipantMeetingAudioWithResponse(participant, Context.NONE).getValue();
    }

    /**
     * Resume Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeParticipantMeetingAudioWithResponse(
        CommunicationIdentifier participant,
        Context context) {
        return callConnectionAsync.resumeParticipantMeetingAudioWithResponseInternal(participant, context).block();
    }

    /**
     * Create Audio Routing Group.
     *
     * @param audioRoutingMode The audio routing group mode.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createAudioRoutingGroup(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets) {
        createAudioRoutingGroupWithResponse(audioRoutingMode,targets, Context.NONE).getValue();
    }

    /**
     * Create Audio Routing Group.
     *
     * @param audioRoutingMode The audio routing group mode.
     * @param targets the targets value to set.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createAudioRoutingGroupWithResponse(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets,
        Context context) {
        return callConnectionAsync.createAudioRoutingGroupWithResponseInternal(audioRoutingMode, targets, context).block();
    }

    /**
     * Update Audio Routing Group.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateAudioRoutingGroup(
        String audioRoutingGroupId,
        List<CommunicationIdentifier> targets) {
            updateAudioRoutingGroupWithResponse(audioRoutingGroupId, targets, Context.NONE).getValue();
    }

    /**
     * Update Audio Routing Group.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @param targets the targets value to set.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateAudioRoutingGroupWithResponse(
        String audioRoutingGroupId,
        List<CommunicationIdentifier> targets,
        Context context) {
        return callConnectionAsync.updateAudioRoutingGroupWithResponseInternal(audioRoutingGroupId, targets, context).block();
    }

    /**
     * Delete Audio Routing Group.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateAudioRoutingGroup(
        String audioRoutingGroupId) {
            deleteAudioRoutingGroupWithResponse(audioRoutingGroupId, Context.NONE).getValue();
    }

    /**
     * Delete Audio Routing Group.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteAudioRoutingGroupWithResponse(
        String audioRoutingGroupId,
        Context context) {
        return callConnectionAsync.deleteAudioRoutingGroupWithResponseInternal(audioRoutingGroupId, context).block();
    }
}
