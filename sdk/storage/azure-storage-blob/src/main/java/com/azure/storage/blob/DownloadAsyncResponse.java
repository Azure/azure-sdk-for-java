// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.ResponseBase;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * {@code DownloadAsyncResponse} wraps the protocol-layer response from {@link BlobAsyncClient#download(BlobRange,
 * ReliableDownloadOptions, BlobAccessConditions, boolean)} to automatically retry failed reads from the body as
 * appropriate. If the download is interrupted, the {@code DownloadAsyncResponse} will make a request to resume the download
 * from where it left off, allowing the user to consume the data as one continuous stream, for any interruptions are
 * hidden. The retry behavior is defined by the options passed to the {@link #body(ReliableDownloadOptions)}. The
 * download will also lock on the blob's etag to ensure consistency.
 * <p>
 * Note that the retries performed as a part of this reader are composed with those of any retries in an {@link
 * com.azure.core.http.HttpPipeline} used in conjunction with this reader. That is, if this object issues a request to resume a download,
 * an underlying pipeline may issue several retries as a part of that request. Furthermore, this reader only retries on
 * network errors; timeouts and unexpected status codes are not retried. Therefore, the behavior of this reader is
 * entirely independent of and in no way coupled to an {@link com.azure.core.http.HttpPipeline}'s retry mechanism.
 */
public final class DownloadAsyncResponse {
    private final HTTPGetterInfo info;

    private final ResponseBase<BlobDownloadHeaders, Flux<ByteBuf>> rawResponse;

    private final Function<HTTPGetterInfo, Mono<DownloadAsyncResponse>> getter;


    // The constructor is package-private because customers should not be creating their own responses.
    DownloadAsyncResponse(ResponseBase<BlobDownloadHeaders, Flux<ByteBuf>> response,
                          HTTPGetterInfo info, Function<HTTPGetterInfo, Mono<DownloadAsyncResponse>> getter) {
        Utility.assertNotNull("getter", getter);
        Utility.assertNotNull("info", info);
        Utility.assertNotNull("info.eTag", info.eTag());
        this.rawResponse = response;
        this.info = info;
        this.getter = getter;
    }

    /**
     * Returns the response body which has been modified to enable reliably reading data if desired (if
     * {@code options.maxRetryRequests > 0}. If retries are enabled, if a connection fails while reading, the stream
     * will make additional requests to reestablish a connection and continue reading.
     *
     * @param options {@link ReliableDownloadOptions}
     * @return A {@link Flux} which emits the data as {@link ByteBuf ByteBufs}
     */
    public Flux<ByteBuf> body(ReliableDownloadOptions options) {
        ReliableDownloadOptions optionsReal = options == null ? new ReliableDownloadOptions() : options;
        if (optionsReal.maxRetryRequests() == 0) {
            return this.rawResponse.value();
        }

        /*
        We pass -1 for currentRetryCount because we want tryContinueFlux to receive a value of 0 for number of
        retries as we have not actually retried yet, only made the initial try. Because applyReliableDownload() will
        add 1 before calling into tryContinueFlux, we set the initial value to -1.
         */
        return this.applyReliableDownload(this.rawResponse.value(), -1, optionsReal);
    }

    private Flux<ByteBuf> tryContinueFlux(Throwable t, int retryCount, ReliableDownloadOptions options) {
        // If all the errors are exhausted, return this error to the user.
        if (retryCount > options.maxRetryRequests() || !(t instanceof IOException)) {
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
                return getter.apply(this.info).flatMapMany(response -> this.applyReliableDownload(this.rawResponse.value(), retryCount, options));
            } catch (Exception e) {
                // If the getter fails, return the getter failure to the user.
                return Flux.error(e);
            }
        }
    }

    private Flux<ByteBuf> applyReliableDownload(Flux<ByteBuf> data, int currentRetryCount, ReliableDownloadOptions options) {
        return data.doOnNext(buffer -> {
            /*
            Update how much data we have received in case we need to retry and propagate to the user the data we
            have received.
             */
            this.info.offset(this.info.offset() + buffer.readableBytes()); // was `remaining()` in Rx world
            if (this.info.count() != null) {
                this.info.count(this.info.count() - buffer.readableBytes()); // was `remaining()` in Rx world
            }
        }).onErrorResume(t2 -> {
            // Increment the retry count and try again with the new exception.
            return tryContinueFlux(t2, currentRetryCount + 1, options);
        });
    }

    /**
     * @return HTTP status of the download
     */
    public int statusCode() {
        return this.rawResponse.statusCode();
    }

    /**
     * @return HTTP headers associated to the download
     */
    public BlobDownloadHeaders headers() {
        return this.rawResponse.deserializedHeaders();
    }

    /**
     * @return all HTTP headers from the response
     */
    public Map<String, String> rawHeaders() {
        return this.rawResponse.headers().toMap();
    }

    /**
     * @return the raw response
     */
    public ResponseBase<BlobDownloadHeaders, Flux<ByteBuf>> rawResponse() {
        return this.rawResponse;
    }
}
