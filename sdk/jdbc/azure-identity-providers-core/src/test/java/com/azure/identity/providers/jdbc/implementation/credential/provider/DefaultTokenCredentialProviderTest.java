// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static Stream<Arguments> provideAuthorityHosts() {
        return Stream.of(
            Arguments.of("", AzureAuthorityHosts.AZURE_PUBLIC_CLOUD),
            Arguments.of(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD),
            Arguments.of(AzureAuthorityHosts.AZURE_CHINA, AzureAuthorityHosts.AZURE_CHINA),
            Arguments.of(AzureAuthorityHosts.AZURE_GERMANY, AzureAuthorityHosts.AZURE_GERMANY),
            Arguments.of(AzureAuthorityHosts.AZURE_GOVERNMENT, AzureAuthorityHosts.AZURE_GOVERNMENT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHosts")
    void testClientSecretCredential(String providedAuthorityHost, String expectedAuthorityHost) throws NoSuchFieldException, IllegalAccessException {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTenantId("fake-tenantId");
        options.setClientId("fake-clientId");
        options.setClientSecret("fake-clientSecret");
        if (!providedAuthorityHost.isEmpty()) {
            options.setAuthorityHost(providedAuthorityHost);
        }
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();

        // verify
        assertTrue(tokenCredential instanceof ClientSecretCredential);
        Field fieldIdentityClient = ClientSecretCredential.class.getDeclaredField("identityClient");
        fieldIdentityClient.setAccessible(true);
        IdentityClient identityClient = (IdentityClient) fieldIdentityClient.get(tokenCredential);
        Field fieldClientSecret = IdentityClient.class.getDeclaredField("clientSecret");
        fieldClientSecret.setAccessible(true);

        assertEquals("fake-tenantId", identityClient.getTenantId());
        assertEquals("fake-clientId", identityClient.getClientId());
        assertEquals("fake-clientSecret", fieldClientSecret.get(identityClient));
        assertEquals(expectedAuthorityHost, identityClient.getIdentityClientOptions().getAuthorityHost());
    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHosts")
    void testClientCertificateCredential(String providedAuthorityHost, String expectedAuthorityHost) throws NoSuchFieldException, IllegalAccessException {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTenantId("fake-tenantId");
        options.setClientId("fake-clientId");
        options.setClientCertificatePath("fake-clientCertificatePath");
        if (!providedAuthorityHost.isEmpty()) {
            options.setAuthorityHost(providedAuthorityHost);
        }
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();

        // getValue
        Field fieldIdentityClient = ClientCertificateCredential.class.getDeclaredField("identityClient");
        fieldIdentityClient.setAccessible(true);
        IdentityClient identityClient = (IdentityClient) fieldIdentityClient.get(tokenCredential);

        Field fieldCertificatePath = IdentityClient.class.getDeclaredField("certificatePath");
        fieldCertificatePath.setAccessible(true);

        // verify
        assertTrue(tokenCredential instanceof ClientCertificateCredential);
        assertEquals("fake-tenantId", identityClient.getTenantId());
        assertEquals("fake-clientId", identityClient.getClientId());
        assertEquals("fake-clientCertificatePath", fieldCertificatePath.get(identityClient));

        assertEquals(expectedAuthorityHost, identityClient.getIdentityClientOptions().getAuthorityHost());

    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHosts")
    void testUsernamePasswordCredential(String providedAuthorityHost, String expectedAuthorityHost) throws NoSuchFieldException, IllegalAccessException {

        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setClientId("fake-clientId");
        options.setUsername("fake-username");
        options.setPassword("fake-password");
        if (!providedAuthorityHost.isEmpty()) {
            options.setAuthorityHost(providedAuthorityHost);
        }
        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();

        // get
        Field fieldIdentityClient = UsernamePasswordCredential.class.getDeclaredField("identityClient");
        fieldIdentityClient.setAccessible(true);
        IdentityClient identityClient = (IdentityClient) fieldIdentityClient.get(tokenCredential);

        Field fieldUsername = UsernamePasswordCredential.class.getDeclaredField("username");
        fieldUsername.setAccessible(true);

        Field fieldPassword = UsernamePasswordCredential.class.getDeclaredField("password");
        fieldPassword.setAccessible(true);

        // verify
        assertTrue(tokenCredential instanceof UsernamePasswordCredential);
        assertEquals("fake-clientId", identityClient.getClientId());
        assertEquals("fake-username", fieldUsername.get(tokenCredential));
        assertEquals("fake-password", fieldPassword.get(tokenCredential));
        assertEquals(expectedAuthorityHost, identityClient.getIdentityClientOptions().getAuthorityHost());
    }

    @Test
    void testManagedIdentityCredential() {
        // setUp
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setManagedIdentityEnabled(true);

        DefaultTokenCredentialProvider provider = new DefaultTokenCredentialProvider(options);
        TokenCredential tokenCredential = provider.get();

        // verify
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
