// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.AuthenticateChallenge;
import com.azure.core.util.CoreUtils;
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
public class ContainerRegistryCredentialsPolicy extends BearerTokenAuthenticationPolicy {
    private static final String SCOPES_PARAMETER = "scope";
    private static final String SERVICE_PARAMETER = "service";
    private final ContainerRegistryTokenService acrCredential;

    /**
     * Creates an instance of ContainerRegistryCredentialsPolicy.
     *
     * @param tokenService the token generation service.
     */
    public ContainerRegistryCredentialsPolicy(ContainerRegistryTokenService tokenService, String scope) {
        super(tokenService, scope);
        this.acrCredential = tokenService;
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        authorizeRequestSync(context);
        return Mono.empty();
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
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (!(response.getStatusCode() == 401 && authHeader != null)) {
            return Mono.just(false);
        } else {
            ContainerRegistryTokenRequestContext tokenRequestContext = createTokenRequestContext(authHeader);

            if (tokenRequestContext != null) {
                return setAuthorizationHeader(context, tokenRequestContext).thenReturn(true);
            }
            return Mono.just(false);
        }
    }

    /**
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     */
    @Override
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        String lastToken = acrCredential.getLastToken();
        if (lastToken != null) {
            context.getHttpRequest().getHeaders().set(HttpHeaderName.AUTHORIZATION, "Bearer " + lastToken);
        }
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
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (!(response.getStatusCode() == 401 && authHeader != null)) {
            return false;
        } else {
            ContainerRegistryTokenRequestContext tokenRequestContext = createTokenRequestContext(authHeader);
            if (tokenRequestContext != null) {
                setAuthorizationHeaderSync(context, tokenRequestContext);
                return true;
            }
        }

        return false;
    }

    private static ContainerRegistryTokenRequestContext createTokenRequestContext(String authHeader) {
        AuthenticateChallenge bearerChallenge = CoreUtils.parseAuthenticateHeader(authHeader)
            .stream()
            .filter(challenge -> "Bearer".equalsIgnoreCase(challenge.getScheme()))
            .findFirst()
            .orElse(null);

        String scope = bearerChallenge.getParameters().get(SCOPES_PARAMETER);
        String serviceName = bearerChallenge.getParameters().get(SERVICE_PARAMETER);

        return (scope != null && serviceName != null)
            ? new ContainerRegistryTokenRequestContext(serviceName, scope)
            : null;
    }
}
