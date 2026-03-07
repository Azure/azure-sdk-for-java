// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Utility class used to forward token authentication to Stainless clients
 */
public final class TokenUtils {

    /**
     * Utility authentication function.
     *
     * @param tokenCredential Token credential to be used
     * @param scopes for which the token credential authentication will be obtained
     * @return token supplier callback
     */
    public static Supplier<String> getBearerTokenSupplier(TokenCredential tokenCredential, String... scopes) {
        // Return a lazy supplier that fetches a fresh token on each invocation.
        // The Stainless OpenAI client calls this supplier to populate its Authorization header.
        return () -> {
            // Build a request context with the required scopes (e.g. "https://cognitiveservices.azure.com/.default")
            TokenRequestContext tokenRequestContext = new TokenRequestContext();
            tokenRequestContext.setScopes(Arrays.asList(scopes));

            // Obtain the token synchronously from the Azure credential (DefaultAzureCredential, etc.).
            // This delegates all caching and refresh logic to the credential implementation itself,
            // avoiding the need to construct an HttpPipeline or issue a dummy HTTP request.
            AccessToken accessToken = tokenCredential.getTokenSync(tokenRequestContext);
            return accessToken.getToken();
        };
    }
}
