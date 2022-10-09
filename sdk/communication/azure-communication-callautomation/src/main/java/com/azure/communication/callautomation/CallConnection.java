// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.AddParticipantsOptions;
import com.azure.communication.callautomation.models.AddParticipantsResult;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.HangUpOptions;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantsOptions;
import com.azure.communication.callautomation.models.RemoveParticipantsResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

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
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResult transferToParticipantCall(CommunicationIdentifier targetParticipant) {
        return callConnectionAsync.transferToParticipantCall(targetParticipant).block();
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
     * @param participants The list of participants to invite.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantsResult addParticipants(List<CommunicationIdentifier> participants) {
        return callConnectionAsync.addParticipants(participants).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param addParticipantsOptions Options bag for addParticipants
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantsResult> addParticipantsWithResponse(AddParticipantsOptions addParticipantsOptions,
                                                                       Context context) {
        return callConnectionAsync.addParticipantsWithResponseInternal(addParticipantsOptions, context).block();
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RemoveParticipantsResult removeParticipants(List<CommunicationIdentifier> participantsToRemove) {
        return callConnectionAsync.removeParticipants(participantsToRemove).block();
    }

    /**
     * Remove a list of participant from the call.
     *
     * @param removeParticipantsOptions The options for removing participants.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResult> removeParticipantsWithResponse(RemoveParticipantsOptions removeParticipantsOptions, Context context) {
        return callConnectionAsync.removeParticipantsWithResponseInternal(removeParticipantsOptions, context).block();
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
