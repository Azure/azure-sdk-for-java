// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A policy that authenticates requests with the Azure Key Vault service. The content added by this policy is
 * leveraged in {@link TokenCredential} to get and set the correct "Authorization" header value.
 *
 * @see TokenCredential
 */
public class KeyVaultCredentialPolicy extends BearerTokenAuthenticationPolicy {
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private static final String KEY_VAULT_STASHED_CONTENT_KEY = "KeyVaultCredentialPolicyStashedBody";
    private static final String KEY_VAULT_STASHED_CONTENT_LENGTH_KEY = "KeyVaultCredentialPolicyStashedContentLength";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final ConcurrentMap<String, String> SCOPE_CACHE = new ConcurrentHashMap<>();
    private String scope;

    /**
     * Creates a {@link KeyVaultCredentialPolicy}.
     *
     * @param credential The token credential to authenticate the request.
     */
    public KeyVaultCredentialPolicy(TokenCredential credential) {
        super(credential);
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

        String[] attributes = authenticateHeader.split(", ");
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
     * @return A boolean indicating if the challenge is a bearer challenge or not.
     */
    private static boolean isBearerChallenge(String authenticateHeader, String authChallengePrefix) {
        return (!CoreUtils.isNullOrEmpty(authenticateHeader)
            && authenticateHeader.toLowerCase(Locale.ROOT).startsWith(authChallengePrefix.toLowerCase(Locale.ROOT)));
    }

    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return Mono.defer(() -> {
            HttpRequest request = context.getHttpRequest();

            // If this policy doesn't have an authorityScope cached try to get it from the static challenge cache.
            if (this.scope == null) {
                String authority = getRequestAuthority(request);
                this.scope = SCOPE_CACHE.get(authority);
            }

            if (this.scope != null) {
                // We fetched the scope from the cache, but we have not initialized the scopes in the base yet.
                TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(this.scope);

                return setAuthorizationHeader(context, tokenRequestContext);
            }

            // The body is removed from the initial request because Key Vault supports other authentication schemes which
            // also protect the body of the request. As a result, before we know the auth scheme we need to avoid sending
            // an unprotected body to Key Vault. We don't currently support this enhanced auth scheme in the SDK but we
            // still don't want to send any unprotected data to vaults which require it.

            // Do not overwrite previous contents if retrying after initial request failed (e.g. timeout).
            if (!context.getData(KEY_VAULT_STASHED_CONTENT_KEY).isPresent()) {
                if (request.getBody() != null) {
                    context.setData(KEY_VAULT_STASHED_CONTENT_KEY, request.getBody());
                    context.setData(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY,
                        request.getHeaders().getValue(CONTENT_LENGTH_HEADER));
                    request.setHeader(CONTENT_LENGTH_HEADER, "0");
                    request.setBody((Flux<ByteBuffer>) null);
                }
            }

            return Mono.empty();
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response) {
        return Mono.defer(() -> {
            HttpRequest request = context.getHttpRequest();
            Optional<Object> contentOptional = context.getData(KEY_VAULT_STASHED_CONTENT_KEY);
            Optional<Object> contentLengthOptional = context.getData(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY);

            if (request.getBody() == null && contentOptional.isPresent() && contentLengthOptional.isPresent()) {
                request.setBody((Flux<ByteBuffer>) contentOptional.get());
                request.setHeader(CONTENT_LENGTH_HEADER, (String) contentLengthOptional.get());
            }

            String authority = getRequestAuthority(request);
            Map<String, String> challengeAttributes =
                extractChallengeAttributes(response.getHeaderValue(WWW_AUTHENTICATE), BEARER_TOKEN_PREFIX);
            String scope = challengeAttributes.get("resource");

            if (scope != null) {
                scope = scope + "/.default";
            } else {
                scope = challengeAttributes.get("scope");
            }

            if (scope == null) {
                this.scope = SCOPE_CACHE.get(authority);

                if (this.scope == null) {
                    return Mono.just(false);
                }
            } else {
                this.scope = scope;

                SCOPE_CACHE.put(authority, this.scope);
            }

            TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(this.scope);

            return setAuthorizationHeader(context, tokenRequestContext)
                .then(Mono.just(true));
        });
    }

    static void clearCache() {
        SCOPE_CACHE.clear();
    }

    private static String getRequestAuthority(HttpRequest request) {
        URL url = request.getUrl();
        String authority = url.getAuthority();
        int port = url.getPort();

        if (!authority.contains(":") && port > 0) {
            authority = authority + ":" + port;
        }

        return authority;
    }
}
