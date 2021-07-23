// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

/**
 * HttpPipelinePolicy to redirect requests when 302 message is received to the new location marked by the
 * Location header.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {
    private static final int MAX_REDIRECTS = 10;
    private static final String LOCATION_HEADER_NAME = "Location";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRedirection(context, next, 0, new HashSet<>());
    }

    private Mono<HttpResponse> attemptRedirection(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
                                                  int redirectNumber, Set<String> locations) {
        return next.clone().process().flatMap(httpResponse -> {
            if (shouldRedirect(httpResponse, redirectNumber, locations)) {
                String newLocation = httpResponse.getHeaderValue(LOCATION_HEADER_NAME);
                locations.add(newLocation);

                HttpRequest newRequest = context.getHttpRequest().copy();
                newRequest.setUrl(newLocation);
                context.setHttpRequest(newRequest);

                return attemptRedirection(context, next, redirectNumber + 1, locations);
            }
            return Mono.just(httpResponse);
        });
    }

    private boolean shouldRedirect(HttpResponse response, int redirectNumber, Set<String> locations) {
        return response.getStatusCode() == 302
            && !locations.contains(response.getHeaderValue(LOCATION_HEADER_NAME))
            && redirectNumber < MAX_REDIRECTS;
    }
}
