// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallConnectionsImpl;
import com.azure.communication.callautomation.implementation.ContentsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.AddParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.ListParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.RemoveParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantsRequestInternal;
import com.azure.communication.callautomation.implementation.models.TransferToParticipantRequestInternal;
import com.azure.communication.callautomation.models.AddParticipantsResult;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.AddParticipantsOptions;
import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.HangUpOptions;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantsOptions;
import com.azure.communication.callautomation.models.RemoveParticipantsResult;
import com.azure.communication.callautomation.models.RepeatabilityHeaders;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallConnectionAsync for mid-call actions
 */
public class CallConnectionAsync {
    private final String callConnectionId;
    private final CallConnectionsImpl callConnectionInternal;
    private final ContentsImpl contentsInternal;
    private final ClientLogger logger;

    CallConnectionAsync(
        String callConnectionId,
        CallConnectionsImpl callConnectionInternal,
        ContentsImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.callConnectionInternal = callConnectionInternal;
        this.contentsInternal = contentsInternal;
        this.logger = new ClientLogger(CallConnectionAsync.class);
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionProperties> getCallProperties() {
        return getCallPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionProperties>> getCallPropertiesWithResponse() {
        return withContext(this::getCallPropertiesWithResponseInternal);
    }

    Mono<Response<CallConnectionProperties>> getCallPropertiesWithResponseInternal(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.getCallWithResponseAsync(callConnectionId, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> {
                    try {
                        return new SimpleResponse<>(response, CallConnectionPropertiesConstructorProxy.create(response.getValue()));
                    } catch (URISyntaxException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<Void> hangUp(boolean isForEveryone) {
        HangUpOptions hangUpOptions = new HangUpOptions(isForEveryone);
        return hangUpWithResponse(hangUpOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Hangup a call.
     *
     * @param hangUpOptions options to hang up
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> hangUpWithResponse(HangUpOptions hangUpOptions) {
        return withContext(context -> hangUpWithResponseInternal(hangUpOptions, context));
    }

    Mono<Response<Void>> hangUpWithResponseInternal(HangUpOptions hangUpOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            if (hangUpOptions.getRepeatabilityHeaders() == null) {
                RepeatabilityHeaders autoRepeatabilityHeaders = new RepeatabilityHeaders(UUID.randomUUID(), Instant.now());
                hangUpOptions.setRepeatabilityHeaders(autoRepeatabilityHeaders);
            }

            return (hangUpOptions.getIsForEveryone() ? callConnectionInternal.terminateCallWithResponseAsync(callConnectionId,
                hangUpOptions.getRepeatabilityHeaders().getRepeatabilityRequestId(),
                hangUpOptions.getRepeatabilityHeaders().getRepeatabilityFirstSentInHttpDateFormat(),
                context)
                : callConnectionInternal.hangupCallWithResponseAsync(callConnectionId, context))
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a specific participant.
     *
     * @param participantMri MRI of the participants to retrieve.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallParticipant> getParticipant(String participantMri) {
        return getParticipantWithResponse(participantMri).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a specific participant.
     *
     * @param participantMri MRI of the participants to retrieve.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallParticipant>> getParticipantWithResponse(String participantMri) {
        return withContext(context -> getParticipantWithResponseInternal(participantMri, context));
    }

    Mono<Response<CallParticipant>> getParticipantWithResponseInternal(String participantMri, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.getParticipantWithResponseAsync(callConnectionId, participantMri, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response ->
                    new SimpleResponse<>(response, CallParticipantConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get all participants.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ListParticipantsResult> listParticipants() {
        return listParticipantsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get all participants.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ListParticipantsResult>> listParticipantsWithResponse() {
        return withContext(this::listParticipantsWithResponseInternal);
    }

    Mono<Response<ListParticipantsResult>> listParticipantsWithResponseInternal(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.getParticipantsWithResponseAsync(callConnectionId, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> new SimpleResponse<>(response,
                    ListParticipantsResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<TransferCallResult> transferToParticipantCall(CommunicationIdentifier targetParticipant) {
        return transferToParticipantCallWithResponse(new TransferToParticipantCallOptions(targetParticipant)).flatMap(FluxUtil::toMono);
    }

    /**
     * Transfer the call to a participant.
     *
     * @param transferToParticipantCallOptions Options bag for transferToParticipantCall
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResult>> transferToParticipantCallWithResponse(
        TransferToParticipantCallOptions transferToParticipantCallOptions) {
        return withContext(context -> transferToParticipantCallWithResponseInternal(transferToParticipantCallOptions, context));
    }

    Mono<Response<TransferCallResult>> transferToParticipantCallWithResponseInternal(
        TransferToParticipantCallOptions transferToParticipantCallOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            TransferToParticipantRequestInternal request = new TransferToParticipantRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(transferToParticipantCallOptions.getTargetParticipant()))
                .setTransfereeCallerId(PhoneNumberIdentifierConverter.convert(transferToParticipantCallOptions.getTransfereeCallerId()))
                .setUserToUserInformation(transferToParticipantCallOptions.getUserToUserInformation())
                .setOperationContext(transferToParticipantCallOptions.getOperationContext());

            if (transferToParticipantCallOptions.getRepeatabilityHeaders() == null) {
                RepeatabilityHeaders autoRepeatabilityHeaders = new RepeatabilityHeaders(UUID.randomUUID(), Instant.now());
                transferToParticipantCallOptions.setRepeatabilityHeaders(autoRepeatabilityHeaders);
            }

            return callConnectionInternal.transferToParticipantWithResponseAsync(callConnectionId, request,
                    transferToParticipantCallOptions.getRepeatabilityHeaders().getRepeatabilityRequestId(),
                    transferToParticipantCallOptions.getRepeatabilityHeaders().getRepeatabilityFirstSentInHttpDateFormat(),
                    context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response ->
                    new SimpleResponse<>(response, TransferCallResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<AddParticipantsResult> addParticipants(List<CommunicationIdentifier> participants) {
        return addParticipantsWithResponse(new AddParticipantsOptions(participants)).flatMap(FluxUtil::toMono);
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
    public Mono<Response<AddParticipantsResult>> addParticipantsWithResponse(AddParticipantsOptions addParticipantsOptions) {
        return withContext(context -> addParticipantsWithResponseInternal(addParticipantsOptions, context));
    }

    Mono<Response<AddParticipantsResult>> addParticipantsWithResponseInternal(AddParticipantsOptions addParticipantsOptions,
                                                                              Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = addParticipantsOptions.getParticipants()
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            AddParticipantsRequestInternal request = new AddParticipantsRequestInternal()
                .setParticipantsToAdd(participantModels)
                .setSourceCallerId(PhoneNumberIdentifierConverter.convert(addParticipantsOptions.getSourceCallerId()))
                .setOperationContext(addParticipantsOptions.getOperationContext());

            // Need to do a null check since it is optional; it might be a null and breaks the get function as well as type casting.
            if (addParticipantsOptions.getInvitationTimeout() != null) {
                request.setInvitationTimeoutInSeconds((int) addParticipantsOptions.getInvitationTimeout().getSeconds());
            }

            if (addParticipantsOptions.getRepeatabilityHeaders() == null) {
                RepeatabilityHeaders autoRepeatabilityHeaders = new RepeatabilityHeaders(UUID.randomUUID(), Instant.now());
                addParticipantsOptions.setRepeatabilityHeaders(autoRepeatabilityHeaders);
            }

            return callConnectionInternal.addParticipantWithResponseAsync(callConnectionId, request,
                    addParticipantsOptions.getRepeatabilityHeaders().getRepeatabilityRequestId(),
                    addParticipantsOptions.getRepeatabilityHeaders().getRepeatabilityFirstSentInHttpDateFormat(),
                    context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> new SimpleResponse<>(response, AddParticipantsResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<RemoveParticipantsResult> removeParticipants(List<CommunicationIdentifier> participantsToRemove) {
        return removeParticipantsWithResponse(new RemoveParticipantsOptions(participantsToRemove)).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param removeParticipantsOptions The options for removing participants.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RemoveParticipantsResult>> removeParticipantsWithResponse(RemoveParticipantsOptions removeParticipantsOptions) {
        return withContext(context -> removeParticipantsWithResponseInternal(removeParticipantsOptions, context));
    }

    Mono<Response<RemoveParticipantsResult>> removeParticipantsWithResponseInternal(RemoveParticipantsOptions removeParticipantsOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = removeParticipantsOptions.getParticipants()
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            if (removeParticipantsOptions.getRepeatabilityHeaders() == null) {
                RepeatabilityHeaders autoRepeatabilityHeaders = new RepeatabilityHeaders(UUID.randomUUID(), Instant.now());
                removeParticipantsOptions.setRepeatabilityHeaders(autoRepeatabilityHeaders);
            }

            RemoveParticipantsRequestInternal request = new RemoveParticipantsRequestInternal()
                .setParticipantsToRemove(participantModels)
                .setOperationContext(removeParticipantsOptions.getOperationContext());

            return callConnectionInternal.removeParticipantsWithResponseAsync(callConnectionId, request,
                    removeParticipantsOptions.getRepeatabilityHeaders().getRepeatabilityRequestId(),
                    removeParticipantsOptions.getRepeatabilityHeaders().getRepeatabilityFirstSentInHttpDateFormat(),
                    context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> new SimpleResponse<>(response, RemoveParticipantsResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    //region Content management Actions
    /***
     * Returns an object of CallContentAsync
     *
     * @return a CallContentAsync.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallMediaAsync getCallMediaAsync() {
        return new CallMediaAsync(callConnectionId, contentsInternal);
    }
    //endregion
}
