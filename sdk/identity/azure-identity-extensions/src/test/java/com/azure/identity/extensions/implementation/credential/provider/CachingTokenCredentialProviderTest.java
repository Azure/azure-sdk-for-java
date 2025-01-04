// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CachingTokenCredentialProviderTest {

    @Test
    void cacheTokenCredential() {
        DefaultTokenCredentialProvider defaultTokenCredentialProvider1 = new DefaultTokenCredentialProvider(null);
        CachingTokenCredentialProvider provider1
            = new CachingTokenCredentialProvider(null, defaultTokenCredentialProvider1);
        TokenCredential tokenCredential1 = provider1.get();

        DefaultTokenCredentialProvider defaultTokenCredentialProvider2 = new DefaultTokenCredentialProvider(null);
        CachingTokenCredentialProvider provider2
            = new CachingTokenCredentialProvider(null, defaultTokenCredentialProvider2);

        TokenCredential tokenCredential2 = provider2.get();
        assertTrue(tokenCredential1 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential2 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
    }

}
