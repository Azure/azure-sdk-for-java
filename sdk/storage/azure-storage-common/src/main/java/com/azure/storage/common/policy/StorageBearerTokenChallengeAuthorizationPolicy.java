package com.azure.storage.common.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.AuthorizationChallengeHandler;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The storage authorization policy which supports challenge.
 */
public class StorageBearerTokenChallengeAuthorizationPolicy extends BearerTokenAuthenticationPolicy {

    private final String defaultScope = "/.default";
    private String[] scopes;

    private static final Pattern AUTHENTICATION_CHALLENGE_PATTERN
        = Pattern.compile(" ((?:\\w+.*?))([a-z]+://[^ ]*)");

    /**
     * Creates StorageBearerTokenChallengeAuthorizationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes used in credential, using default scopes when empty
     */
    public StorageBearerTokenChallengeAuthorizationPolicy(TokenCredential credential, String... scopes) {
        super(credential, scopes);
        this.scopes = scopes;
    }

    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return Mono.defer(() -> {
            String[] scopes = this.scopes;
            scopes = getScopes(context, scopes);
            if (scopes == null) {
                return Mono.empty();
            } else {
                return setAuthorizationHeader(context, new TokenRequestContext().addScopes(scopes));
            }
        });
    }

    @Override
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        String[] scopes = this.scopes;
        scopes = getScopes(context, scopes);

        if (scopes != null) {
            setAuthorizationHeaderSync(context, new TokenRequestContext().addScopes(scopes));
        }
    }

    @Override
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.defer(() -> {
            String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
            Map<String, String> challenges = parseChallenges(authHeader);

            String scope = challenges.get("resource_id=");
            if (scope != null) {
                scope += defaultScope;
                scopes = new String[] { scope };
                scopes = getScopes(context, scopes);
                setAuthorizationHeaderSync(context, new TokenRequestContext().addScopes(scopes));
                return Mono.just(true);
            }
            return Mono.just(false);
        });
    }

    @Override
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        Map<String, String> challenges = parseChallenges(authHeader);

        String scope = challenges.get("resource_id=");
        if (scope != null) {
            scope += defaultScope;
            scopes = new String[] { scope };
            scopes = getScopes(context, scopes);
            setAuthorizationHeaderSync(context, new TokenRequestContext().addScopes(scopes));
            return true;
        }
        return false;
    }

    /**
     * Gets the scopes for the specific request.
     *
     * @param context The request.
     * @param scopes Default scopes used by the policy.
     * @return The scopes for the specific request.
     */
    public String[] getScopes(HttpPipelineCallContext context, String[] scopes) {
        return CoreUtils.clone(scopes);
    }

    Map<String, String> parseChallenges(String header) {
        Matcher matcher = AUTHENTICATION_CHALLENGE_PATTERN.matcher(header);

        Map<String, String> challenges = new HashMap<>();
        while (matcher.find()) {
            challenges.put(matcher.group(1), matcher.group(2));
        }
        return challenges;
    }
}
