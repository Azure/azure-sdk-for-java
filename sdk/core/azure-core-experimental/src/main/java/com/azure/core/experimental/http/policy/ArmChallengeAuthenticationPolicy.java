// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.implementation.AuthenticationChallenge;
import com.azure.core.experimental.implementation.AzureEnvironment;
import com.azure.core.experimental.implementation.ARMScopeHelper;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pipeline policy that applies a token credential to an HTTP request
 * with "Bearer" scheme in ARM challenge based authentication scenarios
 * .
 * This is a temporary class to support ARM Challenge based authentication. It will
 * move to azure-resource-manager package.
 */
public class ArmChallengeAuthenticationPolicy extends BearerTokenAuthenticationChallengePolicy {
    private static final Pattern AUTHENTICATION_CHALLENGE_PATTERN =
        Pattern.compile("(\\w+) ((?:\\w+=\".*?\"(?:, )?)+)(?:, )?");
    private static final Pattern AUTHENTICATION_CHALLENGE_PARAMS_PATTERN =
        Pattern.compile("(?:(\\w+)=\"([^\"\"]*)\")+");
    private static final String CLAIMS_PARAMETER = "claims";
    private final String[] scopes;
    private final AzureEnvironment environment;
    private static final String ARM_SCOPES_KEY = "ARMScopes";

    /**
     * Creates ArmChallengeAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param environment the environment with endpoints for authentication
     * @param scopes the scopes used in credential, using default scopes when empty
     */
    public ArmChallengeAuthenticationPolicy(TokenCredential credential,
                                            AzureEnvironment environment, String... scopes) {
        super(credential, scopes);
        this.scopes = scopes;
        this.environment = environment;
    }


    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return Mono.defer(() -> {
            String[] scopes = this.scopes;
            scopes = getScopes(context, scopes);
            context.setData(ARM_SCOPES_KEY, scopes);
            return setAuthorizationHeader(context, new TokenRequestContext().addScopes(scopes));
        });
    }

    @Override
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.defer(() -> {
            String authHeader = response.getHeaderValue(WWW_AUTHENTICATE);
            if (!(response.getStatusCode() == 401 && authHeader != null)) {
                return Mono.just(false);
            } else {
                List<AuthenticationChallenge> challenges = parseChallenges(authHeader);
                for (AuthenticationChallenge authenticationChallenge : challenges) {
                    Map<String, String> extractedChallengeParams =
                        parseChallengeParams(authenticationChallenge.getChallengeParameters());
                    if (extractedChallengeParams.containsKey(CLAIMS_PARAMETER)) {
                        String claims = new String(Base64.getUrlDecoder()
                            .decode(extractedChallengeParams.get(CLAIMS_PARAMETER)), StandardCharsets.UTF_8);
                        String[] scopes;

                        // We should've retrieved and configured the scopes in on Before logic,
                        // re-use it here as an optimization.
                        try {
                            scopes = (String[]) context.getData(ARM_SCOPES_KEY).get();
                        } catch (NoSuchElementException e) {
                            scopes = this.scopes;
                        }

                        // If scopes wasn't configured in On Before logic or at constructor level,
                        // then this method will retrieve it again.
                        scopes = getScopes(context, scopes);
                        return setAuthorizationHeader(context, new TokenRequestContext()
                            .addScopes(scopes).setClaims(claims))
                            .flatMap(b -> Mono.just(true));
                    }
                }
                return Mono.just(false);
            }
        });
    }

    private String[] getScopes(HttpPipelineCallContext context, String[] scopes) {
        if (CoreUtils.isNullOrEmpty(scopes)) {
            scopes = new String[1];
            scopes[0] = ARMScopeHelper.getDefaultScopeFromRequest(
                context.getHttpRequest(), environment);
        }
        return scopes;
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
