// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme.
 */
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    private final TokenCredential credential;
    private final String[] scopes;
    private final SimpleTokenCache cache;

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
        this.cache = new SimpleTokenCache(() -> credential.getToken(new TokenRequestContext().addScopes(scopes)));
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        return cache.getToken()
            .flatMap(token -> {
                context.getHttpRequest().getHeaders().put(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
                return next.process();
            });
    }
}
