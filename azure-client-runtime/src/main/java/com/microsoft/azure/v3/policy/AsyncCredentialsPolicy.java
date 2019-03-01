/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v3.policy;

import com.microsoft.azure.v3.credentials.AsyncServiceClientCredentials;
import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpPipelineNextPolicy;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * Creates a policy which adds credentials from AsyncServiceClientCredentials to a request.
 */
public class AsyncCredentialsPolicy implements HttpPipelinePolicy {
    private final AsyncServiceClientCredentials credentials;

    /**
     * Creates CredentialsPolicy.
     *
     * @param credentials The credentials to use for authentication.
     */
    public AsyncCredentialsPolicy(AsyncServiceClientCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return credentials.authorizationHeaderValueAsync(context.httpRequest().url().toString())
                .flatMap(token -> {
                    context.httpRequest().headers().set("Authorization", token);
                    return next.process();
                });
    }
}