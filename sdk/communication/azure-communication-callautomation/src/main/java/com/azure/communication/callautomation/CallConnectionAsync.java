// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallConnectionsImpl;
import com.azure.communication.callautomation.implementation.CallMediasImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.AddParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.ListParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.MuteParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.RemoveParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.UnmuteParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.AddParticipantRequestInternal;
import com.azure.communication.callautomation.implementation.models.CustomContext;
import com.azure.communication.callautomation.implementation.models.MuteParticipantsRequestInternal;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantRequestInternal;
import com.azure.communication.callautomation.implementation.models.TransferToParticipantRequestInternal;
import com.azure.communication.callautomation.implementation.models.UnmuteParticipantsRequestInternal;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallParticipant;
import com.azure.communication.callautomation.models.AddParticipantOptions;
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
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallConnectionAsync for mid-call actions
 */
public class CallConnectionAsync {
    private final String callConnectionId;
    private final CallConnectionsImpl callConnectionInternal;
    private final CallMediasImpl callMediasInternal;
    private final ClientLogger logger;

    CallConnectionAsync(
        String callConnectionId,
        CallConnectionsImpl callConnectionInternal,
        CallMediasImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.callConnectionInternal = callConnectionInternal;
        this.callMediasInternal = contentsInternal;
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

            return (hangUpOptions.getIsForEveryone() ? callConnectionInternal.terminateCallWithResponseAsync(callConnectionId, context)
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
     * @param targetCallInvite A {@link CallInvite} representing the target participant of this transfer.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransferCallResult> transferToParticipantCall(CallInvite targetCallInvite) {
        return transferToParticipantCallWithResponse(new TransferToParticipantCallOptions(targetCallInvite)).flatMap(FluxUtil::toMono);
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
                .setTargetParticipant(CommunicationIdentifierConverter.convert(transferToParticipantCallOptions.getTargetCallInvite().getTarget()))
                .setTransfereeCallerId(PhoneNumberIdentifierConverter.convert(transferToParticipantCallOptions.getTargetCallInvite().getSourceCallIdNumber()))
                .setOperationContext(transferToParticipantCallOptions.getOperationContext());

            CallInvite callInvite = transferToParticipantCallOptions.getTargetCallInvite();

            if (callInvite.getSipHeaders() != null || callInvite.getVoipHeaders() != null) {
                request.setCustomContext(new CustomContext()
                            .setSipHeaders(callInvite.getSipHeaders())
                            .setVoipHeaders(callInvite.getVoipHeaders()));
            }

            return callConnectionInternal.transferToParticipantWithResponseAsync(callConnectionId, request,
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
     * @param participant participant to invite.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantResult> addParticipant(CallInvite participant) {
        return addParticipantWithResponse(new AddParticipantOptions(participant)).flatMap(FluxUtil::toMono);
    }

    /**
     * Add a participant to the call.
     *
     * @param addParticipantOptions Options bag for addParticipant
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddParticipantResult>> addParticipantWithResponse(AddParticipantOptions addParticipantOptions) {
        return withContext(context -> addParticipantWithResponseInternal(addParticipantOptions, context));
    }

    Mono<Response<AddParticipantResult>> addParticipantWithResponseInternal(AddParticipantOptions addParticipantOptions,
                                                                              Context context) {
        try {
            AddParticipantRequestInternal request = new AddParticipantRequestInternal()
                .setParticipantToAdd(CommunicationIdentifierConverter.convert(addParticipantOptions.getTargetCallInvite().getTarget()))
                .setSourceDisplayName(addParticipantOptions.getTargetCallInvite().getSourceDisplayName())
                .setSourceCallerIdNumber(PhoneNumberIdentifierConverter.convert(addParticipantOptions.getTargetCallInvite().getSourceCallIdNumber()))
                .setOperationContext(addParticipantOptions.getOperationContext());

            // Need to do a null check since it is optional; it might be a null and breaks the get function as well as type casting.
            if (addParticipantOptions.getInvitationTimeout() != null) {
                request.setInvitationTimeoutInSeconds((int) addParticipantOptions.getInvitationTimeout().getSeconds());
            }

            // Need to do a null check since SipHeaders and VoipHeaders are optional; If they both are null then we do not need to set custom context
            if (addParticipantOptions.getTargetCallInvite().getSipHeaders() != null || addParticipantOptions.getTargetCallInvite().getVoipHeaders() != null) {
                CustomContext customContext = new CustomContext();
                customContext.setSipHeaders(addParticipantOptions.getTargetCallInvite().getSipHeaders());
                customContext.setVoipHeaders(addParticipantOptions.getTargetCallInvite().getVoipHeaders());
                request.setCustomContext(customContext);
            }

            return callConnectionInternal.addParticipantWithResponseAsync(callConnectionId, request,
            context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> new SimpleResponse<>(response, AddParticipantResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantToRemove participant to remove.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RemoveParticipantResult> removeParticipant(CommunicationIdentifier participantToRemove) {
        return removeParticipantWithResponse(new RemoveParticipantOptions(participantToRemove)).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a participant from the call.
     *
     * @param removeParticipantOptions Options bag for removeParticipant
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RemoveParticipantResult>> removeParticipantWithResponse(RemoveParticipantOptions removeParticipantOptions) {
        return withContext(context -> removeParticipantWithResponseInternal(removeParticipantOptions, context));
    }

    Mono<Response<RemoveParticipantResult>> removeParticipantWithResponseInternal(RemoveParticipantOptions removeParticipantOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            RemoveParticipantRequestInternal request = new RemoveParticipantRequestInternal()
                .setParticipantToRemove(CommunicationIdentifierConverter.convert(removeParticipantOptions.getParticipant()))
                .setOperationContext(removeParticipantOptions.getOperationContext());

            return callConnectionInternal.removeParticipantWithResponseAsync(callConnectionId, request,
            context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> new SimpleResponse<>(response, RemoveParticipantResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Mutes participants in the call.
     * @param targetParticipant - Participant to be muted. Only ACS Users are currently supported.
     * @return A MuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MuteParticipantsResult> muteParticipantsAsync(CommunicationIdentifier targetParticipant) {
        return muteParticipantWithResponseInternal(
            new MuteParticipantsOptions(Collections.singletonList(targetParticipant)),
            null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Mute participants in the call.
     * @param muteParticipantsOptions - Options for the request.
     * @return a Response containing the MuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MuteParticipantsResult>> muteParticipantsWithResponse(MuteParticipantsOptions muteParticipantsOptions) {
        return withContext(context -> muteParticipantWithResponseInternal(muteParticipantsOptions, context));
    }

    Mono<Response<MuteParticipantsResult>> muteParticipantWithResponseInternal(MuteParticipantsOptions muteParticipantsOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            MuteParticipantsRequestInternal request = new MuteParticipantsRequestInternal()
                .setTargetParticipants(muteParticipantsOptions.getTargetParticipant().stream()
                    .map(CommunicationIdentifierConverter::convert)
                    .collect(Collectors.toList()))
                .setOperationContext(muteParticipantsOptions.getOperationContext());

            return callConnectionInternal.muteWithResponseAsync(
                    callConnectionId,
                    request,
                    context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(internalResponse -> new SimpleResponse<>(internalResponse, MuteParticipantsResponseConstructorProxy.create(internalResponse.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Unmutes participants in the call.
     * @param targetParticipant - Participant to be unmuted. Only ACS Users are currently supported.
     * @return An UnmuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UnmuteParticipantsResult> unmuteParticipantsAsync(CommunicationIdentifier targetParticipant) {
        return unmuteParticipantWithResponseInternal(
            new UnmuteParticipantsOptions(Collections.singletonList(targetParticipant)),
            null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Unmute participants in the call.
     * @param unmuteParticipantsOptions - Options for the request.
     * @return a Response containing the UnmuteParticipantsResult object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UnmuteParticipantsResult>> unmuteParticipantsWithResponse(UnmuteParticipantsOptions unmuteParticipantsOptions) {
        return withContext(context -> unmuteParticipantWithResponseInternal(unmuteParticipantsOptions, context));
    }

    Mono<Response<UnmuteParticipantsResult>> unmuteParticipantWithResponseInternal(UnmuteParticipantsOptions unmuteParticipantsOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            UnmuteParticipantsRequestInternal request = new UnmuteParticipantsRequestInternal()
                .setTargetParticipants(unmuteParticipantsOptions.getTargetParticipant().stream()
                    .map(CommunicationIdentifierConverter::convert)
                    .collect(Collectors.toList()))
                .setOperationContext(unmuteParticipantsOptions.getOperationContext());

            return callConnectionInternal.unmuteWithResponseAsync(
                    callConnectionId,
                    request,
                    context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(internalResponse -> new SimpleResponse<>(internalResponse, UnmuteParticipantsResponseConstructorProxy.create(internalResponse.getValue())));
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
        return new CallMediaAsync(callConnectionId, callMediasInternal);
    }
    //endregion
}
