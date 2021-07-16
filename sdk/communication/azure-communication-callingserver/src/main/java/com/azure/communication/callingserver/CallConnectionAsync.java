// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.CancelAllMediaOperationsResultConverter;
import com.azure.communication.callingserver.implementation.converters.AddParticipantRequestConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantRequest;
import com.azure.communication.callingserver.implementation.models.CancelAllMediaOperationsRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

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
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri call back uri to receive notifications.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(
        String audioFileUri,
        boolean loop,
        String audioFileId,
        String callbackUri,
        String operationContext) {
        return playAudioInternal(audioFileUri, loop, audioFileId, callbackUri, operationContext);
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
    public Mono<PlayAudioResult> playAudio(String audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioInternal(audioFileUri, playAudioOptions);

    }

    Mono<PlayAudioResult> playAudioInternal(
        String audioFileUri,
        boolean loop,
        String audioFileId,
        String callbackUri,
        String operationContext) {
        try {
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest playAudioRequest =
                new PlayAudioRequest()
                    .setAudioFileUri(audioFileUri)
                    .setLoop(loop)
                    .setAudioFileId(audioFileId)
                    .setOperationContext(operationContext)
                    .setCallbackUri(callbackUri);
            return playAudioInternal(playAudioRequest);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<PlayAudioResult> playAudioInternal(String audioFileUri, PlayAudioOptions playAudioOptions) {
        try {
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
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
        String audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioWithResponseInternal(audioFileUri, playAudioOptions, null);
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponseInternal(
        String audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        try {
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
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
        return hangupWithResponse(null);
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
     * Cancel all media operations in the call.
     *
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload of the cancel all media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CancelAllMediaOperationsResult> cancelAllMediaOperations(String operationContext) {
        try {
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest();
            request.setOperationContext(operationContext);
            return callConnectionInternal.cancelAllMediaOperationsAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(CancelAllMediaOperationsResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload of the cancel all media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CancelAllMediaOperationsResult>> cancelAllMediaOperationsWithResponse(String operationContext) {
        return cancelAllMediaOperationsWithResponse(operationContext, null);
    }

    Mono<Response<CancelAllMediaOperationsResult>> cancelAllMediaOperationsWithResponse(
        String operationContext,
        Context context) {
        try {
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest();
            request.setOperationContext(operationContext);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .cancelAllMediaOperationsWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, CancelAllMediaOperationsResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
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
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            AddParticipantRequest request = AddParticipantRequestConverter.convert(participant,
                alternateCallerId,
                operationContext,
                null);
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
     * @param participant Added participant.
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
        return addParticipantWithResponse(participant, alternateCallerId, operationContext, null);
    }

    Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext,
        Context context) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            AddParticipantRequest request =
                AddParticipantRequestConverter
                    .convert(participant, alternateCallerId, operationContext, null);
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
     * @param participantId Participant id.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String participantId) {
        try {
            return callConnectionInternal.removeParticipantAsync(callConnectionId, participantId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String participantId) {
        return removeParticipantWithResponse(participantId, null);
    }

    Mono<Response<Void>> removeParticipantWithResponse(String participantId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .removeParticipantWithResponseAsync(callConnectionId, participantId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
