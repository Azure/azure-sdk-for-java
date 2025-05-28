// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.core.http.pipeline.BearerTokenAuthenticationPolicy;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.Base64Uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.clientcore.core.http.models.HttpHeaderName.CONTENT_LENGTH;
import static io.clientcore.core.http.models.HttpHeaderName.WWW_AUTHENTICATE;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

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
     * @param disableChallengeResourceVerification A boolean indicating whether to disable the challenge resource
     * verification.
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

        String[] attributes = authenticateHeader.replace("\"", "").substring(authChallengePrefix.length()).split(",");
        Map<String, String> attributeMap = new HashMap<>();

        for (String pair : attributes) {
            // Using trim is ugly, but we need it here because currently the 'claims' attribute comes after two spaces.
            String[] keyValue = pair.trim().split("=", 2);

            attributeMap.put(keyValue[0], keyValue[1]);
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
        return (!isNullOrEmpty(authenticateHeader)
            && authenticateHeader.toLowerCase(Locale.ROOT).startsWith(authChallengePrefix.toLowerCase(Locale.ROOT)));
    }

    private Map<String, Object> authorizeRequestInternal(HttpRequest request) {
        RequestContext requestContext = request.getContext();

        // If this policy doesn't have challenge parameters cached try to get it from the static challenge cache.
        if (this.challenge == null) {
            this.challenge = CHALLENGE_CACHE.get(getRequestAuthority(request));
        }

        if (this.challenge != null) {
            // We fetched the challenge from the cache, but we have not initialized the scopes in the base yet.
            TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(this.challenge.getScopes())
                .setTenantId(this.challenge.getTenantId())
                .setCaeEnabled(true);

            setAuthorizationHeader(request, tokenRequestContext);

            return null;
        }

        // The body is removed from the initial request because Key Vault supports other authentication schemes which
        // also protect the body of the request. As a result, before we know the auth scheme we need to avoid sending an
        // unprotected body to Key Vault. We don't currently support this enhanced auth scheme in the SDK, but we still
        // don't want to send any unprotected data to vaults which require it.

        // Do not overwrite previous contents if retrying after initial request failed (e.g. timeout).
        if (requestContext.getMetadata(KEY_VAULT_STASHED_CONTENT_KEY) == null) {
            if (request.getBody() != null) {
                Map<String, Object> bodyCache = new HashMap<>();

                bodyCache.put(KEY_VAULT_STASHED_CONTENT_KEY, request.getBody());
                bodyCache.put(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY, request.getHeaders().getValue(CONTENT_LENGTH));
                request.getHeaders().set(CONTENT_LENGTH, "0");
                request.setBody(null);

                return bodyCache;
            }
        }

        return null;
    }

    private boolean authorizeRequestOnChallengeInternal(HttpRequest request, Response<?> response,
        Map<String, Object> bodyCache) {

        if (bodyCache != null) {
            Object content = bodyCache.get(KEY_VAULT_STASHED_CONTENT_KEY);
            Object contentLength = bodyCache.get(KEY_VAULT_STASHED_CONTENT_LENGTH_KEY);

            if (request.getBody() == null && content != null && contentLength != null) {
                request.setBody((BinaryData) content);
                request.getHeaders().set(CONTENT_LENGTH, (String) contentLength);
            }
        }

        String authority = getRequestAuthority(request);
        Map<String, String> challengeAttributes
            = extractChallengeAttributes(response.getHeaders().getValue(WWW_AUTHENTICATE), BEARER_TOKEN_PREFIX);
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
                    throw LOGGER.throwableAtError()
                        .addKeyValue("scope", scope)
                        .log("The challenge resource does not match the requested domain. If you wish to disable "
                            + "this check for your client, pass 'true' to the SecretClientBuilder"
                            + ".disableChallengeResourceVerification() method when building it. See "
                            + "https://aka.ms/azsdk/blog/vault-uri for more information.", CoreException::from);
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
                throw LOGGER.throwableAtError()
                    .addKeyValue("authorization", authorization)
                    .log("The challenge authorization URI is not a valid URI.", e, CoreException::from);
            }

            this.challenge = new ChallengeParameters(authorizationUri, new String[] { scope });

            CHALLENGE_CACHE.put(authority, this.challenge);
        }

        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(this.challenge.getScopes())
            .setTenantId(this.challenge.getTenantId())
            .setCaeEnabled(true);

        String error = challengeAttributes.get("error");

        if (error != null) {
            LOGGER.atVerbose().addKeyValue("error", error).log("The challenge response contained an error");

            if ("insufficient_claims".equalsIgnoreCase(error)) {
                String claims = challengeAttributes.get("claims");

                if (claims != null) {
                    tokenRequestContext.setClaims(new String(new Base64Uri(claims).decodedBytes()));
                }
            }
        }

        setAuthorizationHeader(request, tokenRequestContext);

        return true;
    }

    @Override
    public Response<BinaryData> process(HttpRequest request, HttpPipelineNextPolicy next) {
        if (!"https".equals(request.getUri().getScheme())) {
            throw LOGGER.throwableAtError()
                .addKeyValue("scheme", request.getUri().getScheme())
                .log("Token credentials require a URL using the HTTPS protocol scheme.", IllegalStateException::new);
        }

        HttpPipelineNextPolicy nextPolicy = next.copy();

        Map<String, Object> bodyCache = authorizeRequestInternal(request);

        Response<BinaryData> response = next.process();
        String authHeader = response.getHeaders().getValue(WWW_AUTHENTICATE);

        if (response.getStatusCode() == 401 && authHeader != null) {
            return handleChallenge(request, response, nextPolicy, bodyCache);
        }

        return response;
    }

    private Response<BinaryData> handleChallenge(HttpRequest request, Response<BinaryData> response,
        HttpPipelineNextPolicy next, Map<String, Object> bodyCache) {

        if (authorizeRequestOnChallengeInternal(request, response, bodyCache)) {
            // The body needs to be closed or read to the end to release the connection.
            response.close();

            HttpPipelineNextPolicy nextPolicy = next.copy();
            Response<BinaryData> newResponse = next.process();
            String authHeader = newResponse.getHeaders().getValue(WWW_AUTHENTICATE);

            if (newResponse.getStatusCode() == 401
                && authHeader != null
                && isClaimsPresent(newResponse)
                && !isClaimsPresent(response)) {

                return handleChallenge(request, newResponse, nextPolicy, bodyCache);
            }

            return newResponse;
        }

        return response;
    }

    private boolean isClaimsPresent(Response<?> httpResponse) {
        Map<String, String> challengeAttributes
            = extractChallengeAttributes(httpResponse.getHeaders().getValue(WWW_AUTHENTICATE), BEARER_TOKEN_PREFIX);

        String error = challengeAttributes.get("error");

        if (error != null) {
            String base64Claims = challengeAttributes.get("claims");

            return "insufficient_claims".equalsIgnoreCase(error) && base64Claims != null;
        }

        return false;
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
        URI getAuthorizationUri() {
            return authorizationUri;
        }

        /**
         * Get the {@code resource} or {@code scope} parameter from the challenge response. This should end with
         * "/.default".
         */
        String[] getScopes() {
            return scopes;
        }

        /**
         * Get the tenant ID from {@code authorizationUri}.
         */
        String getTenantId() {
            return tenantId;
        }
    }

    /**
     * Clears the challenge cache.
     */
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
        URI uri = request.getUri();
        String authority = uri.getAuthority();
        int port = uri.getPort();

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
            throw LOGGER.throwableAtError()
                .addKeyValue("scope", scope)
                .log("The challenge resource is not a valid URI.", e, IllegalArgumentException::new);
        }

        // Returns false if the host specified in the scope does not match the requested domain.
        return request.getUri()
            .getHost()
            .toLowerCase(Locale.ROOT)
            .endsWith("." + scopeUri.getHost().toLowerCase(Locale.ROOT));
    }
}
