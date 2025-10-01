// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultTokenCredentialProviderTest {

    @Test
    void testOptionsIsNull() {
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(null);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential1);
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

    @Test
    void testDefaultConstructor() {
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider();
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential1);
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

    private static Stream<Arguments> provideAuthorityHosts() {
        return Stream.of(Arguments.of("", AzureAuthorityHosts.AZURE_PUBLIC_CLOUD),
            Arguments.of(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD),
            Arguments.of(AzureAuthorityHosts.AZURE_CHINA, AzureAuthorityHosts.AZURE_CHINA),
            Arguments.of(AzureAuthorityHosts.AZURE_GERMANY, AzureAuthorityHosts.AZURE_GERMANY),
            Arguments.of(AzureAuthorityHosts.AZURE_GOVERNMENT, AzureAuthorityHosts.AZURE_GOVERNMENT));
    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHosts")
    void testClientSecretCredential(String providedAuthorityHost) {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTenantId("fake-tenantId");
        options.setClientId("fake-clientId");
        options.setClientSecret("fake-clientSecret");
        if (!providedAuthorityHost.isEmpty()) {
            options.setAuthorityHost(providedAuthorityHost);
        }
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertInstanceOf(ClientSecretCredential.class, tokenCredential1);
        assertInstanceOf(ClientSecretCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);

    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHosts")
    void testClientCertificateCredential(String providedAuthorityHost) {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTenantId("fake-tenantId");
        options.setClientId("fake-clientId");
        options.setClientCertificatePath("fake-clientCertificatePath");
        if (!providedAuthorityHost.isEmpty()) {
            options.setAuthorityHost(providedAuthorityHost);
        }
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertInstanceOf(ClientCertificateCredential.class, tokenCredential1);
        assertInstanceOf(ClientCertificateCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHosts")
    void testUsernamePasswordCredential(String providedAuthorityHost) {

        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setClientId("fake-clientId");
        options.setUsername("fake-username");
        options.setPassword("fake-password");
        if (!providedAuthorityHost.isEmpty()) {
            options.setAuthorityHost(providedAuthorityHost);
        }
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertInstanceOf(UsernamePasswordCredential.class, tokenCredential1);
        assertInstanceOf(UsernamePasswordCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);

    }

    @Test
    void testManagedIdentityCredential() {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setManagedIdentityEnabled(true);

        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential1);
        assertInstanceOf(ManagedIdentityCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);

    }

    @Test
    void testDefaultAzureCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential1);
        assertInstanceOf(DefaultAzureCredential.class, tokenCredential2);
        assertSame(tokenCredential1, tokenCredential2);
    }

}
