// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.implementation.AccessTokenCacheImpl;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme.
 */
public class BearerTokenAuthenticationChallengePolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    private final TokenRequestContext defaultTokenRequestContext;
    private final AccessTokenCacheImpl cache;

    /**
     * Creates BearerTokenAuthenticationChallengePolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public BearerTokenAuthenticationChallengePolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.defaultTokenRequestContext = new TokenRequestContext().addScopes(scopes);
        this.cache = new AccessTokenCacheImpl(credential, defaultTokenRequestContext);
    }


    /**
     * Hanles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication
     * challenge header is received after the initial request and returns appropriate {@link TokenRequestContext} to
     * be used to re-authentication.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing {@link TokenRequestContext}
     */
    public Mono<TokenRequestContext> tryGetTokenRequestContext(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.empty();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextPolicy nextPolicy = next.clone();

        return Mono.defer(() -> next.process())
               .flatMap(httpResponse -> {
                   String authHeader = httpResponse.getHeaderValue(WWW_AUTHENTICATE);
                   if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                       return tryGetTokenRequestContext(context, httpResponse)
                           .flatMap(trc -> authenticateRequest(context, trc)
                               .then(Mono.defer(() -> nextPolicy.process())))
                           .switchIfEmpty(Mono.just(httpResponse));
                   }
                   return Mono.just(httpResponse);
               });
    }

    /**
     * Authenticates the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request conext to be used for token acquisition.
     * @return a {@link Mono} containing {@link Void}
     */
    public Mono<Void> authenticateRequest(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        return cache.getToken(tokenRequestContext)
           .flatMap(token -> {
               context.getHttpRequest().getHeaders().set(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
               return Mono.empty();
           });
    }

    /**
     * Authenticates the request with the bearer token acquired using the default {@link TokenRequestContext} containing
     * the scopes specified at policy construction time.
     *
     * @param context the HTTP pipeline context.
     * @return a {@link Mono} containing {@link Void}
     */
    public Mono<Void> authenticateRequest(HttpPipelineCallContext context) {
        return cache.getToken(defaultTokenRequestContext)
            .flatMap(token -> {
                context.getHttpRequest().getHeaders().set(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
                return Mono.empty();
            });
    }
}

