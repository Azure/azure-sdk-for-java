// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

/**
 * HttpPipelinePolicy to redirect requests when a redirect response (Http codes 301 or 302) is received to the
 * new location marked by the Location header.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {
    private static final int MAX_REDIRECTS = 10;
    private static final String LOCATION_HEADER_NAME = "Location";
    private static final int SC_MOVED_PERMANENTLY = 301;
    private static final int SC_MOVED_TEMPORARILY = 302;

    private final ClientLogger logger = new ClientLogger(RedirectPolicy.class);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRedirection(context, next, 0, new HashSet<>());
    }

    private Mono<HttpResponse> attemptRedirection(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
                                                  int redirectNumber, Set<String> attemptedRedirectLocations) {
        return next.clone().process().flatMap(httpResponse -> {
            if (isRedirectResponse(httpResponse)
                && shouldRedirect(httpResponse, context, redirectNumber, attemptedRedirectLocations)) {
                String newLocation = httpResponse.getHeaderValue(LOCATION_HEADER_NAME);
                attemptedRedirectLocations.add(newLocation);

                HttpRequest newRequest = context.getHttpRequest().copy();
                newRequest.setUrl(newLocation);
                context.setHttpRequest(newRequest);

                return attemptRedirection(context, next, redirectNumber + 1, attemptedRedirectLocations);
            }
            return Mono.just(httpResponse);
        });
    }

    private boolean isRedirectResponse(HttpResponse response) {
        return response.getStatusCode() == SC_MOVED_TEMPORARILY || response.getStatusCode() == SC_MOVED_PERMANENTLY;
    }

    private boolean shouldRedirect(HttpResponse response, HttpPipelineCallContext context, int retryCount,
                                   Set<String> attemptedRedirectLocations) {
        if (retryCount > MAX_REDIRECTS) {
            logger.error(String.format("Request to %s has been redirected more than %s times.",
                context.getHttpRequest().getUrl(), MAX_REDIRECTS));
            return false;
        }
        if (attemptedRedirectLocations.contains(response.getHeaderValue(LOCATION_HEADER_NAME))) {
            logger.error(String.format("Request to %s was redirected more than once to: %s",
                context.getHttpRequest().getUrl(), response.getHeaderValue(LOCATION_HEADER_NAME)));
            return false;
        }
        return true;
    }

}
