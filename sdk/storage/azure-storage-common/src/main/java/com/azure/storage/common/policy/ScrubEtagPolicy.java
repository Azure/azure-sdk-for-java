// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * Wraps any potential error responses from the service and applies post-processing of the response's eTag header to
 * standardize the value.
 */
public class ScrubEtagPolicy implements HttpPipelinePolicy {
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
    private static final String ETAG = "eTag";

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
        HttpHeader eTagHeader = unprocessedResponse.getHeaders().get(ETAG);
        if (eTagHeader == null) {
            return unprocessedResponse;
        }

        // Just mutate the unprocessed response and return it, callers won't have access to the unprocessed response
        // that was internal to the InnerHttpResponse that was previously being used.
        unprocessedResponse.getHeaders().set(eTagHeader.getName(),
            QUOTE_PATTERN.matcher(eTagHeader.getValue()).replaceAll(""));

        return unprocessedResponse;
    }
}
