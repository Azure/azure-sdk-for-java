// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.AccessTokenCache;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * <p>The {@code BearerTokenAuthenticationPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface.
 * This policy uses a {@link TokenCredential} to authenticate the request with a bearer token.</p>
 *
 * <p>This class is useful when you need to authorize requests with a bearer token from Azure. It ensures that the
 * requests are sent over HTTPS to prevent the token from being leaked.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code BearerTokenAuthenticationPolicy} is created with a {@link TokenCredential} and a scope.
 * The policy can then added to the pipeline. The request sent via the pipeline will then include the
 * Authorization header with the bearer token.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.BearerTokenAuthenticationPolicy.constructor -->
 * <pre>
 * TokenCredential credential = new BasicAuthenticationCredential&#40;&quot;username&quot;, &quot;password&quot;&#41;;
 * BearerTokenAuthenticationPolicy policy = new BearerTokenAuthenticationPolicy&#40;credential,
 *     &quot;https:&#47;&#47;management.azure.com&#47;.default&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.BearerTokenAuthenticationPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.credential.TokenCredential
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(BearerTokenAuthenticationPolicy.class);
    private static final String BEARER = "Bearer";

    private final String[] scopes;
    private final AccessTokenCache cache;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public BearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.scopes = scopes;
        this.cache = new AccessTokenCache(credential);
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        if (this.scopes == null) {
            return Mono.empty();
        }
        return setAuthorizationHeaderHelper(context, new TokenRequestContext().addScopes(this.scopes), false);
    }

    /**
     * Synchronously executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     */
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        setAuthorizationHeaderHelperSync(context, new TokenRequestContext().addScopes(scopes), false);
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link TokenRequestContext} to be used for
     * re-authentication.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing {@link TokenRequestContext}
     */
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.just(false);
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication challenge
     * header is received after the initial request and returns appropriate {@link TokenRequestContext} to be used for
     * re-authentication.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A boolean indicating if containing the {@link TokenRequestContext} for re-authentication
     */
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        return false;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextPolicy nextPolicy = next.clone();

        return authorizeRequest(context).then(Mono.defer(next::process)).flatMap(httpResponse -> {
            String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
            if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                return authorizeRequestOnChallenge(context, httpResponse).flatMap(authorized -> {
                    if (authorized) {
                        // body needs to be closed or read to the end to release the connection
                        httpResponse.close();
                        return nextPolicy.process();
                    } else {
                        return Mono.just(httpResponse);
                    }
                });
            }
            return Mono.just(httpResponse);
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextSyncPolicy nextPolicy = next.clone();

        authorizeRequestSync(context);
        HttpResponse httpResponse = next.processSync();
        String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallengeSync(context, httpResponse)) {
                // body needs to be closed or read to the end to release the connection
                httpResponse.close();
                return nextPolicy.processSync();
            } else {
                return httpResponse;
            }
        }
        return httpResponse;
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     * @return a {@link Mono} containing {@link Void}
     */
    public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        return setAuthorizationHeaderHelper(context, tokenRequestContext, true);
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     */
    public void setAuthorizationHeaderSync(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        setAuthorizationHeaderHelperSync(context, tokenRequestContext, true);
    }

    private Mono<Void> setAuthorizationHeaderHelper(HttpPipelineCallContext context,
        TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
        return cache.getToken(tokenRequestContext, checkToForceFetchToken).flatMap(token -> {
            setAuthorizationHeader(context.getHttpRequest().getHeaders(), token.getToken());
            return Mono.empty();
        });
    }

    private void setAuthorizationHeaderHelperSync(HttpPipelineCallContext context,
        TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
        AccessToken token = cache.getTokenSync(tokenRequestContext, checkToForceFetchToken);
        setAuthorizationHeader(context.getHttpRequest().getHeaders(), token.getToken());
    }

    private static void setAuthorizationHeader(HttpHeaders headers, String token) {
        headers.set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token);
    }
}
