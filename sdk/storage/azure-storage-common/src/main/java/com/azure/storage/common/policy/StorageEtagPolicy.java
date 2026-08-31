// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

/**
 * Standardizes Storage ETag values at the HTTP boundary.
 *
 * <p>Storage clients historically removed quotes from service-returned ETags. To preserve that customer-visible
 * behavior while producing RFC 9110-compliant conditional requests, this policy accepts those legacy values on requests,
 * adds quotes before authentication, and continues removing quotes from responses.</p>
 */
public class StorageEtagPolicy extends ScrubEtagPolicy {

    /**
     * Creates a new instance of {@link StorageEtagPolicy}.
     */
    public StorageEtagPolicy() {
    }

    /**
     * Normalizes request ETag conditions and applies post-processing to the response ETag header.
     *
     * @return an updated response with post-processing steps applied.
     */
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        normalizeRequestETagHeaders(context.getHttpRequest().getHeaders());
        return super.processSync(context, next);
    }

    /**
     * Normalizes request ETag conditions and applies post-processing to the response ETag header.
     *
     * @return an updated response with post-processing steps applied.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        normalizeRequestETagHeaders(context.getHttpRequest().getHeaders());
        return super.process(context, next);
    }

    private static void normalizeRequestETagHeaders(HttpHeaders headers) {
        normalizeRequestETagHeader(headers, HttpHeaderName.IF_MATCH);
        normalizeRequestETagHeader(headers, HttpHeaderName.IF_NONE_MATCH);
        normalizeRequestETagHeader(headers, Constants.HeaderConstants.SOURCE_IF_MATCH);
        normalizeRequestETagHeader(headers, Constants.HeaderConstants.SOURCE_IF_NONE_MATCH);
        normalizeRequestETagHeader(headers, Constants.HeaderConstants.BLOB_IF_MATCH);
        normalizeRequestETagHeader(headers, Constants.HeaderConstants.BLOB_IF_NONE_MATCH);
    }

    private static void normalizeRequestETagHeader(HttpHeaders headers, HttpHeaderName headerName) {
        String value = headers.getValue(headerName);
        if (value != null) {
            // An unquoted value may have come from this policy's response processing, so retain support without warning
            // while restoring the protocol-required representation at the HTTP boundary.
            headers.set(headerName, StorageImplUtils.toETagHeaderValue(value));
        }
    }

}
