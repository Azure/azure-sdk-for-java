// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import java.util.Objects;

/**
 * The pipeline policy that applies a token credential to an HTTP request with "Bearer" scheme.
 */
public class CustomBearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(CustomBearerTokenAuthenticationPolicy.class);
    private static final String BEARER = "Bearer";
    private static final HttpHeaderName X_FORWARDED_HOST = HttpHeaderName.fromString("X-FORWARDED-HOST");

    private final String[] scopes;
    private final String acsEndpoint;
    private final TokenCredential tokenCredential;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param acsEndpoint endpoint of the ACS resource
     * @param scopes the scopes of authentication the credential should get token for
     */
    public CustomBearerTokenAuthenticationPolicy(TokenCredential credential, String acsEndpoint, String... scopes) {
        Objects.requireNonNull(credential);
        this.scopes = scopes;
        this.acsEndpoint = acsEndpoint;
        this.tokenCredential = credential;
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
        addHeaders(context.getHttpRequest().getHeaders());

        HttpPipelineNextPolicy nextPolicy = next.clone();

        return authorizeRequest(context).then(Mono.defer(next::process)).flatMap(httpResponse -> {
            String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
            if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                return authorizeRequestOnChallenge(context, httpResponse).flatMap(retry -> {
                    if (retry) {
                        // Both Netty and OkHttp expect the requestBody to be closed after the response has been read.
                        // Failure to do so results in memory leak.
                        // In case of StreamResponse (or other scenarios where we do not eagerly read the response)
                        // the response body may not be consumed.
                        // This can cause potential leaks in the scenarios like above, where the policy
                        // may intercept the response and it may never be read.
                        // Forcing the read here - so that the memory can be released.
                        return httpResponse.getBody().ignoreElements().then(nextPolicy.process());
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
        addHeaders(context.getHttpRequest().getHeaders());

        HttpPipelineNextSyncPolicy nextPolicy = next.clone();

        authorizeRequestSync(context);
        HttpResponse httpResponse = next.processSync();
        String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallengeSync(context, httpResponse)) {
                // Both Netty and OkHttp expect the requestBody to be closed after the response has been read.
                // Failure to do so results in memory leak.
                // In case of StreamResponse (or other scenarios where we do not eagerly read the response)
                // the response body may not be consumed.
                // This can cause potential leaks in the scenarios like above, where the policy
                // may intercept the response and it may never be read.
                // Forcing the read here - so that the memory can be released.
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
        return tokenCredential.getToken(tokenRequestContext).flatMap(token1 -> {
            setAuthorizationHeader(context.getHttpRequest().getHeaders(), token1.getToken());
            return Mono.empty();
        });
    }

    private void setAuthorizationHeaderHelperSync(HttpPipelineCallContext context,
        TokenRequestContext tokenRequestContext, boolean checkToForceFetchToken) {
        AccessToken token = tokenCredential.getTokenSync(tokenRequestContext);
        setAuthorizationHeader(context.getHttpRequest().getHeaders(), token.getToken());
    }

    private static void setAuthorizationHeader(HttpHeaders headers, String token) {
        headers.set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token);
    }

    private void addHeaders(HttpHeaders headers) {
        if (acsEndpoint != null) {
            headers.set(X_FORWARDED_HOST, acsEndpoint);
        }
    }
}
