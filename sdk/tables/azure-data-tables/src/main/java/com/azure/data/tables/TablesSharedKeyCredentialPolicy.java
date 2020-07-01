// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;


import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * This class helps authenticate an Http request for the Tables service
 */
public final class TablesSharedKeyCredentialPolicy implements HttpPipelinePolicy {

    private final TablesSharedKeyCredential credential;

    /**
     * constructor for the TablesSharedKeyCredentialPolicy class
     *
     * @param credential the credentials of the account
     */
    public TablesSharedKeyCredentialPolicy(TablesSharedKeyCredential credential) {
        this.credential = credential;
    }

    /**
     * creates an Http response
     *
     * @param context the context of the http pipeline
     * @param next the next Http pipeline policy
     * @return an Http response
     */
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String authorizationValue = this.credential.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
            context.getHttpRequest().getHeaders().toMap());
        context.getHttpRequest().setHeader("Authorization", authorizationValue);
        return next.process();
    }
}
