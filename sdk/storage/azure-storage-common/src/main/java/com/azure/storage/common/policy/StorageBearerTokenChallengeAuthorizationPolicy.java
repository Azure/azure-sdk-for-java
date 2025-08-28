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
 * The storage authorization policy which supports challenge.
 */
public class StorageBearerTokenChallengeAuthorizationPolicy extends BearerTokenAuthenticationPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(StorageBearerTokenChallengeAuthorizationPolicy.class);

    private static final String DEFAULT_SCOPE = "/.default";
    static final String BEARER_TOKEN_PREFIX = "Bearer ";

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

        String resource = getScopeFromChallenges(attributes);
        String authUrl = getAuthorizationFromChallenges(attributes);

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

        String resource = getScopeFromChallenges(attributes);
        String authUrl = getAuthorizationFromChallenges(attributes);

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
            if (segments.length > 1) {
                return segments[1];
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException("Invalid authorization URI: tenantId not found"));
            }
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Invalid authorization URI", e));
        }
    }

    Map<String, String> extractChallengeAttributes(String header) {
        if (!isBearerChallenge(header)) {
            return Collections.emptyMap();
        }

        header = header.toLowerCase(Locale.ROOT).replace(BEARER_TOKEN_PREFIX.toLowerCase(Locale.ROOT), "");

        String[] attributes = header.split(" ");
        Map<String, String> attributeMap = new HashMap<>();

        for (String pair : attributes) {
            String[] keyValue = pair.split("=");

            attributeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }

        return attributeMap;
    }

    static boolean isBearerChallenge(String authenticateHeader) {
        return (!CoreUtils.isNullOrEmpty(authenticateHeader)
            && authenticateHeader.toLowerCase(Locale.ROOT).startsWith(BEARER_TOKEN_PREFIX.toLowerCase(Locale.ROOT)));
    }

    String getScopeFromChallenges(Map<String, String> challenges) {
        return challenges.get("resource_id");
    }

    String getAuthorizationFromChallenges(Map<String, String> challenges) {
        return challenges.get("authorization_uri");
    }
}
