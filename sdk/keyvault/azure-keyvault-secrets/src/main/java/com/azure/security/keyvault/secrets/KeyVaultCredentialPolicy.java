package com.azure.security.keyvault.secrets;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A policy that authenticates requests with Azure Key Vault service. The content added by this policy
 * is leveraged in {@link TokenCredential} to get and set the correct "Authorization" header value.
 *
 * @see TokenCredential
 */
public final class KeyVaultCredentialPolicy implements HttpPipelinePolicy {
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private final ScopeTokenCache cache;

    private TokenCredential credential;

    /**
     * Creates KeyVaultCredentialPolicy.
     *
     * @param credential the token credential to authenticate the request
     */
    public KeyVaultCredentialPolicy(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.credential = credential;
        this.cache = new ScopeTokenCache((scopes) -> credential.getToken(scopes));
    }

    /**
     * Adds the required header to authenticate a request to Azure Key Vault service.
     *
     * @param context The request context
     * @param next The next HTTP pipeline policy to process the {@code context's} request after this policy completes.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.clone().process()
                // Ignore body
                .doOnNext(HttpResponse::close)
                .map(res -> res.headerValue(WWW_AUTHENTICATE))
                .map(header -> extractChallenge(header, BEARER_TOKEP_REFIX))
                .flatMap(map -> {
                    cache.scopes(map.get("resource")+"/.default");
                    return cache.getToken();
                })
                .flatMap(token -> {
                    context.httpRequest().header(AUTHORIZATION, BEARER_TOKEP_REFIX + token.token());
                    return next.process();
                });
    }

    /**
     * Extracts the challenge off the authentication header.
     *
     * @param authenticateHeader
     *            the authentication header containing all the challenges.
     * @param authChallengePrefix
     *            the authentication challenge name.
     * @return a challenge map.
     */
    private static Map<String, String> extractChallenge(String authenticateHeader, String authChallengePrefix) {
        if (!isValidChallenge(authenticateHeader, authChallengePrefix)) {
            return null;
        }

        authenticateHeader = authenticateHeader.toLowerCase().replace(authChallengePrefix.toLowerCase(), "");

        String[] challenges = authenticateHeader.split(", ");
        Map<String, String> challengeMap = new HashMap<String, String>();
        for (String pair : challenges) {
            String[] keyValue = pair.split("=");
            challengeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }
        return challengeMap;
    }


    private String parseWWWAuthenticate(String wwwAuthenticate) {
        // Parses an authentication message like:
        // ```
        // Bearer authorization="some_authorization", resource="https://some.url"
        // ```
        String[] authenticateArray = wwwAuthenticate.split(" ");

        // Remove the "Bearer" piece
        authenticateArray[0] = "";

        // Split the KV comma-separated list
        String[] commaSep = String.join("", authenticateArray).split(",");
        for (String item : commaSep) {
            // Split the key/value pairs
            String[] kv = item.split("=");
            if (kv[0].trim() == "resource") {
                // Remove the quotations around the string
                String resource = kv[1].trim().replaceAll("[\'\"]+/g", "");
                return resource;
            }
        }
        return "";
    }

    /**
     * Verifies whether a challenge is bearer or not.
     *
     * @param authenticateHeader
     *            the authentication header containing all the challenges.
     * @param authChallengePrefix
     *            the authentication challenge name.
     * @return
     */
    private static boolean isValidChallenge(String authenticateHeader, String authChallengePrefix) {
        if (authenticateHeader != null && !authenticateHeader.isEmpty()
                && authenticateHeader.toLowerCase().startsWith(authChallengePrefix.toLowerCase())) {
            return true;
        }
        return false;
    }
}
