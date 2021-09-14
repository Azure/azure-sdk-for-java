// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.converters.CallConnectionRequestConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.JoinCallRequestConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.models.CancelMediaOperationRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.CreateCallRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;


/**
 * Asynchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a asynchronous CallingServer client</strong></p>
 *
 * {@codesnippet com.azure.communication.callingserver.CallingServerAsyncClient.pipeline.instantiation}
 *
 * <p>View {@link CallingServerClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallingServerClientBuilder
 */
@ServiceClient(builder = CallingServerClientBuilder.class, isAsync = true)
public final class CallingServerAsyncClient {
    private final CallConnectionsImpl callConnectionInternal;
    private final ServerCallsImpl serverCallInternal;
    private final ClientLogger logger = new ClientLogger(CallingServerAsyncClient.class);
    private final ContentDownloader contentDownloader;

    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallInternal = callServiceClient.getServerCalls();

        contentDownloader = new ContentDownloader(
            callServiceClient.getEndpoint(),
            callServiceClient.getHttpPipeline());
    }

    /**
     * Create a call connection request from a source identity to targets identity.
     *
     * @param source The source identity.
     * @param targets The target identities.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     *
     * {@codesnippet com.azure.communication.callingserver.CallingServerAsyncClient.create.call.connection.async}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCallConnection(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source identity.
     * @param targets The target identities.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallConnectionWithResponse(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallWithResponseAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response, new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<CallConnection> createCallConnectionInternal(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnection(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CallConnection>> createCallConnectionWithResponseInternal(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions,
        Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.createCallWithResponseAsync(request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response -> new SimpleResponse<>(response,
                        new CallConnection(new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal))));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param serverCallId Server call id.
     * @param source Source identity.
     * @param joinCallOptions Join call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> joinCall(
        String serverCallId,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return serverCallInternal
                .joinCallAsync(serverCallId, JoinCallRequestConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a call
     *
     * @param serverCallId Server call id.
     * @param source Source identity.
     * @param joinCallOptions Join call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> joinCallWithResponse(
        String serverCallId,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return serverCallInternal.
                joinCallWithResponseAsync(serverCallId, JoinCallRequestConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<CallConnection> joinInternal(
        String serverCallId,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return serverCallInternal
                .joinCallAsync(serverCallId, JoinCallRequestConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response ->
                    Mono.just(new CallConnection(new CallConnectionAsync(response.getCallConnectionId(),
                        callConnectionInternal))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CallConnection>>joinWithResponseInternal(
        String serverCallId,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions,
        Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .joinCallWithResponseAsync(serverCallId, JoinCallRequestConverter.convert(source, joinCallOptions), contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(
                            response,
                            new CallConnection(new CallConnectionAsync(response.getValue().getCallConnectionId(),
                                callConnectionInternal))));
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get CallConnection object
     *
     * @param callConnectionId Call connection id.
     * @return CallConnection object.
     */
    public CallConnectionAsync getCallConnection(String callConnectionId) {
        Objects.requireNonNull(callConnectionId, "'callConnectionId' cannot be null.");
        return new CallConnectionAsync(callConnectionId, callConnectionInternal);
    }

    CallConnection getCallConnectionInternal(String callConnectionId) {
        Objects.requireNonNull(callConnectionId, "'callConnectionId' cannot be null.");
        return new CallConnection(new CallConnectionAsync(callConnectionId, callConnectionInternal));
    }

    /**
     * Get ServerCall object.
     *
     * @param serverCallId Server call id.
     * @return ServerCall object.
     */
    public ServerCallAsync initializeServerCall(String serverCallId) {
        Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
        return new ServerCallAsync(serverCallId, serverCallInternal);
    }

    ServerCall initializeServerCallInternal(String serverCallId) {
        Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
        return new ServerCall(new ServerCallAsync(serverCallId, serverCallInternal));
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param sourceEndpoint - URL where the content is located.
     * @return A {@link Flux} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream(String sourceEndpoint) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return downloadStream(sourceEndpoint, null);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param sourceEndpoint - URL where the content is located.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Flux} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream(String sourceEndpoint, HttpRange httpRange) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return contentDownloader.downloadStreamWithResponse(sourceEndpoint, httpRange, Context.NONE)
                .map(Response::getValue)
                .flux()
                .flatMap(flux -> flux);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Download the recording content, (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}.
     * @param sourceEndpoint - URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Mono} object containing a {@link Response} with the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> downloadStreamWithResponse(String sourceEndpoint, HttpRange range) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return contentDownloader.downloadStreamWithResponse(sourceEndpoint, range, Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @return Response for a successful downloadTo request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> downloadTo(
        String sourceEndpoint,
        Path destinationPath,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite) {
        try {
            return downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, Context.NONE)
                .then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite) {
        try {
            return downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        OutputStream destinationStream,
        HttpRange httpRange,
        Context context) {

        return contentDownloader.downloadToStreamWithResponse(sourceEndpoint, destinationStream, httpRange, context);
    }

    Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite,
        Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");

        Set<OpenOption> openOptions = new HashSet<>();

        if (overwrite) {
            openOptions.add(StandardOpenOption.CREATE);
        } else {
            openOptions.add(StandardOpenOption.CREATE_NEW);
        }
        openOptions.add(StandardOpenOption.WRITE);

        try {
            AsynchronousFileChannel file = AsynchronousFileChannel.open(destinationPath, openOptions, null);
            return downloadToWithResponse(sourceEndpoint, destinationPath, file, parallelDownloadOptions, context);
        } catch (IOException ex) {
            return monoError(logger, new RuntimeException(ex));
        }
    }

    Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        AsynchronousFileChannel fileChannel,
        ParallelDownloadOptions parallelDownloadOptions,
        Context context
    ) {
        ParallelDownloadOptions finalParallelDownloadOptions =
            parallelDownloadOptions == null
                ? new ParallelDownloadOptions()
                : parallelDownloadOptions;

        return Mono.just(fileChannel).flatMap(
            c -> contentDownloader.downloadToFileWithResponse(sourceEndpoint, c, finalParallelDownloadOptions, context))
            .doFinally(signalType -> contentDownloader.downloadToFileCleanup(fileChannel, destinationPath, signalType));
    }

    /**
     * Play audio in a call.
     *
     * @param serverCallId The server call id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(String serverCallId, String audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioInternal(serverCallId, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<PlayAudioResult> playAudioInternal(String serverCallId, String audioFileUri, 
            PlayAudioOptions playAudioOptions, Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
            }
            return playAudioInternal(serverCallId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<PlayAudioResult> playAudioInternal(String serverCallId, PlayAudioRequest playAudioRequest, Context context) {
        try {
            return serverCallInternal.playAudioAsync(serverCallId, playAudioRequest, context)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(PlayAudioResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }  
    
    /**
     * Play audio in a call.
     *
     * @param serverCallId The server call id.
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
        String serverCallId,
        String audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioWithResponseInternal(serverCallId, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponseInternal(
        String serverCallId,
        String audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
            }
            return playAudioWithResponseInternal(serverCallId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponseInternal(
        String serverCallId,
        PlayAudioRequest playAudioRequest,
        Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .playAudioWithResponseAsync(serverCallId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, 
                                CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Media Operation.
     *
     * @param serverCallId The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelMediaOperationWithResponse(
        String serverCallId,
        String mediaOperationId) {
        return cancelMediaOperationWithResponseInternal(serverCallId, mediaOperationId, Context.NONE);
    }

    /**
     * Cancel Media Operation.
     *
     * @param serverCallId The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelParticipantMediaOperation(
        String serverCallId,
        String mediaOperationId) {
        return cancelMediaOperationWithResponseInternal(serverCallId, mediaOperationId, Context.NONE).block();
    }

    Mono<Response<Void>> cancelMediaOperationWithResponseInternal(
        String serverCallId,
        String mediaOperationId,
        Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(mediaOperationId, "'mediaOperationId' cannot be null.");
            CancelMediaOperationRequest request = new CancelMediaOperationRequest().setMediaOperationId(mediaOperationId);
            return cancelMediaOperationWithResponseInternal(serverCallId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> cancelMediaOperationWithResponseInternal(
        String serverCallId,
        CancelMediaOperationRequest cancelMediaOperationRequest,
        Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .cancelMediaOperationAsync(serverCallId, cancelMediaOperationRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, 
                                CallingServerErrorConverter::translateException)
                    .flatMap(result -> Mono.empty());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param serverCallId The server call id.
     * @param participantId The participant id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelParticipantMediaOperationWithResponse(
        String serverCallId,
        String participantId,
        String mediaOperationId) {
        return cancelParticipantMediaOperationWithResponseInternal(serverCallId, participantId, mediaOperationId, Context.NONE);
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param serverCallId The server call id.
     * @param participantId The participant id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelParticipantMediaOperation(
        String serverCallId,
        String participantId,
        String mediaOperationId) {
        return cancelParticipantMediaOperationWithResponseInternal(serverCallId, participantId, mediaOperationId, Context.NONE).block();
    }

    Mono<Response<Void>> cancelParticipantMediaOperationWithResponseInternal(
        String serverCallId,
        String participantId,
        String mediaOperationId,
        Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            Objects.requireNonNull(mediaOperationId, "'mediaOperationId' cannot be null.");
            CancelMediaOperationRequest request = new CancelMediaOperationRequest().setMediaOperationId(mediaOperationId);
            return cancelParticipantMediaOperationWithResponseInternal(serverCallId, participantId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> cancelParticipantMediaOperationWithResponseInternal(
        String serverCallId,
        String participantId,
        CancelMediaOperationRequest cancelMediaOperationRequest,
        Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .cancelParticipantMediaOperationAsync(serverCallId, participantId, cancelMediaOperationRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, 
                                CallingServerErrorConverter::translateException)
                    .flatMap(result -> Mono.empty());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio to a participant.
     *
     * @param serverCallId The server call id.
     * @param participantId The participant id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio to participant operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudioToParticipant(String serverCallId, String participantId, String audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantInternal(serverCallId, participantId, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<PlayAudioResult> playAudioToParticipantInternal(String serverCallId, String participantId, String audioFileUri, 
            PlayAudioOptions playAudioOptions, Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
            }
            return playAudioToParticipantInternal(serverCallId, participantId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<PlayAudioResult> playAudioToParticipantInternal(String serverCallId, String participantId, PlayAudioRequest playAudioRequest, Context context) {
        try {
            return serverCallInternal.participantPlayAudioAsync(serverCallId, participantId, playAudioRequest, context)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(PlayAudioResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }  
    
    /**
     * Play audio to a participant.
     *
     * @param serverCallId The server call id.
     * @param participantId The participant id.
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
        String serverCallId,
        String participantId,
        String audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantWithResponseInternal(serverCallId, participantId, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioToParticipantWithResponseInternal(
        String serverCallId,
        String participantId,
        String audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");
            PlayAudioRequest request = new PlayAudioRequest().setAudioFileUri(audioFileUri);
            if (playAudioOptions != null) {
                request
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri());
            }
            return playAudioToParticipantWithResponseInternal(serverCallId, participantId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PlayAudioResult>> playAudioToParticipantWithResponseInternal(
        String serverCallId,
        String participantId,
        PlayAudioRequest playAudioRequest,
        Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .participantPlayAudioWithResponseAsync(serverCallId, participantId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, 
                                CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}