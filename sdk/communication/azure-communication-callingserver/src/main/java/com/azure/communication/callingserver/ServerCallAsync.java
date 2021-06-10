// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.converters.AddParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequest;
import com.azure.communication.callingserver.models.CallRecordingStateResult;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
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
 * Async client that supports server call operations.
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
     * Get the server call id property
     *
     * @return the id value.
     */
    public String getServerCallId() {
        return serverCallId;
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(CommunicationIdentifier participant,
                                     String callBackUri,
                                     String alternateCallerId,
                                     String operationContext) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request =
                AddParticipantConverter.convert(participant,
                    alternateCallerId,
                    operationContext,
                    callBackUri);
            return this.serverCallInternal.inviteParticipantsAsync(serverCallId, request)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(CommunicationIdentifier participant,
                                                           String callBackUri,
                                                           String alternateCallerId,
                                                           String operationContext) {
        return addParticipantWithResponse(participant,
            callBackUri,
            alternateCallerId,
            operationContext,
            Context.NONE);
    }

    Mono<Response<Void>> addParticipantWithResponse(CommunicationIdentifier participant,
                                                    String callBackUri,
                                                    String alternateCallerId,
                                                    String operationContext,
                                                    Context context) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request =
                AddParticipantConverter.convert(participant,
                    alternateCallerId,
                    operationContext,
                    callBackUri);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .inviteParticipantsWithResponseAsync(serverCallId, request, contextValue)
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
            return this.serverCallInternal.removeParticipantAsync(serverCallId, participantId)
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

    Mono<Response<Void>> removeParticipantWithResponse(String participantId, Context context) {
        try {
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .removeParticipantWithResponseAsync(serverCallId, participantId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Start recording
     *
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StartCallRecordingResult> startRecording(String recordingStateCallbackUri) {
        try {
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }
            StartCallRecordingRequest request = new StartCallRecordingRequest();
            request.setRecordingStateCallbackUri(recordingStateCallbackUri);
            return this.serverCallInternal.startRecordingAsync(serverCallId, request)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Start recording
     *
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(String recordingStateCallbackUri) {
        return startRecordingWithResponse(recordingStateCallbackUri, Context.NONE);
    }

    Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(String recordingStateCallbackUri,
                                                                          Context context) {
        try {
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }
            StartCallRecordingRequest request = new StartCallRecordingRequest();
            request.setRecordingStateCallbackUri(recordingStateCallbackUri);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .startRecordingWithResponseAsync(serverCallId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Stop recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopRecording(String recordingId) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.serverCallInternal.stopRecordingAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stop recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopRecordingWithResponse(String recordingId) {
        return stopRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String recordingId, Context context) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .stopRecordingWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> pauseRecording(String recordingId) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.serverCallInternal.pauseRecordingAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> pauseRecordingWithResponse(String recordingId) {
        return pauseRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String recordingId, Context context) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .pauseRecordingWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resumeRecording(String recordingId) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.serverCallInternal.resumeRecordingAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeRecordingWithResponse(String recordingId) {
        return resumeRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String recordingId, Context context) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .resumeRecordingWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get recording state
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallRecordingStateResult> getRecordingState(String recordingId) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.serverCallInternal.recordingStateAsync(serverCallId, recordingId)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get recording state
     *
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallRecordingStateResult>> getRecordingStateWithResponse(String recordingId) {
        return getRecordingStateWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<CallRecordingStateResult>> getRecordingStateWithResponse(String recordingId, Context context) {
        try {
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal
                    .recordingStateWithResponseAsync(serverCallId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
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
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(String audioFileUri,
                                           String audioFileId,
                                           String callbackUri,
                                           String operationContext) {
        try {
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
            PlayAudioRequest playAudioRequest = new PlayAudioRequest();
            playAudioRequest.setAudioFileUri(audioFileUri);
            playAudioRequest.setLoop(false);
            playAudioRequest.setAudioFileId(audioFileId);
            playAudioRequest.setOperationContext(operationContext);
            playAudioRequest.setCallbackUri(callbackUri);
            return playAudio(playAudioRequest);
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
            return this.serverCallInternal.playAudioAsync(serverCallId, playAudioRequest)
                .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
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
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(String audioFileUri,
                                                                   String audioFileId,
                                                                   String callbackUri,
                                                                   String operationContext) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = new PlayAudioRequest();
        playAudioRequest.setAudioFileUri(audioFileUri);
        playAudioRequest.setLoop(false);
        playAudioRequest.setAudioFileId(audioFileId);
        playAudioRequest.setOperationContext(operationContext);
        playAudioRequest.setCallbackUri(callbackUri);
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
                return this.serverCallInternal
                    .playAudioWithResponseAsync(serverCallId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
