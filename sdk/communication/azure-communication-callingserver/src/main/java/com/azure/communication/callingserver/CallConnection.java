// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsOptions;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.ListParticipantsResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.callingserver.models.TransferToParticipantCallOptions;
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> terminateCallWithResponse(Context context) {
        return callConnectionAsync.terminateCallWithResponseInternal(context).block();
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
    public ListParticipantsResponse listParticipants() {
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
    public Response<ListParticipantsResponse> listParticipantsWithResponse(Context context) {
        return callConnectionAsync.listParticipantsWithResponseInternal(context).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param transferToParticipantCallOptions Options bag for transferToParticipantCall
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public TransferCallResponse transferToParticipantCall(TransferToParticipantCallOptions transferToParticipantCallOptions) {
        return callConnectionAsync.transferToParticipantCall(transferToParticipantCallOptions).block();
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
    public Response<TransferCallResponse> transferToParticipantCallWithResponse(
        TransferToParticipantCallOptions transferToParticipantCallOptions, Context context) {
        return callConnectionAsync.transferToParticipantCallWithResponseInternal(transferToParticipantCallOptions, context).block();
    }

    /**
     * Add a participant to the call.
     *
     * @param addParticipantsOptions Options bag for addParticipants
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantsResponse addParticipants(AddParticipantsOptions addParticipantsOptions) {
        return callConnectionAsync.addParticipants(addParticipantsOptions).block();
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
    public Response<AddParticipantsResponse> addParticipantsWithResponse(AddParticipantsOptions addParticipantsOptions,
                                                                         Context context) {
        return callConnectionAsync.addParticipantsWithResponseInternal(addParticipantsOptions, context).block();
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param operationContext The operation context. Optional
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResponse> removeParticipantsWithResponse(List<CommunicationIdentifier> participantsToRemove,
                                                                               String operationContext, Context context) {
        return callConnectionAsync.removeParticipantsWithResponseInternal(participantsToRemove, operationContext, context).block();
    }
}
