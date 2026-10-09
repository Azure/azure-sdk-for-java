// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import reactor.core.publisher.Mono;

/**
 * Codesnippets for {@link AccessTokenCache}.
 */
public class AccessTokenCacheJavadocCodeSnippets {

    public void accessTokenCacheSnippet() {
        // BEGIN: com.azure.core.credential.accessTokenCache
        TokenCredential credential = new BasicAuthenticationCredential("username", "password");
        AccessTokenCache tokenCache = new AccessTokenCache(credential);
        TokenRequestContext requestContext = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        // Async usage
        Mono<AccessToken> tokenMono = tokenCache.getToken(requestContext, false);
        // Sync usage
        AccessToken token = tokenCache.getTokenSync(requestContext, false);
        // END: com.azure.core.credential.accessTokenCache
    }

}
