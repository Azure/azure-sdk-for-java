// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.credential.AccessTokenCache;
import com.azure.core.experimental.implementation.AuthenticationChallenge;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme.
 */
public class BearerTokenAuthenticationChallengePolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";
    public static final Pattern AUTHENTICATION_CHALLENGE_PATTERN =
        Pattern.compile("(\\w+) ((?:\\w+=\".*?\"(?:, )?)+)(?:, )?");
    public static final Pattern AUTHENTICATION_CHALLENGE_PARAMS_PATTERN =
        Pattern.compile("(?:(\\w+)=\"([^\"\"]*)\")+");
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String CLAIMS_PARAMETER = "claims";

    private final TokenCredential credential;
    private final String[] scopes;
    private final Supplier<Mono<AccessToken>> defaultTokenSupplier;
    private final AccessTokenCache cache;

    /**
     * Creates BearerTokenAuthenticationChallengePolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public BearerTokenAuthenticationChallengePolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        this.credential = credential;
        this.scopes = scopes;
        this.defaultTokenSupplier = () -> credential.getToken(new TokenRequestContext().addScopes(scopes));
        this.cache = new AccessTokenCache(defaultTokenSupplier);
    }

    /**
     *
     * Executed before sending the initial request and authenticates the request.
     *
     * @param context The request context.
     * @return A {@link Mono} containing {@link Void}
     */
    public Mono<Void> onBeforeRequest(HttpPipelineCallContext context) {
        return authenticateRequest(context, defaultTokenSupplier, false);
    }

    /**
     * Handles the authentication challenge in the event a 401 response with a WWW-Authenticate authentication
     * challenge header is received after the initial request.
     *
     * @param context The request context.
     * @param response The Http Response containing the authentication challenge header.
     * @return A {@link Mono} containing the status, whether the challenge was successfully extracted and handled.
     *  if true then a follow up request needs to be sent authorized with the challenge based bearer token.
     */
    public Mono<Boolean> onChallenge(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(WWW_AUTHENTICATE);
        if (response.getStatusCode() == 401 && authHeader != null) {
            List<AuthenticationChallenge> challenges = parseChallenges(authHeader);
            for (AuthenticationChallenge authenticationChallenge : challenges) {
                Map<String, String> extractedChallengeParams =
                    parseChallengeParams(authenticationChallenge.getChallengeParameters());
                if (extractedChallengeParams.containsKey(CLAIMS_PARAMETER)) {
                    String claims = new String(Base64.getUrlDecoder()
                        .decode(extractedChallengeParams.get(CLAIMS_PARAMETER)), StandardCharsets.UTF_8);
                    return authenticateRequest(context,
                        () -> credential.getToken(new TokenRequestContext()
                                                      .addScopes(scopes).setClaims(claims)), true)
                               .flatMap(b -> Mono.just(true));
                }
            }
        }
        return Mono.just(false);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }
        HttpPipelineNextPolicy nextPolicy = next.clone();

        return onBeforeRequest(context)
               .then(next.process())
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
     * Get the {@link AccessTokenCache} holding the cached access tokens and the logic to retrieve and refresh
     * access tokens.
     *
     * @return the {@link AccessTokenCache}
     */
    public AccessTokenCache getTokenCache() {
        return cache;
    }

    private Mono<Void> authenticateRequest(HttpPipelineCallContext context, Supplier<Mono<AccessToken>> tokenSupplier,
                                           boolean forceTokenRefresh) {
        return cache.getToken(tokenSupplier, forceTokenRefresh)
           .flatMap(token -> {
               context.getHttpRequest().getHeaders().set(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
               return Mono.empty();
           });
    }

    List<AuthenticationChallenge> parseChallenges(String header) {
        Matcher matcher = AUTHENTICATION_CHALLENGE_PATTERN.matcher(header);

        List<AuthenticationChallenge> challenges = new ArrayList<>();
        while (matcher.find()) {
            challenges.add(new AuthenticationChallenge(matcher.group(1), matcher.group(2)));
        }

        return challenges;
    }

    Map<String, String> parseChallengeParams(String challengeParams) {
        Matcher matcher = AUTHENTICATION_CHALLENGE_PARAMS_PATTERN.matcher(challengeParams);

        Map<String, String> challengeParameters = new HashMap<>();
        while (matcher.find()) {
            challengeParameters.put(matcher.group(1), matcher.group(2));
        }
        return challengeParameters;
    }
}

