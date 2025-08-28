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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/**
 * The storage authorization policy which supports challenges.
 */
public class StorageBearerTokenChallengeAuthorizationPolicy extends BearerTokenAuthenticationPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StorageBearerTokenChallengeAuthorizationPolicy.class);

    private static final String DEFAULT_SCOPE = "/.default";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
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
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        Map<String, String> attributes = extractChallengeAttributes(authHeader);

        if (attributes.isEmpty()) {
            return Mono.just(false);
        }

        String resource = attributes.get(RESOURCE_ID);
        String authUrl = attributes.get(AUTHORIZATION_URI);

        String[] scopesToUse = initialScopes;
        if (!CoreUtils.isNullOrEmpty(resource)) {
            scopesToUse = new String[] { resource.endsWith(DEFAULT_SCOPE) ? resource : resource + DEFAULT_SCOPE };
        }

        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(scopesToUse).setCaeEnabled(true);

        if (!CoreUtils.isNullOrEmpty(authUrl)) {
            tokenRequestContext.setTenantId(extractTenantIdFromUri(authUrl));
        }
        return setAuthorizationHeader(context, tokenRequestContext).thenReturn(true);
    }

    @Override
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        String authHeader = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        Map<String, String> attributes = extractChallengeAttributes(authHeader);

        if (attributes.isEmpty()) {
            return false;
        }

        String resource = attributes.get(RESOURCE_ID);
        String authUrl = attributes.get(AUTHORIZATION_URI);

        String[] scopesToUse = initialScopes;
        if (!CoreUtils.isNullOrEmpty(resource)) {
            scopesToUse = new String[] { resource.endsWith(DEFAULT_SCOPE) ? resource : resource + DEFAULT_SCOPE };
        }

        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(scopesToUse).setCaeEnabled(true);

        if (!CoreUtils.isNullOrEmpty(authUrl)) {
            tokenRequestContext.setTenantId(extractTenantIdFromUri(authUrl));
        }

        setAuthorizationHeaderSync(context, tokenRequestContext);
        return true;
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

    Map<String, String> extractChallengeAttributes(String header) {
        if (header == null || !header.regionMatches(true, 0, BEARER_TOKEN_PREFIX, 0, BEARER_TOKEN_PREFIX.length())) {
            return Collections.emptyMap();
        }

        // Remove "Bearer " prefix and trim any leading whitespace
        // Don't lowercase the entire header as values can be corrupted
        String remainingHeader = header.substring(BEARER_TOKEN_PREFIX.length()).trim();

        // Split on commas first; if no commas present fall back to spaces.
        String[] parts = remainingHeader.contains(",") ? remainingHeader.split(",") : remainingHeader.split(" ");

        Map<String, String> output = new HashMap<>();
        for (String pair : parts) {
            String part = pair.trim();
            //
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

    private static String stripQuotes(String v) {
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }
}
