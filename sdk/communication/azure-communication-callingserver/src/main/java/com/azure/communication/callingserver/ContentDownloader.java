// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.Constants;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static java.lang.StrictMath.toIntExact;

class ContentDownloader {
    private final String resourceEndpoint;
    private final HttpPipeline httpPipeline;
    private final ClientLogger logger = new ClientLogger(ContentDownloader.class);

    ContentDownloader(String resourceEndpoint, HttpPipeline httpPipeline) {
        this.resourceEndpoint = resourceEndpoint;
        this.httpPipeline = httpPipeline;
    }

    Mono<Response<Void>> downloadToStreamWithResponse(
        String sourceEndpoint,
        OutputStream destinationStream,
        HttpRange httpRange,
        Context context) {
        return downloadStreamWithResponse(sourceEndpoint, httpRange, context)
            .flatMap(response -> response.getValue().reduce(destinationStream, (outputStream, buffer) -> {
                try {
                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
                    return outputStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), null)));
    }

    Mono<Response<Flux<ByteBuffer>>> downloadStreamWithResponse(
        String sourceEndpoint,
        HttpRange httpRange,
        Context context) {
        Mono<HttpResponse> httpResponse = makeDownloadRequest(sourceEndpoint, httpRange, context);
        return httpResponse.map(response -> {
            Flux<ByteBuffer> result = getFluxStream(response, sourceEndpoint, httpRange, context);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), result);
        });
    }

    Mono<Response<Void>> downloadToFileWithResponse(
        String sourceEndpoint,
        AsynchronousFileChannel destinationFile,
        ParallelDownloadOptions parallelDownloadOptions,
        Context context) {
        Lock progressLock = new ReentrantLock();
        AtomicLong totalProgress = new AtomicLong(0);

        Function<HttpRange, Mono<Response<Flux<ByteBuffer>>>> downloadFunc =
            range -> downloadStreamWithResponse(sourceEndpoint, range, context);

        return downloadFirstChunk(parallelDownloadOptions, downloadFunc)
            .flatMap(setupTuple2 -> {
                long newCount = setupTuple2.getT1();
                int numChunks = calculateNumBlocks(newCount, parallelDownloadOptions.getBlockSize());

                // In case it is an empty blob, this ensures we still actually perform a download operation.
                numChunks = numChunks == 0 ? 1 : numChunks;

                Response<Flux<ByteBuffer>> initialResponse = setupTuple2.getT2();
                return Flux.range(0, numChunks)
                    .flatMap(chunkNum -> downloadChunk(chunkNum, initialResponse,
                        parallelDownloadOptions, newCount, downloadFunc,
                        response ->
                            writeBodyToFile(response, destinationFile, chunkNum,
                                parallelDownloadOptions, progressLock, totalProgress).flux()))
                    .then(Mono.just(new SimpleResponse<>(initialResponse, null)));
            });
    }

    private Flux<ByteBuffer> getFluxStream(
        HttpResponse httpResponse,
        String sourceEndpoint,
        HttpRange httpRange,
        Context context) {
        return FluxUtil.createRetriableDownloadFlux(
            () -> getResponseBody(httpResponse),
            (Throwable throwable, Long aLong) -> {
                if (throwable instanceof CallingServerErrorException) {
                    CallingServerErrorException exception = (CallingServerErrorException) throwable;
                    if (exception.getResponse().getStatusCode() == 416) {
                        return  makeDownloadRequest(sourceEndpoint, null, context)
                            .map(this::getResponseBody)
                            .flux()
                            .flatMap(flux -> flux);
                    }
                }

                HttpRange range;
                if (httpRange != null) {
                    range = new HttpRange(aLong + 1, httpRange.getLength() - aLong - 1);
                } else {
                    range = new HttpRange(aLong + 1);
                }

                return makeDownloadRequest(sourceEndpoint, range, context)
                    .map(this::getResponseBody)
                    .flux()
                    .flatMap(flux -> flux);
            },
            Constants.ContentDownloader.MAX_RETRIES
        );
    }

    private Flux<ByteBuffer> getResponseBody(HttpResponse response) {
        switch (response.getStatusCode()) {
            case 200:
            case 206:
                return response.getBody();
            case 416:   // Retriable with new HttpRange, potentially bytes=0-
                return FluxUtil.fluxError(logger,
                    new CallingServerErrorException(formatExceptionMessage(response), response));
            default:
                throw logger.logExceptionAsError(
                    new CallingServerErrorException(formatExceptionMessage(response), response)
                );
        }
    }

    private String formatExceptionMessage(HttpResponse httpResponse) {
        return String.format("Service Request failed!%nStatus: %s", httpResponse.getStatusCode());
    }

    private Mono<HttpResponse> makeDownloadRequest(
        String sourceEndpoint,
        HttpRange httpRange,
        Context context) {
        HttpRequest request = getHttpRequest(sourceEndpoint, httpRange);
        URL urlToSignWith = getUrlToSignRequestWith(sourceEndpoint);

        Context finalContext;
        if (context == null) {
            finalContext = new Context("hmacSignatureURL", urlToSignWith);
        } else {
            finalContext = context.addData("hmacSignatureURL", urlToSignWith);
        }

        return httpPipeline.send(request, finalContext);
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

    private HttpRequest getHttpRequest(String sourceEndpoint, HttpRange httpRange) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, sourceEndpoint);

        if (null != httpRange) {
            request.setHeader(Constants.HeaderNames.RANGE, httpRange.toString());
        }

        return request;
    }

    private Mono<Tuple2<Long, Response<Flux<ByteBuffer>>>> downloadFirstChunk(
        ParallelDownloadOptions parallelDownloadOptions,
        Function<HttpRange, Mono<Response<Flux<ByteBuffer>>>> downloader) {
        return downloader.apply(new HttpRange(0, parallelDownloadOptions.getBlockSize()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(response -> {
                // Extract the total length of the blob from the contentRange header. e.g. "bytes 1-6/7"
                long totalLength = extractTotalBlobLength(
                    response.getHeaders().getValue(Constants.HeaderNames.CONTENT_RANGE)
                );

                return Mono.zip(Mono.just(totalLength), Mono.just(response));
            });
    }

    private long extractTotalBlobLength(String contentRange) {
        return contentRange == null ? 0 : Long.parseLong(contentRange.split("/")[1]);
    }

    private int calculateNumBlocks(long dataSize, long blockLength) {
        // Can successfully cast to an int because MaxBlockSize is an int, which this expression must be less than.
        int numBlocks = toIntExact(dataSize / blockLength);
        // Include an extra block for trailing data.
        if (dataSize % blockLength != 0) {
            numBlocks++;
        }
        return numBlocks;
    }

    private <T> Flux<T> downloadChunk(
        Integer chunkNum,
        Response<Flux<ByteBuffer>> initialResponse,
        ParallelDownloadOptions parallelDownloadOptions,
        long newCount,
        Function<HttpRange, Mono<Response<Flux<ByteBuffer>>>> downloader,
        Function<Response<Flux<ByteBuffer>>, Flux<T>> returnTransformer) {
        if (chunkNum == 0) {
            return returnTransformer.apply(initialResponse);
        }

        // Calculate whether we need a full chunk or something smaller because we are at the end.
        long modifier = chunkNum.longValue() * parallelDownloadOptions.getBlockSize();
        long chunkSizeActual = Math.min(parallelDownloadOptions.getBlockSize(),
            newCount - modifier);
        HttpRange chunkRange = new HttpRange(modifier, chunkSizeActual);

        // Make the download call.
        return downloader.apply(chunkRange)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(returnTransformer);
    }

    private static Mono<Void> writeBodyToFile(
        Response<Flux<ByteBuffer>> response,
        AsynchronousFileChannel file,
        long chunkNum,
        ParallelDownloadOptions parallelDownloadOptions,
        Lock progressLock,
        AtomicLong totalProgress) {
        // Extract the body.
        Flux<ByteBuffer> data = response.getValue();

        // Report progress as necessary.
        data = ProgressReporter.addParallelProgressReporting(data,
            parallelDownloadOptions.getProgressReceiver(), progressLock, totalProgress);

        // Write to the file.
        return FluxUtil.writeFile(data, file, chunkNum * parallelDownloadOptions.getBlockSize());
    }

    void downloadToFileCleanup(AsynchronousFileChannel channel, Path filePath, SignalType signalType) {
        try {
            channel.close();
            if (!signalType.equals(SignalType.ON_COMPLETE)) {
                Files.deleteIfExists(filePath);
                logger.verbose("Downloading to file failed. Cleaning up resources.");
            }
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
