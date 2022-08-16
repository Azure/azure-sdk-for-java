// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.CacheableTokenCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProviderOptions;
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
