// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallRecordingsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.RecordingStateResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CallLocatorInternal;
import com.azure.communication.callautomation.implementation.models.CallLocatorKindInternal;
import com.azure.communication.callautomation.implementation.models.ChannelAffinityInternal;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.RecordingChannelInternal;
import com.azure.communication.callautomation.implementation.models.RecordingContentInternal;
import com.azure.communication.callautomation.implementation.models.RecordingFormatInternal;
import com.azure.communication.callautomation.implementation.models.RecordingStorageInternal;
import com.azure.communication.callautomation.implementation.models.RecordingStorageTypeInternal;
import com.azure.communication.callautomation.implementation.models.StartCallRecordingRequestInternal;
import com.azure.communication.callautomation.models.AzureBlobContainerRecordingStorage;
import com.azure.communication.callautomation.models.CallLocator;
import com.azure.communication.callautomation.models.CallLocatorKind;
import com.azure.communication.callautomation.models.ChannelAffinity;
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
public final class CallRecordingAsync {
    private final CallRecordingsImpl callRecordingsInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceUrl;

    CallRecordingAsync(CallRecordingsImpl callRecordingsInternal, ContentDownloader contentDownloader,
                       HttpPipeline httpPipelineInternal, String resourceUrl) {
        this.callRecordingsInternal = callRecordingsInternal;
        this.contentDownloader = contentDownloader;
        this.httpPipelineInternal = httpPipelineInternal;
        this.resourceUrl = resourceUrl;
        this.logger = new ClientLogger(CallRecordingAsync.class);
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecordingStateResult> start(StartRecordingOptions options) {
        return startWithResponse(options)
            .flatMap(response -> Mono.just(response.getValue()));
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecordingStateResult>> startWithResponse(StartRecordingOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        return startWithResponseInternal(options, null);
    }

    Mono<Response<RecordingStateResult>> startWithResponseInternal(StartRecordingOptions options, Context context) {
        try {
            StartCallRecordingRequestInternal request = getStartCallRecordingRequest(options);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callRecordingsInternal
                    .startRecordingWithResponseAsync(
                        request,
                        contextValue)
                    .map(response ->
                        new SimpleResponse<>(response, RecordingStateResponseConstructorProxy.create(response.getValue()))
                    );
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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
        if (options.getRecordingStorage() != null) {
            if (options.getRecordingStorage() instanceof AzureBlobContainerRecordingStorage) {
                AzureBlobContainerRecordingStorage blobStorage = (AzureBlobContainerRecordingStorage) options.getRecordingStorage();
                RecordingStorageInternal recordingStorageInternal = new RecordingStorageInternal()
                    .setRecordingDestinationContainerUrl(blobStorage.getRecordingDestinationContainerUrl())
                    .setRecordingStorageKind(RecordingStorageTypeInternal.AZURE_BLOB_STORAGE);
                request.setExternalStorage(recordingStorageInternal);
            }
        }
        if (options.getAudioChannelParticipantOrdering() != null) {
            List<CommunicationIdentifierModel> audioChannelParticipantOrdering = options.getAudioChannelParticipantOrdering()
                .stream().map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList());
            request.setAudioChannelParticipantOrdering(audioChannelParticipantOrdering);
        }
        if (options.getChannelAffinity() != null) {
            List<ChannelAffinityInternal> channelAffinityInternals = options.getChannelAffinity()
                .stream().map(this::getChannelAffinityInternal)
                .collect(Collectors.toList());
            request.setChannelAffinity(channelAffinityInternals);
        }
        if (options.isPauseOnStart() != null) {
            request.setPauseOnStart(options.isPauseOnStart());
        }

        return request;
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stop(String recordingId) {
        return stopWithResponse(recordingId).then();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopWithResponse(String recordingId) {
        return stopWithResponseInternal(recordingId, null);
    }

    Mono<Response<Void>> stopWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callRecordingsInternal
                    .stopRecordingWithResponseAsync(recordingId, contextValue);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> pause(String recordingId) {
        return pauseWithResponse(recordingId).then();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> pauseWithResponse(String recordingId) {
        return pauseWithResponseInternal(recordingId, null);
    }

    Mono<Response<Void>> pauseWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callRecordingsInternal
                    .pauseRecordingWithResponseAsync(recordingId, contextValue);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resume(String recordingId) {
        return resumeWithResponse(recordingId).then();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeWithResponse(String recordingId) {
        return resumeWithResponseInternal(recordingId, null);
    }

    Mono<Response<Void>> resumeWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callRecordingsInternal
                    .resumeRecordingWithResponseAsync(recordingId, contextValue);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get current recording state by recording id.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecordingStateResult> getState(String recordingId) {
        return getStateWithResponse(recordingId).flatMap(response -> Mono.just(response.getValue()));
    }

    /**
     * Get current recording state by recording id.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecordingStateResult>> getStateWithResponse(String recordingId) {
        return getStateWithResponseInternal(recordingId, null);
    }

    Mono<Response<RecordingStateResult>> getStateWithResponseInternal(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callRecordingsInternal
                    .getRecordingPropertiesWithResponseAsync(recordingId, contextValue)
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
     * @param sourceUrl - URL where the content is located.
     * @return A {@link Flux} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream(String sourceUrl) {
        return downloadStreamWithResponse(sourceUrl, null)
            .map(Response::getValue)
            .flux()
            .flatMap(flux -> flux);
    }

    /**
     * Download the recording content, (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}.
     * @param sourceUrl - URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Mono} object containing a {@link Response} with the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> downloadStreamWithResponse(String sourceUrl, HttpRange range) {
        return downloadStreamWithResponseInternal(sourceUrl, range, null);
    }

    Mono<Response<Flux<ByteBuffer>>> downloadStreamWithResponseInternal(String sourceUrl, HttpRange range, Context context) {
        try {
            Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return contentDownloader.downloadStreamWithResponse(sourceUrl, range, contextValue);
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
     * @param sourceUrl - URL where the content is located.
     * @return A reactive response containing the content data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> downloadContent(String sourceUrl) {
        return downloadStreamWithResponse(sourceUrl, null)
            .flatMap(response -> BinaryData.fromFlux(response.getValue()));
    }

    /**
     * Reads a range of bytes from a content.
     *
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadStreamWithResponse(String, HttpRange)}
     * to download larger blobs.</p>
     *
     * @param sourceUrl - URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A reactive response containing the blob data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> downloadContentWithResponse(String sourceUrl, HttpRange range) {
        return downloadContentWithResponseInternal(sourceUrl, range, null);
    }

    Mono<Response<BinaryData>> downloadContentWithResponseInternal(String sourceUrl, HttpRange range, Context context) {
        return withContext(contextValue -> {
            contextValue = context == null ? contextValue : context;
            return downloadStreamWithResponseInternal(sourceUrl, range, contextValue)
                .flatMap(response -> BinaryData.fromFlux(response.getValue())
                    .map(data -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), data)));
        });
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @return Response for a successful downloadTo request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> downloadTo(
        String sourceUrl,
        Path destinationPath) {
        try {
            DownloadToFileOptions options = new DownloadToFileOptions();
            return downloadTo(sourceUrl, destinationPath, options).then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param options - an optional {@link DownloadToFileOptions} object to modify how the
     *                download will work.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> downloadTo(
        String sourceUrl,
        Path destinationPath,
        DownloadToFileOptions options) {
        return downloadToInternal(sourceUrl, destinationPath, options, null);
    }

    Mono<Void> downloadToInternal(
        String sourceUrl,
        Path destinationPath,
        DownloadToFileOptions options,
        Context context) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null");
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
                return downloadTo(sourceUrl, destinationPath, file, options, contextValue);
            });
        } catch (IOException ex) {
            return monoError(logger, new RuntimeException(ex));
        }
    }

    Mono<Void> downloadTo(
        String sourceUrl,
        OutputStream destinationStream,
        HttpRange httpRange,
        Context context) {

        return contentDownloader.downloadToStreamWithResponse(sourceUrl, destinationStream, httpRange, context).then();
    }

    Mono<Void> downloadTo(
        String sourceUrl,
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
                c -> contentDownloader.downloadToFileWithResponse(sourceUrl, c, finalParallelDownloadOptions, context))

            .doFinally(signalType -> contentDownloader.downloadToFileCleanup(fileChannel, destinationPath, signalType))
            .then();
    }

    /**
     * Delete the content located at the deleteUrl
     * @param deleteUrl - ACS URL where the content is located.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for successful delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete(String deleteUrl) {
        try {
            return deleteWithResponseInternal(deleteUrl, null).then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete the content located at the deleteUrl
     * Recording deletion will be done using parallel workers.
     * @param deleteUrl - ACS URL where the content is located.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @return Response for successful delete request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(String deleteUrl) {
        return deleteWithResponseInternal(deleteUrl, null);
    }

    Mono<Response<Void>> deleteWithResponseInternal(String deleteUrl, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, deleteUrl);
        URL urlToSignWith = getUrlToSignRequestWith(deleteUrl);
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                contextValue = contextValue.addData("hmacSignatureURL", urlToSignWith);
                return httpPipelineInternal
                    .send(request, contextValue)
                    .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private URL getUrlToSignRequestWith(String url) {
        try {
            String path = new URL(url).getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return new URL(resourceUrl + path);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException(ex));
        }
    }

    private ChannelAffinityInternal getChannelAffinityInternal(ChannelAffinity channelAffinity) {
        ChannelAffinityInternal channelAffinityInternal = new ChannelAffinityInternal();
        CommunicationIdentifierModel communicationIdentifierModel = CommunicationIdentifierConverter.convert(channelAffinity.getParticipant());
        channelAffinityInternal.setParticipant(communicationIdentifierModel);
        if (channelAffinity.getChannel() != null) {
            channelAffinityInternal.setChannel(channelAffinity.getChannel());
        }
        return channelAffinityInternal;
    }
}
