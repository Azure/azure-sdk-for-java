// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A policy that authenticates requests with the Storage Table Service and supports challenges including tenantId
 * discovery. The content added by this policy is leveraged in {@link TokenCredential} to get and set the correct
 * "Authorization" header value.
 *
 * @see TokenCredential
 * @see BearerTokenAuthenticationPolicy
 */
public class TableBearerTokenChallengeAuthorizationPolicy extends BearerTokenAuthenticationPolicy {
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private String[] scopes;
    private volatile String tenantId;
    private boolean enableTenantDiscovery;

    /**
     * Creates a {@link TableBearerTokenChallengeAuthorizationPolicy}.
     *
     * @param credential The token credential to authenticate the request.
     */
    public TableBearerTokenChallengeAuthorizationPolicy(TokenCredential credential, boolean enableTenantDiscovery,
                                                        String... scopes) {
        super(credential, scopes);
        this.scopes = scopes;
        this.enableTenantDiscovery = enableTenantDiscovery;
    }

    /**
     * Extracts attributes off the bearer challenge in the authentication header.
     *
     * @param authenticateHeader The authentication header containing the challenge.
     * @param authChallengePrefix The authentication challenge name.
     *
     * @return A challenge attributes map.
     */
    private static Map<String, String> extractChallengeAttributes(String authenticateHeader,
                                                                  String authChallengePrefix) {
        if (!isBearerChallenge(authenticateHeader, authChallengePrefix)) {
            return Collections.emptyMap();
        }

        authenticateHeader =
            authenticateHeader.toLowerCase(Locale.ROOT).replace(authChallengePrefix.toLowerCase(Locale.ROOT), "");

        String[] attributes = authenticateHeader.split(" ");
        Map<String, String> attributeMap = new HashMap<>();

        for (String pair : attributes) {
            String[] keyValue = pair.split("=");

            attributeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }

        return attributeMap;
    }

    /**
     * Verifies whether a challenge is bearer or not.
     *
     * @param authenticateHeader The authentication header containing all the challenges.
     * @param authChallengePrefix The authentication challenge name.
     *
     * @return A boolean indicating if the challenge is a bearer challenge or not.
     */
    private static boolean isBearerChallenge(String authenticateHeader, String authChallengePrefix) {
        return (!CoreUtils.isNullOrEmpty(authenticateHeader)
            && authenticateHeader.toLowerCase(Locale.ROOT).startsWith(authChallengePrefix.toLowerCase(Locale.ROOT)));
    }

    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return Mono.defer(() -> {
            if (this.tenantId != null || !enableTenantDiscovery) {
                TokenRequestContext tokenRequestContext = new TokenRequestContext()
                    .addScopes(this.scopes)
                    .setTenantId(this.tenantId);

                return setAuthorizationHeader(context, tokenRequestContext);
            }

            return Mono.empty();
        });
    }

    @Override
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.defer(() -> {
            Map<String, String> challengeAttributes =
                extractChallengeAttributes(response.getHeaderValue(WWW_AUTHENTICATE), BEARER_TOKEN_PREFIX);

            String authorizationUriString = challengeAttributes.get("authorization_uri");
            final URI authorizationUri;

            try {
                authorizationUri = new URI(authorizationUriString);
            } catch (URISyntaxException e) {
                // The challenge authorization URI is invalid.
                return Mono.just(false);
            }

            this.tenantId = authorizationUri.getPath().split("/")[1];

            TokenRequestContext tokenRequestContext = new TokenRequestContext()
                .addScopes(this.scopes)
                .setTenantId(this.tenantId);

            return setAuthorizationHeader(context, tokenRequestContext)
                .then(Mono.just(true));
        });
    }
}
