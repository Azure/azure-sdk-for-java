// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.HttpGetterInfo;
import com.azure.storage.blob.implementation.models.BlobsDownloadResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * This class automatically retries failed reads from a blob download stream.
 *
 * <p>
 * Failed downloads are automatically retried based on the {@link DownloadRetryOptions download options}, the retry
 * will be resumed from the point where the download failed. This allows for the download to be consumed as one
 * continuous stream.
 * </p>
 */
final class ReliableDownload {
    private final BlobsDownloadResponse rawResponse;
    private final DownloadRetryOptions options;
    private final HttpGetterInfo info;
    private final Function<HttpGetterInfo, Mono<ReliableDownload>> getter;

    ReliableDownload(BlobsDownloadResponse rawResponse, DownloadRetryOptions options, HttpGetterInfo info,
                     Function<HttpGetterInfo, Mono<ReliableDownload>> getter) {
        StorageImplUtils.assertNotNull("getter", getter);
        StorageImplUtils.assertNotNull("info", info);
        StorageImplUtils.assertNotNull("info.eTag", info.getETag());

        this.rawResponse = rawResponse;
        this.options = (options == null) ? new DownloadRetryOptions() : options;
        this.info = info;
        this.getter = getter;
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
        return rawResponse.getDeserializedHeaders();
    }

    Flux<ByteBuffer> getValue() {
        /*
        We pass -1 for currentRetryCount because we want tryContinueFlux to receive a value of 0 for number of
        retries as we have not actually retried yet, only made the initial try. Because applyReliableDownload() will
        add 1 before calling into tryContinueFlux, we set the initial value to -1.
         */
        Flux<ByteBuffer> value = (options.getMaxRetryRequests() == 0)
            ? rawResponse.getValue()
            : applyReliableDownload(rawResponse.getValue(), -1, options);

        return value.switchIfEmpty(Flux.just(ByteBuffer.wrap(new byte[0])));
    }

    private Flux<ByteBuffer> tryContinueFlux(Throwable t, int retryCount, DownloadRetryOptions options) {
        // If all the errors are exhausted, return this error to the user.
        if (retryCount > options.getMaxRetryRequests() || !(t instanceof IOException)) {
            return Flux.error(t);
        } else {
            /*
            We wrap this in a try catch because we don't know the behavior of the getter. Most errors would probably
            come from an unsuccessful request, which would be propagated through the onError methods. However, it is
            possible the method call that returns a Single is what throws (like how our apis throw some exceptions at
            call time rather than at subscription time.
             */
            try {
                /*Get a new response and try reading from it.
                Do not compound the number of retries by passing in another set of downloadOptions; just get
                the raw body.
                */
                return getter.apply(info)
                    .flatMapMany(ignored -> applyReliableDownload(rawResponse.getValue(), retryCount, options));
            } catch (Exception e) {
                // If the getter fails, return the getter failure to the user.
                return Flux.error(e);
            }
        }
    }

    private Flux<ByteBuffer> applyReliableDownload(Flux<ByteBuffer> data, int currentRetryCount,
        DownloadRetryOptions options) {
        return data.doOnNext(buffer -> {
            /*
            Update how much data we have received in case we need to retry and propagate to the user the data we
            have received.
             */
            this.info.setOffset(this.info.getOffset() + buffer.remaining());
            if (this.info.getCount() != null) {
                this.info.setCount(this.info.getCount() - buffer.remaining());
            }
        }).onErrorResume(t2 -> {
            // Increment the retry count and try again with the new exception.
            return tryContinueFlux(t2, currentRetryCount + 1, options);
        });
    }
}
