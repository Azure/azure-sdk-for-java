/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.credentials.ServiceClientCredentials;
import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.NextPolicy;
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
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        try {
            String token = credentials.authorizationHeaderValue(context.httpRequest().url().toString());
            context.httpRequest().headers().set("Authorization", token);
            return next.process();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
