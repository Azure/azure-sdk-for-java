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

    private final AccessTokenCacheImpl cache;

    /**
     * Creates BearerTokenAuthenticationChallengePolicy.
     *
     * @param credential the token credential to authenticate the request
     */
    public BearerTokenAuthenticationChallengePolicy(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.cache = new AccessTokenCacheImpl(credential);
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    public Mono<Void> onBeforeRequest(HttpPipelineCallContext context) {
        return Mono.empty();
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication
     * challenge header is received after the initial request and returns appropriate {@link TokenRequestContext} to
     * be used for re-authentication.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing {@link TokenRequestContext}
     */
    public Mono<Boolean> onChallenge(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.just(false);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextPolicy nextPolicy = next.clone();

        return onBeforeRequest(context)
            .then(Mono.defer(() -> next.process()))
               .flatMap(httpResponse -> {
                   String authHeader = httpResponse.getHeaderValue(WWW_AUTHENTICATE);
                   if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                       return onChallenge(context, httpResponse).flatMap(retry -> {
                           if (retry) {
                               return nextPolicy.process();
                           } else {
                               return Mono.just(httpResponse);
                           }
                       });
                   }
                   return Mono.just(httpResponse);
               });
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request conext to be used for token acquisition.
     * @return a {@link Mono} containing {@link Void}
     */
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        return cache.getToken(tokenRequestContext)
           .flatMap(token -> {
               context.getHttpRequest().getHeaders().set(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
               return Mono.empty();
           });
    }
}

