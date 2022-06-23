// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.AcsCallParticipantConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.GetParticipantRequestInternal;
import com.azure.communication.callingserver.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.RemoveParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.CreateCallResponseInternal;
import com.azure.communication.callingserver.implementation.models.AnswerCallResponseInternal;
import com.azure.communication.callingserver.implementation.models.TransferToParticipantRequestInternal;
import com.azure.communication.callingserver.implementation.models.GetCallResponseInternal;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.models.AcsCallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.CallConnectionState;


import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Asynchronous client that supports call connection operations.
 */
public final class CallConnectionAsync {
    private final String callConnectionId;
    private final CallConnectionsImpl callConnectionInternal;
    private CommunicationIdentifier source;
    private PhoneNumberIdentifier alternateCallerId;
    private List<CommunicationIdentifier> targets;
    private CallConnectionState callConnectionState;
    private String subject;
    private String callbackUri;
    private final ClientLogger logger;

    CallConnectionAsync(GetCallResponseInternal getCallResponseInternal, CallConnectionsImpl callConnectionInternal) {
        this.callConnectionId = getCallResponseInternal.getCallConnectionId();
        this.source = CommunicationIdentifierConverter.convert(getCallResponseInternal.getSource());
        this.alternateCallerId = PhoneNumberIdentifierConverter.convert(getCallResponseInternal.getAlternateCallerId());
        this.targets = null;
        getCallResponseInternal.getTargets().forEach(target -> this.targets.add(CommunicationIdentifierConverter.convert(target)));
        this.callConnectionState = CallConnectionState.fromString(getCallResponseInternal.getCallConnectionState().toString());
        this.subject = getCallResponseInternal.getSubject();
        this.callbackUri = getCallResponseInternal.getCallbackUri();
        this.callConnectionInternal = callConnectionInternal;
        this.logger = new ClientLogger(CallConnectionAsync.class);
    }

    CallConnectionAsync(CreateCallResponseInternal callResponseInternal, CallConnectionsImpl callConnectionInternal) {
        this.callConnectionId = callResponseInternal.getCallConnectionId();
        this.callConnectionInternal = callConnectionInternal;
        this.logger = new ClientLogger(CallConnectionAsync.class);
    }

    CallConnectionAsync(AnswerCallResponseInternal answerCallResponse, CallConnectionsImpl callConnectionInternal) {
        this.callConnectionId = answerCallResponse.getCallConnectionId();
        this.callConnectionInternal = callConnectionInternal;
        this.logger = new ClientLogger(CallConnectionAsync.class);
    }

    /**
     * Get the source property.
     *
     * @return source value.
     */
    public CommunicationIdentifier getSource() {
        return source;
    }

    /**
     * Set the source property.
     *
     * @param source The source.
     * @return CallConnectionAsync itself.
     */
    public CallConnectionAsync setSource(CommunicationIdentifier source) {
        this.source = source;
        return this;
    }

    /**
     * Get the targets property.
     *
     * @return list of targets
     */
    public List<CommunicationIdentifier> getTargets() {
        return targets;
    }

    /**
     * Set the targets property.
     *
     * @param targets The targets.
     * @return CallConnectionAsync itself.
     */
    public CallConnectionAsync setTargets(List<CommunicationIdentifier> targets) {
        this.targets = targets;
        return this;
    }

    /**
     * Get the alternateCallerId property.
     *
     * @return alternateCallerId value.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return alternateCallerId;
    }

    /**
     * Set the alternateCallerId property.
     *
     * @param alternateCallerId The alternateCallerId.
     * @return CallConnectionAsync itself.
     */
    public CallConnectionAsync setAlternateCallerId(PhoneNumberIdentifier alternateCallerId) {
        this.alternateCallerId = alternateCallerId;
        return this;
    }

    /**
     * Get the callConnectionState property.
     *
     * @return callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return callConnectionState;
    }

    /**
     * Set the callConnectionState property.
     *
     * @param callConnectionState The callConnectionState.
     * @return CallConnectionAsync itself.
     */
    public CallConnectionAsync setCallConnectionState(CallConnectionState callConnectionState) {
        this.callConnectionState = callConnectionState;
        return this;
    }

    /**
     * Get the subject property.
     *
     * @return subject value.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the subject property.
     *
     * @param subject The subject.
     * @return subject value.
     */
    public CallConnectionAsync setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the callbackUri property.
     *
     * @return callbackUri value.
     */
    public String getCallbackUri() {
        return callbackUri;
    }

    /**
     * Set the callbackUri property.
     *
     * @param callbackUri The callbackUri.
     * @return callbackUri value.
     */
    public CallConnectionAsync setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
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
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number. Optional
     * @param userToUserInformation The user to user information. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransferCallResponse> transferToParticipantCall(CommunicationIdentifier targetParticipant,
                                                                PhoneNumberIdentifier transfereeCallerId,
                                                                String userToUserInformation, String operationContext) {
        return transferToParticipantCallWithResponse(
            targetParticipant, transfereeCallerId, userToUserInformation, operationContext).flatMap(FluxUtil::toMono);
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
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponse(
        CommunicationIdentifier targetParticipant, PhoneNumberIdentifier transfereeCallerId, String userToUserInformation,
        String operationContext) {
        return withContext(context -> transferToParticipantCallWithResponseInternal(
            targetParticipant, transfereeCallerId, userToUserInformation, operationContext, context));
    }

    Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponseInternal(
        CommunicationIdentifier targetParticipant, PhoneNumberIdentifier transfereeCallerId, String userToUserInformation,
        String operationContext, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            TransferToParticipantRequestInternal request = new TransferToParticipantRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
                .setTransfereeCallerId(PhoneNumberIdentifierConverter.convert(transfereeCallerId))
                .setUserToUserInformation(userToUserInformation)
                .setOperationContext(operationContext);

            return callConnectionInternal.transferToParticipantWithResponseAsync(callConnectionId, request, context)
                .map(response ->
                    new SimpleResponse<>(response, new TransferCallResponse(response.getValue())));
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
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param invitationTimeoutInSeconds The timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantsResponse> addParticipants(List<CommunicationIdentifier> participants,
                                                         PhoneNumberIdentifier sourceCallerId,
                                                         Integer invitationTimeoutInSeconds,
                                                         String operationContext) {
        return addParticipantsWithResponse(participants, sourceCallerId, invitationTimeoutInSeconds,
            operationContext).flatMap(FluxUtil::toMono);
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
    public Mono<Response<AddParticipantsResponse>> addParticipantsWithResponse(
        List<CommunicationIdentifier> participants, PhoneNumberIdentifier sourceCallerId,
        Integer invitationTimeoutInSeconds, String operationContext) {
        return withContext(context -> addParticipantsWithResponseInternal(participants, sourceCallerId,
            invitationTimeoutInSeconds, operationContext, context));
    }

    Mono<Response<AddParticipantsResponse>> addParticipantsWithResponseInternal(List<CommunicationIdentifier> participants,
                                                                                PhoneNumberIdentifier sourceCallerId,
                                                                                Integer invitationTimeoutInSeconds,
                                                                                String operationContext,
                                                                                Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = participants
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            AddParticipantsRequestInternal request = new AddParticipantsRequestInternal()
                .setParticipantsToAdd(participantModels)
                .setSourceCallerId(PhoneNumberIdentifierConverter.convert(sourceCallerId))
                .setInvitationTimeoutInSeconds(invitationTimeoutInSeconds)
                .setOperationContext(operationContext);

            return callConnectionInternal.addParticipantWithResponseAsync(callConnectionId, request, context).map(
                response -> new SimpleResponse<>(response, new AddParticipantsResponse(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<RemoveParticipantsResponse> removeParticipants(List<CommunicationIdentifier> participantsToRemove,
                                                               String operationContext) {
        return removeParticipantsWithResponse(participantsToRemove, operationContext).flatMap(FluxUtil::toMono);
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
    public Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponse(
        List<CommunicationIdentifier> participantsToRemove,
        String operationContext) {
        return withContext(context -> removeParticipantsWithResponseInternal(participantsToRemove, operationContext,
            context));
    }

    Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponseInternal(
        List<CommunicationIdentifier> participantsToRemove, String operationContext, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = participantsToRemove
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            RemoveParticipantsRequestInternal request = new RemoveParticipantsRequestInternal()
                .setParticipantsToRemove(participantModels)
                .setOperationContext(operationContext);

            return callConnectionInternal.removeParticipantsWithResponseAsync(callConnectionId, request, context).map(
                response -> new SimpleResponse<>(response, new RemoveParticipantsResponse(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
