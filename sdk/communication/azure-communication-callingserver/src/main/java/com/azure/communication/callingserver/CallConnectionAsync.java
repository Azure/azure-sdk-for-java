// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import com.azure.communication.callingserver.implementation.converters.AddParticipantResponseConverter;
import com.azure.communication.callingserver.implementation.converters.RemoveParticipantsResponseConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.GetCallResponseConverter;
import com.azure.communication.callingserver.implementation.converters.TransferCallResponseConverter;
import com.azure.communication.callingserver.implementation.converters.AcsCallParticipantConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.GetParticipantRequestInternal;
import com.azure.communication.callingserver.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.RemoveParticipantsRequestInternal;
import com.azure.communication.callingserver.models.AcsCallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsOptions;
import com.azure.communication.callingserver.models.GetCallResponse;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.callingserver.models.TransferCallOptions;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsOptions;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.models.TransferToParticipantRequestInternal;
import com.azure.communication.common.CommunicationIdentifier;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Asynchronous client that supports call connection operations.
 */
public final class CallConnectionAsync {

    private final String callConnectionId;
    private final CallConnectionsImpl callConnectionInternal;
    private final ClientLogger logger;

    CallConnectionAsync(String callConnectionId, CallConnectionsImpl callConnectionInternal) {
        this.callConnectionId = callConnectionId;
        this.callConnectionInternal = callConnectionInternal;
        this.logger = new ClientLogger(CallConnectionAsync.class);
    }

    /**
     * Get the callConnectionId property, which is the call connection id.
     *
     * @return callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Get call connection properties.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetCallResponse> getCall() {
        return getCallWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get call connection properties.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GetCallResponse>> getCallWithResponse() {
        return withContext(this::getCallWithResponseInternal);
    }

    Mono<Response<GetCallResponse>> getCallWithResponseInternal(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.getCallWithResponseAsync(callConnectionId, context).map(response ->
                new SimpleResponse<>(response, GetCallResponseConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hangup a call.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> hangup() {
        return hangupWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Hangup a call.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> hangupWithResponse() {
        return withContext(this::hangupWithResponseInternal);
    }

    Mono<Response<Void>> hangupWithResponseInternal(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.hangupCallWithResponseAsync(callConnectionId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> terminateCall() {
        return terminateCallWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> terminateCallWithResponse() {
        return withContext(this::terminateCallWithResponseInternal);
    }

    Mono<Response<Void>> terminateCallWithResponseInternal(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.terminateCallWithResponseAsync(callConnectionId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<TransferCallResponse> transferToParticipantCall(CommunicationIdentifier targetParticipant,
                                                TransferCallOptions options) {
        return transferToParticipantCallWithResponse(targetParticipant, options).flatMap(FluxUtil::toMono);
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponse(
        CommunicationIdentifier targetParticipant, TransferCallOptions options) {
        return withContext(context -> transferToParticipantCallWithResponseInternal(targetParticipant, options, context));
    }

    Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponseInternal(
        CommunicationIdentifier targetParticipant, TransferCallOptions options, Context context) {
        try {
            Objects.requireNonNull(targetParticipant, "The targetParticipant parameter cannot be null.");
            context = context == null ? Context.NONE : context;

            TransferToParticipantRequestInternal request = new TransferToParticipantRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant));
            if (options != null) {
                request.setTransfereeCallerId(PhoneNumberIdentifierConverter.convert(options.getTransfereeCallerId()))
                    .setUserToUserInformation(options.getUserToUserInformation())
                    .setOperationContext(options.getOperationContext());
            }

            return callConnectionInternal.transferToParticipantWithResponseAsync(callConnectionId, request, context)
                .map(response ->
                    new SimpleResponse<>(response, TransferCallResponseConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a specific participant.
     *
     * @param participant The participant.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcsCallParticipant> getParticipant(CommunicationIdentifier participant) {
        return getParticipantWithResponse(participant).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a specific participant.
     *
     * @param participant The participant.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcsCallParticipant>> getParticipantWithResponse(CommunicationIdentifier participant) {
        return withContext(context -> getParticipantWithResponseInternal(participant, context));
    }

    Mono<Response<AcsCallParticipant>> getParticipantWithResponseInternal(CommunicationIdentifier participant,
                                                                          Context context) {
        try {
            Objects.requireNonNull(participant, "The participant parameter cannot be null.");
            context = context == null ? Context.NONE : context;

            GetParticipantRequestInternal getParticipantRequestInternal = new GetParticipantRequestInternal()
                .setParticipant(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.getParticipantWithResponseAsync(callConnectionId,
                    getParticipantRequestInternal, context).map(response ->
                    new SimpleResponse<>(response, AcsCallParticipantConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<AddParticipantsResponse> addParticipants(List<CommunicationIdentifier> participants,
                                                        AddParticipantsOptions addParticipantsOptions) {
        return addParticipantsWithResponse(participants, addParticipantsOptions).flatMap(FluxUtil::toMono);
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
    public Mono<Response<AddParticipantsResponse>> addParticipantsWithResponse(
        List<CommunicationIdentifier> participants, AddParticipantsOptions addParticipantsOptions) {
        return withContext(context -> addParticipantsWithResponseInternal(participants, addParticipantsOptions, context));
    }

    Mono<Response<AddParticipantsResponse>> addParticipantsWithResponseInternal(List<CommunicationIdentifier> participants,
                                                                                AddParticipantsOptions addParticipantsOptions,
                                                                                Context context) {
        try {
            Objects.requireNonNull(participants, "The participants parameter cannot be null.");
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = null;
            for (CommunicationIdentifier participant : participants) {
                participantModels.add(CommunicationIdentifierConverter.convert(participant));
            }

            AddParticipantsRequestInternal request = new AddParticipantsRequestInternal()
                .setParticipantsToAdd(participantModels);
            if (addParticipantsOptions != null) {
                request.setSourceCallerId(PhoneNumberIdentifierConverter.convert(addParticipantsOptions.getSourceCallerId()))
                    .setInvitationTimeoutInSeconds(addParticipantsOptions.getInvitationTimeoutInSeconds())
                    .setOperationContext(addParticipantsOptions.getOperationContext());
            }

            return callConnectionInternal.addParticipantWithResponseAsync(callConnectionId, request, context).map(
                response -> new SimpleResponse<>(response, AddParticipantResponseConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param removeParticipantsOptions The options for remove participants.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RemoveParticipantsResponse> removeParticipants(List<CommunicationIdentifier> participantsToRemove,
                                                               RemoveParticipantsOptions removeParticipantsOptions) {
        return removeParticipantsWithResponse(participantsToRemove, removeParticipantsOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param removeParticipantsOptions The options for remove participants.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponse(
        List<CommunicationIdentifier> participantsToRemove,
        RemoveParticipantsOptions removeParticipantsOptions) {
        return withContext(context -> removeParticipantsWithResponseInternal(participantsToRemove,
            removeParticipantsOptions, context));
    }

    Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponseInternal(
        List<CommunicationIdentifier> participantsToRemove, RemoveParticipantsOptions removeParticipantsOptions,
        Context context) {
        try {
            Objects.requireNonNull(participantsToRemove, "The participantsToRemove parameter cannot be null.");
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = null;
            for (CommunicationIdentifier participant : participantsToRemove) {
                participantModels.add(CommunicationIdentifierConverter.convert(participant));
            }

            RemoveParticipantsRequestInternal request = new RemoveParticipantsRequestInternal()
                .setParticipantsToRemove(participantModels);
            if (removeParticipantsOptions != null) {
                request.setOperationContext(removeParticipantsOptions.getOperationContext());
            }

            return callConnectionInternal.removeParticipantsWithResponseAsync(callConnectionId, request, context).map(
                response -> new SimpleResponse<>(response, RemoveParticipantsResponseConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
