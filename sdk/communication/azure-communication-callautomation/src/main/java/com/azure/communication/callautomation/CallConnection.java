// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.MuteParticipantsOptions;
import com.azure.communication.callautomation.models.MuteParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantOptions;
import com.azure.communication.callautomation.models.RemoveParticipantResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferCallToParticipantOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsOptions;
import com.azure.communication.callautomation.models.UnmuteParticipantsResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.exception.HttpResponseException;

/**
 * CallConnection for mid-call actions
 */
public final class CallConnection {
    private final CallConnectionAsync callConnectionAsync;

    CallConnection(CallConnectionAsync callConnectionAsync) {
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Get call connection properties.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
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
     * @throws HttpResponseException thrown if the request is rejected by server.
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
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void hangUp(boolean isForEveryone) {
        callConnectionAsync.hangUp(isForEveryone).block();
    }

    /**
     * Hangup a call.
     *
     * @param isForEveryone determine if the call is handed up for all participants.
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with Void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> hangUpWithResponse(boolean isForEveryone, Context context) {
        return callConnectionAsync.hangUpWithResponseInternal(isForEveryone, context).block();
    }

    /**
     * Get a specific participant.
     *
     * @param targetParticipant The participant to retrieve.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return The desired call participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallParticipant getParticipant(CommunicationIdentifier targetParticipant) {
        return callConnectionAsync.getParticipant(targetParticipant).block();
    }

    /**
     * Get all participants.
     *
     * @param targetParticipant The participant to retrieve.
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with the desired call participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallParticipant> getParticipantWithResponse(CommunicationIdentifier targetParticipant, Context context) {
        return callConnectionAsync.getParticipantWithResponseInternal(targetParticipant, context).block();
    }

    /**
     * Get all participants.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A list of all participants in the call.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CallParticipant> listParticipants() {
        return new PagedIterable<>(callConnectionAsync.listParticipants());
    }

    /**
     * Get all participants.
     *
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with a list of all participants in the call.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CallParticipant> listParticipants(Context context) {
        return new PagedIterable<>(callConnectionAsync.listParticipantsWithContext(context));
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the targetParticipant participant of this transfer.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of transferring the call to a designated participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResult transferCallToParticipant(CommunicationIdentifier targetParticipant) {
        return callConnectionAsync.transferCallToParticipant(targetParticipant).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param transferCallToParticipantOptions Options bag for transferToParticipantCall
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of transferring the call to a designated participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResult> transferCallToParticipantWithResponse(
        TransferCallToParticipantOptions transferCallToParticipantOptions, Context context) {
        return callConnectionAsync.transferCallToParticipantWithResponseInternal(transferCallToParticipantOptions, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participant participant to invite.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of adding a participant.
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
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of adding a participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantResult> addParticipantWithResponse(AddParticipantOptions addParticipantOptions,
                                                                       Context context) {
        return callConnectionAsync.addParticipantWithResponseInternal(addParticipantOptions, context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantToRemove participant to be removed.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of removing a participant from the call
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
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of removing a participant from the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantResult> removeParticipantWithResponse(RemoveParticipantOptions removeParticipantOptions, Context context) {
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
     * @return a CallMediaAsync.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallMedia getCallMedia() {
        return new CallMedia(callConnectionAsync.getCallMediaAsync());
    }

    //endregion
}
