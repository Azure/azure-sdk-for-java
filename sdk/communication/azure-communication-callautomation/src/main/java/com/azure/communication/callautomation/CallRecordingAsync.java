// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.ContentsImpl;
import com.azure.communication.callautomation.implementation.ServerCallsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.RecordingStateResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CallLocatorInternal;
import com.azure.communication.callautomation.implementation.models.CallLocatorKindInternal;
import com.azure.communication.callautomation.implementation.models.ChannelAffinityInternal;
import com.azure.communication.callautomation.implementation.models.RecordingContentInternal;
import com.azure.communication.callautomation.implementation.models.RecordingFormatInternal;
import com.azure.communication.callautomation.implementation.models.RecordingChannelInternal;
import com.azure.communication.callautomation.implementation.models.StartCallRecordingRequestInternal;
import com.azure.communication.callautomation.models.CallLocator;
import com.azure.communication.callautomation.models.CallLocatorKind;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.DownloadToFileOptions;
import com.azure.communication.callautomation.models.GroupCallLocator;
import com.azure.communication.callautomation.models.ParallelDownloadOptions;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallRecordingAsync.
 */
public class CallRecordingAsync {
    private final ServerCallsImpl serverCallsInternal;
    private final ContentsImpl contentsInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;

    CallRecordingAsync(ServerCallsImpl serverCallsInternal, ContentsImpl contentsInternal,
                       ContentDownloader contentDownloader, HttpPipeline httpPipelineInternal, String resourceEndpoint) {
        this.serverCallsInternal = serverCallsInternal;
        this.contentsInternal = contentsInternal;
        this.contentDownloader = contentDownloader;
        this.httpPipelineInternal = httpPipelineInternal;
        this.resourceEndpoint = resourceEndpoint;
        this.logger = new ClientLogger(CallRecordingAsync.class);
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecordingStateResult> startRecording(StartRecordingOptions options) {
        return startRecordingWithResponse(options)
            .flatMap(response -> Mono.just(response.getValue()));
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecordingStateResult>> startRecordingWithResponse(StartRecordingOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        return startRecordingWithResponseInternal(options, null);
    }

    Mono<Response<RecordingStateResult>> startRecordingWithResponseInternal(StartRecordingOptions options, Context context) {
        try {
            String callbackUrl = options.getRecordingStateCallbackUrl();
            if (callbackUrl != null && !callbackUrl.isEmpty() && !Boolean.TRUE.equals(new URI(callbackUrl).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingRequestInternal request = getStartCallRecordingRequest(options);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return contentsInternal
                    .recordingWithResponseAsync(request, null, null, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                    .map(response ->
                        new SimpleResponse<>(response, RecordingStateResponseConstructorProxy.create(response.getValue()))
                    );
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex));
        }
    }

    private StartCallRecordingRequestInternal getStartCallRecordingRequest(StartRecordingOptions options) {
        CallLocator callLocator = options.getCallLocator();
        CallLocatorInternal callLocatorInternal = new CallLocatorInternal()
            .setKind(CallLocatorKindInternal.fromString(callLocator.getKind().toString()));

        if (callLocator.getKind() == CallLocatorKind.GROUP_CALL_LOCATOR) {
            callLocatorInternal.setGroupCallId(((GroupCallLocator) callLocator).getGroupCallId());
        } else if (callLocator.getKind() == CallLocatorKind.SERVER_CALL_LOCATOR) {
            callLocatorInternal.setServerCallId(((ServerCallLocator) callLocator).getServerCallId());
        } else {
            throw logger.logExceptionAsError(new InvalidParameterException("callLocator has invalid kind."));
        }

        StartCallRecordingRequestInternal request = new StartCallRecordingRequestInternal()
            .setCallLocator(callLocatorInternal);

        if (options.getRecordingContent() != null) {
            request.setRecordingContentType(RecordingContentInternal.fromString(options.getRecordingContent().toString()));
        }
        if (options.getRecordingFormat() != null) {
            request.setRecordingFormatType(RecordingFormatInternal.fromString(options.getRecordingFormat().toString()));
        }
        if (options.getRecordingChannel() != null) {
            request.setRecordingChannelType(RecordingChannelInternal.fromString(options.getRecordingChannel().toString()));
        }
        if (options.getRecordingStateCallbackUrl() != null) {
            request.setRecordingStateCallbackUri(options.getRecordingStateCallbackUrl());
        }
        if (options.getChannelAffinity() != null) {
            List<ChannelAffinityInternal> channelAffinityInternal = options.getChannelAffinity()
                .stream()
                .map(c -> new ChannelAffinityInternal()
                    .setChannel(c.getChannel())
                    .setParticipant(CommunicationIdentifierConverter.convert(c.getParticipant())))
                .collect(Collectors.toList());
            request.setChannelAffinity(channelAffinityInternal);
        }

        return request;
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
        return stopRecordingWithResponse(recordingId).then();
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
        return stopRecordingWithResponseInternal(recordingId, null);
    }

    Mono<Response<Void>> stopRecordingWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .stopRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
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
        return pauseRecordingWithResponse(recordingId).then();
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
        return pauseRecordingWithResponseInternal(recordingId, null);
    }

    Mono<Response<Void>> pauseRecordingWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .pauseRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
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
        return resumeRecordingWithResponse(recordingId).then();
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
        return resumeRecordingWithResponseInternal(recordingId, null);
    }

    Mono<Response<Void>> resumeRecordingWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .resumeRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
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
    public Mono<RecordingStateResult> getRecordingState(String recordingId) {
        return getRecordingStateWithResponse(recordingId).flatMap(response -> Mono.just(response.getValue()));
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
    public Mono<Response<RecordingStateResult>> getRecordingStateWithResponse(String recordingId) {
        return getRecordingStateWithResponseInternal(recordingId, null);
    }

    Mono<Response<RecordingStateResult>> getRecordingStateWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .getRecordingPropertiesWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                    .map(response ->
                        new SimpleResponse<>(response, RecordingStateResponseConstructorProxy.create(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param sourceEndpoint - URL where the content is located.
     * @return A {@link Flux} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream(String sourceEndpoint) {
        return downloadStreamWithResponse(sourceEndpoint, null)
            .map(Response::getValue)
            .flux()
            .flatMap(flux -> flux);
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
        return downloadStreamWithResponseInternal(sourceEndpoint, range, null);
    }

    Mono<Response<Flux<ByteBuffer>>> downloadStreamWithResponseInternal(String sourceEndpoint, HttpRange range, Context context) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return contentDownloader.downloadStreamWithResponse(sourceEndpoint, range, contextValue);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Reads the entire content.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStream(String)} ()} to download larger blobs.</p>
     *
     * @param sourceEndpoint - URL where the content is located.
     * @return A reactive response containing the content data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> downloadContent(String sourceEndpoint) {
        return downloadStreamWithResponse(sourceEndpoint, null)
            .flatMap(response -> BinaryData.fromFlux(response.getValue()));
    }

    /**
     * Reads a range of bytes from a content.
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStreamWithResponse(String, HttpRange)}
     * to download larger blobs.</p>
     *
     * @param sourceEndpoint - URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> downloadContentWithResponse(String sourceEndpoint, HttpRange range) {
        return downloadContentWithResponseInternal(sourceEndpoint, range, null);
    }

    Mono<Response<BinaryData>> downloadContentWithResponseInternal(String sourceEndpoint, HttpRange range, Context context) {
        return withContext(contextValue -> {
            contextValue = context == null ? contextValue : context;
            return downloadStreamWithResponseInternal(sourceEndpoint, range, contextValue)
                .flatMap(response -> BinaryData.fromFlux(response.getValue())
                    .map(data -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), data)));
        });
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @return Response for a successful downloadTo request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> downloadTo(
        String sourceEndpoint,
        Path destinationPath) {
        try {
            DownloadToFileOptions options = new DownloadToFileOptions();
            return downloadToWithResponse(sourceEndpoint, destinationPath, options).then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param options - an optional {@link DownloadToFileOptions} object to modify how the
     *                download will work.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        DownloadToFileOptions options) {
        return downloadToWithResponseInternal(sourceEndpoint, destinationPath, options, null);
    }

    Mono<Response<Void>> downloadToWithResponseInternal(
        String sourceEndpoint,
        Path destinationPath,
        DownloadToFileOptions options,
        Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");

        Set<OpenOption> openOptions = new HashSet<>();

        if (options.isOverwrite()) {
            openOptions.add(StandardOpenOption.CREATE);
        } else {
            openOptions.add(StandardOpenOption.CREATE_NEW);
        }
        openOptions.add(StandardOpenOption.WRITE);

        try {
            AsynchronousFileChannel file = AsynchronousFileChannel.open(destinationPath, openOptions, null);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return downloadToWithResponse(sourceEndpoint, destinationPath, file, options, contextValue);
            });
        } catch (IOException ex) {
            return monoError(logger, new RuntimeException(ex));
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
        AsynchronousFileChannel fileChannel,
        DownloadToFileOptions options,
        Context context
    ) {
        ParallelDownloadOptions finalParallelDownloadOptions =
            options.getParallelDownloadOptions() == null
                ? new ParallelDownloadOptions()
                : options.getParallelDownloadOptions();

        return Mono.just(fileChannel).flatMap(
                c -> contentDownloader.downloadToFileWithResponse(sourceEndpoint, c, finalParallelDownloadOptions, context))
            .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
            .doFinally(signalType -> contentDownloader.downloadToFileCleanup(fileChannel, destinationPath, signalType));
    }

    /**
     * Delete the content located at the deleteEndpoint
     * @param deleteEndpoint - ACS URL where the content is located.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for successful delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRecording(String deleteEndpoint) {
        try {
            return deleteRecordingWithResponseInternal(deleteEndpoint, null)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete the content located at the deleteEndpoint
     * Recording deletion will be done using parallel workers.
     * @param deleteEndpoint - ACS URL where the content is located.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @return Response for successful delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRecordingWithResponse(String deleteEndpoint) {
        return deleteRecordingWithResponseInternal(deleteEndpoint, null);
    }

    Mono<Response<Void>> deleteRecordingWithResponseInternal(String deleteEndpoint, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, deleteEndpoint);
        URL urlToSignWith = getUrlToSignRequestWith(deleteEndpoint);
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                contextValue = contextValue.addData("hmacSignatureURL", urlToSignWith);
                return httpPipelineInternal
                    .send(request, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                    .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private URL getUrlToSignRequestWith(String endpoint) {
        try {
            String path = new URL(endpoint).getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return new URL(resourceEndpoint + path);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException(ex));
        }
    }
}
