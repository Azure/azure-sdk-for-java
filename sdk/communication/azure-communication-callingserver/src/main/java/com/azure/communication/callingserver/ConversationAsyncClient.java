// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.ConversationsImpl;
import com.azure.communication.callingserver.implementation.models.GetCallRecordingStateResponse;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequestInternal;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingResponse;
import com.azure.communication.callingserver.models.GetCallRecordingStateResult;
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
}
