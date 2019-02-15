/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.NextPolicy;
import com.microsoft.rest.v3.protocol.HttpResponseDecoder;
import reactor.core.publisher.Mono;

/**
 * The Pipeline policy that decodes the response body and headers.
 */
public class DecodingPolicy implements HttpPipelinePolicy {
    private HttpResponseDecoder responseDecoder;

    /**
     * Creates DecodingPolicy.
     *
     * @param responseDecoder the response responseDecoder
     */
    public DecodingPolicy(HttpResponseDecoder responseDecoder) {
        this.responseDecoder = responseDecoder;
    }

    /**
     * Creates DecodingPolicy that uses decoder set in request to decode the response.
     */
    public DecodingPolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        return next.process().flatMap(response -> {
            if (context.httpRequest().responseDecoder() != null) {
                return context.httpRequest().responseDecoder().decode(response);
            }
            if (responseDecoder != null) {
                return responseDecoder.decode(response);
            } else {
                return Mono.error(new NullPointerException("HttpRequest.responseDecoder() and DecodingPolicy.responseDecoder was null when decoding."));
            }
        });
    }
}
