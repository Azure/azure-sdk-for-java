// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AccessTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.AuthenticateChallenge;
import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.azure.core.http.HttpHeaderName.AUTHORIZATION;
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
    private static final String KEY_VAULT_STASHED_CONTENT_KEY = "KeyVaultCredentialPolicyStashedBody";
    private static final String KEY_VAULT_STASHED_CONTENT_LENGTH_KEY = "KeyVaultCredentialPolicyStashedContentLength";
    private static final String KEY_VAULT_REQUEST_ORIGIN_KEY = "KeyVaultCredentialPolicyRequestOrigin";
    private static final String KEY_VAULT_REQUEST_CHALLENGE_KEY = "KeyVaultCredentialPolicyRequestChallenge";
    private static final ConcurrentMap<String, ChallengeParameters> CHALLENGE_CACHE = new ConcurrentHashMap<>();
    private volatile ChallengeParameters challenge;
    private final boolean disableChallengeResourceVerification;
    private final TokenCredential credential;
    private final ConcurrentMap<String, TokenCache> accessTokenCaches = new ConcurrentHashMap<>();

    /**
     * Creates a {@link KeyVaultCredentialPolicy}.
     *
     * @param credential The token credential to authenticate the request.
     */
    public KeyVaultCredentialPolicy(TokenCredential credential, boolean disableChallengeResourceVerification) {
        super(credential);

        this.disableChallengeResourceVerification = disableChallengeResourceVerification;
        this.credential = credential;
    }

    /**
     * Extracts attributes off the bearer challenge in the authentication header.
     *
     * @param authenticateHeader The authentication header containing the challenge.
     * @return A challenge attributes map.
     */
    private static Map<String, String> extractChallengeAttributes(String authenticateHeader) {
        AuthenticateChallenge bearerChallenge = CoreUtils.parseAuthenticateHeader(authenticateHeader)
            .stream()
            .filter(challenge -> "Bearer".equalsIgnoreCase(challenge.getScheme()))
            .findFirst()
            .orElse(null);

        if (bearerChallenge == null) {
            return Collections.emptyMap();
        }

        return bearerChallenge.getParameters();
    }

    @Override
    public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
        return Mono.defer(() -> {
            HttpRequest request = context.getHttpRequest();

            ChallengeParameters challenge = getChallenge(context);

            if (challenge != null) {
                return setAuthorizationHeader(context, challenge.toTokenRequestContext());
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

            String authority = getRequestAuthority(context);
            Map<String, String> challengeAttributes
                = extractChallengeAttributes(response.getHeaderValue(WWW_AUTHENTICATE));
            String scope = challengeAttributes.get("resource");

            if (scope != null) {
                scope = scope + "/.default";
            } else {
                scope = challengeAttributes.get("scope");
            }

            ChallengeParameters challenge;
            if (scope == null) {
                challenge = CHALLENGE_CACHE.get(authority);

                if (challenge == null) {
                    return Mono.just(false);
                }
            } else {
                if (!disableChallengeResourceVerification) {
                    if (!isChallengeResourceValid(request, scope)) {
                        throw LOGGER.logExceptionAsError(new RuntimeException(String
                            .format("The challenge resource '%s' does not match the requested domain. If you wish to "
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
                    throw LOGGER.logExceptionAsError(new RuntimeException(
                        String.format("The challenge authorization URI '%s' is invalid.", authorization), e));
                }

                challenge = new ChallengeParameters(authority, authorizationUri, new String[] { scope });

                CHALLENGE_CACHE.put(authority, challenge);
            }
            this.challenge = challenge;

            return setAuthorizationHeader(context, saveRequestChallenge(context, challenge, challengeAttributes))
                .then(Mono.just(true));
        });
    }

    @Override
    public void authorizeRequestSync(HttpPipelineCallContext context) {
        HttpRequest request = context.getHttpRequest();

        ChallengeParameters challenge = getChallenge(context);

        if (challenge != null) {
            setAuthorizationHeaderSync(context, challenge.toTokenRequestContext());

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
                context.setData(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY, request.getHeaders().getValue(CONTENT_LENGTH));
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

        String authority = getRequestAuthority(context);
        Map<String, String> challengeAttributes = extractChallengeAttributes(response.getHeaderValue(WWW_AUTHENTICATE));
        String scope = challengeAttributes.get("resource");

        if (scope != null) {
            scope = scope + "/.default";
        } else {
            scope = challengeAttributes.get("scope");
        }

        ChallengeParameters challenge;
        if (scope == null) {
            challenge = CHALLENGE_CACHE.get(authority);

            if (challenge == null) {
                return false;
            }
        } else {
            if (!disableChallengeResourceVerification) {
                if (!isChallengeResourceValid(request, scope)) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(String.format(
                        "The challenge resource '%s' does not match the requested domain. If you wish to disable "
                            + "this check for your client, pass 'true' to the SecretClientBuilder"
                            + ".disableChallengeResourceVerification() method when building it. See "
                            + "https://aka.ms/azsdk/blog/vault-uri for more information.",
                        scope)));
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
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("The challenge authorization URI '%s' is invalid.", authorization), e));
            }

            challenge = new ChallengeParameters(authority, authorizationUri, new String[] { scope });

            CHALLENGE_CACHE.put(authority, challenge);
        }
        this.challenge = challenge;

        setAuthorizationHeaderSync(context, saveRequestChallenge(context, challenge, challengeAttributes));

        return true;
    }

    @Override
    public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        return Mono.defer(() -> {
            String authority = getRequestAuthority(context);
            return getAccessTokenCache(authority, tokenRequestContext).getToken(tokenRequestContext, true)
                .flatMap(token -> {
                    // The request is mutable while asynchronous credential acquisition is in progress.
                    validateRequestAuthority(context, authority);
                    context.getHttpRequest().setHeader(AUTHORIZATION, "Bearer " + token.getToken());
                    return Mono.empty();
                });
        });
    }

    @Override
    public void setAuthorizationHeaderSync(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext) {
        String authority = getRequestAuthority(context);
        AccessToken token = getAccessTokenCache(authority, tokenRequestContext).getTokenSync(tokenRequestContext, true);
        validateRequestAuthority(context, authority);
        context.getHttpRequest().setHeader(AUTHORIZATION, "Bearer " + token.getToken());
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("Token credentials require a URL using the HTTPS protocol scheme."));
        }

        HttpPipelineNextPolicy nextPolicy = next.clone();

        return authorizeRequest(context).then(Mono.defer(next::process)).flatMap(httpResponse -> {
            String authHeader = httpResponse.getHeaderValue(WWW_AUTHENTICATE);

            if (httpResponse.getStatusCode() == 401 && authHeader != null) {
                return handleChallenge(context, httpResponse, nextPolicy);
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

        HttpPipelineNextSyncPolicy nextPolicy = next.clone();

        authorizeRequestSync(context);

        HttpResponse httpResponse = next.processSync();
        String authHeader = httpResponse.getHeaderValue(WWW_AUTHENTICATE);

        if (httpResponse.getStatusCode() == 401 && authHeader != null) {
            return handleChallengeSync(context, httpResponse, nextPolicy);
        }

        return httpResponse;
    }

    private Mono<HttpResponse> handleChallenge(HttpPipelineCallContext context, HttpResponse httpResponse,
        HttpPipelineNextPolicy next) {
        return authorizeRequestOnChallenge(context, httpResponse).flatMap(authorized -> {
            if (authorized) {
                // The body needs to be closed or read to the end to release the connection.
                httpResponse.close();

                HttpPipelineNextPolicy nextPolicy = next.clone();

                return next.process().flatMap(newResponse -> {
                    String authHeader = newResponse.getHeaderValue(WWW_AUTHENTICATE);

                    if (newResponse.getStatusCode() == 401
                        && authHeader != null
                        && isClaimsPresent(newResponse)
                        && !isClaimsPresent(httpResponse)) {

                        return handleChallenge(context, newResponse, nextPolicy);
                    } else {
                        return Mono.just(newResponse);
                    }
                });
            }

            return Mono.just(httpResponse);
        });
    }

    private HttpResponse handleChallengeSync(HttpPipelineCallContext context, HttpResponse httpResponse,
        HttpPipelineNextSyncPolicy next) {
        if (authorizeRequestOnChallengeSync(context, httpResponse)) {
            // The body needs to be closed or read to the end to release the connection.
            httpResponse.close();

            HttpPipelineNextSyncPolicy nextPolicy = next.clone();
            HttpResponse newResponse = next.processSync();
            String authHeader = newResponse.getHeaderValue(WWW_AUTHENTICATE);

            if (newResponse.getStatusCode() == 401
                && authHeader != null
                && isClaimsPresent(newResponse)
                && !isClaimsPresent(httpResponse)) {

                return handleChallengeSync(context, newResponse, nextPolicy);
            }

            return newResponse;
        }

        return httpResponse;
    }

    private boolean isClaimsPresent(HttpResponse httpResponse) {
        Map<String, String> challengeAttributes
            = extractChallengeAttributes(httpResponse.getHeaderValue(WWW_AUTHENTICATE));

        String error = challengeAttributes.get("error");

        if (error != null) {
            String base64Claims = challengeAttributes.get("claims");

            return "insufficient_claims".equalsIgnoreCase(error) && base64Claims != null;
        }

        return false;
    }

    private static TokenRequestContext saveRequestChallenge(HttpPipelineCallContext context,
        ChallengeParameters challenge, Map<String, String> attributes) {
        String claims = null;
        String error = attributes.get("error");
        if (error != null) {
            LOGGER.verbose("The challenge response contained an error: {}", error);
            if ("insufficient_claims".equalsIgnoreCase(error) && attributes.get("claims") != null) {
                claims = new String(Base64Util.decodeString(attributes.get("claims")), StandardCharsets.UTF_8);
            }
        }
        ChallengeParameters requestChallenge
            = new ChallengeParameters(challenge.authority, challenge.authorizationUri, challenge.scopes, claims);
        // Persist an immutable, request-local snapshot before acquisition, including when acquisition fails.
        context.setData(KEY_VAULT_REQUEST_CHALLENGE_KEY, requestChallenge);
        return requestChallenge.toTokenRequestContext();
    }

    private AccessTokenCache getAccessTokenCache(String authority, TokenRequestContext context) {
        if (context == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'tokenRequestContext' cannot be null."));
        }
        // Keep one current context per origin. A failed refresh must never expose a token from an older context.
        return accessTokenCaches.compute(authority,
            (ignored, existing) -> existing != null && existing.matches(context)
                ? existing
                : new TokenCache(credential, context)).cache;
    }

    private static final class TokenCache {
        private final AccessTokenCache cache;
        private final List<String> scopes;
        private final String tenantId;
        private final String claims;
        private final boolean caeEnabled;

        private TokenCache(TokenCredential credential, TokenRequestContext context) {
            cache = new AccessTokenCache(credential);
            scopes = new ArrayList<>(context.getScopes());
            tenantId = context.getTenantId();
            claims = context.getClaims();
            caeEnabled = context.isCaeEnabled();
        }

        private boolean matches(TokenRequestContext context) {
            return scopes.equals(context.getScopes())
                && Objects.equals(tenantId, context.getTenantId())
                && Objects.equals(claims, context.getClaims())
                && caeEnabled == context.isCaeEnabled();
        }
    }

    private static class ChallengeParameters {
        private final String authority;
        private final URI authorizationUri;
        private final String tenantId;
        private final String[] scopes;
        private final String claims;

        ChallengeParameters(String authority, URI authorizationUri, String[] scopes) {
            this(authority, authorizationUri, scopes, null);
        }

        private ChallengeParameters(String authority, URI authorizationUri, String[] scopes, String claims) {
            this.authority = authority;
            this.authorizationUri = authorizationUri;
            tenantId = authorizationUri.getPath().split("/")[1];
            this.scopes = scopes.clone();
            this.claims = claims;
        }

        private TokenRequestContext toTokenRequestContext() {
            return new TokenRequestContext().addScopes(scopes)
                .setTenantId(tenantId)
                .setClaims(claims)
                .setCaeEnabled(true);
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

    private ChallengeParameters getChallenge(HttpPipelineCallContext context) {
        String authority = getRequestAuthority(context);
        ChallengeParameters requestChallenge
            = (ChallengeParameters) context.getData(KEY_VAULT_REQUEST_CHALLENGE_KEY).orElse(null);
        if (requestChallenge != null) {
            return requestChallenge;
        }
        // Use one immutable snapshot so concurrent requests cannot mix origins, scopes, or tenants.
        ChallengeParameters cachedChallenge = this.challenge;
        if (cachedChallenge == null || !authority.equals(cachedChallenge.authority)) {
            cachedChallenge = CHALLENGE_CACHE.get(authority);
            this.challenge = cachedChallenge;
        }
        return cachedChallenge;
    }

    private static String getRequestAuthority(HttpPipelineCallContext context) {
        String authority = (String) context.getData(KEY_VAULT_REQUEST_ORIGIN_KEY).orElse(null);
        if (authority == null) {
            authority = KeyVaultEndpointValidation.getOrigin(context.getHttpRequest().getUrl().toString());
            context.setData(KEY_VAULT_REQUEST_ORIGIN_KEY, authority);
        }
        validateRequestAuthority(context, authority);
        return authority;
    }

    private static void validateRequestAuthority(HttpPipelineCallContext context, String authority) {
        HttpRequest request = context.getHttpRequest();
        try {
            if (!authority.equals(KeyVaultEndpointValidation.getOrigin(request.getUrl().toString()))) {
                throw new IllegalArgumentException(
                    "The request URL must have the same origin as the original request.");
            }
        } catch (RuntimeException e) {
            // A previous attempt may already have authorized this request before its destination changed.
            request.getHeaders().remove(AUTHORIZATION);
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private static boolean isChallengeResourceValid(HttpRequest request, String scope) {
        final URI scopeUri;

        try {
            scopeUri = new URI(scope);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException(String.format("The challenge resource '%s' is not a valid URI.", scope), e));
        }

        String host = request.getUrl().getHost();
        String toEndWith = "." + scopeUri.getHost();

        // Returns false if the host specified in the scope does not match the requested domain.
        return host.regionMatches(true, host.length() - toEndWith.length(), toEndWith, 0, toEndWith.length());
    }
}
