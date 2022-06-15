// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AddParticipantsOptions;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.callingserver.models.TransferCallOptions;
import com.azure.communication.callingserver.models.AcsCallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.GetCallResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Synchronous Client that supports call connection operations.
 */
public final class CallConnection {
    private final CallConnectionAsync callConnectionAsync;

    CallConnection(CallConnectionAsync callConnectionAsync) {
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Get the callConnectionId property, which is the call connection id.
     *
     * @return callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionAsync.getCallConnectionId();
    }

    /**
     * Get call connection properties.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public GetCallResponse getCall() {
        return callConnectionAsync.getCall().block();
    }

    /**
     * Get call connection properties.
     *
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<GetCallResponse> getCallWithResponse(Context context) {
        return callConnectionAsync.getCallWithResponse(context).block();
    }

    /**
     * Hangup a call.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void hangup() {
        return callConnectionAsync.hangup().block();
    }

    /**
     * Hangup a call.
     *
     * @param context A {@link Context} representing the request context.
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
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void terminateCall() {
        return callConnectionAsync.terminateCall().block();
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> terminateCallWithResponse(Context context) {
        return callConnectionAsync.terminateCallWithResponse(context).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResponse transferToParticipantCall(CommunicationIdentifier targetParticipant,
                                                          TransferCallOptions options) {
        return callConnectionAsync.transferToParticipantCall(targetParticipant, options).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResponse> transferToParticipantCallWithResponse(
        CommunicationIdentifier targetParticipant, TransferCallOptions options, Context context) {
        return callConnectionAsync.transferToParticipantCallWithResponse(targetParticipant, options, context).block();
    }

    /**
     * Get a specific participant.
     *
     * @param participant The participant.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AcsCallParticipant getParticipant(CommunicationIdentifier participant) {
        return callConnectionAsync.getParticipant(participant).block();
    }

    /**
     * Get a specific participant.
     *
     * @param participant The participant.
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcsCallParticipant> getParticipantWithResponse(CommunicationIdentifier participant,
                                                                         Context context) {
        return callConnectionAsync.getParticipantWithResponse(participant, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participants The participants to invite.
     * @param addParticipantsOptions Options of adding participants
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantsResponse addParticipants(List<CommunicationIdentifier> participants,
                                                   AddParticipantsOptions addParticipantsOptions) {
        return callConnectionAsync.addParticipants(participants, addParticipantsOptions).block();
    }

    /**
     * Add a participant to the call.
     *
     *
     * @param participants The participants to invite.
     * @param addParticipantsOptions Options of adding participants
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantsResponse> addParticipantsWithResponse(List<CommunicationIdentifier> participants,
                                                                         AddParticipantsOptions addParticipantsOptions,
                                                                         Context context) {
        return callConnectionAsync.addParticipantsWithResponse(participants, addParticipantsOptions, context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param removeParticipantsOptions The options of removing participants.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RemoveParticipantsResponse removeParticipant(List<CommunicationIdentifier> participantsToRemove,
                                                        RemoveParticipantsOptions removeParticipantsOptions) {
        return callConnectionAsync.removeParticipant(participantsToRemove, removeParticipantsOptions).block();
    }

    /**
     * Remove a list of participant from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param removeParticipantsOptions The options of removing participants.
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResponse> removeParticipantsWithResponse(
        List<CommunicationIdentifier> participantsToRemove,
        RemoveParticipantsOptions removeParticipantsOptions,
        Context context) {
        return callConnectionAsync.removeParticipantsWithResponse(participantsToRemove, removeParticipantsOptions, context)
            .block();
    }
}
