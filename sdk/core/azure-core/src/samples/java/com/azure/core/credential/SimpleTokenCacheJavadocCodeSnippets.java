// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Codesnippets for {@link AzureNamedKeyCredential}.
 */
public class SimpleTokenCacheJavadocCodeSnippets {

    public void azureNamedKeyCredenialSasKey() {
        // BEGIN: com.azure.core.credential.simpleTokenCache
        SimpleTokenCache simpleTokenCache =
            new SimpleTokenCache(() -> {
                // Your logic to retrieve access token goes here.
                return Mono.just(new AccessToken("dummy-token", OffsetDateTime.now().plusHours(2)));
            });
        // END: com.azure.core.credential.simpleTokenCache
    }

}
