// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.HangUpOptions;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.MuteParticipantsOptions;
import com.azure.communication.callautomation.models.MuteParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * CallConnection for mid-call actions
 */
public class CallConnection {
    private final CallConnectionAsync callConnectionAsync;

    CallConnection(CallConnectionAsync callConnectionAsync) {
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnectionProperties getCallProperties() {
        return callConnectionAsync.getCallProperties().block();
    }

    /**
     * Get call connection properties.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnectionProperties> getCallPropertiesWithResponse(Context context) {
        return callConnectionAsync.getCallPropertiesWithResponseInternal(context).block();
    }

    /**
     * Hangup a call.
     *
     * @param isForEveryone determine if the call is handed up for all participants.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void hangUp(boolean isForEveryone) {
        return callConnectionAsync.hangUp(isForEveryone).block();
    }

    /**
     * Hangup a call.
     *
     * @param hangUpOptions options to hang up
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> hangUpWithResponse(HangUpOptions hangUpOptions, Context context) {
        return callConnectionAsync.hangUpWithResponseInternal(hangUpOptions, context).block();
    }

    /**
     * Get a specific participant.
     *
     * @param participantMri The participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallParticipant getParticipant(String participantMri) {
        return callConnectionAsync.getParticipant(participantMri).block();
    }

    /**
     * Get all participants.
     *
     * @param participantMri The participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallParticipant> getParticipantWithResponse(String participantMri, Context context) {
        return callConnectionAsync.getParticipantWithResponseInternal(participantMri, context).block();
    }

    /**
     * Get all participants.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ListParticipantsResult listParticipants() {
        return callConnectionAsync.listParticipants().block();
    }

    /**
     * Get all participants.
     *
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ListParticipantsResult> listParticipantsWithResponse(Context context) {
        return callConnectionAsync.listParticipantsWithResponseInternal(context).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetCallInvite A {@link CallInvite} representing the target participant of this transfer.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResult transferToParticipantCall(CallInvite targetCallInvite) {
        return callConnectionAsync.transferToParticipantCall(targetCallInvite).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param transferToParticipantCallOptions Options bag for transferToParticipantCall
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResult> transferToParticipantCallWithResponse(
        TransferToParticipantCallOptions transferToParticipantCallOptions, Context context) {
        return callConnectionAsync.transferToParticipantCallWithResponseInternal(transferToParticipantCallOptions, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant participant to invite.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantResult addParticipant(CallInvite participant) {
        return callConnectionAsync.addParticipant(participant).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param addParticipantOptions Options bag for addParticipant
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantResult> addParticipantWithResponse(AddParticipantOptions addParticipantOptions,
                                                                       Context context) {
        return callConnectionAsync.addParticipantWithResponseInternal(addParticipantOptions, context).block();
    }

    /**
     * Remove a participants from the call.
     *
     * @param participantToRemove participant to be removed.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RemoveParticipantResult removeParticipant(CommunicationIdentifier participantToRemove) {
        return callConnectionAsync.removeParticipant(participantToRemove).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param removeParticipantOptions The options for removing participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantResult> removeParticipantsWithResponse(RemoveParticipantOptions removeParticipantOptions, Context context) {
        return callConnectionAsync.removeParticipantWithResponseInternal(removeParticipantOptions, context).block();
    }

    /**
     * Mutes participants in the call.
     *
     * @param targetParticipant - Participant to be muted. Only ACS Users are currently supported.
     * @return A MuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MuteParticipantsResult muteParticipants(CommunicationIdentifier targetParticipant) {
        return callConnectionAsync.muteParticipantsAsync(targetParticipant).block();
    }

    /**
     * Mute participants in the call.
     * @param muteParticipantsOptions - Options for the request.
     * @param context A {@link Context} representing the request context.
     * @return a Response containing the MuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MuteParticipantsResult> muteParticipantsWithResponse(MuteParticipantsOptions muteParticipantsOptions, Context context) {
        return callConnectionAsync.muteParticipantWithResponseInternal(muteParticipantsOptions, context).block();
    }

    /**
     * Unmutes participants in the call.
     * @param targetParticipant - Participant to be unmuted. Only ACS Users are currently supported.
     * @return An UnmuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UnmuteParticipantsResult unmuteParticipants(CommunicationIdentifier targetParticipant) {
        return callConnectionAsync.unmuteParticipantsAsync(targetParticipant).block();
    }

    /**
     * Unmutes participants in the call.
     * @param unmuteParticipantsOptions - Options for the request.
     * @param context A {@link Context} representing the request context.
     * @return a Response containing the UnmuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UnmuteParticipantsResult> unmuteParticipantsWithResponse(UnmuteParticipantsOptions unmuteParticipantsOptions, Context context) {
        return callConnectionAsync.unmuteParticipantWithResponseInternal(unmuteParticipantsOptions, context).block();
    }

    //region Content management Actions
    /***
     * Returns an object of CallContent
     *
     * @return a CallContentAsync.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallMedia getCallMedia() {
        return new CallMedia(callConnectionAsync.getCallMediaAsync());
    }

    //endregion
}
