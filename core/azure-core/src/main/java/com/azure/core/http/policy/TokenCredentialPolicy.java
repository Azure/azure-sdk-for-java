// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * The Pipeline policy that adds credential from ServiceClientCredentials to a request.
 */
public class TokenCredentialPolicy implements HttpPipelinePolicy {
    private final TokenCredential credential;

    /**
     * Creates CredentialsPolicy.
     *
     * @param credential the credential
     */
    public TokenCredentialPolicy(TokenCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return credential.getTokenAsync(context.httpRequest().url().toString())
            .flatMap(token -> {
                context.httpRequest().headers().put("Authorization", credential.scheme() + " " + token);
                return next.process();
            });
    }
}
