// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.HttpGetterInfo;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class automatically retries failed reads from a blob download stream.
 *
 * <p>
 * Failed downloads are automatically retried based on the {@link DownloadRetryOptions download options}, the retry
 * will be resumed from the point where the download failed. This allows for the download to be consumed as one
 * continuous stream.
 * </p>
 * @deprecated use {@link com.azure.core.util.FluxUtil#createRetriableDownloadFlux(Supplier, BiFunction, int)} instead.
 */
@Deprecated
final class ReliableDownload {
    private final ClientLogger logger = new ClientLogger(ReliableDownload.class);

    private static final Duration TIMEOUT_VALUE = Duration.ofSeconds(60);
    private final StreamResponse rawResponse;
    private final BlobsDownloadHeaders deserializedHeaders;
    private final DownloadRetryOptions options;
    private final HttpGetterInfo info;
    private final Function<HttpGetterInfo, Mono<ReliableDownload>> getter;

    ReliableDownload(StreamResponse rawResponse, DownloadRetryOptions options, HttpGetterInfo info,
                     Function<HttpGetterInfo, Mono<ReliableDownload>> getter) {
        StorageImplUtils.assertNotNull("getter", getter);
        StorageImplUtils.assertNotNull("info", info);
        // Note: We do not check for eTag since it is possible for the service to not return the etag on large downloads.

        this.rawResponse = rawResponse;
        this.deserializedHeaders = ModelHelper.transformBlobDownloadHeaders(rawResponse.getHeaders());
        this.options = (options == null) ? new DownloadRetryOptions() : options;
        this.info = info;
        this.getter = getter;
        /*
        If the customer did not specify a count, they are reading to the end of the blob. Extract this value
        from the response for better book keeping towards the end.
         */
        if (this.info.getCount() == null) {
            long blobLength = BlobAsyncClientBase.getBlobLength(
                ModelHelper.populateBlobDownloadHeaders(deserializedHeaders, ModelHelper.getErrorCode(rawResponse.getHeaders())));
            info.setCount(blobLength - info.getOffset());
        }
    }

    HttpRequest getRequest() {
        return rawResponse.getRequest();
    }

    int getStatusCode() {
        return rawResponse.getStatusCode();
    }

    HttpHeaders getHeaders() {
        return rawResponse.getHeaders();
    }

    BlobDownloadHeaders getDeserializedHeaders() {
        return ModelHelper.populateBlobDownloadHeaders(deserializedHeaders, ModelHelper.getErrorCode(rawResponse.getHeaders()));
    }

    Flux<ByteBuffer> getValue() {
        /*
        We pass -1 for currentRetryCount because we want tryContinueFlux to receive a value of 0 for number of
        retries as we have not actually retried yet, only made the initial try. Because applyReliableDownload() will
        add 1 before calling into tryContinueFlux, we set the initial value to -1.
         */
        Flux<ByteBuffer> value = (options.getMaxRetryRequests() == 0)
            ? rawResponse.getValue().timeout(TIMEOUT_VALUE)
            : applyReliableDownload(rawResponse.getValue(), -1, options);

        return value.switchIfEmpty(Flux.just(ByteBuffer.wrap(new byte[0])));
    }

    private Flux<ByteBuffer> tryContinueFlux(Throwable t, int retryCount, DownloadRetryOptions options) {
        // If all the errors are exhausted or the error is not retryable, return this error to the user.
        if (retryCount >= options.getMaxRetryRequests()
            || !(t instanceof IOException || t instanceof TimeoutException)) {
            return Flux.error(t);
        } else {
            /*
            We wrap this in a try catch because we don't know the behavior of the getter. Most errors would probably
            come from an unsuccessful request, which would be propagated through the onError methods. However, it is
            possible the method call that returns a Single is what throws (like how our apis throw some exceptions at
            call time rather than at subscription time.
             */
            try {
                /*Get a new stream from the new response and try reading from it.
                Do not compound the number of retries by calling getValue on the DownloadResponse; just get
                the raw body.
                */
                return getter.apply(info)
                    .flatMapMany(newResponse ->
                        applyReliableDownload(newResponse.rawResponse.getValue(), retryCount, options));
            } catch (Exception e) {
                // If the getter fails, return the getter failure to the user.
                return Flux.error(e);
            }
        }
    }

    private Flux<ByteBuffer> applyReliableDownload(Flux<ByteBuffer> data, int currentRetryCount,
        DownloadRetryOptions options) {
        return data
            .timeout(TIMEOUT_VALUE)
            .doOnNext(buffer -> {
                /*
                Update how much data we have received in case we need to retry and propagate to the user the data we
                have received.
                 */
                this.info.setOffset(this.info.getOffset() + buffer.remaining());
                if (this.info.getCount() != null) {
                    this.info.setCount(this.info.getCount() - buffer.remaining());
                }
            }).onErrorResume(t2 -> {
                /*
                 It is possible that the network stream will throw an error after emitting all data but before
                 completing. Issuing a retry at this stage would leave the download in a bad state with incorrect count
                 and offset values. Because we have read the intended amount of data, we can ignore the error at the end
                 of the stream.
                 */
                if (this.info.getCount() != null && this.info.getCount() == 0) {
                    logger.warning("Exception encountered in ReliableDownload after all data read from the network but "
                        + "but before stream signaled completion. Returning success as all data was downloaded. "
                        + "Exception message: " + t2.getMessage());
                    return Flux.empty();
                }
                // Increment the retry count and try again with the new exception.
                return tryContinueFlux(t2, currentRetryCount + 1, options);
            });
    }
}
