// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.converters.AudioRoutingGroupResultConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CallConnectionPropertiesConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.converters.RemoveParticipantRequestConverter;
import com.azure.communication.callingserver.implementation.converters.TransferCallRequestConverter;
import com.azure.communication.callingserver.implementation.converters.TransferCallResultConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantRequest;
import com.azure.communication.callingserver.implementation.models.AudioRoutingGroupRequest;
import com.azure.communication.callingserver.implementation.models.CancelParticipantMediaOperationRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.GetParticipantRequest;
import com.azure.communication.callingserver.implementation.models.HoldMeetingAudioRequest;
import com.azure.communication.callingserver.implementation.models.MuteParticipantRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioToParticipantRequest;
import com.azure.communication.callingserver.implementation.models.ResumeMeetingAudioRequest;
import com.azure.communication.callingserver.implementation.models.TransferCallRequest;
import com.azure.communication.callingserver.implementation.models.UnmuteParticipantRequest;
import com.azure.communication.callingserver.implementation.models.UpdateAudioRoutingGroupRequest;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.AudioRoutingGroupResult;
import com.azure.communication.callingserver.models.AudioRoutingMode;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateAudioRoutingGroupResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.TransferCallResult;
import com.azure.communication.common.CommunicationIdentifier;
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
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantResult> addParticipant(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext) {
        try {
            AddParticipantRequest request = getAddParticipantRequest(participant,
                alternateCallerId,
                operationContext
                );
            return callConnectionInternal.addParticipantAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new AddParticipantResult(result.getParticipantId())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant The identifier of the participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext) {
        return addParticipantWithResponse(participant, alternateCallerId, operationContext, Context.NONE);
    }

    Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String alternateCallerId,
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
                        new SimpleResponse<>(response, new AddParticipantResult(response.getValue().getParticipantId())));
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
     * Transfer the call.
     *
     * @param targetParticipant The identifier of the participant.
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransferCallResult> transfer(CommunicationIdentifier targetParticipant, String targetCallConnectionId, String userToUserInformation) {
        try {
            TransferCallRequest request = TransferCallRequestConverter.convert(targetParticipant, targetCallConnectionId, userToUserInformation);
            return callConnectionInternal.transferAsync(callConnectionId, request)
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
     * @param targetCallConnectionId The target call connection id to transfer to.
     * @param userToUserInformation The user to user information.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful transfer to participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResult>> transferWithResponse(CommunicationIdentifier participant, String targetCallConnectionId, String userToUserInformation) {
        return transferWithResponse(participant, userToUserInformation, targetCallConnectionId, Context.NONE);
    }

    Mono<Response<TransferCallResult>> transferWithResponse(CommunicationIdentifier targetParticipant, String targetCallConnectionId, String userToUserInformation, Context context) {
        try {
            TransferCallRequest request = TransferCallRequestConverter.convert(targetParticipant, targetCallConnectionId, userToUserInformation);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .transferWithResponseAsync(callConnectionId, request, contextValue)
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
     * Hold Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> holdParticipantMeetingAudio(
        CommunicationIdentifier participant) {
        try {
            HoldMeetingAudioRequest holdParticipantMeetingAudioRequest =
                new HoldMeetingAudioRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.holdParticipantMeetingAudioAsync(callConnectionId, holdParticipantMeetingAudioRequest, Context.NONE)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hold Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> holdParticipantMeetingAudioWithResponse(
        CommunicationIdentifier participant) {
        return holdParticipantMeetingAudioWithResponseInternal(participant, Context.NONE);
    }

    Mono<Response<Void>> holdParticipantMeetingAudioWithResponseInternal(
        CommunicationIdentifier participant,
        Context context) {
        try {
            HoldMeetingAudioRequest holdParticipantMeetingAudioRequest =
                new HoldMeetingAudioRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.holdParticipantMeetingAudioWithResponseAsync(callConnectionId, holdParticipantMeetingAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resumeParticipantMeetingAudio(
        CommunicationIdentifier participant) {
        try {
            ResumeMeetingAudioRequest resumeParticipantMeetingAudioRequest =
                new ResumeMeetingAudioRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.resumeParticipantMeetingAudioAsync(callConnectionId, resumeParticipantMeetingAudioRequest, Context.NONE)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume Participant's meeting audio.
     *
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeParticipantMeetingAudioWithResponse(
        CommunicationIdentifier participant) {
        return resumeParticipantMeetingAudioWithResponseInternal(participant, Context.NONE);
    }

    Mono<Response<Void>> resumeParticipantMeetingAudioWithResponseInternal(
        CommunicationIdentifier participant,
        Context context) {
        try {
            ResumeMeetingAudioRequest resumeParticipantMeetingAudioRequest =
                new ResumeMeetingAudioRequest()
                    .setIdentifier(CommunicationIdentifierConverter.convert(participant));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.resumeParticipantMeetingAudioWithResponseAsync(callConnectionId, resumeParticipantMeetingAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create Audio Routing Group.
     *
     * @param audioRoutingMode The audio routing group mode.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio routing group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateAudioRoutingGroupResult> createAudioRoutingGroup(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets) {
        try {
            AudioRoutingGroupRequest request = getAudioRoutingGroupRequest(audioRoutingMode, targets);
            return callConnectionInternal.createAudioRoutingGroupAsync(callConnectionId, request)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new CreateAudioRoutingGroupResult(result.getAudioRoutingGroupId())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create Audio Routing Group.
     *
     * @param audioRoutingMode The audio routing group mode.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio routing group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateAudioRoutingGroupResult>> createAudioRoutingGroupWithResponse(
        AudioRoutingMode audioRoutingMode,
        List<CommunicationIdentifier> targets) {
        return createAudioRoutingGroupWithResponseInternal(audioRoutingMode, targets, Context.NONE);
    }

    Mono<Response<CreateAudioRoutingGroupResult>> createAudioRoutingGroupWithResponseInternal(AudioRoutingMode audioRoutingMode, List<CommunicationIdentifier> targets, Context context) {
        try {
            AudioRoutingGroupRequest request = getAudioRoutingGroupRequest(audioRoutingMode, targets);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .createAudioRoutingGroupWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new CreateAudioRoutingGroupResult(response.getValue().getAudioRoutingGroupId())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update Audio Routing Group.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateAudioRoutingGroup(
        String audioRoutingGroupId,
        List<CommunicationIdentifier> targets) {
        try {
            UpdateAudioRoutingGroupRequest request = getUpdateAudioRoutingGroupRequest(audioRoutingGroupId, targets);
            return callConnectionInternal.updateAudioRoutingGroupAsync(callConnectionId, audioRoutingGroupId, request)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update Audio Routing Group.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @param targets the targets value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateAudioRoutingGroupWithResponse(
        String audioRoutingGroupId,
        List<CommunicationIdentifier> targets) {
        return updateAudioRoutingGroupWithResponseInternal(audioRoutingGroupId, targets, Context.NONE);
    }

    Mono<Response<Void>> updateAudioRoutingGroupWithResponseInternal(String audioRoutingGroupId, List<CommunicationIdentifier> targets, Context context) {
        try {
            UpdateAudioRoutingGroupRequest request = getUpdateAudioRoutingGroupRequest(audioRoutingGroupId, targets);
            return callConnectionInternal.updateAudioRoutingGroupWithResponseAsync(callConnectionId, audioRoutingGroupId, request, context)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get audio routing groups in a call.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio routing group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AudioRoutingGroupResult> getAudioRoutingGroups(
        String audioRoutingGroupId) {
        try {
            return callConnectionInternal.getAudioRoutingGroupsAsync(callConnectionId, audioRoutingGroupId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(AudioRoutingGroupResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get audio routing groups in a call.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful create audio routing group request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AudioRoutingGroupResult>> getAudioRoutingGroupsWithResponse(
        String audioRoutingGroupId) {
        return getAudioRoutingGroupsWithResponseInternal(audioRoutingGroupId, Context.NONE);
    }

    Mono<Response<AudioRoutingGroupResult>> getAudioRoutingGroupsWithResponseInternal(String audioRoutingGroupId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .getAudioRoutingGroupsWithResponseAsync(callConnectionId, audioRoutingGroupId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, AudioRoutingGroupResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete audio routing group from a call.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAudioRoutingGroup(
        String audioRoutingGroupId) {
        try {
            return callConnectionInternal.deleteAudioRoutingGroupAsync(callConnectionId, audioRoutingGroupId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete audio routing group from a call.
     *
     * @param audioRoutingGroupId The audio routing group id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteAudioRoutingGroupWithResponse(
        String audioRoutingGroupId) {
        return deleteAudioRoutingGroupWithResponseInternal(audioRoutingGroupId, Context.NONE);
    }

    Mono<Response<Void>> deleteAudioRoutingGroupWithResponseInternal(String audioRoutingGroupId, Context context) {
        try {
            return callConnectionInternal.deleteAudioRoutingGroupWithResponseAsync(callConnectionId, audioRoutingGroupId, context)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private AddParticipantRequest getAddParticipantRequest(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext) {
        AddParticipantRequest request = new AddParticipantRequest()
            .setParticipant(CommunicationIdentifierConverter.convert(participant))
            .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(alternateCallerId))
            .setOperationContext(operationContext);
        return request;
    }

    private AudioRoutingGroupRequest getAudioRoutingGroupRequest(AudioRoutingMode audioRoutingMode, List<CommunicationIdentifier> targets) {
        AudioRoutingGroupRequest request = new AudioRoutingGroupRequest()
            .setAudioRoutingMode(audioRoutingMode)
            .setTargets(targets
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));
        return request;
    }

    private UpdateAudioRoutingGroupRequest getUpdateAudioRoutingGroupRequest(String audioRoutingGroupId, List<CommunicationIdentifier> targets) {
        UpdateAudioRoutingGroupRequest request = new UpdateAudioRoutingGroupRequest()
            .setTargets(targets
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));
        return request;
    }
}
