// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import java.net.URI;
import java.util.List;

import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.AudioGroupResult;
import com.azure.communication.callingserver.models.AudioRoutingMode;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateAudioGroupResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.TransferCallResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
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
     * Terminates the conversation for all participants in the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        callConnectionAsync.delete().block();
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Context context) {
        return callConnectionAsync.deleteWithResponse(context).block();
    }

    /**
     * Cancel all media operations in the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelAllMediaOperations() {
        callConnectionAsync.cancelAllMediaOperations().block();
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful cancel all media operations request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelAllMediaOperationsWithResponse(
        Context context) {
        return callConnectionAsync.cancelAllMediaOperationsWithResponse(context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param alternateCallerId Phone number identifier to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantResult addParticipant(
        CommunicationIdentifier participant,
        PhoneNumberIdentifier alternateCallerId,
        String operationContext) {
        return callConnectionAsync.addParticipant(participant, alternateCallerId, operationContext).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param alternateCallerId Phone number identifier to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantResult> addParticipantWithResponse(
        CommunicationIdentifier participant,
        PhoneNumberIdentifier alternateCallerId,
        String operationContext,
        Context context) {
        return callConnectionAsync
            .addParticipantWithResponse(participant, alternateCallerId, operationContext, context).block();
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
     * @param alternateCallerId The phone number identifier to use when transferring to a pstn participant.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResult transferToParticipant(CommunicationIdentifier targetParticipant, PhoneNumberIdentifier alternateCallerId, String userToUserInformation, String operationContext) {
        return callConnectionAsync.transferToParticipant(targetParticipant, alternateCallerId, userToUserInformation, operationContext).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when transferring to a pstn participant.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResult> transferToParticipantWithResponse(CommunicationIdentifier targetParticipant, PhoneNumberIdentifier alternateCallerId, String userToUserInformation, String operationContext, Context context) {
        return callConnectionAsync.transferToParticipantWithResponse(targetParticipant,  alternateCallerId, userToUserInformation, operationContext, context).block();
    }

    /**
     * Transfer the call to another call.
     *
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResult transferToCall(String targetCallConnectionId, String userToUserInformation, String operationContext) {
        return callConnectionAsync.transferToCall(targetCallConnectionId, userToUserInformation, operationContext).block();
    }

    /**
     * Transfer the call to another call..
     *
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResult> transferToCallWithResponse(String targetCallConnectionId, String userToUserInformation, String operationContext, Context context) {
        return callConnectionAsync.transferToCallWithResponse(targetCallConnectionId, userToUserInformation, operationContext, context).block();
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnectionProperties getCall() {
        return callConnectionAsync.getCall().block();
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
    public Response<CallConnectionProperties> getCallWithResponse(Context context) {
        return callConnectionAsync.getCallWithResponse(context).block();
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
    public CallParticipant getParticipant(CommunicationIdentifier participant) {
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
    public Response<CallParticipant> getParticipantWithResponse(CommunicationIdentifier participant, Context context) {
        return callConnectionAsync.getParticipantWithResponse(participant, context).block();
    }

    /**
     * Keep call connection alive.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void keepAlive() {
        callConnectionAsync.keepAlive().block();
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelParticipantMediaOperation(
        CommunicationIdentifier participant,
        String mediaOperationId) {
        cancelParticipantMediaOperationWithResponse(participant, mediaOperationId, Context.NONE).getValue();
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void muteParticipant(
        CommunicationIdentifier participant) {
        muteParticipantWithResponse(participant, Context.NONE).getValue();
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void unmuteParticipant(
        CommunicationIdentifier participant) {
        unmuteParticipantWithResponse(participant, Context.NONE).getValue();
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
     * Remove Participant's From Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipantFromDefaultAudioGroup(
        CommunicationIdentifier participant) {
        removeParticipantFromDefaultAudioGroupWithResponse(participant, Context.NONE).getValue();
    }

    /**
     * Remove Participant's From Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantFromDefaultAudioGroupWithResponse(
        CommunicationIdentifier participant,
        Context context) {
        return callConnectionAsync.removeParticipantFromDefaultAudioGroupWithResponseInternal(participant, context).block();
    }

    /**
     *  Add Participant's To Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addParticipantToDefaultAudioGroup(
        CommunicationIdentifier participant) {
        addParticipantToDefaultAudioGroupWithResponse(participant, Context.NONE).getValue();
    }

    /**
     * Add Participant's To Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantToDefaultAudioGroupWithResponse(
        CommunicationIdentifier participant,
        Context context) {
        return callConnectionAsync.addParticipantToDefaultAudioGroupWithResponseInternal(participant, context).block();
    }

    /**
     * Create Audio Group.
     *
     * @param audioRoutingMode The audio routing group mode.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CreateAudioGroupResult createAudioGroup(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets) {
        return createAudioGroupWithResponse(audioRoutingMode, targets, Context.NONE).getValue();
    }

    /**
     * Create Audio Group.
     *
     * @param audioRoutingMode The audio routing group mode.
     * @param targets the targets value to set.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CreateAudioGroupResult> createAudioGroupWithResponse(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets,
        Context context) {
        return callConnectionAsync.createAudioGroupWithResponseInternal(audioRoutingMode, targets, context).block();
    }

    /**
     * Update Audio Group.
     *
     * @param audioGroupId The audio group id.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateAudioGroup(
        String audioGroupId,
        List<CommunicationIdentifier> targets) {
        updateAudioGroupWithResponse(audioGroupId, targets, Context.NONE).getValue();
    }

    /**
     * Update Audio Group.
     *
     * @param audioGroupId The audio group id.
     * @param targets the targets value to set.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateAudioGroupWithResponse(
        String audioGroupId,
        List<CommunicationIdentifier> targets,
        Context context) {
        return callConnectionAsync.updateAudioGroupWithResponseInternal(audioGroupId, targets, context).block();
    }

    /**
     * Get audio groups in a call.
     *
     * @param audioGroupId The audio group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AudioGroupResult getAudioGroups(
        String audioGroupId) {
        return getAudioGroupsWithResponse(audioGroupId, Context.NONE).getValue();
    }

    /**
     * Get audio groups in a call.
     *
     * @param audioGroupId The audio group id.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AudioGroupResult> getAudioGroupsWithResponse(
        String audioGroupId,
        Context context) {
        return callConnectionAsync.getAudioGroupsWithResponseInternal(audioGroupId, context).block();
    }

    /**
     * Delete Audio Group.
     *
     * @param audioGroupId The audio group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteAudioGroup(
        String audioGroupId) {
        deleteAudioGroupWithResponse(audioGroupId, Context.NONE).getValue();
    }

    /**
     * Delete Audio Group.
     *
     * @param audioGroupId The audio group id.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteAudioGroupWithResponse(
        String audioGroupId,
        Context context) {
        return callConnectionAsync.deleteAudioGroupWithResponseInternal(audioGroupId, context).block();
    }
}
