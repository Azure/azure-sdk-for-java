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
 * The Pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme.
 */
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    private final TokenCredential credential;
    private final String[] scopes;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scope the scope of authentication the credential should get token for
     */
    public BearerTokenAuthenticationPolicy(TokenCredential credential, String scope) {
        this(credential, new String[] { scope });
    }

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
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
