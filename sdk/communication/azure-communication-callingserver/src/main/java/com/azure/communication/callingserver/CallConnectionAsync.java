// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.converters.AddParticipantResultConverter;
import com.azure.communication.callingserver.implementation.converters.AudioGroupResultConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CallConnectionPropertiesConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.converters.RemoveParticipantRequestConverter;
import com.azure.communication.callingserver.implementation.converters.TransferToCallRequestConverter;
import com.azure.communication.callingserver.implementation.converters.TransferToParticipantRequestConverter;
import com.azure.communication.callingserver.implementation.converters.TransferCallResultConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantRequest;
import com.azure.communication.callingserver.implementation.models.AudioGroupRequest;
import com.azure.communication.callingserver.implementation.models.CancelParticipantMediaOperationRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.GetParticipantRequest;
import com.azure.communication.callingserver.implementation.models.RemoveFromDefaultAudioGroupRequest;
import com.azure.communication.callingserver.implementation.models.MuteParticipantRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioToParticipantRequest;
import com.azure.communication.callingserver.implementation.models.AddToDefaultAudioGroupRequest;
import com.azure.communication.callingserver.implementation.models.TransferToCallRequest;
import com.azure.communication.callingserver.implementation.models.TransferToParticipantRequest;
import com.azure.communication.callingserver.implementation.models.UnmuteParticipantRequest;
import com.azure.communication.callingserver.implementation.models.UpdateAudioGroupRequest;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.AudioGroupResult;
import com.azure.communication.callingserver.models.AudioRoutingMode;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateAudioGroupResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.TransferCallResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

/**
 * Asynchronous client that supports call connection operations.
 */
public final class CallConnectionAsync {

    private final String callConnectionId;
    private final CallConnectionsImpl callConnectionInternal;
    private final ClientLogger logger = new ClientLogger(CallConnectionAsync.class);

    CallConnectionAsync(String callConnectionId, CallConnectionsImpl callConnectionInternal) {
        this.callConnectionId = callConnectionId;
        this.callConnectionInternal = callConnectionInternal;
    }

    /**
     * Get the call connection id property.
     *
     * @return Call connection id value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(URI audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioInternal(audioFileUri, playAudioOptions);

    }

    Mono<PlayAudioResult> playAudioInternal(URI audioFileUri, PlayAudioOptions playAudioOptions) {
        try {
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri.toString());
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId());
            }
            return playAudioInternal(request);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<PlayAudioResult> playAudioInternal(PlayAudioRequest playAudioRequest) {
        try {
            return callConnectionInternal.playAudioAsync(callConnectionId, playAudioRequest)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(PlayAudioResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(
        URI audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioWithResponseInternal(audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponseInternal(
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        try {
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri.toString());
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId());
            }
            return playAudioWithResponseInternal(request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponseInternal(
        PlayAudioRequest playAudioRequest,
        Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .playAudioWithResponseAsync(callConnectionId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
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
        try {
            return callConnectionInternal.hangupCallAsync(callConnectionId)
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
        return hangupWithResponse(Context.NONE);
    }

    Mono<Response<Void>> hangupWithResponse(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.hangupCallWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
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
    public Mono<Void> delete() {
        try {
            return callConnectionInternal.deleteCallAsync(callConnectionId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
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
    public Mono<Response<Void>> deleteWithResponse() {
        return deleteWithResponse(Context.NONE);
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.deleteCallWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload of the cancel all media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelAllMediaOperations() {
        try {
            return callConnectionInternal.cancelAllMediaOperationsAsync(callConnectionId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload of the cancel all media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelAllMediaOperationsWithResponse() {
        return cancelAllMediaOperationsWithResponse(Context.NONE);
    }

    Mono<Response<Void>> cancelAllMediaOperationsWithResponse(
        Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .cancelAllMediaOperationsWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when adding a phone number participant.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantResult> addParticipant(
        CommunicationIdentifier participant,
        PhoneNumberIdentifier alternateCallerId,
        String operationContext) {
        try {
            AddParticipantRequest request = getAddParticipantRequest(participant,
                alternateCallerId,
                operationContext
                );
            return callConnectionInternal.addParticipantAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(AddParticipantResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when adding a phone number participant.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        PhoneNumberIdentifier alternateCallerId,
        String operationContext) {
        return addParticipantWithResponse(participant, alternateCallerId, operationContext, Context.NONE);
    }

    Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        PhoneNumberIdentifier alternateCallerId,
        String operationContext,
        Context context) {
        try {
            AddParticipantRequest request = getAddParticipantRequest(participant,
                alternateCallerId,
                operationContext);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .addParticipantWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, AddParticipantResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(CommunicationIdentifier participant) {
        try {
            return callConnectionInternal.removeParticipantAsync(callConnectionId, RemoveParticipantRequestConverter.convert(participant))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(CommunicationIdentifier participant) {
        return removeParticipantWithResponse(participant, Context.NONE);
    }

    Mono<Response<Void>> removeParticipantWithResponse(CommunicationIdentifier participant, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .removeParticipantWithResponseAsync(callConnectionId, RemoveParticipantRequestConverter.convert(participant), contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call to a participant.
     *
     * @param targetParticipant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when transferring to a pstn participant.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransferCallResult> transferToParticipant(CommunicationIdentifier targetParticipant, PhoneNumberIdentifier alternateCallerId, String userToUserInformation, String operationContext) {
        try {
            TransferToParticipantRequest request = TransferToParticipantRequestConverter.convert(targetParticipant, alternateCallerId, userToUserInformation, operationContext);
            return callConnectionInternal.transferToParticipantAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(TransferCallResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call to a participant.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number identifier to use when transferring to a pstn participant.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResult>> transferToParticipantWithResponse(CommunicationIdentifier participant, PhoneNumberIdentifier alternateCallerId, String userToUserInformation, String operationContext) {
        return transferToParticipantWithResponse(participant, alternateCallerId, userToUserInformation, operationContext, Context.NONE);
    }

    Mono<Response<TransferCallResult>> transferToParticipantWithResponse(CommunicationIdentifier targetParticipant, PhoneNumberIdentifier alternateCallerId, String userToUserInformation, String operationContext, Context context) {
        try {
            TransferToParticipantRequest request = TransferToParticipantRequestConverter.convert(targetParticipant, alternateCallerId, userToUserInformation, operationContext);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .transferToParticipantWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, TransferCallResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call to another call.
     *
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransferCallResult> transferToCall(String targetCallConnectionId, String userToUserInformation, String operationContext) {
        try {
            TransferToCallRequest request = TransferToCallRequestConverter.convert(targetCallConnectionId, userToUserInformation, operationContext);
            return callConnectionInternal.transferToCallAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(TransferCallResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call to another call.
     *
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @param operationContext The operation context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResult>> transferToCallWithResponse(String targetCallConnectionId, String userToUserInformation, String operationContext) {
        return transferToCallWithResponse(targetCallConnectionId, userToUserInformation, operationContext, Context.NONE);
    }

    Mono<Response<TransferCallResult>> transferToCallWithResponse(String targetCallConnectionId, String userToUserInformation, String operationContext, Context context) {
        try {
            TransferToCallRequest request = TransferToCallRequestConverter.convert(targetCallConnectionId, userToUserInformation, operationContext);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .transferToCallWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, TransferCallResultConverter.convert(response.getValue())));
            });
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
    public Mono<CallConnectionProperties> getCall() {
        try {

            return callConnectionInternal.getCallAsync(callConnectionId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
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
        return getCallWithResponse(Context.NONE);
    }

    Mono<Response<CallConnectionProperties>> getCallWithResponse(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.getCallWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, CallConnectionPropertiesConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get participants of the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<List<CallParticipant>> getParticipants() {
        try {
            return callConnectionInternal.getParticipantsAsync(callConnectionId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(
                    result.stream().map(CallParticipantConverter::convert).collect(Collectors.toList())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get participants of the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<List<CallParticipant>>> getParticipantsWithResponse() {
        return getParticipantsWithResponse(Context.NONE);
    }

    Mono<Response<List<CallParticipant>>> getParticipantsWithResponse(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.getParticipantsWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response,
                            response.getValue()
                            .stream()
                            .map(CallParticipantConverter::convert)
                            .collect(Collectors.toList()
                        )
                    )
                );
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get participant from the call using identifier.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<CallParticipant> getParticipant(CommunicationIdentifier participant) {
        try {
            GetParticipantRequest request = new GetParticipantRequest().setIdentifier(CommunicationIdentifierConverter.convert(participant));
            return callConnectionInternal.getParticipantAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(CallParticipantConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get participant from the call using identifier.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<CallParticipant>> getParticipantWithResponse(CommunicationIdentifier participant) {
        return getParticipantWithResponse(participant, Context.NONE);
    }

    Mono<Response<CallParticipant>> getParticipantWithResponse(CommunicationIdentifier participant, Context context) {
        try {
            GetParticipantRequest request = new GetParticipantRequest().setIdentifier(CommunicationIdentifierConverter.convert(participant));
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.getParticipantWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response,
                            CallParticipantConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Keep call connection alive.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful keep alive request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> keepAlive() {
        try {
            return callConnectionInternal.keepAliveAsync(callConnectionId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Keep call connection alive.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful keep alive request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> keepAliveWithResponse() {
        return keepAliveWithResponse(null);
    }

    Mono<Response<Void>> keepAliveWithResponse(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.keepAliveWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio to a participant.
     *
     * @param participant The identifier of the participant.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudioToParticipant(
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantInternal(participant, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<PlayAudioResult> playAudioToParticipantInternal(CommunicationIdentifier participant, URI audioFileUri,
            PlayAudioOptions playAudioOptions, Context context) {
        try {
            PlayAudioToParticipantRequest playAudioToParticipantRequest = new PlayAudioToParticipantRequest()
                .setAudioFileUri(audioFileUri.toString())
                .setIdentifier(CommunicationIdentifierConverter.convert(participant));
            if (playAudioOptions != null) {
                playAudioToParticipantRequest
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId());
            }

            return callConnectionInternal.participantPlayAudioAsync(callConnectionId, playAudioToParticipantRequest, context)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(PlayAudioResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio to a participant.
     *
     * @param participant The identifier of the participant.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioToParticipantWithResponse(
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantWithResponseInternal(participant, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioToParticipantWithResponseInternal(CommunicationIdentifier participant, URI audioFileUri,
            PlayAudioOptions playAudioOptions, Context context) {
        try {
            PlayAudioToParticipantRequest playAudioToParticipantRequest = new PlayAudioToParticipantRequest()
                .setAudioFileUri(audioFileUri.toString())
                .setIdentifier(CommunicationIdentifierConverter.convert(participant));
            if (playAudioOptions != null) {
                playAudioToParticipantRequest
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId());
            }

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.participantPlayAudioWithResponseAsync(callConnectionId, playAudioToParticipantRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                    new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param participant The identifier of the participant.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelParticipantMediaOperation(
        CommunicationIdentifier participant,
        String mediaOperationId) {
        try {
            CancelParticipantMediaOperationRequest cancelParticipantMediaOperationRequest =
                new CancelParticipantMediaOperationRequest()
                    .setMediaOperationId(mediaOperationId)
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.cancelParticipantMediaOperationAsync(callConnectionId, cancelParticipantMediaOperationRequest, Context.NONE)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param participant The identifier of the participant.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelParticipantMediaOperationWithResponse(
        CommunicationIdentifier participant,
        String mediaOperationId) {
        return cancelParticipantMediaOperationWithResponseInternal(participant, mediaOperationId, Context.NONE);
    }

    Mono<Response<Void>> cancelParticipantMediaOperationWithResponseInternal(
        CommunicationIdentifier participant,
        String mediaOperationId,
        Context context) {
        try {
            CancelParticipantMediaOperationRequest cancelParticipantMediaOperationRequest =
                new CancelParticipantMediaOperationRequest()
                    .setMediaOperationId(mediaOperationId)
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.cancelParticipantMediaOperationWithResponseAsync(callConnectionId, cancelParticipantMediaOperationRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Mute Participant.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> muteParticipant(
        CommunicationIdentifier participant) {
        try {
            MuteParticipantRequest muteParticipantRequest =
                new MuteParticipantRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.muteParticipantAsync(callConnectionId, muteParticipantRequest, Context.NONE)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Mute Participant.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> muteParticipantWithResponse(
        CommunicationIdentifier participant) {
        return muteParticipantWithResponseInternal(participant, Context.NONE);
    }

    Mono<Response<Void>> muteParticipantWithResponseInternal(
        CommunicationIdentifier participant,
        Context context) {
        try {
            MuteParticipantRequest muteParticipantRequest =
                new MuteParticipantRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.muteParticipantWithResponseAsync(callConnectionId, muteParticipantRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Unmute Participant.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> unmuteParticipant(
        CommunicationIdentifier participant) {
        try {
            UnmuteParticipantRequest unmuteParticipantRequest =
                new UnmuteParticipantRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.unmuteParticipantAsync(callConnectionId, unmuteParticipantRequest, Context.NONE)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Unmute Participant.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> unmuteParticipantWithResponse(
        CommunicationIdentifier participant) {
        return unmuteParticipantWithResponseInternal(participant, Context.NONE);
    }

    Mono<Response<Void>> unmuteParticipantWithResponseInternal(
        CommunicationIdentifier participant,
        Context context) {
        try {
            UnmuteParticipantRequest unmuteParticipantRequest =
                new UnmuteParticipantRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.unmuteParticipantWithResponseAsync(callConnectionId, unmuteParticipantRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove Participant's From Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipantFromDefaultAudioGroup(
        CommunicationIdentifier participant) {
        try {
            RemoveFromDefaultAudioGroupRequest removeParticipantFromDefaultAudioGroupRequest =
                new RemoveFromDefaultAudioGroupRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.removeParticipantFromDefaultAudioGroupAsync(callConnectionId, removeParticipantFromDefaultAudioGroupRequest)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove Participant's From Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantFromDefaultAudioGroupWithResponse(
        CommunicationIdentifier participant) {
        return removeParticipantFromDefaultAudioGroupWithResponseInternal(participant, Context.NONE);
    }

    Mono<Response<Void>> removeParticipantFromDefaultAudioGroupWithResponseInternal(
        CommunicationIdentifier participant,
        Context context) {
        try {
            RemoveFromDefaultAudioGroupRequest removeParticipantFromDefaultAudioGroupRequest =
                new RemoveFromDefaultAudioGroupRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.removeParticipantFromDefaultAudioGroupWithResponseAsync(callConnectionId, removeParticipantFromDefaultAudioGroupRequest, context)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add Participant's To Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipantToDefaultAudioGroup(
        CommunicationIdentifier participant) {
        try {
            AddToDefaultAudioGroupRequest addParticipantToDefaultAudioGroupRequest =
                new AddToDefaultAudioGroupRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.addParticipantToDefaultAudioGroupAsync(callConnectionId, addParticipantToDefaultAudioGroupRequest)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add Participant's To Default Audio Group.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantToDefaultAudioGroupWithResponse(
        CommunicationIdentifier participant) {
        return addParticipantToDefaultAudioGroupWithResponseInternal(participant, Context.NONE);
    }

    Mono<Response<Void>> addParticipantToDefaultAudioGroupWithResponseInternal(
        CommunicationIdentifier participant,
        Context context) {
        try {
            AddToDefaultAudioGroupRequest addParticipantToDefaultAudioGroupRequest =
                new AddToDefaultAudioGroupRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.addParticipantToDefaultAudioGroupWithResponseAsync(callConnectionId, addParticipantToDefaultAudioGroupRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create Audio Group.
     *
     * @param audioRoutingMode The audio group mode.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateAudioGroupResult> createAudioGroup(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets) {
        try {
            AudioGroupRequest request = getAudioGroupRequest(audioRoutingMode, targets);
            return callConnectionInternal.createAudioGroupAsync(callConnectionId, request)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new CreateAudioGroupResult(result.getAudioGroupId())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create Audio Group.
     *
     * @param audioRoutingMode The audio group mode.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateAudioGroupResult>> createAudioGroupWithResponse(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets) {
        return createAudioGroupWithResponseInternal(audioRoutingMode, targets, Context.NONE);
    }

    Mono<Response<CreateAudioGroupResult>> createAudioGroupWithResponseInternal(AudioRoutingMode audioRoutingMode, List<CommunicationIdentifier> targets, Context context) {
        try {
            AudioGroupRequest request = getAudioGroupRequest(audioRoutingMode, targets);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .createAudioGroupWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new CreateAudioGroupResult(response.getValue().getAudioGroupId())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update Audio Group.
     *
     * @param audioGroupId The audio group id.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateAudioGroup(
        String audioGroupId,
        List<CommunicationIdentifier> targets) {
        try {
            UpdateAudioGroupRequest request = getUpdateAudioGroupRequest(audioGroupId, targets);
            return callConnectionInternal.updateAudioGroupAsync(callConnectionId, audioGroupId, request)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update Audio Group.
     *
     * @param audioGroupId The audio group id.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateAudioGroupWithResponse(
        String audioGroupId,
        List<CommunicationIdentifier> targets) {
        return updateAudioGroupWithResponseInternal(audioGroupId, targets, Context.NONE);
    }

    Mono<Response<Void>> updateAudioGroupWithResponseInternal(String audioGroupId, List<CommunicationIdentifier> targets, Context context) {
        try {
            UpdateAudioGroupRequest request = getUpdateAudioGroupRequest(audioGroupId, targets);
            return callConnectionInternal.updateAudioGroupWithResponseAsync(callConnectionId, audioGroupId, request, context)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get audio groups in a call.
     *
     * @param audioGroupId The audio group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AudioGroupResult> getAudioGroups(
        String audioGroupId) {
        try {
            return callConnectionInternal.getAudioGroupsAsync(callConnectionId, audioGroupId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(AudioGroupResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get audio groups in a call.
     *
     * @param audioGroupId The audio group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AudioGroupResult>> getAudioGroupsWithResponse(
        String audioGroupId) {
        return getAudioGroupsWithResponseInternal(audioGroupId, Context.NONE);
    }

    Mono<Response<AudioGroupResult>> getAudioGroupsWithResponseInternal(String audioGroupId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .getAudioGroupsWithResponseAsync(callConnectionId, audioGroupId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, AudioGroupResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete audio group from a call.
     *
     * @param audioGroupId The audio group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAudioGroup(
        String audioGroupId) {
        try {
            return callConnectionInternal.deleteAudioGroupAsync(callConnectionId, audioGroupId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete audio group from a call.
     *
     * @param audioGroupId The audio group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteAudioGroupWithResponse(
        String audioGroupId) {
        return deleteAudioGroupWithResponseInternal(audioGroupId, Context.NONE);
    }

    Mono<Response<Void>> deleteAudioGroupWithResponseInternal(String audioGroupId, Context context) {
        try {
            return callConnectionInternal.deleteAudioGroupWithResponseAsync(callConnectionId, audioGroupId, context)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private AddParticipantRequest getAddParticipantRequest(
        CommunicationIdentifier participant,
        PhoneNumberIdentifier alternateCallerId,
        String operationContext) {
        AddParticipantRequest request = new AddParticipantRequest()
            .setParticipant(CommunicationIdentifierConverter.convert(participant))
            .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(alternateCallerId))
            .setOperationContext(operationContext);
        return request;
    }

    private AudioGroupRequest getAudioGroupRequest(AudioRoutingMode audioRoutingMode, List<CommunicationIdentifier> targets) {
        AudioGroupRequest request = new AudioGroupRequest()
            .setAudioRoutingMode(audioRoutingMode)
            .setTargets(targets
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));
        return request;
    }

    private UpdateAudioGroupRequest getUpdateAudioGroupRequest(String audioGroupId, List<CommunicationIdentifier> targets) {
        UpdateAudioGroupRequest request = new UpdateAudioGroupRequest()
            .setTargets(targets
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));
        return request;
    }
}
