// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenRequestContext;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
final class ContainerRegistryCredentialsPolicy extends BearerTokenAuthenticationPolicy {

    private static final String BEARER = "Bearer";
    public static final Pattern AUTHENTICATION_CHALLENGE_PARAMS_PATTERN =
        Pattern.compile("(?:(\\w+)=\"([^\"\"]*)\")+");
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String SCOPES_PARAMETER = "scope";
    public static final String SERVICE_PARAMETER = "service";
    public static final String AUTHORIZATION = "Authorization";

    private final ContainerRegistryTokenService tokenService;
    private final ClientLogger logger = new ClientLogger(ContainerRegistryCredentialsPolicy.class);

    /**
     * Creates an instance of ContainerRegistryCredentialsPolicy.
     *
     * @param tokenService the token generation service.
     */
    ContainerRegistryCredentialsPolicy(ContainerRegistryTokenService tokenService) {
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
            .flatMap((token) -> {
                context.getHttpRequest().getHeaders().set(AUTHORIZATION, BEARER + " " + token.getToken());
                return Mono.empty();
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
        return Mono.defer(() -> {
            String authHeader = response.getHeaderValue(WWW_AUTHENTICATE);
            if (!(response.getStatusCode() == 401 && authHeader != null)) {
                return Mono.just(false);
            } else {
                Map<String, String> extractedChallengeParams = parseBearerChallenge(authHeader);
                if (extractedChallengeParams != null && extractedChallengeParams.containsKey(SCOPES_PARAMETER)) {
                    String scope = extractedChallengeParams.get(SCOPES_PARAMETER);
                    String serviceName = extractedChallengeParams.get(SERVICE_PARAMETER);
                    return setAuthorizationHeader(context, new ContainerRegistryTokenRequestContext(serviceName, scope))
                        .then(Mono.defer(() -> Mono.just(true)));
                }

                return Mono.just(false);
            }
        });
    }

    private Map<String, String> parseBearerChallenge(String header) {
        if (header.startsWith(BEARER)) {
            String challengeParams = header.substring(BEARER.length());

            Matcher matcher2 = AUTHENTICATION_CHALLENGE_PARAMS_PATTERN.matcher(challengeParams);

            Map<String, String> challengeParameters = new HashMap<>();
            while (matcher2.find()) {
                challengeParameters.put(matcher2.group(1), matcher2.group(2));
            }
            return challengeParameters;
        }

        return null;
    }
}
