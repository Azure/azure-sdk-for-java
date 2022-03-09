// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
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
    private static final ConcurrentMap<String, ChallengeParameters> CHALLENGE_CACHE = new ConcurrentHashMap<>();
    private ChallengeParameters challenge;

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
            HttpRequest request = context.getHttpRequest();

            // If this policy doesn't have challenge parameters cached try to get it from the static challenge cache.
            if (this.challenge == null) {
                String authority = getRequestAuthority(request);
                this.challenge = CHALLENGE_CACHE.get(authority);
            }

            if (this.challenge != null) {
                // We fetched the challenge from the cache, but we have not initialized the scopes in the base yet.
                TokenRequestContext tokenRequestContext = new TokenRequestContext()
                    .addScopes(this.challenge.getScopes())
                    .setTenantId(this.challenge.getTenantId());

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
                this.challenge = CHALLENGE_CACHE.get(authority);

                if (this.challenge == null) {
                    return Mono.just(false);
                }
            } else {
                String authorization = challengeAttributes.get("authorization");

                if (authorization == null) {
                    authorization = challengeAttributes.get("authorization_uri");
                }

                final URI authorizationUri;

                try {
                    authorizationUri = new URI(authorization);
                } catch (URISyntaxException e) {
                    // The challenge authorization URI is invalid.
                    return Mono.just(false);
                }

                this.challenge = new ChallengeParameters(authorizationUri, new String[] { scope });

                CHALLENGE_CACHE.put(authority, this.challenge);
            }

            TokenRequestContext tokenRequestContext = new TokenRequestContext()
                .addScopes(this.challenge.getScopes())
                .setTenantId(this.challenge.getTenantId());

            return setAuthorizationHeader(context, tokenRequestContext)
                .then(Mono.just(true));
        });
    }

    private static class ChallengeParameters {
        private final URI authorizationUri;
        private final String tenantId;
        private final String[] scopes;

        ChallengeParameters(URI authorizationUri, String[] scopes) {
            this.authorizationUri = authorizationUri;
            tenantId = authorizationUri.getPath().split("/")[1];
            this.scopes = scopes;
        }

        /**
         * Get the {@code authorization} or {@code authorization_uri} parameter from the challenge response.
         */
        public URI getAuthorizationUri() {
            return authorizationUri;
        }

        /**
         * Get the {@code resource} or {@code scope} parameter from the challenge response. This should end with
         * "/.default".
         */
        public String[] getScopes() {
            return scopes;
        }

        /**
         * Get the tenant ID from {@code authorizationUri}.
         */
        public String getTenantId() {
            return tenantId;
        }
    }

    public static void clearCache() {
        CHALLENGE_CACHE.clear();
    }

    /**
     * Gets the host name and port of the Key Vault or Managed HSM endpoint.
     *
     * @param request The {@link HttpRequest} to extract the host name and port from.
     *
     * @return The host name and port of the Key Vault or Managed HSM endpoint.
     */
    private static String getRequestAuthority(HttpRequest request) {
        URL url = request.getUrl();
        String authority = url.getAuthority();
        int port = url.getPort();

        // Append port for complete authority.
        if (!authority.contains(":") && port > 0) {
            authority = authority + ":" + port;
        }

        return authority;
    }
}
