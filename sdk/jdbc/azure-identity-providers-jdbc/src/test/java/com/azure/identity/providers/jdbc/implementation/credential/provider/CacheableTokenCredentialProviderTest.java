// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.api.credential.provider.TokenCredentialProvider;
import com.azure.identity.providers.jdbc.implementation.credential.CacheableTokenCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheableTokenCredentialProviderTest {

    @Test
    void testGetCacheableTokenCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        CacheableTokenCredentialProvider provider = new CacheableTokenCredentialProvider(
            TokenCredentialProvider.createDefault(options),
            options);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof CacheableTokenCredential);
    }
}
