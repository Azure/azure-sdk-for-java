// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.credentials.ServiceClientCredentials;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
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
            context.httpRequest().headers().set("Authorization", token);
            return next.process();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
