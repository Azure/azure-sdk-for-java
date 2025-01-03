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
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.azure.identity.extensions.implementation.utils.ClassUtil.instantiateClass;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("rawtypes")
class DefaultTokenCredentialProviderTest {

    @ParameterizedTest
    @ValueSource(classes = { DefaultTokenCredentialProvider.class, CachingTokenCredentialProvider.class })
    void testOptionsIsNull(Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();
        assertTrue(tokenCredential1 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential2 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
    }

    @ParameterizedTest
    @ValueSource(classes = { DefaultTokenCredentialProvider.class, CachingTokenCredentialProvider.class })
    void testDefaultConstructor(Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();
        assertTrue(tokenCredential1 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential2 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
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
        verifyClientSecretCredentialByProvider(options, DefaultTokenCredentialProvider.class);
        verifyClientSecretCredentialByProvider(options, CachingTokenCredentialProvider.class);
    }

    private static void verifyClientSecretCredentialByProvider(TokenCredentialProviderOptions options,
                                                               Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass, options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertTrue(tokenCredential1 instanceof ClientSecretCredential);
        assertTrue(tokenCredential2 instanceof ClientSecretCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
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
        verifyClientCertificateCredentialByProvider(options, DefaultTokenCredentialProvider.class);
        verifyClientCertificateCredentialByProvider(options, CachingTokenCredentialProvider.class);
    }

    private static void verifyClientCertificateCredentialByProvider(TokenCredentialProviderOptions options,
                                                               Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass, options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertTrue(tokenCredential1 instanceof ClientCertificateCredential);
        assertTrue(tokenCredential2 instanceof ClientCertificateCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
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
        verifyUsernamePasswordCredentialByProvider(options, DefaultTokenCredentialProvider.class);
        verifyUsernamePasswordCredentialByProvider(options, CachingTokenCredentialProvider.class);
    }

    private static void verifyUsernamePasswordCredentialByProvider(TokenCredentialProviderOptions options,
                                                                   Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass, options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertTrue(tokenCredential1 instanceof UsernamePasswordCredential);
        assertTrue(tokenCredential2 instanceof UsernamePasswordCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
    }

    @Test
    void testManagedIdentityCredential() {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setManagedIdentityEnabled(true);

        verifyManagedIdentityCredentialByProvider(options, DefaultTokenCredentialProvider.class);
        verifyManagedIdentityCredentialByProvider(options, CachingTokenCredentialProvider.class);
    }

    private static void verifyManagedIdentityCredentialByProvider(TokenCredentialProviderOptions options,
                                                                  Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass, options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertTrue(tokenCredential1 instanceof ManagedIdentityCredential);
        assertTrue(tokenCredential2 instanceof ManagedIdentityCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
    }

    @Test
    void testDefaultAzureCredential() {
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        verifyDefaultCredentialByProvider(options, DefaultTokenCredentialProvider.class);
        verifyDefaultCredentialByProvider(options, CachingTokenCredentialProvider.class);
    }

    private static void verifyDefaultCredentialByProvider(TokenCredentialProviderOptions options,
                                                          Class providerClass) {
        TokenCredentialProvider provider = instantiateClass(providerClass, options);
        TokenCredential tokenCredential1 = provider.get();
        TokenCredential tokenCredential2 = provider.get();

        // verify
        assertTrue(tokenCredential1 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential2 instanceof DefaultAzureCredential);
        assertTrue(tokenCredential1 == tokenCredential2);
    }

}
