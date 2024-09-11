// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.experimental.implementation.AccessTokenCache;
import com.azure.core.experimental.implementation.AuthorizationChallengeParser;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.azure.core.experimental.implementation.AuthorizationChallengeParser.getChallengeParameterFromResponse;

/**
 * The Pop token authentication policy for use with Azure SDK clients.
 */
public class PopTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(PopTokenAuthenticationPolicy.class);
    private final List<String> scopes = new ArrayList<>();
    private final AccessTokenCache cache;
    private String popNonce;

    /**
     * Creates a new instance of the PopTokenAuthenticationPolicy.
     * @param credential The credential to use for authentication.
     * @param scopes The scopes required for the token.
     */
    public PopTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.scopes.clear();
        this.scopes.addAll(Arrays.asList(scopes));
        this.cache = new AccessTokenCache(credential);
    }

    /**
     * Authorizes the request.
     * @param context The context of the request.
     * @return A {@link Mono} containing {@link Void} .
     */
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return this.scopes == null ? Mono.empty() : this.setAuthorizationHeaderHelper(context, false);
    }

    /**
     * Authorizes the request synchronously.
     * @param context The context of the request.
     */
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        this.setAuthorizationHeaderHelperSync(context, false);
    }

    /**
     * Authorizes the request on challenge.
     * @param context The context of the request.
     * @param response The response of the request.
     * @return A {@link Mono} containing a {@link Boolean} indicating if the request was authorized.
     */
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        popNonce = getChallengeParameterFromResponse(response, "PoP", "nonce");
        if (CoreUtils.isNullOrEmpty(popNonce)) {
            return Mono.just(false);
        }

        return this.scopes == null
            ? Mono.just(false)
            : this.setAuthorizationHeaderHelper(context, true).flatMap((ignored) -> Mono.just(true));
    }

    /**
     * Authorizes the request on challenge synchronously.
     * @param context The context of the request.
     * @param response The response of the request.
     * @return A {@link Boolean} indicating if the request was authorized.
     */
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        popNonce = AuthorizationChallengeParser.getChallengeParameterFromResponse(response, "PoP", "nonce");

        if (CoreUtils.isNullOrEmpty(popNonce)) {
            return false;
        }

        if (this.scopes == null) {
            return false;
        } else {
            this.setAuthorizationHeaderHelperSync(context, true);
            return true;
        }
    }

    /**
     * Processes the request.
     * @param context The context of the request.
     * @param next The next policy in the pipeline.
     * @return A {@link Mono} containing the {@link HttpResponse}.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException(
                "Proof of possession token authentication is not permitted for non TLS-protected (HTTPS) endpoints."));
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
            } else if (authHeader != null) {
                popNonce = AuthorizationChallengeParser.getChallengeParameterFromResponse(httpResponse, "PoP", "nonce");
            }
            return Mono.just(httpResponse);
        });
    }

    /**
     * Processes the request synchronously.
     * @param context The context of the request.
     * @param next The next policy in the pipeline.
     * @return The {@link HttpResponse}.
     */
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Proof of possession token authentication is not permitted for non TLS-protected (HTTPS) endpoints."));
        } else {
            HttpPipelineNextSyncPolicy nextPolicy = next.clone();
            this.authorizeRequestSync(context);
            HttpResponse httpResponse = next.processSync();
            String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
            if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                if (this.authorizeRequestOnChallengeSync(context, httpResponse)) {
                    httpResponse.close();
                    return nextPolicy.processSync();
                } else {
                    return httpResponse;
                }
            } else if (authHeader != null) {
                popNonce = AuthorizationChallengeParser.getChallengeParameterFromResponse(httpResponse, "PoP", "nonce");
                return httpResponse;
            } else {
                return httpResponse;
            }
        }
    }

    private Mono<Void> setAuthorizationHeaderHelper(HttpPipelineCallContext context, boolean checkToForceFetchToken) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Proof of possession token authentication is not permitted for non TLS-protected (HTTPS) endpoints."));
        }

        PopTokenRequestContext popTokenRequestContext = new PopTokenRequestContext().addScopes(this.scopes.get(0))
            .setProofOfPossessionNonce(popNonce)
            .setResourceRequestUrl(context.getHttpRequest().getUrl())
            .setResourceRequestMethod(context.getHttpRequest().getHttpMethod());

        if (!CoreUtils.isNullOrEmpty(popNonce)) {
            return this.cache.getToken(popTokenRequestContext, checkToForceFetchToken).flatMap((token) -> {
                setAuthorizationHeader(context.getHttpRequest().getHeaders(), token.getToken());
                return Mono.empty();
            });
        }
        return Mono.empty();
    }

    private void setAuthorizationHeaderHelperSync(HttpPipelineCallContext context, boolean checkToForceFetchToken) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Proof of possession token authentication is not permitted for non TLS-protected (HTTPS) endpoints."));
        }
        PopTokenRequestContext popTokenRequestContext = new PopTokenRequestContext().addScopes(this.scopes.get(0))
            .setProofOfPossessionNonce(popNonce)
            .setResourceRequestUrl(context.getHttpRequest().getUrl())
            .setResourceRequestMethod(context.getHttpRequest().getHttpMethod());

        AccessToken token = this.cache.getTokenSync(popTokenRequestContext, checkToForceFetchToken);
        setAuthorizationHeader(context.getHttpRequest().getHeaders(), token.getToken());
    }

    private static void setAuthorizationHeader(HttpHeaders headers, String token) {
        headers.set(HttpHeaderName.AUTHORIZATION, "Pop " + token);
    }
}
