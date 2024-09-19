// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
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

import static com.azure.core.http.HttpHeaderName.CONTENT_LENGTH;
import static com.azure.core.http.HttpHeaderName.WWW_AUTHENTICATE;

/**
 * A policy that authenticates requests with the Azure Key Vault service. The content added by this policy is
 * leveraged in {@link TokenCredential} to get and set the correct "Authorization" header value.
 *
 * @see TokenCredential
 */
public class KeyVaultCredentialPolicy extends BearerTokenAuthenticationPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultCredentialPolicy.class);
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String KEY_VAULT_STASHED_CONTENT_KEY = "KeyVaultCredentialPolicyStashedBody";
    private static final String KEY_VAULT_STASHED_CONTENT_LENGTH_KEY = "KeyVaultCredentialPolicyStashedContentLength";
    private static final ConcurrentMap<String, ChallengeParameters> CHALLENGE_CACHE = new ConcurrentHashMap<>();
    private ChallengeParameters challenge;
    private final boolean disableChallengeResourceVerification;

    /**
     * Creates a {@link KeyVaultCredentialPolicy}.
     *
     * @param credential The token credential to authenticate the request.
     */
    public KeyVaultCredentialPolicy(TokenCredential credential, boolean disableChallengeResourceVerification) {
        super(credential);

        this.disableChallengeResourceVerification = disableChallengeResourceVerification;
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

        String[] attributes = authenticateHeader.substring(authChallengePrefix.length()).split(", ");
        Map<String, String> attributeMap = new HashMap<>();

        for (String pair : attributes) {
            // This is ugly, but we need to trim here because currently the 'claims' attribute comes after two spaces.
            pair = pair.trim();

            if (pair.startsWith("claims=")) {
                attributeMap.put("claims", pair.substring("claims=".length()).replaceAll("\"", ""));
            } else {
                String[] keyValue = pair.split("=");

                attributeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
            }
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
                this.challenge = CHALLENGE_CACHE.get(getRequestAuthority(request));
            }

            if (this.challenge != null) {
                // We fetched the challenge from the cache, but we have not initialized the scopes in the base yet.
                TokenRequestContext tokenRequestContext = new TokenRequestContext()
                    .addScopes(this.challenge.getScopes())
                    .setTenantId(this.challenge.getTenantId());

                return setAuthorizationHeader(context, tokenRequestContext);
            }

            // The body is removed from the initial request because Key Vault supports other authentication schemes
            // which also protect the body of the request. As a result, before we know the auth scheme we need to
            // avoid sending an unprotected body to Key Vault. We don't currently support this enhanced auth scheme
            // in the SDK, but we still don't want to send any unprotected data to vaults which require it.

            // Do not overwrite previous contents if retrying after initial request failed (e.g. timeout).
            if (!context.getData(KEY_VAULT_STASHED_CONTENT_KEY).isPresent()) {
                if (request.getBody() != null) {
                    context.setData(KEY_VAULT_STASHED_CONTENT_KEY, request.getBody());
                    context.setData(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY,
                        request.getHeaders().getValue(CONTENT_LENGTH));
                    request.setHeader(CONTENT_LENGTH, "0");
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
                request.setHeader(CONTENT_LENGTH, (String) contentLengthOptional.get());
            }

            String authority = getRequestAuthority(request);
            Map<String, String> challengeAttributes =
                extractChallengeAttributes(response.getHeaderValue(WWW_AUTHENTICATE), BEARER_TOKEN_PREFIX);

            String error = challengeAttributes.get("error");
            String claims = null;

            if (error != null) {
                LOGGER.verbose(String.format("The challenge response contained an error: %s", error));

                String base64Claims = challengeAttributes.get("claims");

                if (error.equalsIgnoreCase("insufficient_claims") && base64Claims != null) {
                    claims = new String(Base64Util.decodeString(base64Claims));
                }
            }

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
                if (!disableChallengeResourceVerification) {
                    if (!isChallengeResourceValid(request, scope)) {
                        throw LOGGER.logExceptionAsError(
                            new RuntimeException(String.format(
                                "The challenge resource '%s' does not match the requested domain. If you wish to "
                                    + "disable this check for your client, pass 'true' to the SecretClientBuilder"
                                    + ".disableChallengeResourceVerification() method when building it. See "
                                    + "https://aka.ms/azsdk/blog/vault-uri for more information.", scope)));
                    }
                }

                String authorization = challengeAttributes.get("authorization");

                if (authorization == null) {
                    authorization = challengeAttributes.get("authorization_uri");
                }

                final URI authorizationUri;

                try {
                    authorizationUri = new URI(authorization);
                } catch (URISyntaxException e) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException(
                            String.format("The challenge authorization URI '%s' is invalid.", authorization), e));
                }

                this.challenge = new ChallengeParameters(authorizationUri, new String[] {scope});

                CHALLENGE_CACHE.put(authority, this.challenge);
            }

            TokenRequestContext tokenRequestContext = new TokenRequestContext()
                .addScopes(this.challenge.getScopes())
                .setTenantId(this.challenge.getTenantId());

            if (claims != null) {
                tokenRequestContext
                    .setCaeEnabled(true)
                    .setClaims(claims);
            }

            return setAuthorizationHeader(context, tokenRequestContext)
                .then(Mono.just(true));
        });
    }

    @Override
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        HttpRequest request = context.getHttpRequest();

        // If this policy doesn't have challenge parameters cached try to get it from the static challenge cache.
        if (this.challenge == null) {
            this.challenge = CHALLENGE_CACHE.get(getRequestAuthority(request));
        }

        if (this.challenge != null) {
            // We fetched the challenge from the cache, but we have not initialized the scopes in the base yet.
            TokenRequestContext tokenRequestContext = new TokenRequestContext()
                .addScopes(this.challenge.getScopes())
                .setTenantId(this.challenge.getTenantId());

            setAuthorizationHeaderSync(context, tokenRequestContext);

            return;
        }

        // The body is removed from the initial request because Key Vault supports other authentication schemes which
        // also protect the body of the request. As a result, before we know the auth scheme we need to avoid sending an
        // unprotected body to Key Vault. We don't currently support this enhanced auth scheme in the SDK, but we still
        // don't want to send any unprotected data to vaults which require it.

        // Do not overwrite previous contents if retrying after initial request failed (e.g. timeout).
        if (!context.getData(KEY_VAULT_STASHED_CONTENT_KEY).isPresent()) {
            if (request.getBodyAsBinaryData() != null) {
                context.setData(KEY_VAULT_STASHED_CONTENT_KEY, request.getBodyAsBinaryData());
                context.setData(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY,
                    request.getHeaders().getValue(CONTENT_LENGTH));
                request.setHeader(CONTENT_LENGTH, "0");
                request.setBody((BinaryData) null);
            }
        }
    }

    @Override
    public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response) {
        HttpRequest request = context.getHttpRequest();
        Optional<Object> contentOptional = context.getData(KEY_VAULT_STASHED_CONTENT_KEY);
        Optional<Object> contentLengthOptional = context.getData(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY);

        if (request.getBody() == null && contentOptional.isPresent() && contentLengthOptional.isPresent()) {
            request.setBody((BinaryData) (contentOptional.get()));
            request.setHeader(CONTENT_LENGTH, (String) contentLengthOptional.get());
        }

        String authority = getRequestAuthority(request);
        Map<String, String> challengeAttributes =
            extractChallengeAttributes(response.getHeaderValue(WWW_AUTHENTICATE), BEARER_TOKEN_PREFIX);

        String error = challengeAttributes.get("error");
        String claims = null;

        if (error != null) {
            LOGGER.verbose(String.format("The challenge response contained an error: %s", error));

            String base64Claims = challengeAttributes.get("claims");

            if (error.equalsIgnoreCase("insufficient_claims") && base64Claims != null) {
                claims = new String(Base64Util.decodeString(base64Claims));
            }
        }

        String scope = challengeAttributes.get("resource");

        if (scope != null) {
            scope = scope + "/.default";
        } else {
            scope = challengeAttributes.get("scope");
        }

        if (scope == null) {
            this.challenge = CHALLENGE_CACHE.get(authority);

            if (this.challenge == null) {
                return false;
            }
        } else {
            if (!disableChallengeResourceVerification) {
                if (!isChallengeResourceValid(request, scope)) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException(String.format(
                            "The challenge resource '%s' does not match the requested domain. If you wish to disable "
                                + "this check for your client, pass 'true' to the SecretClientBuilder"
                                + ".disableChallengeResourceVerification() method when building it. See "
                                + "https://aka.ms/azsdk/blog/vault-uri for more information.", scope)));
                }
            }

            String authorization = challengeAttributes.get("authorization");

            if (authorization == null) {
                authorization = challengeAttributes.get("authorization_uri");
            }

            final URI authorizationUri;

            try {
                authorizationUri = new URI(authorization);
            } catch (URISyntaxException e) {
                throw LOGGER.logExceptionAsError(
                    new RuntimeException(
                        String.format("The challenge authorization URI '%s' is invalid.", authorization), e));
            }

            this.challenge = new ChallengeParameters(authorizationUri, new String[] {scope});

            CHALLENGE_CACHE.put(authority, this.challenge);
        }

        TokenRequestContext tokenRequestContext = new TokenRequestContext()
            .addScopes(this.challenge.getScopes())
            .setTenantId(this.challenge.getTenantId());

        if (claims != null) {
            tokenRequestContext
                .setCaeEnabled(true)
                .setClaims(claims);
        }

        setAuthorizationHeaderSync(context, tokenRequestContext);

        return true;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("Token credentials require a URL using the HTTPS protocol scheme."));
        }

        HttpPipelineNextPolicy nextPolicy = next.clone();

        return authorizeRequest(context).then(Mono.defer(next::process)).flatMap(httpResponse -> {
            String authHeader = httpResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);

            if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                return authorizeRequestOnChallenge(context, httpResponse).flatMap(authorized -> {
                    if (authorized) {
                        // The body needs to be closed or read to the end to release the connection.
                        httpResponse.close();

                        return nextPolicy.process();
                    } else {
                        return Mono.just(httpResponse);
                    }
                });
            }

            return Mono.just(httpResponse);
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Token credentials require a URL using the HTTPS protocol scheme."));
        }

        HttpPipelineNextSyncPolicy firstClone = next.clone();

        authorizeRequestSync(context);

        HttpResponse firstResponse = next.processSync();
        String firstAuthHeader = firstResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);

        if (firstResponse.getStatusCode() == 401 && firstAuthHeader != null) {
            if (authorizeRequestOnChallengeSync(context, firstResponse)) {
                // The body needs to be closed or read to the end to release the connection.
                firstResponse.close();

                HttpPipelineNextSyncPolicy secondClone = firstClone.clone();
                HttpResponse secondResponse = firstClone.processSync();
                String secondAuthHeader = secondResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);

                if (secondResponse.getStatusCode() == 401
                    && secondAuthHeader != null
                    // We should only retry if the first challenge does not have an 'insufficient claims' error.
                    && !firstAuthHeader.contains("insufficient_claims")) {

                    if (authorizeRequestOnChallengeSync(context, secondResponse)) {
                        // The body needs to be closed or read to the end to release the connection.
                        secondResponse.close();

                        return secondClone.processSync();
                    } else {
                        return secondResponse;
                    }
                } else {
                    return secondResponse;
                }
            } else {
                return firstResponse;
            }
        }

        return firstResponse;
    }

    private HttpResponse handleChallenge(HttpPipelineCallContext context, HttpResponse httpResponse,
                                         HttpPipelineNextSyncPolicy nextPolicy) {
        if (authorizeRequestOnChallengeSync(context, httpResponse)) {
            // The body needs to be closed or read to the end to release the connection.
            httpResponse.close();

            return nextPolicy.processSync();
        } else {
            return httpResponse;
        }
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

    private static boolean isChallengeResourceValid(HttpRequest request, String scope) {
        final URI scopeUri;

        try {
            scopeUri = new URI(scope);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException(
                    String.format("The challenge resource '%s' is not a valid URI.", scope), e));
        }

        // Returns false if the host specified in the scope does not match the requested domain.
        return request.getUrl().getHost().toLowerCase(Locale.ROOT)
            .endsWith("." + scopeUri.getHost().toLowerCase(Locale.ROOT));
    }
}
