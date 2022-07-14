// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ContentsImpl;
import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.converters.ErrorConverter;
import com.azure.communication.callingserver.implementation.models.CallLocatorInternal;
import com.azure.communication.callingserver.implementation.models.CallLocatorKindInternal;
import com.azure.communication.callingserver.implementation.models.RecordingChannelInternal;
import com.azure.communication.callingserver.implementation.models.RecordingContentInternal;
import com.azure.communication.callingserver.implementation.models.RecordingFormatInternal;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequest;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.CallLocatorKind;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.GroupCallLocator;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.RecordingChannel;
import com.azure.communication.callingserver.models.RecordingContent;
import com.azure.communication.callingserver.models.RecordingFormat;
import com.azure.communication.callingserver.models.RecordingIdResponse;
import com.azure.communication.callingserver.models.RecordingStateResponse;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallRecordingAsyncClient.
 */
public class CallRecordingAsyncClient {
    private final ServerCallsImpl serverCallsInternal;
    private final ContentsImpl contentsInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;

    CallRecordingAsyncClient(ServerCallsImpl serverCallsInternal, ContentsImpl contentsInternal,
                             ContentDownloader contentDownloader, HttpPipeline httpPipelineInternal, String resourceEndpoint) {
        this.serverCallsInternal = serverCallsInternal;
        this.contentsInternal = contentsInternal;
        this.contentDownloader = contentDownloader;
        this.httpPipelineInternal = httpPipelineInternal;
        this.resourceEndpoint = resourceEndpoint;
        this.logger = new ClientLogger(CallRecordingAsyncClient.class);
    }

    /**
     * Start recording of the call.
     *
     * @param callLocator Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecordingIdResponse> startRecording(CallLocator callLocator, URI recordingStateCallbackUri) {
        try {
            if (!Boolean.TRUE.equals(recordingStateCallbackUri.isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingRequest requestWithCallLocator = getStartCallRecordingWithCallLocatorRequest(callLocator,
                recordingStateCallbackUri, null, null, null);

            return contentsInternal.recordingAsync(requestWithCallLocator, null)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .flatMap(result -> Mono.just(new RecordingIdResponse(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Start recording of the call.
     *
     * @param callLocator Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @param content Content Type
     * @param format format Type
     * @param channel Channel Type
     * @param context A {@link Context} representing the request context.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecordingIdResponse>> startRecordingWithResponse(
        CallLocator callLocator,
        URI recordingStateCallbackUri,
        RecordingContent content,
        RecordingFormat format,
        RecordingChannel channel,
        Context context) {
        try {
            if (!Boolean.TRUE.equals(recordingStateCallbackUri.isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingRequest requestWithCallLocator = getStartCallRecordingWithCallLocatorRequest(callLocator,
                recordingStateCallbackUri, content, format, channel);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return contentsInternal
                    .recordingWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                    .map(response -> new SimpleResponse<>(response, new RecordingIdResponse(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private StartCallRecordingRequest getStartCallRecordingWithCallLocatorRequest(CallLocator callLocator,
                                                                                  URI recordingStateCallbackUri,
                                                                                  RecordingContent content,
                                                                                  RecordingFormat format,
                                                                                  RecordingChannel channel) {
        CallLocatorInternal callLocatorInternal = new CallLocatorInternal()
            .setKind(CallLocatorKindInternal.fromString(callLocator.getKind().toString()));

        if (callLocator.getKind() == CallLocatorKind.GROUP_CALL_LOCATOR) {
            callLocatorInternal.setGroupCallId(((GroupCallLocator) callLocator).getGroupCallId());
        } else if (callLocator.getKind() == CallLocatorKind.SERVER_CALL_LOCATOR) {
            callLocatorInternal.setServerCallId(((ServerCallLocator) callLocator).getServerCallId());
        } else {
            throw logger.logExceptionAsError(new InvalidParameterException("callLocator has invalid kind."));
        }

        StartCallRecordingRequest request = new StartCallRecordingRequest()
            .setCallLocator(callLocatorInternal)
            .setRecordingStateCallbackUri(recordingStateCallbackUri.toString());

        if (content != null) {
            request.setRecordingContentType(RecordingContentInternal.fromString(content.toString()));
        }
        if (format != null) {
            request.setRecordingFormatType(RecordingFormatInternal.fromString(format.toString()));
        }
        if (channel != null) {
            request.setRecordingChannelType(RecordingChannelInternal.fromString(channel.toString()));
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
        try {
            return serverCallsInternal.stopRecordingAsync(recordingId)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
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
        return stopRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .stopRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConverter::translateException);
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
            return serverCallsInternal.pauseRecordingAsync(recordingId)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
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
        return pauseRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .pauseRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConverter::translateException);
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
            return serverCallsInternal.resumeRecordingAsync(recordingId)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
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
        return resumeRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .resumeRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConverter::translateException);
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
    public Mono<RecordingStateResponse> getRecordingState(String recordingId) {
        try {
            return serverCallsInternal.getRecordingPropertiesAsync(recordingId)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .flatMap(result -> Mono.just(new RecordingStateResponse(result)));
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
    public Mono<Response<RecordingStateResponse>> getRecordingStateWithResponse(String recordingId) {
        return getRecordingStateWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<RecordingStateResponse>> getRecordingStateWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallsInternal
                    .getRecordingPropertiesWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new RecordingStateResponse(response.getValue())));
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
            return contentDownloader.downloadStreamWithResponse(sourceEndpoint, httpRange, null)
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
            return contentDownloader.downloadStreamWithResponse(sourceEndpoint, range, null);
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
            return downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, null)
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
            return downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, null);
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
            .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
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
            return deleteRecordingWithResponse(deleteEndpoint, null)
                .onErrorMap(HttpResponseException.class, ErrorConverter::translateException)
                .then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete the content located at the deleteEndpoint
     * Recording deletion will be done using parallel workers.
     * @param deleteEndpoint - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @return Response for successful delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<HttpResponse>> deleteRecordingWithResponse(String deleteEndpoint, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, deleteEndpoint);
        URL urlToSignWith = getUrlToSignRequestWith(deleteEndpoint);
        Context finalContext;
        if (context == null) {
            finalContext = new Context("hmacSignatureURL", urlToSignWith);
        } else {
            finalContext = context.addData("hmacSignatureURL", urlToSignWith);
        }
        Mono<HttpResponse> httpResponse = httpPipelineInternal
            .send(request, finalContext)
            .onErrorMap(HttpResponseException.class, ErrorConverter::translateException);
        try {
            return httpResponse.map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
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
