// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.implementation.AuthenticationChallenge;
import com.azure.core.experimental.implementation.AzureEnvironment;
import com.azure.core.experimental.implementation.ARMScopeHelper;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme in ARM challenge based authentication scenarios
 * .
 * This is a temporary class to support ARM Challenge based authentication. It will
 * move to azure-resource-manager package.
 */
public class ARMChallengeAuthenticationPolicy extends BearerTokenAuthenticationChallengePolicy {
    private static final Pattern AUTHENTICATION_CHALLENGE_PATTERN =
        Pattern.compile("(\\w+) ((?:\\w+=\".*?\"(?:, )?)+)(?:, )?");
    private static final Pattern AUTHENTICATION_CHALLENGE_PARAMS_PATTERN =
        Pattern.compile("(?:(\\w+)=\"([^\"\"]*)\")+");
    private static final String CLAIMS_PARAMETER = "claims";
    private final String[] scopes;
    private final AzureEnvironment environment;

    /**
     * Creates ARMChallengeAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param environment the environment with endpoints for authentication
     * @param scopes the scopes used in credential, using default scopes when empty
     */
    public ARMChallengeAuthenticationPolicy(TokenCredential credential,
                                            AzureEnvironment environment, String... scopes) {
        super(credential, scopes);
        this.scopes = scopes;
        this.environment = environment;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol().toLowerCase(Locale.ROOT))) {
            return Mono.error(new RuntimeException("token credentials require a URL using the HTTPS protocol scheme"));
        }

        TokenRequestContext trc;
        if (scopes == null || scopes.length == 0) {
            String defaultScope = ARMScopeHelper.getDefaultScopeFromRequest(
                context.getHttpRequest(), environment);
            trc = new TokenRequestContext().addScopes(defaultScope);
        } else {
            trc = new TokenRequestContext().addScopes(scopes);
        }
        return authorizeRequest(context, trc)
            .then(Mono.defer(() -> super.process(context, next)));
    }


    @Override
    public Mono<TokenRequestContext> tryGetTokenRequestContext(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(WWW_AUTHENTICATE);
        if (response.getStatusCode() == 401 && authHeader != null) {
            List<AuthenticationChallenge> challenges = parseChallenges(authHeader);
            for (AuthenticationChallenge authenticationChallenge : challenges) {
                Map<String, String> extractedChallengeParams =
                    parseChallengeParams(authenticationChallenge.getChallengeParameters());
                if (extractedChallengeParams.containsKey(CLAIMS_PARAMETER)) {
                    String claims = new String(Base64.getUrlDecoder()
                        .decode(extractedChallengeParams.get(CLAIMS_PARAMETER)), StandardCharsets.UTF_8);
                    return Mono.just(new TokenRequestContext()
                        .addScopes(scopes).setClaims(claims));
                }
            }
        }
        return Mono.empty();
    }

    private List<AuthenticationChallenge> parseChallenges(String header) {
        Matcher matcher = AUTHENTICATION_CHALLENGE_PATTERN.matcher(header);

        List<AuthenticationChallenge> challenges = new ArrayList<>();
        while (matcher.find()) {
            challenges.add(new AuthenticationChallenge(matcher.group(1), matcher.group(2)));
        }
        return challenges;
    }

    private Map<String, String> parseChallengeParams(String challengeParams) {
        Matcher matcher = AUTHENTICATION_CHALLENGE_PARAMS_PATTERN.matcher(challengeParams);

        Map<String, String> challengeParameters = new HashMap<>();
        while (matcher.find()) {
            challengeParameters.put(matcher.group(1), matcher.group(2));
        }
        return challengeParameters;
    }
}
