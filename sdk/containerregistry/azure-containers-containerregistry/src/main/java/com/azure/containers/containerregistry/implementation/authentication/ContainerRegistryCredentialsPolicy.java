// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * <p>Credential policy for the container registry. It follows the challenge based authorization scheme.</p>
 *
 * <p>For example GET /api/v1/acr/repositories translates into the following calls.</p>
 *
 * <p>Step1: GET /api/v1/acr/repositories
 * Return Header: 401: www-authenticate header - Bearer realm=&quot;{url}&quot;,service=&quot;{serviceName}&quot;,scope=&quot;{scope}&quot;,error=&quot;invalid_token&quot;.</p>
 *
 * <p>Step2: Parse the serviceName, scope from the service.</p>
 *
 * <p>Step3: POST /api/oauth2/exchange  Request Body : {service, scope, grant-type, aadToken with ARM scope}
 * Response Body: {acrRefreshToken}</p>
 *
 * <p>Step4: POST /api/oauth2/token  Request Body: {acrRefreshToken, scope, grant-type}
 * Response Body: {acrAccessToken}</p>
 *
 * <p>Step5: GET /api/v1/acr/repositories
 * Request Header: {Bearer acrTokenAccess}</p>
 */
public final class ContainerRegistryCredentialsPolicy extends BearerTokenAuthenticationPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryCredentialsPolicy.class);
    private static final String BEARER = "Bearer";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String SCOPES_PARAMETER = "scope";
    public static final String SERVICE_PARAMETER = "service";

    private final ContainerRegistryTokenService tokenService;

    /**
     * Creates an instance of ContainerRegistryCredentialsPolicy.
     *
     * @param tokenService the token generation service.
     */
    public ContainerRegistryCredentialsPolicy(ContainerRegistryTokenService tokenService) {
        super(tokenService);
        this.tokenService = tokenService;
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return Mono.empty();
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request conext to be used for token acquisition.
     * @return a {@link Mono} containing {@link Void}
     */
    @Override
    public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        return tokenService.getToken(tokenRequestContext)
            .doOnNext(token -> context.getHttpRequest().getHeaders().set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token.getToken())).then();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }

        // Since we will need to replay this call, adding duplicate to make this replayable.
        if (context.getHttpRequest().getBody() != null) {
            context.getHttpRequest().setBody(context.getHttpRequest().getBody().map(buffer -> buffer.duplicate()));
        }

        HttpPipelineNextPolicy nextPolicy = next.clone();
        return authorizeRequest(context)
            .then(next.process())
            .flatMap(httpResponse -> {
                String authHeader = httpResponse.getHeaderValue(WWW_AUTHENTICATE);
                if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                    return authorizeRequestOnChallenge(context, httpResponse).flatMap(retry -> {
                        if (retry) {
                            return nextPolicy.process()
                                .doFinally(ignored -> {
                                    // Both Netty and OkHttp expect the requestBody to be closed after the connection is closed.
                                    // Failure to do so results in memory leak.
                                    // In case of StreamResponse (or other scenarios where we do not eagerly read the response)
                                    // we let the client close the connection after the stream read.
                                    // This can cause potential leaks in the scenarios like above, where the policy
                                    // may intercept the response and prevent it from reaching the client.
                                    // Hence, the policy needs to ensure that the connection is closed.
                                    httpResponse.close();
                                });
                        } else {
                            return Mono.just(httpResponse);
                        }
                    });
                }
                return Mono.just(httpResponse);
            });
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication
     * challenge header is received after the initial request and returns appropriate {@link TokenRequestContext} to
     * be used for re-authentication.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing {@link Boolean}
     */
    @Override
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(WWW_AUTHENTICATE);
        if (!(response.getStatusCode() == 401 && authHeader != null)) {
            return Mono.just(false);
        } else {
            String scope = extractValue(authHeader, SCOPES_PARAMETER);
            String serviceName = extractValue(authHeader, SERVICE_PARAMETER);

            if (scope != null && serviceName != null) {
                return setAuthorizationHeader(context, new ContainerRegistryTokenRequestContext(serviceName, scope))
                    .thenReturn(true);
            }
            return Mono.just(false);
        }
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    @Override
    public void authorizeRequestSync(HttpPipelineCallContext context) {
    }

    /**
     * Authorizes the request with the bearer token acquired using the specified {@code tokenRequestContext}
     *
     * @param context the HTTP pipeline context.
     * @param tokenRequestContext the token request context to be used for token acquisition.
     * @return a {@link Mono} containing {@link Void}
     */
    @Override
    public void setAuthorizationHeaderSync(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        AccessToken token = tokenService.getTokenSync(tokenRequestContext);
        context.getHttpRequest().getHeaders().set(HttpHeaderName.AUTHORIZATION, BEARER + " " + token.getToken());
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }

        // Since we will need to replay this call, adding duplicate to make this replayable.
        if (context.getHttpRequest().getBody() != null) {
            context.getHttpRequest().setBody(context.getHttpRequest().getBodyAsBinaryData().toReplayableBinaryData());
        }

        HttpPipelineNextSyncPolicy nextPolicy = next.clone();
        authorizeRequestSync(context);
        HttpResponse httpResponse = next.processSync();
        String authHeader = httpResponse.getHeaderValue(WWW_AUTHENTICATE);
        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            if (authorizeRequestOnChallengeSync(context, httpResponse)) {
                // Both Netty and OkHttp expect the requestBody to be closed after the connection is closed.
                // Failure to do so results in memory leak.
                // In case of StreamResponse (or other scenarios where we do not eagerly read the response)
                // we let the client close the connection after the stream read.
                // This can cause potential leaks in the scenarios like above, where the policy
                // may intercept the response and prevent it from reaching the client.
                // Hence, the policy needs to ensure that the connection is closed.
                httpResponse.close();
                return nextPolicy.processSync();
            } else {
                return httpResponse;
            }
        }
        return httpResponse;
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication
     * challenge header is received after the initial request and returns appropriate {@link TokenRequestContext} to
     * be used for re-authentication.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing {@link Boolean}
     */
    @Override
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(WWW_AUTHENTICATE);
        if (!(response.getStatusCode() == 401 && authHeader != null)) {
            return false;
        } else {
            String scope =  extractValue(authHeader, SCOPES_PARAMETER);
            String serviceName = extractValue(authHeader, SERVICE_PARAMETER);

            if (scope != null && serviceName != null) {
                setAuthorizationHeaderSync(context, new ContainerRegistryTokenRequestContext(serviceName, scope));
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts value for given key in www-authenticate header.
     * Expects key="value" format and return value without quotes.
     *
     * returns if value is not found
     */
    private String extractValue(String authHeader, String key) {
        int start = authHeader.indexOf(key);
        if (start < 0 || authHeader.length() - start < key.length() + 3) {
            return null;
        }

        start += key.length();
        if (authHeader.charAt(start) == '=' && authHeader.charAt(start + 1) == '"') {
            start += 2;
            int end = authHeader.indexOf('"', start);
            if (end > start) {
                return authHeader.substring(start, end);
            }
        }

        return null;
    }
}
