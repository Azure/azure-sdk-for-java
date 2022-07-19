// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.accesshelpers.AddParticipantsResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.ErrorConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.GetParticipantRequestInternal;
import com.azure.communication.callingserver.implementation.models.RemoveParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.TransferToParticipantRequestInternal;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsOptions;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.callingserver.models.TransferToParticipantCallOptions;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallConnectionAsync for mid-call actions
 */
public class CallConnectionAsync {
    private final String callConnectionId;
    private final CallConnectionsImpl callConnectionInternal;
    private final ClientLogger logger;

    CallConnectionAsync(String callConnectionId, CallConnectionsImpl callConnectionInternal) {
        this.callConnectionId = callConnectionId;
        this.callConnectionInternal = callConnectionInternal;
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
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .map(response -> {
                    try {
                        return new SimpleResponse<>(response, new CallConnectionProperties(response.getValue()));
                    } catch (URISyntaxException e) {
                        logger.logExceptionAsError(new RuntimeException(e));
                        return null;
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hangup a call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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

            return callConnectionInternal.hangupCallWithResponseAsync(callConnectionId, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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

            return callConnectionInternal.terminateCallWithResponseAsync(callConnectionId, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a specific participant.
     *
     * @param participant The participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallParticipant> getParticipant(CommunicationIdentifier participant) {
        return getParticipantWithResponse(participant).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a specific participant.
     *
     * @param participant The participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallParticipant>> getParticipantWithResponse(CommunicationIdentifier participant) {
        return withContext(context -> getParticipantWithResponseInternal(participant, context));
    }

    Mono<Response<CallParticipant>> getParticipantWithResponseInternal(CommunicationIdentifier participant,
                                                                       Context context) {
        try {
            context = context == null ? Context.NONE : context;

            GetParticipantRequestInternal getParticipantRequestInternal = new GetParticipantRequestInternal()
                .setParticipant(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.getParticipantWithResponseAsync(callConnectionId,
                    getParticipantRequestInternal, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
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
    public Mono<List<CallParticipant>> listParticipants() {
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
    public Mono<Response<List<CallParticipant>>> listParticipantsWithResponse() {
        return withContext(this::listParticipantsWithResponseInternal);
    }

    Mono<Response<List<CallParticipant>>> listParticipantsWithResponseInternal(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.getParticipantsWithResponseAsync(callConnectionId, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response,
                    response.getValue().stream().map(CallParticipantConverter::convert).collect(Collectors.toList())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<TransferCallResponse> transferToParticipantCall(
        TransferToParticipantCallOptions transferToParticipantCallOptions) {
        return transferToParticipantCallWithResponse(transferToParticipantCallOptions).flatMap(FluxUtil::toMono);
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
    public Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponse(
        TransferToParticipantCallOptions transferToParticipantCallOptions) {
        return withContext(context -> transferToParticipantCallWithResponseInternal(transferToParticipantCallOptions, context));
    }

    Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponseInternal(
        TransferToParticipantCallOptions transferToParticipantCallOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            TransferToParticipantRequestInternal request = new TransferToParticipantRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(transferToParticipantCallOptions.getTargetParticipant()))
                .setTransfereeCallerId(PhoneNumberIdentifierConverter.convert(transferToParticipantCallOptions.getTransfereeCallerId()))
                .setUserToUserInformation(transferToParticipantCallOptions.getUserToUserInformation())
                .setOperationContext(transferToParticipantCallOptions.getOperationContext());

            return callConnectionInternal.transferToParticipantWithResponseAsync(callConnectionId, request, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .map(response ->
                    new SimpleResponse<>(response, TransferCallResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<AddParticipantsResponse> addParticipants(AddParticipantsOptions addParticipantsOptions) {
        return addParticipantsWithResponse(addParticipantsOptions).flatMap(FluxUtil::toMono);
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
    public Mono<Response<AddParticipantsResponse>> addParticipantsWithResponse(AddParticipantsOptions addParticipantsOptions) {
        return withContext(context -> addParticipantsWithResponseInternal(addParticipantsOptions, context));
    }

    Mono<Response<AddParticipantsResponse>> addParticipantsWithResponseInternal(AddParticipantsOptions addParticipantsOptions,
                                                                                Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = addParticipantsOptions.getParticipants()
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            AddParticipantsRequestInternal request = new AddParticipantsRequestInternal()
                .setParticipantsToAdd(participantModels)
                .setSourceCallerId(PhoneNumberIdentifierConverter.convert(addParticipantsOptions.getSourceCallerId()))
                .setInvitationTimeoutInSeconds((int) addParticipantsOptions.getInvitationTimeout().getSeconds())
                .setOperationContext(addParticipantsOptions.getOperationContext());

            return callConnectionInternal.addParticipantWithResponseAsync(callConnectionId, request, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response, AddParticipantsResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<RemoveParticipantsResponse> removeParticipants(List<CommunicationIdentifier> participantsToRemove,
                                                               String operationContext) {
        return removeParticipantsWithResponse(participantsToRemove, operationContext).flatMap(FluxUtil::toMono);
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
    public Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponse(List<CommunicationIdentifier> participantsToRemove,
                                                                                     String operationContext) {
        return withContext(context -> removeParticipantsWithResponseInternal(participantsToRemove, operationContext, context));
    }

    Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponseInternal(List<CommunicationIdentifier> participantsToRemove,
                                                                                      String operationContext, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = participantsToRemove
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            RemoveParticipantsRequestInternal request = new RemoveParticipantsRequestInternal()
                .setParticipantsToRemove(participantModels)
                .setOperationContext(operationContext);

            return callConnectionInternal.removeParticipantsWithResponseAsync(callConnectionId, request, context)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response, new RemoveParticipantsResponse(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

}
