/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.azurekeyvaultprovider;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A policy that authenticates requests with Azure Key Vault service.
 */
class KeyVaultCustomCredentialPolicy implements HttpPipelinePolicy {
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private final ScopeTokenCache cache;
    private final AzureKeyVaultProviderTokenCredential keyVaultTokenCredential;

    /**
     * Creates KeyVaultCustomCredentialPolicy.
     *
     * @param credential
     *        the token credential to authenticate the request
     * @throws MicrosoftDataEncryptionException
     */
    KeyVaultCustomCredentialPolicy(
            AzureKeyVaultProviderTokenCredential credential) throws MicrosoftDataEncryptionException {
        if (null == credential) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Credential"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        this.cache = new ScopeTokenCache(credential::getToken);
        this.keyVaultTokenCredential = credential;
    }

    /**
     * Adds the required header to authenticate a request to Azure Key Vault service.
     *
     * @param context
     *        The request context
     * @param next
     *        The next HTTP pipeline policy to process the {@code context's} request after this policy completes.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException(MicrosoftDataEncryptionException.getErrString("R_TokenRequireUrl")));
        }

        return next.clone().process()
                // Ignore body
                .doOnNext(HttpResponse::close).map(res -> res.getHeaderValue(WWW_AUTHENTICATE))
                .map(header -> extractChallenge(header, BEARER_TOKEN_PREFIX)).flatMap(map -> {
                    keyVaultTokenCredential.setAuthorization(map.get("authorization"));
                    keyVaultTokenCredential.setResource(map.get("resource"));
                    keyVaultTokenCredential.setScope(map.get("scope"));
                    cache.setRequest(new TokenRequestContext().addScopes(map.get("resource") + "/.default"));
                    return cache.getToken();
                }).flatMap(token -> {
                    context.getHttpRequest().setHeader(AUTHORIZATION, BEARER_TOKEN_PREFIX + token.getToken());
                    return next.process();
                });
    }

    /**
     * Extracts the challenge off the authentication header.
     *
     * @param authenticateHeader
     *        The authentication header containing all the challenges.
     * @param authChallengePrefix
     *        The authentication challenge name.
     * @return a challenge map.
     */
    private static Map<String, String> extractChallenge(String authenticateHeader, String authChallengePrefix) {
        if (!isValidChallenge(authenticateHeader, authChallengePrefix)) {
            return null;
        }
        authenticateHeader = authenticateHeader.toLowerCase(Locale.ROOT)
                .replace(authChallengePrefix.toLowerCase(Locale.ROOT), "");

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
     * @param authenticateHeader
     *        The authentication header containing all the challenges.
     * @param authChallengePrefix
     *        The authentication challenge name.
     * @return A boolean indicating the challenge is valid or not.
     */
    private static boolean isValidChallenge(String authenticateHeader, String authChallengePrefix) {
        return (!CoreUtils.isNullOrEmpty(authenticateHeader) && authenticateHeader.toLowerCase(Locale.ROOT)
                .startsWith(authChallengePrefix.toLowerCase(Locale.ROOT)));
    }
}
