// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.ConversationsImpl;
import com.azure.communication.callingserver.implementation.converters.ResultInfoConverter;
import com.azure.communication.callingserver.implementation.models.GetCallRecordingStateResponse;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequestInternal;
import com.azure.communication.callingserver.implementation.models.PlayAudioResponse;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequestInternal;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingResponse;
import com.azure.communication.callingserver.models.GetCallRecordingStateResult;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.implementation.converters.InviteParticipantsRequestConverter;
import com.azure.communication.callingserver.implementation.converters.JoinCallRequestConverter;
import com.azure.communication.callingserver.implementation.models.JoinCallResponse;
import com.azure.communication.callingserver.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.models.JoinCallRequest;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Async client that supports server conversation operations.
 */
@ServiceClient(builder = CallClientBuilder.class, isAsync = true)
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
     * @param request Join Call request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JoinCallResponse> joinCall(String conversationId, JoinCallRequest request) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.conversationsClient.joinCallAsync(conversationId, JoinCallRequestConverter.convert(request));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param conversationId The conversation id.
     * @param request Join Call request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JoinCallResponse>>joinCallWithResponse(String conversationId, JoinCallRequest request) {
        return joinCallWithResponse(conversationId, request, null);
    }

    Mono<Response<JoinCallResponse>> joinCallWithResponse(
        String conversationId,
        JoinCallRequest request,
        Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.joinCallWithResponseAsync(conversationId,
                    JoinCallRequestConverter.convert(request));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Invite Participats to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> inviteParticipants(String conversationId, InviteParticipantsRequest request) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.conversationsClient.inviteParticipantsAsync(conversationId, InviteParticipantsRequestConverter.convert(request));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Invite Participats to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> inviteParticipantsWithResponse(String conversationId, InviteParticipantsRequest request) {
        return inviteParticipantsWithResponse(conversationId, request, null);
    }

    Mono<Response<Void>> inviteParticipantsWithResponse(
        String conversationId,
        InviteParticipantsRequest request,
        Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.inviteParticipantsWithResponseAsync(conversationId,
                        InviteParticipantsRequestConverter.convert(request));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

       /**
     * Remove participant from the Conversation.
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

            return this.conversationsClient.removeParticipantAsync(conversationId, participantId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participant from the Conversation.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String conversationId, String participantId) {
        return removeParticipantWithResponse(conversationId, participantId, null);
    }

    /**
     * Remove participant from the Conversation.
     */
    Mono<Response<Void>> removeParticipantWithResponse(String conversationId, String participantId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.removeParticipantWithResponseAsync(conversationId, participantId);
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
    public Mono<StartCallRecordingResult> startRecording(String conversationId, URI recordingStateCallbackUri) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(recordingStateCallbackUri.isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }

            StartCallRecordingRequestInternal request = createStartCallRecordingRequest(recordingStateCallbackUri);
            return this.conversationsClient.startRecordingAsync(conversationId, request)
                    .flatMap((StartCallRecordingResponse response) -> {
                        StartCallRecordingResult startCallRecordingResult = convertGetCallRecordingStateResponse(
                                response);
                        return Mono.just(startCallRecordingResult);
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
    public Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(String conversationId,
        URI recordingStateCallbackUri) {    
        return startRecordingWithResponse(conversationId, recordingStateCallbackUri, null);
    }

    Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(String conversationId,
            URI recordingStateCallbackUri, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(recordingStateCallbackUri.isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                StartCallRecordingRequestInternal request = createStartCallRecordingRequest(recordingStateCallbackUri);
                return this.conversationsClient.startRecordingWithResponseAsync(conversationId, request)
                        .flatMap((Response<StartCallRecordingResponse> response) -> {
                            StartCallRecordingResult startCallRecordingResult = convertGetCallRecordingStateResponse(
                                    response.getValue());
                            return Mono.just(new SimpleResponse<>(response, startCallRecordingResult));
                        });
            });
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
    public Mono<Void> stopRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.stopRecordingAsync(conversationId, recordingId);
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
        return stopRecordingWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }            
                return this.conversationsClient.stopRecordingWithResponseAsync(conversationId, recordingId);
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
            return this.conversationsClient.pauseRecordingAsync(conversationId, recordingId);
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
        return pauseRecordingWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                } 
                return this.conversationsClient.pauseRecordingWithResponseAsync(conversationId, recordingId);
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
            return this.conversationsClient.resumeRecordingAsync(conversationId, recordingId);
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
        return resumeRecordingWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }             
                return this.conversationsClient.resumeRecordingWithResponseAsync(conversationId, recordingId);
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
    public Mono<GetCallRecordingStateResult> getRecordingState(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.recordingStateAsync(conversationId, recordingId)
                    .flatMap((GetCallRecordingStateResponse response) -> {
                        GetCallRecordingStateResult getRecordingStateResult = convertGetCallRecordingStateResponse(
                                response);
                        return Mono.just(getRecordingStateResult);
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
    public Mono<Response<GetCallRecordingStateResult>> getRecordingStateWithResponse(String conversationId,
            String recordingId) {
        return getRecordingStateWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<GetCallRecordingStateResult>> getRecordingStateWithResponse(String conversationId,
            String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                } 
                return this.conversationsClient.recordingStateWithResponseAsync(conversationId, recordingId)
                        .flatMap((Response<GetCallRecordingStateResponse> response) -> {
                            GetCallRecordingStateResult getRecordingStateResult = convertGetCallRecordingStateResponse(
                                    response.getValue());
                            return Mono.just(new SimpleResponse<>(response, getRecordingStateResult));
                        });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     * 
     * @param conversationId The conversation id.
     * @param playAudioUri The uri of the audio file .
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(String conversationId, URI playAudioUri, String audioFileId, URI callbackUri, String operationContext) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(playAudioUri, "'playAudioUri' cannot be null.");

            return this.conversationsClient.playAudioAsync(conversationId, convertPlayAudioRequest(playAudioUri, audioFileId, callbackUri, operationContext)).flatMap(
                (PlayAudioResponse response) -> {
                        PlayAudioResult playAudioResult = convertPlayAudioResponse(response);
                        return Mono.just(playAudioResult);
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param playAudioUri The uri of the audio file .
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(String conversationId, URI playAudioUri, String audioFileId, URI callbackUri, String operationContext) {
        return playAudioWithResponse(conversationId, playAudioUri, audioFileId, callbackUri, operationContext, null);
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponse(String conversationId, URI playAudioUri, String audioFileId, URI callbackUri, String operationContext, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(playAudioUri, "'playAudioUri' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.playAudioWithResponseAsync(conversationId, convertPlayAudioRequest(playAudioUri, audioFileId, callbackUri, operationContext)).flatMap((
                        Response<PlayAudioResponse> response) -> {
                    PlayAudioResult playAudioResult = convertPlayAudioResponse(response.getValue());
                    return Mono.just(new SimpleResponse<>(response, playAudioResult));
                });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private StartCallRecordingRequestInternal createStartCallRecordingRequest(URI recordingStateCallbackUri) {
        StartCallRecordingRequestInternal request = new StartCallRecordingRequestInternal();
        request.setRecordingStateCallbackUri(recordingStateCallbackUri.toString());
        return request;
    }

    private GetCallRecordingStateResult convertGetCallRecordingStateResponse(
            com.azure.communication.callingserver.implementation.models.GetCallRecordingStateResponse response) {
        return new GetCallRecordingStateResult()
                .setRecordingState(com.azure.communication.callingserver.models.CallRecordingState
                        .fromString(response.getRecordingState().toString()));
    }

    private StartCallRecordingResult convertGetCallRecordingStateResponse(StartCallRecordingResponse response) {
        return new StartCallRecordingResult().setRecordingId(response.getRecordingId());
    }

    private PlayAudioRequestInternal convertPlayAudioRequest(URI playAudioUri, String audioFileId, URI callbackUri, String operationContext) {
        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        return new PlayAudioRequestInternal()
            .setOperationContext(operationContext)
            .setAudioFileUri(playAudioUri.toString())
            .setAudioFileId(audioFileId)
            .setCallbackUri(callbackUri.toString())
            .setLoop(false);
    }

    private PlayAudioResult convertPlayAudioResponse(PlayAudioResponse response) {
        return new PlayAudioResult().setId(response.getId())
                .setStatus(OperationStatus.fromString(response.getStatus().toString()))
                .setOperationContext(response.getOperationContext())
                .setResultInfo(ResultInfoConverter.convert(response.getResultInfo()));
    }
}
