// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.credentials;

import io.clientcore.core.credentials.oauth.AccessToken;

/**
 * Code snippets for {@link AccessTokenCache}.
 */
public class AccessTokenCacheJavadocCodeSnippets {

    /**
     * Demonstrates caching tokens from an existing credential.
     *
     * @param credential the credential used to acquire tokens.
     */
    public void accessTokenCacheSnippet(TokenCredential credential) {
        // BEGIN: com.azure.v2.core.credentials.accessTokenCache
        AccessTokenCache tokenCache = new AccessTokenCache(credential);
        TokenRequestContext requestContext = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        AccessToken token = tokenCache.getToken(requestContext, false);
        // END: com.azure.v2.core.credentials.accessTokenCache
    }
}
