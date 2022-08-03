// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.EmptyEnvironmentConfigurationSource;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class AzureApplicationCredentialTest {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testUseEnvironmentCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        // setup
        String secret = "secret";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        configuration.put("AZURE_CLIENT_ID", CLIENT_ID);
        configuration.put("AZURE_CLIENT_SECRET", secret);
        configuration.put("AZURE_TENANT_ID", TENANT_ID);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        })) {
            // test
            AzureApplicationCredential credential = new AzureApplicationCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testUseManagedIdentityCredential() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));

        }); MockedConstruction<IntelliJCredential> intelliCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {
            // test
            AzureApplicationCredential credential = new AzureApplicationCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
            Assert.assertNotNull(intelliCredentialMock);
        }
    }

    @Test
    public void testNoCredentialWorks() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();
        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateToIMDSEndpoint(request))
                .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from managed identity")));
        })) {
            // test
            AzureApplicationCredential credential = new AzureApplicationCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage()
                    .startsWith("EnvironmentCredential authentication unavailable. "))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testCredentialUnavailable() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        // mock
        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
            when(managedIdentityCredential.getToken(request))
                .thenReturn(Mono.error(
                    new CredentialUnavailableException("Cannot get token from Managed Identity credential")));
        })) {
            // test
            AzureApplicationCredential credential = new AzureApplicationCredentialBuilder().configuration(configuration)
                .build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage()
                    .startsWith("EnvironmentCredential authentication unavailable. "))
                .verify();
            Assert.assertNotNull(managedIdentityCredentialMock);
        }
    }
}
