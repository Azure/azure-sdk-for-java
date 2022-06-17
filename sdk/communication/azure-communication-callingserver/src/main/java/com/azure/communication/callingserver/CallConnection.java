// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.callingserver.models.AcsCallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.GetCallResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
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
        return callConnectionAsync.getCallWithResponseInternal(context).block();
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
        return callConnectionAsync.hangupWithResponseInternal(context).block();
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
        return callConnectionAsync.terminateCallWithResponseInternal(context).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number. Optional
     * @param userToUserInformation The user to user information. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResponse transferToParticipantCall(CommunicationIdentifier targetParticipant,
                                                          PhoneNumberIdentifier transfereeCallerId,
                                                          String userToUserInformation, String operationContext) {
        return callConnectionAsync.transferToParticipantCall(
            targetParticipant, transfereeCallerId, userToUserInformation, operationContext).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number.
     * @param userToUserInformation The user to user information. Optional
     * @param operationContext The operation context. Optional
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResponse> transferToParticipantCallWithResponse(
        CommunicationIdentifier targetParticipant, PhoneNumberIdentifier transfereeCallerId,
        String userToUserInformation, String operationContext, Context context) {
        return callConnectionAsync.transferToParticipantCallWithResponseInternal(
            targetParticipant, transfereeCallerId, userToUserInformation, operationContext, context).block();
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
        return callConnectionAsync.getParticipantWithResponseInternal(participant, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param participants The participants to invite.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param invitationTimeoutInSeconds The timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantsResponse addParticipants(List<CommunicationIdentifier> participants,
                                                   PhoneNumberIdentifier sourceCallerId, Integer invitationTimeoutInSeconds,
                                                   String operationContext) {
        return callConnectionAsync.addParticipants(participants, sourceCallerId,
            invitationTimeoutInSeconds, operationContext).block();
    }

    /**
     * Add a participant to the call.
     *
     *
     * @param participants The participants to invite.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param invitationTimeoutInSeconds The timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds. Optional
     * @param operationContext The operation context. Optional
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantsResponse> addParticipantsWithResponse(List<CommunicationIdentifier> participants,
                                                                         PhoneNumberIdentifier sourceCallerId,
                                                                         Integer invitationTimeoutInSeconds,
                                                                         String operationContext,
                                                                         Context context) {
        return callConnectionAsync.addParticipantsWithResponseInternal(participants, sourceCallerId,
            invitationTimeoutInSeconds, operationContext, context).block();
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RemoveParticipantsResponse removeParticipants(List<CommunicationIdentifier> participantsToRemove,
                                                         String operationContext) {
        return callConnectionAsync.removeParticipants(participantsToRemove, operationContext).block();
    }

    /**
     * Remove a list of participant from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param operationContext The operation context. Optional
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResponse> removeParticipantsWithResponse(
        List<CommunicationIdentifier> participantsToRemove,
        String operationContext,
        Context context) {
        return callConnectionAsync.removeParticipantsWithResponseInternal(participantsToRemove, operationContext,
            context).block();
    }
}
