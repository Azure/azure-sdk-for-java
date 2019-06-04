// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The Pipeline policy that adds credential from ServiceClientCredentials to a request.
 */
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    private final TokenCredential credential;
    private final String[] scopes;

    /**
     * Creates CredentialsPolicy.
     *
     * @param credential the credential
     */
    public BearerTokenAuthenticationPolicy(TokenCredential credential, String scope) {
        this(credential, new String[] { scope });
    }

    public BearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        Objects.requireNonNull(scopes);
        assert scopes.length > 0;
        this.credential = credential;
        this.scopes = scopes;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return credential.getToken(scopes)
            .flatMap(token -> {
                context.httpRequest().headers().put(AUTHORIZATION_HEADER, BEARER + " " + token);
                return next.process();
            });
    }
}
