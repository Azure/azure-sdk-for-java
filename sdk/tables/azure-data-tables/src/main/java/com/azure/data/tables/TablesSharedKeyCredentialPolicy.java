// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Policy that adds the SharedKey into the request's Authorization header.
 */
public final class TablesSharedKeyCredentialPolicy implements HttpPipelinePolicy {

    private final TablesSharedKeyCredential credential;

    /**
     * Creates a SharedKey pipeline policy that adds the SharedKey into the request's authorization header.
     *
     * @param credential The SharedKey credential used to create the policy.
     */
    public TablesSharedKeyCredentialPolicy(TablesSharedKeyCredential credential) {
        this.credential = credential;
    }

    /**
     * Authorizes a {@link com.azure.core.http.HttpRequest} with the SharedKey credential.
     *
     * @param context The context of the request.
     * @param next The next policy in the pipeline.
     * @return A reactive result containing the HTTP response.
     */
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String authorizationValue = credential.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
            context.getHttpRequest().getHeaders().toMap());
        context.getHttpRequest().setHeader("Authorization", authorizationValue);
        return next.process();
    }
}
