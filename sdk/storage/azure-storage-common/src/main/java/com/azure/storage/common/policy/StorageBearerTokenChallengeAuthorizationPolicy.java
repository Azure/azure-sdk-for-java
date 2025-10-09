// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The storage authorization policy which supports challenges.
 */
public class StorageBearerTokenChallengeAuthorizationPolicy extends BearerTokenAuthenticationPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StorageBearerTokenChallengeAuthorizationPolicy.class);

    private static final String DEFAULT_SCOPE = "/.default";
    private static final String BEARER_TOKEN_PREFIX = "Bearer";
    private static final String RESOURCE_ID = "resource_id";
    private static final String AUTHORIZATION_URI = "authorization_uri";

    // Immutable constructor scopes (base class retains them); challenge may supply new scopes dynamically.
    private final String[] initialScopes;

    /**
     * Creates StorageBearerTokenChallengeAuthorizationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param scopes the scopes used in credential, using default scopes when empty
     */
    public StorageBearerTokenChallengeAuthorizationPolicy(TokenCredential credential, String... scopes) {
        super(credential, scopes);
        this.initialScopes = CoreUtils.clone(scopes);
    }

    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        // Delegate to superclass to maintain previous behavior
        return super.authorizeRequest(context);
    }

    @Override
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        // Delegate to superclass to maintain previous behavior
        super.authorizeRequestSync(context);
    }

    @Override
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        TokenRequestContext tokenRequestContext = processBearerChallenge(authHeader);

        if (tokenRequestContext == null) {
            return Mono.just(false);
        }

        return setAuthorizationHeader(context, tokenRequestContext).thenReturn(true);
    }

    @Override
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        TokenRequestContext tokenRequestContext = processBearerChallenge(authHeader);

        if (tokenRequestContext == null) {
            return false;
        }

        setAuthorizationHeaderSync(context, tokenRequestContext);
        return true;
    }

    // Processes the bearer challenge from the authentication header.
    TokenRequestContext processBearerChallenge(String authHeader) {
        Map<String, String> challengeAttributes = extractChallengeAttributes(authHeader);
        if (challengeAttributes == null || challengeAttributes.isEmpty()) {
            return null;
        }

        return createTokenRequestContext(challengeAttributes);
    }

    // Creates a token request context from challenge attributes.
    TokenRequestContext createTokenRequestContext(Map<String, String> attributes) {
        String resource = attributes.get(RESOURCE_ID);
        String authUrl = attributes.get(AUTHORIZATION_URI);

        // Determine scopes to use based on resource
        String[] scopesToUse = determineScopesToUse(resource);

        // Build the token request context
        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(scopesToUse).setCaeEnabled(true);

        // Set tenant ID if authorization URI is available
        if (!CoreUtils.isNullOrEmpty(authUrl)) {
            tokenRequestContext.setTenantId(extractTenantIdFromUri(authUrl));
        }

        return tokenRequestContext;
    }

    // Determines which scopes to use based on the resource.
    String[] determineScopesToUse(String resource) {
        if (CoreUtils.isNullOrEmpty(resource)) {
            return initialScopes;
        }

        String scope = resource.endsWith(DEFAULT_SCOPE) ? resource : resource + DEFAULT_SCOPE;
        return new String[] { scope };
    }

    // Extracts challenge attributes from the authentication header and parses them into a map.
    static Map<String, String> extractChallengeAttributes(String header) {
        if (header == null) {
            return null;
        }

        // Find the beginning of the Bearer challenge even if it is not the first challenge.
        int bearerIndex = indexOfBearerChallenge(header);
        if (bearerIndex < 0) {
            return null;
        }

        // Substring starting at "Bearer"
        String bearerPortion = header.substring(bearerIndex);

        if (!bearerPortion.regionMatches(true, 0, BEARER_TOKEN_PREFIX, 0, BEARER_TOKEN_PREFIX.length())) {
            return null; // Defensive, should not happen.
        }

        // Remove "Bearer" prefix and trim any leading whitespace
        String remainingHeader = bearerPortion.substring(BEARER_TOKEN_PREFIX.length()).trim();

        // Split on commas first; if no commas present fall back to spaces.
        String[] parts = remainingHeader.contains(",") ? remainingHeader.split(",") : remainingHeader.split(" ");

        Map<String, String> output = new HashMap<>();
        for (String pair : parts) {
            String part = pair.trim();
            if (part.isEmpty()) {
                continue;
            }
            // Validate presence of '=' and that it's not the last character
            int eq = part.indexOf('=');
            if (eq < 0 || eq == part.length() - 1) {
                continue; // ignore malformed
            }

            // Extract key/value, trim, lowercase key
            // Strip surrounding quotes from value if present
            String key = part.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            String value = stripQuotes(part.substring(eq + 1).trim());

            output.put(key, value);
        }
        return output;
    }

    // Finds the index of a Bearer challenge token with a valid boundary (start, space, or comma before).
    private static int indexOfBearerChallenge(String header) {
        String lower = header.toLowerCase(Locale.ROOT);
        String needle = BEARER_TOKEN_PREFIX.toLowerCase(Locale.ROOT);
        for (int i = 0; i <= lower.length() - needle.length(); i++) {
            if (lower.regionMatches(i, needle, 0, needle.length())) {
                // Ensure boundary before (start, space, or comma)
                if (i == 0) {
                    return i;
                }
                char prev = header.charAt(i - 1);
                if (Character.isWhitespace(prev) || prev == ',') {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String stripQuotes(String v) {
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    String extractTenantIdFromUri(String uri) {
        try {
            String[] segments = new URI(uri).getPath().split("/");
            if (segments.length > 1 && !segments[1].isEmpty()) {
                return segments[1];
            }
            throw LOGGER.logExceptionAsError(new RuntimeException("Invalid authorization URI: tenantId not found"));
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Invalid authorization URI", e));
        }
    }
}
