// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * HttpPipelinePolicy to redirect requests to the new location.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {
    private static final int MAX_REDIRECTS = 10;
    private final List<String> locations = new ArrayList<>();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRedirection(context, next, 0);
    }

    private Mono<HttpResponse> attemptRedirection(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
                                                  int redirectNumber) {
        return next.clone().process().flatMap(httpResponse -> {
            if (shouldRedirect(httpResponse, redirectNumber)) {
                String newLocation = httpResponse.getHeaderValue("Location");
                locations.add(newLocation);

                HttpRequest newRequest = context.getHttpRequest().copy();
                newRequest.setUrl(newLocation);
                context.setHttpRequest(newRequest);

                return attemptRedirection(context, next, redirectNumber + 1);
            }
            return Mono.just(httpResponse);
        });
    }

    private boolean shouldRedirect(HttpResponse response, int redirectNumber) {
        return response.getStatusCode() == 302
            && !locations.contains(response.getHeaderValue("Location"))
            && redirectNumber < MAX_REDIRECTS;
    }
}
