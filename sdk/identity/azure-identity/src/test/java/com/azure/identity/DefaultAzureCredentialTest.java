// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;


import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;


public class DefaultAzureCredentialTest {

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


        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request1)).thenReturn(Mono.empty());
        })) {

            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request1)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assert.assertNotNull(mocked);
            Assert.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testUseManagedIdentityCredential() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {


            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assert.assertNotNull(mocked);
            Assert.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testUseAzureCliCredential() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureCli(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithSharedTokenCache(request, null)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithIntelliJ(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithVsCodeCredential(any(), any())).thenReturn(Mono.empty());
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assert.assertNotNull(mocked);
            Assert.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testNoCredentialWorks() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");


        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from managed identity")));
        }); MockedConstruction<SharedTokenCacheCredential> sharedTokenCacheCredentialMock = mockConstruction(SharedTokenCacheCredential.class, (sharedTokenCacheCredential, context) -> {
            when(sharedTokenCacheCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from shared token cache")));
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure CLI credential")));
        }); MockedConstruction<AzurePowerShellCredential> azurePowerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (azurePowerShellCredential, context) -> {
            when(azurePowerShellCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure PowerShell credential")));
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
        }); MockedConstruction<VisualStudioCodeCredential> vscodeCredentialMock = mockConstruction(VisualStudioCodeCredential.class, (vscodeCredential, context) -> {
            when(vscodeCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from VS Code credential")));
        })) {

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request)).expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage().startsWith("EnvironmentCredential authentication unavailable. ")).verify();
            Assert.assertNotNull(identityClientMock);
            Assert.assertNotNull(sharedTokenCacheCredentialMock);
            Assert.assertNotNull(azureCliCredentialMock);
            Assert.assertNotNull(azurePowerShellCredentialMock);
            Assert.assertNotNull(intelliJCredentialMock);
            Assert.assertNotNull(vscodeCredentialMock);
        }
    }

    @Test
    public void testCredentialUnavailable() throws Exception {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
            when(managedIdentityCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Managed Identity credential")));
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
        }); MockedConstruction<VisualStudioCodeCredential> vsCodeCredentialMock = mockConstruction(VisualStudioCodeCredential.class, (vscodeCredential, context) -> {
            when(vscodeCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from VS Code credential")));
        }); MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
            when(powerShellCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Powershell credential")));
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Cli credential")));
        })) {
            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request)).expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage().startsWith("EnvironmentCredential authentication unavailable. ")).verify();
            Assert.assertNotNull(managedIdentityCredentialMock);
            Assert.assertNotNull(intelliJCredentialMock);
            Assert.assertNotNull(vsCodeCredentialMock);
            Assert.assertNotNull(powerShellCredentialMock);
            Assert.assertNotNull(azureCliCredentialMock);
        }

    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidIdCombination() {
        // setup
        String resourceId = "/subscriptions/" + UUID.randomUUID() + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";

        // test
        new DefaultAzureCredentialBuilder().managedIdentityClientId(CLIENT_ID).managedIdentityResourceId(resourceId).build();
    }
}
