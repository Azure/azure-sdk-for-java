// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class CachingTokenCredentialProviderTest {

    @Test
    void returnCacheUsingDefaultAuthMethodViaDifferentProviderInstances() {
        DefaultTokenCredentialProvider defaultTokenCredentialProvider1 = new DefaultTokenCredentialProvider(null);
        CachingTokenCredentialProvider provider1
            = new CachingTokenCredentialProvider(null, defaultTokenCredentialProvider1);
        TokenCredential tokenCredential1 = provider1.get();

        DefaultTokenCredentialProvider defaultTokenCredentialProvider2 = new DefaultTokenCredentialProvider(null);
        CachingTokenCredentialProvider provider2
            = new CachingTokenCredentialProvider(null, defaultTokenCredentialProvider2);

        TokenCredential tokenCredential2 = provider2.get();
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential1);
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

    @Test
    void returnCacheUsingSameAuthMethodViaDifferentProviderInstances() {
        TokenCredentialProviderOptions customOptions = getSystemManagedIdentityCredentialProviderOptions();

        DefaultTokenCredentialProvider defaultTokenCredentialProvider1
            = new DefaultTokenCredentialProvider(customOptions);
        CachingTokenCredentialProvider cachingProvider1
            = new CachingTokenCredentialProvider(customOptions, defaultTokenCredentialProvider1);
        TokenCredential tokenCredential1 = cachingProvider1.get();

        TokenCredentialProviderOptions customOptions2 = getSystemManagedIdentityCredentialProviderOptions();
        DefaultTokenCredentialProvider defaultTokenCredentialProvider2
            = new DefaultTokenCredentialProvider(customOptions2);
        CachingTokenCredentialProvider cachingProvider2
            = new CachingTokenCredentialProvider(customOptions2, defaultTokenCredentialProvider2);

        TokenCredential tokenCredential2 = cachingProvider2.get();
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential1);
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

    @Test
    void returnCacheUsingSameAuthMethodAndInvokingDifferentGetMethods() {
        TokenCredentialProviderOptions customOptions = getSystemManagedIdentityCredentialProviderOptions();

        DefaultTokenCredentialProvider defaultTokenCredentialProvider1
            = new DefaultTokenCredentialProvider(customOptions);
        CachingTokenCredentialProvider cachingProvider1
            = new CachingTokenCredentialProvider(customOptions, defaultTokenCredentialProvider1);
        TokenCredential tokenCredential1 = cachingProvider1.get();

        TokenCredentialProviderOptions customOptions2 = getSystemManagedIdentityCredentialProviderOptions();
        DefaultTokenCredentialProvider defaultTokenCredentialProvider2
            = new DefaultTokenCredentialProvider(customOptions2);
        CachingTokenCredentialProvider cachingProvider2
            = new CachingTokenCredentialProvider(customOptions2, defaultTokenCredentialProvider2);

        TokenCredential tokenCredential2 = cachingProvider2.get(customOptions2);
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential1);
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

    @Test
    void returnDifferentCachesUsingDifferentAuthenticationMethods() {
        TokenCredentialProviderOptions customOptions = getSystemManagedIdentityCredentialProviderOptions();

        DefaultTokenCredentialProvider defaultTokenCredentialProvider1
            = new DefaultTokenCredentialProvider(customOptions);
        CachingTokenCredentialProvider cachingProvider1
            = new CachingTokenCredentialProvider(customOptions, defaultTokenCredentialProvider1);
        TokenCredential tokenCredential1 = cachingProvider1.get();

        TokenCredentialProviderOptions customOptions2
            = getUserManagedIdentityCredentialProviderOptions("test-client-id");
        DefaultTokenCredentialProvider defaultTokenCredentialProvider2
            = new DefaultTokenCredentialProvider(customOptions2);
        CachingTokenCredentialProvider cachingProvider2
            = new CachingTokenCredentialProvider(customOptions2, defaultTokenCredentialProvider2);

        TokenCredential tokenCredential2 = cachingProvider2.get(customOptions2);
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential1);
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential2);
        assertNotSame(tokenCredential1, tokenCredential2);
    }

    private static TokenCredentialProviderOptions getUserManagedIdentityCredentialProviderOptions(String clientId) {
        TokenCredentialProviderOptions customOptions = new TokenCredentialProviderOptions();
        customOptions.setManagedIdentityEnabled(true);
        customOptions.setClientId(clientId);
        return customOptions;
    }

    private static TokenCredentialProviderOptions getSystemManagedIdentityCredentialProviderOptions() {
        TokenCredentialProviderOptions customOptions = new TokenCredentialProviderOptions();
        customOptions.setManagedIdentityEnabled(true);
        return customOptions;
    }
}
