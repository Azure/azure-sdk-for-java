// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

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
public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    private final TokenCredential credential;
    private final String[] scopes;
    private final Supplier<Mono<AccessToken>> defaultTokenSupplier;
    private final SimpleTokenCache cache;

    /**
     * Creates BearerTokenAuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes of authentication the credential should get token for
     */
    public BearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes) {
        Objects.requireNonNull(credential);
        Objects.requireNonNull(scopes);
        assert scopes.length > 0;
        this.credential = credential;
        this.scopes = scopes;
        this.defaultTokenSupplier = () -> credential.getToken(new TokenRequestContext().addScopes(scopes));
        this.cache = new SimpleTokenCache(defaultTokenSupplier);
    }


    private Mono<Void> authenticateRequest(HttpPipelineCallContext context,
                                  Supplier<Mono<AccessToken>> tokenSupplier) {
        return cache.getToken(tokenSupplier)
           .flatMap(token -> {
               context.getHttpRequest().getHeaders().put(AUTHORIZATION_HEADER, BEARER + " " + token.getToken());
               return Mono.empty();
           });
    }

    public Mono<Void> onBeforeRequest(HttpPipelineCallContext context) {
        return authenticateRequest(context, defaultTokenSupplier);
    }

    public Mono<Boolean> onChallengeAsync(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue("WWW-Authenticate");
        if (response.getStatusCode() == 401 && authHeader != null) {
            List<AuthenticationChallenge> challenges = parseChallenges(authHeader);
            for (AuthenticationChallenge authenticationChallenge : challenges) {
                Map<String, String> extractedChallengeParams =
                    parseChallengeParams(authenticationChallenge.getChallengeParameters());
                if (extractedChallengeParams.containsKey("claims")) {
                    String claims = new String(Base64.getUrlDecoder()
                                                   .decode(extractedChallengeParams.get("claims")));
                    return authenticateRequest(context,
                        () -> credential.getToken(new TokenRequestContext()
                                                      .addScopes(scopes).setClaims(claims)))
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
               String authHeader = httpResponse.getHeaderValue("WWW-Authenticate");
               if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                   return onChallengeAsync(context, httpResponse).flatMap(retry -> {
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

    private List<AuthenticationChallenge> parseChallenges(String header) {
        Pattern pattern = Pattern.compile("(\\w+) ((?:\\w+=\".*?\"(?:, )?)+)(?:, )?");
        Matcher matcher = pattern.matcher(header);

        List<AuthenticationChallenge> challenges = new ArrayList<>();
        while (matcher.find()) {
            challenges.add(new AuthenticationChallenge(matcher.group(1), matcher.group(2)));
        }

        return challenges;
    }

    private Map<String, String> parseChallengeParams(String challengeParams) {
        Pattern pattern = Pattern.compile("(?:(\\w+)=\"([^\"\"]*)\")+");
        Matcher matcher = pattern.matcher(challengeParams);

        Map<String, String> challengeParameters = new HashMap<>();
        while (matcher.find()) {
            challengeParameters.put(matcher.group(1), matcher.group(2));
        }
        return challengeParameters;
    }
}
