// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.credentials.SharedKeyCredential;
import reactor.core.publisher.Mono;

/**
 * Policy that adds the SharedKey into the request's Authorization header.
 */
public final class SharedKeyCredentialPolicy implements HttpPipelinePolicy {
    private final SharedKeyCredential credential;

    /**
     * Creates a SharedKey pipeline policy that adds the SharedKey into the request's authorization header.
     *
     * @param credential the SharedKey credential used to create the policy.
     */
    public SharedKeyCredentialPolicy(SharedKeyCredential credential) {
        this.credential = credential;
    }

    /**
     * @return the {@link SharedKeyCredential} linked to the policy.
     */
    public SharedKeyCredential sharedKeyCredential() {
        return this.credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String authorizationValue = credential.generateAuthorizationHeader(context.httpRequest().url(),
            context.httpRequest().httpMethod().toString(),
            context.httpRequest().headers().toMap());
        context.httpRequest().header("Authorization", authorizationValue);
        return next.process();
    }
}
