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
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.internal.functions.Functions;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * {@code DownloadResponse} wraps the protocol-layer response from {@link BlobURL#download(BlobRange,
 * BlobAccessConditions, boolean)} to help provide information for retrying.
 */
public class DownloadResponse extends RestResponse<BlobDownloadHeaders, Flowable<ByteBuffer>> implements Closeable {
    private BlobURL blobURL;

    private RetryReader.HTTPGetterInfo info;

    DownloadResponse(int statusCode, BlobDownloadHeaders blobsDownloadHeaders, Map<String, String> rawHeaders,
            Flowable<ByteBuffer> byteBufferFlowable) {
        super(statusCode, blobsDownloadHeaders, rawHeaders, byteBufferFlowable);
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
        options = options == null ? new RetryReaderOptions() : options;
        if (options.maxRetryRequests == 0) {
            return super.body();
        }
        return new RetryReader(Single.just(this), this.info, options,
                newInfo -> this.blobURL.download(new BlobRange(newInfo.offset, newInfo.count), new BlobAccessConditions(
                        new HTTPAccessConditions(null, null, info.eTag, null),
                        null, null, null),
                        false));
    }

    @Override
    public int statusCode() {
        return super.statusCode();
    }

    @Override
    public BlobDownloadHeaders headers() {
        return super.headers();
    }

    @Override
    public Map<String, String> rawHeaders() {
        return super.rawHeaders();
    }

    /**
     * Equivalent to calling {@link #body(RetryReaderOptions)} with {@code null}.
     */
    @Override
    public Flowable<ByteBuffer> body() {
        return this.body(null);
    }

    /**
     * Disposes of the connection associated with this stream response.
     */
    @Override
    public void close() {
        // Taken from BlobsDownloadResponse.
        body().subscribe(Functions.emptyConsumer(), Functions.<Throwable>emptyConsumer()).dispose();
    }
}
