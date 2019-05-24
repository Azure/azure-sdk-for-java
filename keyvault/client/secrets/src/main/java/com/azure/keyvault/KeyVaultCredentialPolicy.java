package com.azure.keyvault;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * A policy that authenticates requests with Azure Key Vault service. The content added by this policy
 * is leveraged in {@link TokenCredential} to get and set the correct "Authorization" header value.
 *
 * @see TokenCredential
 * @see SecretClient
 * @see SecretAsyncClient
 * @see SecretClientBuilder
 * @see SecretAsyncClientBuilder
 */
public class KeyVaultCredentialPolicy implements HttpPipelinePolicy {
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";

    private TokenCredential credential;

    public KeyVaultCredentialPolicy(TokenCredential credential) {
        this.credential = credential;
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
            .flatMap(map -> credential.getTokenAsync(map.get("resource")))
            .flatMap(token -> {
                context.httpRequest().withHeader(AUTHORIZATION, BEARER_TOKEP_REFIX + token);
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
