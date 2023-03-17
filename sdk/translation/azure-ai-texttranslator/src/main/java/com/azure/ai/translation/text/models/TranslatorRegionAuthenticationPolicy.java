// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text.models;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * Adds MT custom authentication headers to the requests.
 */
public class TranslatorRegionAuthenticationPolicy implements HttpPipelinePolicy {

    private static final String REGION_HEADER_NAME = "Ocp-Apim-Subscription-Region";

    private final String region;

    /**
     * Creates an instance of TranslatorRegionAuthenticationPolicy class.
     *
     * @param region - region where the Translator resource was created.
     */
    public TranslatorRegionAuthenticationPolicy(String region)
    {
        Objects.requireNonNull(region, "'region' cannot be null.");
        this.region = region;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy nextPolicy) {
        return Mono.fromRunnable(() -> {
            HttpRequest request = context.getHttpRequest();
            request.setHeader(REGION_HEADER_NAME, this.region);

        }).then(nextPolicy.process());
    }
}
