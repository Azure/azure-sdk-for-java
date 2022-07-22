// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.StreamResponse;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.DownloadRetryOptions;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/**
 * Helper class to access private values of {@link BlobDownloadAsyncResponse} across package boundaries.
 */
public final class BlobDownloadAsyncResponseConstructorProxy {
    private static BlobDownloadAsyncResponseConstructorProxy.BlobDownloadAsyncResponseConstructorAccessor accessor;

    private BlobDownloadAsyncResponseConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link BlobDownloadAsyncResponseConstructorProxy.BlobDownloadAsyncResponseConstructorAccessor}
     * instance.
     */
    public interface BlobDownloadAsyncResponseConstructorAccessor {
        /**
         * Creates a {@link BlobDownloadAsyncResponse}.
         *
         * @param sourceResponse The initial Stream Response
         * @param onErrorResume Function used to resume.
         * @param retryOptions Retry options.
         */
        BlobDownloadAsyncResponse create(StreamResponse sourceResponse,
                                         BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
                                         DownloadRetryOptions retryOptions);
    }

    /**
     * The method called from {@link BlobDownloadAsyncResponse} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlobDownloadAsyncResponseConstructorProxy.BlobDownloadAsyncResponseConstructorAccessor accessor) {
        BlobDownloadAsyncResponseConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a {@link BlobDownloadAsyncResponse}.
     *
     * @param sourceResponse The initial Stream Response
     * @param onErrorResume Function used to resume.
     * @param retryOptions Retry options.
     */
    public static BlobDownloadAsyncResponse create(StreamResponse sourceResponse,
                                                   BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
                                                   DownloadRetryOptions retryOptions) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadAsyncResponse which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new BlobDownloadAsyncResponse(
                new HttpRequest(HttpMethod.GET, "http://microsoft.com"),
                200, new HttpHeaders(), null, null);
        }

        assert accessor != null;
        return accessor.create(sourceResponse, onErrorResume, retryOptions);
    }
}
