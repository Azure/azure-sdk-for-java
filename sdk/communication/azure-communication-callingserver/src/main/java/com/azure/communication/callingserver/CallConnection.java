// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import java.util.List;

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
    public PlayAudioResult playAudio(String audioFileUri, PlayAudioOptions playAudioOptions) {
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
        String audioFileUri,
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantResult addParticipant(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext) {
        return callConnectionAsync.addParticipant(participant, alternateCallerId, operationContext).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipant(String participantId) {
        callConnectionAsync.removeParticipant(participantId).block();
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
    public Response<Void> removeParticipantWithResponse(String participantId, Context context) {
        return callConnectionAsync.removeParticipantWithResponse(participantId, context).block();
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
        callConnectionAsync.removeParticipantById(participant).block();
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
    public Response<Void> removeParticipantByIdWithResponse(CommunicationIdentifier participant, Context context) {
        return callConnectionAsync.removeParticipantByIdWithResponse(participant, context).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant The target participant.
     * @param userToUserInformation The user to user information.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void transferToParticipant(CommunicationIdentifier targetParticipant, String userToUserInformation) {
        callConnectionAsync.transferToParticipant(targetParticipant, userToUserInformation).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant The target participant.
     * @param userToUserInformation The user to user information.
     * @param context {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> transferToParticipantWithResponse(CommunicationIdentifier targetParticipant, String userToUserInformation, Context context) {
        return callConnectionAsync.transferToParticipantWithResponse(targetParticipant, userToUserInformation, context).block();
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
     * @param participantId The participant id.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallParticipant getParticipant(String participantId) {
        return callConnectionAsync.getParticipant(participantId).block();
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
    public Response<CallParticipant> getParticipantWithResponse(String participantId, Context context) {
        return callConnectionAsync.getParticipantWithResponse(participantId, context).block();
    }

    /**
     * Get participant from the call using identifier.
     *
     * @param participant The identifier of the participant to be removed from the call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public List<CallParticipant> getParticipantById(CommunicationIdentifier participant) {
        return callConnectionAsync.getParticipantById(participant).block();
    }

    /**
     * Get participant from the call using identifier.
     *
     * @param participant The identifier of the participant to be removed from the call.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<List<CallParticipant>> getParticipantByIdWithResponse(CommunicationIdentifier participant, Context context) {
        return callConnectionAsync.getParticipantByIdWithResponse(participant, context).block();
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
        return callConnectionAsync.startHoldMusic(participantId).block();
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
    public Response<StartHoldMusicResult> startHoldMusicWithResponse(String participantId, Context context) {  
        return callConnectionAsync.startHoldMusicWithResponse(participantId, context).block();
    }

    /**
     * Hold the participant and play custom audio.
     *
     * @param participantId The participant id.
     * @param audioFileUri The uri of the audio file.
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive StartHoldMusic status notifications.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for start hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartHoldMusicResult startHoldMusic(String participantId, String audioFileUri, String audioFileId, String callbackUri) {  
        return callConnectionAsync.startHoldMusic(participantId, audioFileUri, audioFileId, callbackUri).block();
    }

    /**
     * Hold the participant and play custom audio.
     *
     * @param participantId The participant id.
     * @param audioFileUri The uri of the audio file.
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive StartHoldMusic status notifications.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for start hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartHoldMusicResult> startHoldMusicWithResponse(String participantId, String audioFileUri, String audioFileId, String callbackUri, Context context) {  
        return callConnectionAsync.startHoldMusicWithResponse(participantId, audioFileUri, audioFileId, callbackUri, context).block();
    }

    /**
     * Remove participant from the hold and stop playing audio.
     *
     * @param participantId The participant id.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for stop hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StopHoldMusicResult stopHoldMusic(String participantId) {  
        return callConnectionAsync.stopHoldMusic(participantId).block();
    }

    /**
     * Remove participant from the hold and stop playing audio.
     *
     * @param participantId The participant id.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for stop hold music operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StopHoldMusicResult> stopHoldMusicWithResponse(String participantId, Context context) {  
        return callConnectionAsync.stopHoldMusicWithResponse(participantId, context).block();
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
     * @param participantId The participant id.
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
    public PlayAudioResult PlayAudioToParticipant(
        String participantId,
        String audioFileUri,
        PlayAudioOptions playAudioOptions,        
        Context context
    ) {
        return callConnectionAsync.PlayAudioToParticipantInternal(participantId, audioFileUri, playAudioOptions, context).block();
    }

    /**
     * Play audio to a participant.
     *
     * @param participantId The participant id.
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
    public Response<PlayAudioResult> PlayAudioToParticipantWithResponse(
        String participantId,
        String audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context
    ) {
        return callConnectionAsync.PlayAudioToParticipantWithResponseInternal(participantId, audioFileUri, playAudioOptions, context).block();
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param participantId The participant id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void CancelParticipantMediaOperation(
        String participantId,
        String mediaOperationId,
        Context context
    ) {
        return callConnectionAsync.CancelParticipantMediaOperationWithResponseInternal(participantId, mediaOperationId, context).block().getValue();
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param participantId The participant id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> CancelParticipantMediaOperationWithResponse(
        String participantId,
        String mediaOperationId,
        Context context) {
        return callConnectionAsync.CancelParticipantMediaOperationWithResponseInternal(participantId, mediaOperationId, context).block();
    }
}