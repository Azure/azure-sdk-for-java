// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Wraps any potential error responses from the service and applies post-processing of the response's eTag header to
 * standardize the value.
 */
public class ScrubEtagPolicy implements HttpPipelinePolicy {

    /**
     * Creates a new instance of {@link ScrubEtagPolicy}.
     */
    public ScrubEtagPolicy() {
    }

    /**
     * Wraps any potential error responses from the service and applies post-processing of the response's eTag header to
     * standardize the value.
     *
     * @return an updated response with post-processing steps applied.
     */
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpResponse response = next.processSync();
        return scrubETagHeader(response);
    }

    /**
     * Wraps any potential error responses from the service and applies post-processing of the response's eTag header to
     * standardize the value.
     *
     * @return an updated response with post-processing steps applied.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(response -> Mono.just(scrubETagHeader(response)));
    }


    /*
    The service is inconsistent in whether the eTag header value has quotes. This method will check if the
    response returns an eTag value, and if it does, remove any quotes that may be present to give the user a more
    predictable format to work with.
     */
    private HttpResponse scrubETagHeader(HttpResponse unprocessedResponse) {
        HttpHeader eTagHeader = unprocessedResponse.getHeaders().get(HttpHeaderName.ETAG);
        if (eTagHeader == null) {
            return unprocessedResponse;
        }

        String etag = eTagHeader.getValue();
        boolean startsWithQuote = etag.startsWith("\"");
        boolean endsWithQuote = etag.endsWith("\"");

        // Just mutate the unprocessed response and return it, callers won't have access to the unprocessed response
        // that was internal to the InnerHttpResponse that was previously being used.
        if (startsWithQuote && endsWithQuote) {
            unprocessedResponse.getHeaders().set(HttpHeaderName.ETAG, etag.substring(1, etag.length() - 1));
        } else if (startsWithQuote) {
            unprocessedResponse.getHeaders().set(HttpHeaderName.ETAG, etag.substring(1));
        } else if (endsWithQuote) {
            unprocessedResponse.getHeaders().set(HttpHeaderName.ETAG, etag.substring(0, etag.length() - 1));
        }
        return unprocessedResponse;
    }
}
