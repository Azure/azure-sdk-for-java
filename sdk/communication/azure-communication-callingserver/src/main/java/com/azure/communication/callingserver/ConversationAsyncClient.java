// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.ConversationsImpl;
import com.azure.communication.callingserver.implementation.converters.AddParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.JoinCallConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.converters.ServerCallingErrorConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequest;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.JoinCallResponse;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.ServerCallingError;
import com.azure.communication.callingserver.models.ServerCallingErrorException;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
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
 * Async client that supports server conversation operations.
 */
@ServiceClient(builder = ConversationClientBuilder.class, isAsync = true)
public final class ConversationAsyncClient {
    private final ConversationsImpl conversationsClient;
    private final ClientLogger logger = new ClientLogger(ConversationAsyncClient.class);

    ConversationAsyncClient(AzureCommunicationCallingServerServiceImpl conversationServiceClient) {
        conversationsClient = conversationServiceClient.getConversations();
    }

    /**
     * Join a Call
     *
     * @param conversationId The conversation id.
     * @param source to Join Call.
     * @param joinCallOptions join call options.
     * @return response for a successful joinCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JoinCallResponse> joinCall(String conversationId,
                                           CommunicationIdentifier source,
                                           JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return this.conversationsClient
                .joinCallAsync(conversationId, JoinCallConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param conversationId The conversation id.
     * @param source to Join Call.
     * @param joinCallOptions join call options.
     * @return response for a successful joincall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JoinCallResponse>>joinCallWithResponse(String conversationId,
                                                                CommunicationIdentifier source,
                                                                JoinCallOptions joinCallOptions) {
        return joinCallWithResponse(conversationId, source, joinCallOptions, Context.NONE);
    }

    Mono<Response<JoinCallResponse>> joinCallWithResponse(String conversationId,
                                                          CommunicationIdentifier source,
                                                          JoinCallOptions joinCallOptions,
                                                          Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient.
                    joinCallWithResponseAsync(conversationId,
                    JoinCallConverter.convert(source, joinCallOptions),
                    contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(String conversationId,
                                     CommunicationIdentifier participant,
                                     String callBackUri,
                                     String alternateCallerId,
                                     String operationContext) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request =
                AddParticipantConverter.convert(participant,
                    alternateCallerId,
                    operationContext,
                    callBackUri);
            return this.conversationsClient.inviteParticipantsAsync(conversationId, request)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(String conversationId,
                                                           CommunicationIdentifier participant,
                                                           String callBackUri,
                                                           String alternateCallerId,
                                                           String operationContext) {
        return addParticipantWithResponse(conversationId,
            participant,
            callBackUri,
            alternateCallerId,
            operationContext,
            Context.NONE);
    }

    Mono<Response<Void>> addParticipantWithResponse(String conversationId,
                                                    CommunicationIdentifier participant,
                                                    String callBackUri,
                                                    String alternateCallerId,
                                                    String operationContext,
                                                    Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request =
                AddParticipantConverter.convert(participant,
                    alternateCallerId,
                    operationContext,
                    callBackUri);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .inviteParticipantsWithResponseAsync(conversationId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String conversationId, String participantId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return this.conversationsClient.removeParticipantAsync(conversationId, participantId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String conversationId, String participantId) {
        return removeParticipantWithResponse(conversationId, participantId, Context.NONE);
    }

    /**
     * Remove participant from the Conversation.
     */
    Mono<Response<Void>> removeParticipantWithResponse(String conversationId, String participantId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .removeParticipantWithResponseAsync(conversationId, participantId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StartCallRecordingResponse> startRecording(String conversationId, String recordingStateCallbackUri) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }
            StartCallRecordingRequest request = new StartCallRecordingRequest();
            request.setRecordingStateCallbackUri(recordingStateCallbackUri);
            return this.conversationsClient.startRecordingAsync(conversationId, request)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StartCallRecordingResponse>> startRecordingWithResponse(String conversationId,
                                                                                 String recordingStateCallbackUri) {
        return startRecordingWithResponse(conversationId, recordingStateCallbackUri, Context.NONE);
    }

    Mono<Response<StartCallRecordingResponse>> startRecordingWithResponse(String conversationId,
                                                                          String recordingStateCallbackUri,
                                                                          Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }
            StartCallRecordingRequest request = new StartCallRecordingRequest();
            request.setRecordingStateCallbackUri(recordingStateCallbackUri);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .startRecordingWithResponseAsync(conversationId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
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
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.stopRecordingAsync(conversationId, recordingId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopRecordingWithResponse(String conversationId, String recordingId) {
        return stopRecordingWithResponse(conversationId, recordingId, Context.NONE);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .stopRecordingWithResponseAsync(conversationId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> pauseRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.pauseRecordingAsync(conversationId, recordingId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> pauseRecordingWithResponse(String conversationId, String recordingId) {
        return pauseRecordingWithResponse(conversationId, recordingId, Context.NONE);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .pauseRecordingWithResponseAsync(conversationId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resumeRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.resumeRecordingAsync(conversationId, recordingId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeRecordingWithResponse(String conversationId, String recordingId) {
        return resumeRecordingWithResponse(conversationId, recordingId, Context.NONE);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .resumeRecordingWithResponseAsync(conversationId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallRecordingStateResponse> getRecordingState(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.recordingStateAsync(conversationId, recordingId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallRecordingStateResponse>> getRecordingStateWithResponse(String conversationId,
                                                                                    String recordingId) {
        return getRecordingStateWithResponse(conversationId, recordingId, Context.NONE);
    }

    Mono<Response<CallRecordingStateResponse>> getRecordingStateWithResponse(String conversationId,
                                                                             String recordingId,
                                                                             Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .recordingStateWithResponseAsync(conversationId, recordingId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResponse> playAudio(String conversationId,
                                             String audioFileUri,
                                             String audioFileId,
                                             String callbackUri,
                                             String operationContext) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
            PlayAudioRequest playAudioRequest = new PlayAudioRequest();
            playAudioRequest.setAudioFileUri(audioFileUri);
            playAudioRequest.setLoop(false);
            playAudioRequest.setAudioFileId(audioFileId);
            playAudioRequest.setOperationContext(operationContext);
            playAudioRequest.setCallbackUri(callbackUri);
            return playAudio(conversationId, playAudioRequest);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResponse> playAudio(String conversationId,
                                             String audioFileUri,
                                             PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return playAudio(conversationId, playAudioRequest);
    }

    Mono<PlayAudioResponse> playAudio(String conversationId, PlayAudioRequest playAudioRequest) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(playAudioRequest.getAudioFileUri(), "'audioFileUri' cannot be null.");
            return this.conversationsClient.playAudioAsync(conversationId, playAudioRequest)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResponse>> playAudioWithResponse(String conversationId,
                                                                   String audioFileUri,
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
        return playAudioWithResponse(conversationId, playAudioRequest, Context.NONE);
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResponse>> playAudioWithResponse(String conversationId,
                                                                   String audioFileUri,
                                                                   PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return playAudioWithResponse(conversationId, playAudioRequest, Context.NONE);
    }

    Mono<Response<PlayAudioResponse>> playAudioWithResponse(String conversationId,
                                                            PlayAudioRequest playAudioRequest,
                                                            Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(playAudioRequest.getAudioFileUri(), "'audioFileUri' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.conversationsClient
                    .playAudioWithResponseAsync(conversationId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private ServerCallingErrorException translateException(CommunicationErrorException exception) {
        ServerCallingError error = null;
        if (exception.getValue() != null) {
            error = ServerCallingErrorConverter.convert(exception.getValue());
        }
        return new ServerCallingErrorException(exception.getMessage(), exception.getResponse(), error);
    }
}
