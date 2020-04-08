// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;

import com.azure.management.resources.fluentcore.authentication.AzureTokenCredential;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Rewrite the BearerTokenAuthenticationPolicy, it will use default scope when scopes parameter is empty.
 */
public class AuthenticationPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE_FORMAT = "Bearer %s";

    private final TokenCredential credential;
    private final String[] scopes;
    private final AzureEnvironment environment;

    /**
     * Creates AuthenticationPolicy.
     *
     * @param credential the Azure token credential to authenticate the request
     * @param scopes the scopes used in credential, using default scopes when empty
     */
    public AuthenticationPolicy(AzureTokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.credential = credential.getValue();
        this.scopes = scopes;
        this.environment = credential.getEnvironment();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol().toLowerCase())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }

        Mono<AccessToken> tokenResult;
        if (this.scopes == null || this.scopes.length == 0) {
            String defaultScope = Utils.getDefaultScopeFromRequest(context.getHttpRequest(), environment);
            tokenResult = this.credential.getToken(new TokenRequestContext().addScopes(defaultScope));
        } else {
            tokenResult = this.credential.getToken(new TokenRequestContext().addScopes(scopes));
        }

        return tokenResult
                .flatMap(accessToken -> {
                    context.getHttpRequest().getHeaders().put(AUTHORIZATION_HEADER_KEY,
                        String.format(AUTHORIZATION_HEADER_VALUE_FORMAT, accessToken.getToken()));
                    return next.process();
                });
    }
}
