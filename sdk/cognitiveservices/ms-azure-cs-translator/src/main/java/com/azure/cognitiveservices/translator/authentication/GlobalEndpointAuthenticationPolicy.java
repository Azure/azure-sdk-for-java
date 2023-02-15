// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cognitiveservices.translator.authentication;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * Adds MT custom authentication headers to the requests.
 */
public class GlobalEndpointAuthenticationPolicy implements HttpPipelinePolicy {

    private static final String KEY_HEADER_NAME = "Ocp-Apim-Subscription-Key";
    private static final String REGION_HEADER_NAME = "Ocp-Apim-Subscription-Region";

    private final AzureRegionalKeyCredential credentials;

    /**
     * Creates an instance of GlobalEndpointAuthenticationPolicy class.
     *
     * @param credentials Regional Azure Key Credentials..
     */
    public GlobalEndpointAuthenticationPolicy(AzureRegionalKeyCredential credentials)
    {
        Objects.requireNonNull(credentials, "'credentials' cannot be null.");
        this.credentials = credentials;
    }
    
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy nextPolicy) {
        return Mono.fromRunnable(() -> {
            var request = context.getHttpRequest();
            request.setHeader(KEY_HEADER_NAME, this.credentials.getKey().getKey());
            request.setHeader(REGION_HEADER_NAME, this.credentials.getRegion());
            
        }).then(nextPolicy.process());
    }    
}
