// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A policy that authenticates requests with Azure Key Vault service. The content added by this policy is leveraged
 * in {@link TokenCredential} to get and set the correct "Authorization" header value.
 *
 * @see TokenCredential
 */
public final class KeyVaultCredentialPolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(KeyVaultCredentialPolicy.class);
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private final ScopeTokenCache cache;

    /**
     * Creates KeyVaultCredentialPolicy.
     *
     * @param credential the token credential to authenticate the request
     */
    public KeyVaultCredentialPolicy(TokenCredential credential) {
        Objects.requireNonNull(credential);

        this.cache = new ScopeTokenCache(credential::getToken);
    }

    /**
     * Adds the required header to authenticate a request to Azure Key Vault service.
     *
     * @param context The request {@link HttpPipelineCallContext context}.
     * @param next    The next HTTP pipeline policy to process the {@link HttpPipelineCallContext context's} request
     *                after this policy completes.
     * @return A {@link Mono} representing the {@link HttpResponse HTTP response} that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!context.getHttpRequest().getUrl().getProtocol().startsWith("https")) {
            return Mono.error(new RuntimeException("Token credentials require a URL using the HTTPS protocol scheme"));
        }

        return next.clone().process()
            .doOnNext(httpResponse -> {
                // KV follows challenge based auth. Currently every service
                // call hit the endpoint for challenge and then resend the
                // request with token. The challenge response body is not
                // consumed, not draining/closing the body will result in leak.
                // Ref: https://github.com/Azure/azure-sdk-for-java/issues/7934
                //      https://github.com/Azure/azure-sdk-for-java/issues/10467
                try {
                    httpResponse.getBody().subscribe().dispose();
                } catch (RuntimeException ignored) {
                    logger.logExceptionAsWarning(ignored);
                }
                // The ReactorNettyHttpResponse::close() should be sufficient
                // and should take care similar body disposal but looks like that
                // is not happening, need to re-visit the close() method.
            })
            .map(res -> res.getHeaderValue(WWW_AUTHENTICATE))
            .map(header -> extractChallenge(header, BEARER_TOKEN_PREFIX))
            .flatMap(map -> {
                cache.setTokenRequest(new TokenRequestContext().addScopes(map.get("resource") + "/.default"));
                return cache.getToken();
            })
            .flatMap(token -> {
                context.getHttpRequest().setHeader(AUTHORIZATION, BEARER_TOKEN_PREFIX + token.getToken());
                return next.process();
            });
    }

    /**
     * Extracts the challenge off the authentication header.
     *
     * @param authenticateHeader  The authentication header containing all the challenges.
     * @param authChallengePrefix The authentication challenge name.
     * @return A challenge map.
     */
    private static Map<String, String> extractChallenge(String authenticateHeader, String authChallengePrefix) {
        if (!isValidChallenge(authenticateHeader, authChallengePrefix)) {
            return null;
        }

        authenticateHeader =
            authenticateHeader.toLowerCase(Locale.ROOT).replace(authChallengePrefix.toLowerCase(Locale.ROOT), "");

        String[] challenges = authenticateHeader.split(", ");
        Map<String, String> challengeMap = new HashMap<>();

        for (String pair : challenges) {
            String[] keyValue = pair.split("=");
            challengeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }

        return challengeMap;
    }

    /**
     * Verifies whether a challenge is bearer or not.
     *
     * @param authenticateHeader  The authentication header containing all the challenges.
     * @param authChallengePrefix The authentication challenge name.
     * @return A boolean indicating tha challenge is valid or not.
     */
    private static boolean isValidChallenge(String authenticateHeader, String authChallengePrefix) {
        return (!CoreUtils.isNullOrEmpty(authenticateHeader)
            && authenticateHeader.toLowerCase(Locale.ROOT).startsWith(authChallengePrefix.toLowerCase(Locale.ROOT)));
    }
}
