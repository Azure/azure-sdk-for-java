// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt.policy;

import com.azure.common.credentials.AsyncServiceClientCredentials;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
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
        return credentials.authorizationHeaderValueAsync(context.httpRequest())
                .flatMap(token -> {
                    context.httpRequest().headers().set("Authorization", token);
                    return next.process();
                });
    }
}
