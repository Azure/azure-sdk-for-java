// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credentials.ServiceClientCredentials;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * The Pipeline policy that adds credentials from ServiceClientCredentials to a request.
 */
public class CredentialsPolicy implements HttpPipelinePolicy {
    private final ServiceClientCredentials credentials;

    /**
     * Creates CredentialsPolicy.
     *
     * @param credentials the credentials
     */
    public CredentialsPolicy(ServiceClientCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            String token = credentials.authorizationHeaderValue(context.httpRequest().url().toString());
            context.httpRequest().headers().put("Authorization", token);
            return next.process();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
