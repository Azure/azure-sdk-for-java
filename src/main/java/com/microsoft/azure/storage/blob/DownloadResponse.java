/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders;
import com.microsoft.azure.storage.blob.models.BlobDownloadResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * {@code DownloadResponse} wraps the protocol-layer response from {@link BlobURL#download(BlobRange,
 * BlobAccessConditions, boolean)} to help provide information for retrying.
 */
public class DownloadResponse  {
    private BlobURL blobURL;

    private RetryReader.HTTPGetterInfo info;

    private BlobDownloadResponse rawResponse;

    private final Function<RetryReader.HTTPGetterInfo, Single<DownloadResponse>> getter = newInfo ->
            this.blobURL.download(new BlobRange().withOffset(newInfo.offset).withCount(newInfo.count),
                    new BlobAccessConditions(new HTTPAccessConditions(null, null, info.eTag, null),
                            null, null, null), false);

    DownloadResponse(BlobDownloadResponse response, BlobURL blobURL, RetryReader.HTTPGetterInfo info) {
        info = info == null ? new RetryReader.HTTPGetterInfo() : info;
        if (info.count != null) {
            Utility.assertInBounds("info.count", info.count, 0, Integer.MAX_VALUE);
        }
        this.rawResponse = response;
        this.blobURL = blobURL;
        this.info = info;
    }

    /**
     * Constructs a new {@link RetryReader} stream for reliably reading data if desired (if {@code options != null} and
     * {@code options.maxRetryRequests > 0}. If retries are enabled, if a connection fails while reading, the {@code
     * RetryReader} will make additional requests to reestablish a connection and continue reading.
     *
     * @param options
     *         {@link RetryReaderOptions}
     * @return A {@code Flowable} which emits the data as {@code ByteBuffer}s.
     */
    public Flowable<ByteBuffer> body(RetryReaderOptions options) {
        RetryReaderOptions optionsReal = options == null ? new RetryReaderOptions() : options;
        Utility.assertInBounds("options.maxRetryRequests", optionsReal.maxRetryRequests(), 0, Integer.MAX_VALUE);
        if (optionsReal.maxRetryRequests() == 0) {
            return this.rawResponse.body();
        }

        return this.rawResponse.body()
                /*
                Update how much data we have received in case we need to retry and propagate to the user the data we
                have received.
                 */
                .doOnNext(buffer -> {
                    this.info.offset += buffer.remaining();
                    if (info.count != null) {
                        this.info.count -= buffer.remaining();
                    }
                })
                .onErrorResumeNext(throwable -> {
                    return tryContinueFlowable(throwable, 1, optionsReal);
                });


        /*return new RetryReader(Single.just(this), this.info, options,
                newInfo -> this.blobURL.download(new BlobRange(newInfo.offset, newInfo.count), new BlobAccessConditions(
                        new HTTPAccessConditions(null, null, info.eTag, null),
                        null, null, null),
                        false));*/
    }

    private Flowable<ByteBuffer> tryContinueFlowable(Throwable t, int retryCount, RetryReaderOptions options) {
        // If all the errors are exhausted, return this error to the user.
        if (retryCount > options.maxRetryRequests() || !(t instanceof IOException)) {
            return Flowable.error(t);
        }
        else {
            try {
                // Get a new response and try reading from it.
                return getter.apply(this.info)
                        .flatMapPublisher(response ->
                            // Do not compound the number of retries.
                            response.body()
                                    .onErrorResumeNext(t2 -> {
                                        // Increment the retry count and try again with the new exception.
                                        return tryContinueFlowable(t2, retryCount + 1, options);
                                    }));
            } catch (Exception e) {
                // If the getter fails, return the getter failure to the user.
                return Flowable.error(e);
            }
        }
    }

    public int statusCode() {
        return this.rawResponse.statusCode();
    }

    public BlobDownloadHeaders headers() {
        return this.rawResponse.headers();
    }

    public Map<String, String> rawHeaders() {
        return this.rawResponse.rawHeaders();
    }

    /**
     * Equivalent to calling {@link #body(RetryReaderOptions)} with {@code null}.
     */
    public Flowable<ByteBuffer> body() {
        return this.body(null);
    }

    /**
     * Disposes of the connection associated with this stream response.
     */
    public void close() {
        // Taken from BlobsDownloadResponse.
        this.rawResponse.body().subscribe(Functions.emptyConsumer(), Functions.<Throwable>emptyConsumer()).dispose();
    }
}
