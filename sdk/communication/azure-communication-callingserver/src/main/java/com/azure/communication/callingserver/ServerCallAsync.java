// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.AddParticipantRequestConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.RecordingChannelType;
import com.azure.communication.callingserver.models.RecordingContentType;
import com.azure.communication.callingserver.models.RecordingFormatType;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequest;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous client that supports server call operations.
 */
public final class ServerCallAsync {
    private final String serverCallId;
    private final ServerCallsImpl serverCallInternal;
    private final ClientLogger logger = new ClientLogger(ServerCallAsync.class);

    ServerCallAsync(String serverCallId, ServerCallsImpl serverCallInternal) {
        this.serverCallId = serverCallId;
        this.serverCallInternal = serverCallInternal;
    }

    /**
     * Get server call id property
     *
     * @return Server call id value.
     */
    public String getServerCallId() {
        return serverCallId;
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Added participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantResult> addParticipant(
        CommunicationIdentifier participant,
        String callBackUri,
        String alternateCallerId,
        String operationContext) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            AddParticipantRequest request =
                AddParticipantRequestConverter.convert(participant,
                    alternateCallerId,
                    operationContext,
                    callBackUri);
            return serverCallInternal.addParticipantAsync(serverCallId, request)
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
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String callBackUri,
        String alternateCallerId,
        String operationContext) {
        return addParticipantWithResponse(participant,
            callBackUri,
            alternateCallerId,
            operationContext,
            null);
    }

    Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CommunicationIdentifier participant,
        String callBackUri,
        String alternateCallerId,
        String operationContext,
        Context context) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            AddParticipantRequest request =
                AddParticipantRequestConverter.convert(participant,
                    alternateCallerId,
                    operationContext,
                    callBackUri);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .addParticipantWithResponseAsync(serverCallId, request, contextValue)
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
            return serverCallInternal.removeParticipantAsync(serverCallId, participantId)
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
                return serverCallInternal
                    .removeParticipantWithResponseAsync(serverCallId, participantId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Start recording of the call.
     *
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StartCallRecordingResult> startRecording(String recordingStateCallbackUri) {
        return startRecording(recordingStateCallbackUri, null, null, null);
    }

    /**
     * Start recording of the call.
     *
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @param recordingChannelType recordingChannelType to send custom options
     * @param recordingContentType recordingContentType to send custom options
     * @param recordingFormatType recordingFormatType to send custom options
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StartCallRecordingResult> startRecording(String recordingStateCallbackUri, 
        RecordingChannelType recordingChannelType, 
        RecordingContentType recordingContentType, 
        RecordingFormatType recordingFormatType) {
        try {
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingRequest request = new StartCallRecordingRequest();
            request.setRecordingStateCallbackUri(recordingStateCallbackUri);
            request.setRecordingChannelType(recordingChannelType);
            request.setRecordingContentType(recordingContentType);
            request.setRecordingFormatType(recordingFormatType);
            return serverCallInternal.startRecordingAsync(serverCallId, request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new StartCallRecordingResult(result.getRecordingId())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Start recording of the call.
     *
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(String recordingStateCallbackUri) {
        return startRecordingWithResponse(recordingStateCallbackUri, null, null, null, null);
    }

    Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(
        String recordingStateCallbackUri,
        RecordingChannelType recordingChannelType, 
        RecordingContentType recordingContentType, 
        RecordingFormatType recordingFormatType,
        Context context) {
        try {
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingRequest request = new StartCallRecordingRequest();
            request.setRecordingStateCallbackUri(recordingStateCallbackUri);
            request.setRecordingChannelType(recordingChannelType);
            request.setRecordingContentType(recordingContentType);
            request.setRecordingFormatType(recordingFormatType);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .startRecordingWithResponseAsync(serverCallId, request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new StartCallRecordingResult(response.getValue().getRecordingId())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopRecording(String recordingId) {
        try {
            return serverCallInternal.stopRecordingAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop. 
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopRecordingWithResponse(String recordingId) {
        return stopRecordingWithResponse(recordingId, null);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .stopRecordingWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> pauseRecording(String recordingId) {
        try {
            return serverCallInternal.pauseRecordingAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> pauseRecordingWithResponse(String recordingId) {
        return pauseRecordingWithResponse(recordingId, null);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .pauseRecordingWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resumeRecording(String recordingId) {
        try {
            return serverCallInternal.resumeRecordingAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeRecordingWithResponse(String recordingId) {
        return resumeRecordingWithResponse(recordingId, null);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .resumeRecordingWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get current recording state by recording id.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallRecordingProperties> getRecordingState(String recordingId) {
        try {
            return serverCallInternal.getRecordingPropertiesAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new CallRecordingProperties(result.getRecordingState())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get current recording state by recording id.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallRecordingProperties>> getRecordingStateWithResponse(String recordingId) {
        return getRecordingStateWithResponse(recordingId, null);
    }

    Mono<Response<CallRecordingProperties>> getRecordingStateWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .getRecordingPropertiesWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new CallRecordingProperties(response.getValue().getRecordingState())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri Media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId Id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri Callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(
        String audioFileUri,
        String audioFileId,
        String callbackUri,
        String operationContext) {
        return playAudioInternal(audioFileUri, audioFileId, callbackUri, operationContext);
    }

    /**
     * Play audio in a call.
     *
     * @param audioFileUri Media resource uri of the play audio request. Currently only Wave file (.wav) format
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
        String audioFileId,
        String callbackUri,
        String operationContext) {
        try {
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
            PlayAudioRequest playAudioRequest =
                new PlayAudioRequest()
                    .setAudioFileUri(audioFileUri)
                    .setLoop(false)
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

            //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(false)
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
            return serverCallInternal.playAudioAsync(serverCallId, playAudioRequest)
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
     * @return the response payload for play audio operation.
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

            //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(false)
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
            }
            return playAudioWithResponse(request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponse(PlayAudioRequest playAudioRequest, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .playAudioWithResponseAsync(serverCallId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}

