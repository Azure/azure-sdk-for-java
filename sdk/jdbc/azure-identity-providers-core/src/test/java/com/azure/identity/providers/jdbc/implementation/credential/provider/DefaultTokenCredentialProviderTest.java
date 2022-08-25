// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTokenCredentialProviderTest {

    @Test
    void testOptionsIsNull() {
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(null);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof DefaultAzureCredential);
    }

    @Test
    void testDefaultConstructor() {
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider();
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof DefaultAzureCredential);
    }

    @Test
    void testClientSecretCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTenantId("fake-tenantId");
        options.setClientId("fake-clientId");
        options.setClientSecret("fake-clientSecret");
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof ClientSecretCredential);
    }

    @Test
    void testClientCertificateCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTenantId("fake-tenantId");
        options.setClientId("fake-clientId");
        options.setClientCertificatePath("fake-clientCertificatePath");
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof ClientCertificateCredential);
    }

    @Test
    void testUsernamePasswordCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setClientId("fake-clientId");
        options.setUsername("fake-username");
        options.setPassword("fake-password");
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof UsernamePasswordCredential);
    }

    @Test
    void testManagedIdentityCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setManagedIdentityEnabled(true);
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof ManagedIdentityCredential);
    }

    @Test
    void testDefaultAzureCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();
        assertTrue(tokenCredential instanceof DefaultAzureCredential);
    }

}
