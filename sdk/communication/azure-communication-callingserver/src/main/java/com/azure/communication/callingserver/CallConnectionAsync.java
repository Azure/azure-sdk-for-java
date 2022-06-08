// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.converters.PagedConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CallConnectionPropertiesConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.TransferCallRequest;
import com.azure.communication.callingserver.implementation.models.TerminateCallRequest;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.callingserver.models.TransferCallOptions;
import com.azure.communication.common.PhoneNumberIdentifier;

import reactor.core.publisher.Mono;

/**
 * Asynchronous client that supports call connection operations.
 */
public final class CallConnectionAsync {

    private final String callLegId;
    private final CallConnectionsImpl callConnectionInternal;
    private final ClientLogger logger;

    CallConnectionAsync(String callLegId, CallConnectionsImpl callConnectionInternal) {
        this.callLegId = callLegId;
        this.callConnectionInternal = callConnectionInternal;
        this.logger = new ClientLogger(CallConnectionAsync.class);
    }

    /**
     * Get the callLegId property, which is the call connection id.
     *
     * @return callLegId value.
     */
    public String getCallLegId() {
        return callLegId;
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionProperties> getCall() {
        return getCall(null);
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
    public Mono<CallConnectionProperties> getCall(Context context) {
        try {
            return (context == null ? callConnectionInternal.getCallAsync(callLegId)
                : callConnectionInternal.getCallAsync(callLegId, context)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException))
                .flatMap(result -> Mono.just(CallConnectionPropertiesConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get call connection properties.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionProperties>> getCallWithResponse() {
        return getCallWithResponse(null);
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
    public Mono<Response<CallConnectionProperties>> getCallWithResponse(Context context) {
        try {
            return (context == null ? callConnectionInternal.getCallWithResponseAsync(callLegId)
                : callConnectionInternal.getCallWithResponseAsync(callLegId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response ->
                    new SimpleResponse<>(response, CallConnectionPropertiesConverter.convert(response.getValue())));
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
        return hangup(null);
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
    public Mono<Void> hangup(Context context) {
        try {
            return (context == null ? callConnectionInternal.hangUpCallAsync(callLegId)
                : callConnectionInternal.hangUpCallAsync(callLegId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
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
    public Mono<Response<Void>> hangupWithResponse() {
        return hangupWithResponse(null);
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
    public Mono<Response<Void>> hangupWithResponse(Context context) {
        try {
            return (context == null ? callConnectionInternal.hangUpCallWithResponseAsync(callLegId)
                : callConnectionInternal.hangUpCallWithResponseAsync(callLegId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param reason A {@link String} representing the reason of termination.
     * @param callbackUri A {@link String} representing the callback uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> terminateCall(String reason, String callbackUri) {
        return terminateCall(reason, callbackUri, null);
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param reason A {@link String} representing the reason of termination.
     * @param callbackUri A {@link String} representing the callback uri.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> terminateCall(String reason, String callbackUri, Context context) {
        try {
            TerminateCallRequest request = new TerminateCallRequest()
                .setReason(reason)
                .setCallbackUri(callbackUri);
            return (context == null ? callConnectionInternal.terminateCallAsync(callLegId, request)
                : callConnectionInternal.terminateCallAsync(callLegId, request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param reason A {@link String} representing the reason of termination.
     * @param callbackUri A {@link String} representing the callback uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> terminateCallWithResponse(String reason, String callbackUri) {
        return terminateCallWithResponse(reason, callbackUri, null);
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param reason A {@link String} representing the reason of termination.
     * @param callbackUri A {@link String} representing the callback uri.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> terminateCallWithResponse(String reason, String callbackUri, Context context) {
        try {
            TerminateCallRequest request = new TerminateCallRequest()
                .setReason(reason)
                .setCallbackUri(callbackUri);
            return (context == null ? callConnectionInternal.terminateCallWithResponseAsync(callLegId, request)
                : callConnectionInternal.terminateCallWithResponseAsync(callLegId, request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param targetCallLegId A {@link String} representing the call leg id of the target call.
     * @param callbackUri A {@link String} representing the callback uri.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> transferCall(CommunicationIdentifier targetParticipant, String targetCallLegId,
                                   String callbackUri, TransferCallOptions options) {
        return transferCall(targetParticipant, targetCallLegId, callbackUri, options, null);
    }

    /**
     * Transfer the call
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param targetCallLegId A {@link String} representing the call leg id of the target call.
     * @param callbackUri A {@link String} representing the callback uri.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> transferCall(CommunicationIdentifier targetParticipant, String targetCallLegId,
                                   String callbackUri, TransferCallOptions options, Context context) {
        try {
            TransferCallRequest request = new TransferCallRequest()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
                .setTargetCallLegId(targetCallLegId)
                .setCallbackUri(callbackUri)
                .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(options.getAlternateCallerId()))
                .setUserToUserInformation(options.getUserToUserInformation());
            return (context == null ? callConnectionInternal.transferCallAsync(callLegId, request)
                : callConnectionInternal.transferCallAsync(callLegId, request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param targetCallLegId A {@link String} representing the call leg id of the target call.
     * @param callbackUri A {@link String} representing the callback uri.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> transferCallWithResponse(CommunicationIdentifier targetParticipant, String targetCallLegId,
                                                         String callbackUri, TransferCallOptions options) {
        return transferCallWithResponse(targetParticipant, targetCallLegId, callbackUri, options, null);
    }

    /**
     * Transfer the call
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param targetCallLegId A {@link String} representing the call leg id of the target call.
     * @param callbackUri A {@link String} representing the callback uri.
     * @param options A {@link TransferCallOptions} representing the options of the transfer
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> transferCallWithResponse(CommunicationIdentifier targetParticipant, String targetCallLegId,
                                                  String callbackUri, TransferCallOptions options, Context context) {
        try {
            TransferCallRequest request = new TransferCallRequest()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
                .setTargetCallLegId(targetCallLegId)
                .setCallbackUri(callbackUri)
                .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(options.getAlternateCallerId()))
                .setUserToUserInformation(options.getUserToUserInformation());
            return (context == null ? callConnectionInternal.transferCallWithResponseAsync(callLegId, request)
                : callConnectionInternal.transferCallWithResponseAsync(callLegId, request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a specific participant.
     *
     * @param participantId The id of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallParticipant> getParticipant(String participantId) {
        return getParticipant(participantId, null);
    }

    /**
     * Get a specific participant.
     *
     * @param participantId The id of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallParticipant> getParticipant(String participantId, Context context) {
        try {
            return (context == null ? callConnectionInternal.getParticipantAsync(callLegId, participantId)
                : callConnectionInternal.getParticipantAsync(callLegId, participantId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(CallParticipantConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a specific participant.
     *
     * @param participantId The id of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallParticipant>> getParticipantWithResponse(String participantId) {
        return getParticipantWithResponse(participantId, null);
    }

    /**
     * Get a specific participant.
     *
     * @param participantId The id of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallParticipant>> getParticipantWithResponse(String participantId, Context context) {
        try {
            return (context == null ? callConnectionInternal.getParticipantWithResponseAsync(callLegId, participantId)
                : callConnectionInternal.getParticipantWithResponseAsync(callLegId, participantId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response ->
                    new SimpleResponse<>(response,
                        CallParticipantConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get all participants.
     * @param maxPageSize The max element of each page.
     * @param continuationToken The continuation token.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CallParticipant> getParticipants(int maxPageSize, String continuationToken) {
        return getParticipants(maxPageSize, continuationToken, null);
    }

    /**
     * Get all participants.
     * @param maxPageSize The max element of each page.
     * @param continuationToken The continuation token.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CallParticipant> getParticipants(int maxPageSize, String continuationToken, Context context) {
        return context == null ? PagedConverter.mapPage(callConnectionInternal.listParticipantAsync(callLegId, maxPageSize, continuationToken), item -> CallParticipantConverter.convert(item))
            : PagedConverter.mapPage(callConnectionInternal.listParticipantAsync(callLegId, maxPageSize, continuationToken, context), item -> CallParticipantConverter.convert(item));
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when adding a phone number participant.
     * @param callBackUri The call back URI.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(CommunicationIdentifier participant, PhoneNumberIdentifier alternateCallerId,
        String callBackUri, String operationContext) {
        return addParticipant(participant, alternateCallerId, callBackUri, operationContext, null);
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when adding a phone number participant.
     * @param callBackUri The call back URI.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(CommunicationIdentifier participant, PhoneNumberIdentifier alternateCallerId,
                                     String callBackUri, String operationContext, Context context) {
        try {
            AddParticipantRequest request = new AddParticipantRequest()
                .setParticipant(CommunicationIdentifierConverter.convert(participant))
                .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(alternateCallerId))
                .setCallbackUri(callBackUri)
                .setOperationContext(operationContext);
            return (context == null ? callConnectionInternal.addParticipantAsync(callLegId, request)
                : callConnectionInternal.addParticipantAsync(callLegId, request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when adding a phone number participant.
     * @param callBackUri The call back URI.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(CommunicationIdentifier participant,
                                                           PhoneNumberIdentifier alternateCallerId,
                                                           String callBackUri, String operationContext) {
        return addParticipantWithResponse(participant, alternateCallerId, callBackUri, operationContext, null);
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when adding a phone number participant.
     * @param callBackUri The call back URI.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(CommunicationIdentifier participant,
                                                    PhoneNumberIdentifier alternateCallerId,
                                                    String callBackUri, String operationContext, Context context) {
        try {
            AddParticipantRequest request = new AddParticipantRequest()
                .setParticipant(CommunicationIdentifierConverter.convert(participant))
                .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(alternateCallerId))
                .setCallbackUri(callBackUri)
                .setOperationContext(operationContext);
            return (context == null ? callConnectionInternal.addParticipantWithResponseAsync(callLegId, request)
                : callConnectionInternal.addParticipantWithResponseAsync(callLegId, request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String participantId) {
        return removeParticipant(participantId, null);
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String participantId, Context context) {
        try {
            return (context == null ? callConnectionInternal.removeParticipantAsync(callLegId, participantId)
                : callConnectionInternal.removeParticipantAsync(callLegId, participantId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String participantId) {
        return removeParticipantWithResponse(participantId, null);
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String participantId, Context context) {
        try {
            return (context == null ? callConnectionInternal.removeParticipantWithResponseAsync(callLegId, participantId)
                : callConnectionInternal.removeParticipantWithResponseAsync(callLegId, participantId, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
