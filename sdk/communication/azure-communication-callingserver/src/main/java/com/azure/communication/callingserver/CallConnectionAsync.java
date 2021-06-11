// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import java.util.Objects;

import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.converters.AddParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CancelAllMediaOperationsResultConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.implementation.models.CancelAllMediaOperationsRequest;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
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

/**
 * Async client that supports call connection operations.
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
     * Get the call connection id property
     *
     * @return the id value.
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
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(String audioFileUri,
                                           boolean loop,
                                           String audioFileId,
                                           String callbackUri,
                                           String operationContext) {
        PlayAudioRequest playAudioRequest =
            new PlayAudioRequest()
                .setAudioFileUri(audioFileUri)
                .setLoop(loop)
                .setAudioFileId(audioFileId)
                .setOperationContext(operationContext)
                .setCallbackUri(callbackUri);
        return playAudio(playAudioRequest);
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(String audioFileUri,
                                             PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return playAudio(playAudioRequest);

    }

    Mono<PlayAudioResult> playAudio(PlayAudioRequest playAudioRequest) {
        try {
            Objects.requireNonNull(playAudioRequest.getAudioFileUri(), "'audioFileUri' cannot be null.");
            return callConnectionInternal.playAudioAsync(callConnectionId, playAudioRequest)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException)
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
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri call back uri to receive notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(String audioFileUri,
                                                                 boolean loop,
                                                                 String audioFileId,
                                                                 String callbackUri,
                                                                 String operationContext) {
        PlayAudioRequest playAudioRequest =
            new PlayAudioRequest()
                .setAudioFileUri(audioFileUri)
                .setLoop(loop)
                .setAudioFileId(audioFileId)
                .setOperationContext(operationContext)
                .setCallbackUri(callbackUri);
        return playAudioWithResponse(playAudioRequest, Context.NONE);
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(String audioFileUri,
                                                                 PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return playAudioWithResponse(playAudioRequest, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponse(PlayAudioRequest playAudioRequest,
                                                            Context context) {
        try {
            Objects.requireNonNull(playAudioRequest.getAudioFileUri(), "'audioFileUri' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal
                    .playAudioWithResponseAsync(callConnectionId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException)
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
     * @return response for a successful Hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> hangup() {
        try {
            return this.callConnectionInternal.hangupCallAsync(callConnectionId)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hangup a call.
     *
     * @return response for a successful Hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> hangupWithResponse() {
        return hangupWithResponse(Context.NONE);
    }

    Mono<Response<Void>> hangupWithResponse(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callConnectionInternal.hangupCallWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext operationContext
     * @return the response payload of the cancel media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CancelAllMediaOperationsResult> cancelAllMediaOperations(String operationContext) {
        try {
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest();
            request.setOperationContext(operationContext);
            return this.callConnectionInternal.cancelAllMediaOperationsAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(CancelAllMediaOperationsResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param operationContext operationContext
     * @return the response payload of the cancel media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CancelAllMediaOperationsResult>> cancelAllMediaOperationsWithResponse(String operationContext) {
        return cancelAllMediaOperationsWithResponse(operationContext, Context.NONE);
    }

    Mono<Response<CancelAllMediaOperationsResult>> cancelAllMediaOperationsWithResponse(String operationContext,
                                                                                          Context context) {
        try {
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest();
            request.setOperationContext(operationContext);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callConnectionInternal.cancelAllMediaOperationsWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException)
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
     * @param participant Invited participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(CommunicationIdentifier participant,
                                     String alternateCallerId,
                                     String operationContext) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request = AddParticipantConverter.convert(participant,
                alternateCallerId,
                operationContext,
                null);
            return this.callConnectionInternal.inviteParticipantsAsync(callConnectionId, request)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(CommunicationIdentifier participant,
                                                           String alternateCallerId,
                                                           String operationContext) {
        return addParticipantWithResponse(participant, alternateCallerId, operationContext, Context.NONE);
    }

    Mono<Response<Void>> addParticipantWithResponse(CommunicationIdentifier participant,
                                                    String alternateCallerId,
                                                    String operationContext,
                                                    Context context) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request = AddParticipantConverter.convert(participant, alternateCallerId, operationContext, null);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callConnectionInternal.inviteParticipantsWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String participantId) {
        try {
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return this.callConnectionInternal.removeParticipantAsync(callConnectionId, participantId)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String participantId) {
        return removeParticipantWithResponse(participantId, Context.NONE);
    }

    /**
     * Remove participant from the call.
     */
    Mono<Response<Void>> removeParticipantWithResponse(String participantId, Context context) {
        try {
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callConnectionInternal.removeParticipantWithResponseAsync(callConnectionId, participantId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
