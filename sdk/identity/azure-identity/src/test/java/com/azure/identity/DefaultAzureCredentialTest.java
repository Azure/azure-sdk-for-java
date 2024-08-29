// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.EmptyEnvironmentConfigurationSource;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    public void testUseEnvironmentCredential() {
        // setup
        String secret = "secret";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("AZURE_CLIENT_ID", CLIENT_ID)
            .put("AZURE_CLIENT_SECRET", secret)
            .put("AZURE_TENANT_ID", TENANT_ID));

        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureDeveloperCli(request1)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request1)).thenReturn(Mono.empty());
        })) {

            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request1)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assertions.assertNotNull(mocked);
            Assertions.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testUseManagedIdentityCredential() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {


            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assertions.assertNotNull(mocked);
            Assertions.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testUseWorkloadIdentityCredentialWithManagedIdentityClientId() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "dummy-tenant")
            .put(ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE, "dummy-path"));

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithWorkloadIdentityConfidentialClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {


            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .managedIdentityClientId("dummy-client-id")
                .configuration(configuration)
                .build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assertions.assertNotNull(mocked);
            Assertions.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testUseWorkloadIdentityCredentialWithWorkloadClientId() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "dummy-tenant")
            .put(ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE, "dummy-path"));

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithWorkloadIdentityConfidentialClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        })) {
            // test
            String clientId = "dummy-client-id";
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .workloadIdentityClientId(clientId)
                .configuration(configuration)
                .build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assertions.assertNotNull(mocked);
        }
    }

    @Test
    public void testUseWorkloadIdentityCredentialWithClientIdFlow() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "dummy-tenant")
            .put(ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE, "dummy-path"));

        // test
        String clientId = "dummy-client-id";
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
            .workloadIdentityClientId(clientId)
            .configuration(configuration)
            .build();
        WorkloadIdentityCredential workloadIdentityCredential = credential.getWorkloadIdentityCredentialIfPresent();
        Assertions.assertNotNull(workloadIdentityCredential);
        Assertions.assertEquals(clientId, workloadIdentityCredential.getClientId());

        credential = new DefaultAzureCredentialBuilder()
            .managedIdentityClientId(clientId)
            .configuration(configuration)
            .build();
        workloadIdentityCredential = credential.getWorkloadIdentityCredentialIfPresent();
        Assertions.assertNotNull(workloadIdentityCredential);
        Assertions.assertEquals(clientId, workloadIdentityCredential.getClientId());

        configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "dummy-tenant")
            .put(ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE, "dummy-path")
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, clientId));
        credential = new DefaultAzureCredentialBuilder()
            .configuration(configuration)
            .build();
        workloadIdentityCredential = credential.getWorkloadIdentityCredentialIfPresent();
        Assertions.assertNotNull(workloadIdentityCredential);
        Assertions.assertEquals(clientId, workloadIdentityCredential.getClientId());
    }



    @Test
    public void testUseAzureCliCredential() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithAzureCli(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithSharedTokenCache(request, null)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithIntelliJ(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithVsCodeCredential(any(), any())).thenReturn(Mono.empty());
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assertions.assertNotNull(mocked);
            Assertions.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testUseAzureDeveloperCliCredential() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        // mock
        try (MockedConstruction<IdentityClient> mocked = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            when(identityClient.authenticateWithAzureCli(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithAzurePowerShell(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithSharedTokenCache(request, null)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithIntelliJ(request)).thenReturn(Mono.empty());
            when(identityClient.authenticateWithVsCodeCredential(any(), any())).thenReturn(Mono.empty());
        }); MockedConstruction<IntelliJCredential> ijcredential = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
        })) {

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();
            Assertions.assertNotNull(mocked);
            Assertions.assertNotNull(ijcredential);
        }
    }

    @Test
    public void testNoCredentialWorks() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from managed identity")));
        }); MockedConstruction<SharedTokenCacheCredential> sharedTokenCacheCredentialMock = mockConstruction(SharedTokenCacheCredential.class, (sharedTokenCacheCredential, context) -> {
            when(sharedTokenCacheCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from shared token cache")));
        }); MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock = mockConstruction(AzureDeveloperCliCredential.class, (AzureDeveloperCliCredential, context) -> {
            when(AzureDeveloperCliCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure Developer CLI credential")));
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure CLI credential")));
        }); MockedConstruction<AzurePowerShellCredential> azurePowerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (azurePowerShellCredential, context) -> {
            when(azurePowerShellCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure PowerShell credential")));
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
        })) {

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request)).expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage().startsWith("EnvironmentCredential authentication unavailable. ")).verify();
            Assertions.assertNotNull(identityClientMock);
            Assertions.assertNotNull(sharedTokenCacheCredentialMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
            Assertions.assertNotNull(azurePowerShellCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
        }
    }

    @Test
    public void testCredentialUnavailable() {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
            when(managedIdentityCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Managed Identity credential")));
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
        }); MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
            when(powerShellCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Powershell credential")));
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Cli credential")));
        }); MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock = mockConstruction(AzureDeveloperCliCredential.class, (AzureDeveloperCliCredential, context) -> {
            when(AzureDeveloperCliCredential.getToken(request)).thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure Developer CLI credential")));
        })) {
            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request)).expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage().startsWith("EnvironmentCredential authentication unavailable. ")).verify();
            Assertions.assertNotNull(managedIdentityCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
            Assertions.assertNotNull(powerShellCredentialMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
        }

    }

    @Test
    public void testCredentialUnavailableSync() {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
            when(managedIdentityCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Managed Identity credential"));
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from IntelliJ Credential"));
        }); MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
            when(powerShellCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Powershell credential"));
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Cli credential"));
        }); MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock = mockConstruction(AzureDeveloperCliCredential.class, (azureDeveloperCliCredential, context) -> {
            when(azureDeveloperCliCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Azure Developer Cli credential"));
        })) {
            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            try {
                credential.getTokenSync(request);
            } catch (Exception e) {
                Assertions.assertTrue(e instanceof CredentialUnavailableException && e.getMessage().startsWith("EnvironmentCredential authentication unavailable. "));
            }
            Assertions.assertNotNull(managedIdentityCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
            Assertions.assertNotNull(powerShellCredentialMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
        }

    }

    @Test
    public void testInvalidIdCombination() {
        // setup
        String resourceId = "/subscriptions/" + UUID.randomUUID() + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";
        String objectId = "2323-sd2323s-32323-32334-34343";

        // test
        Assertions.assertThrows(IllegalStateException.class, () -> new DefaultAzureCredentialBuilder()
            .managedIdentityClientId(CLIENT_ID).managedIdentityResourceId(resourceId).build());

        Assertions.assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder()
                .managedIdentityClientId(CLIENT_ID).managedIdentityResourceId(resourceId).
                managedIdentityObjectId(objectId).build());

        Assertions.assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder()
                .managedIdentityClientId(CLIENT_ID).managedIdentityObjectId(objectId).build());

        Assertions.assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder()
                .managedIdentityResourceId(resourceId).managedIdentityObjectId(objectId).build());
    }

    @Test
    public void testInvalidAdditionalTenant() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
            .additionallyAllowedTenants("RANDOM")
            .configuration(configuration)
            .build();

        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().contains("The current credential is not configured to")));
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
            .configuration(configuration)
            .build();

        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().contains("The current credential is not configured to")));
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
            .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
            .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
            .additionallyAllowedTenants("*")
            .configuration(configuration)
            .build();

        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e.getCause() instanceof MsalServiceException);
    }

    @Test
    public void testCredentialCaching() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
            when(managedIdentityCredential.getToken(request))
                .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Managed Identity credential")))
                .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getToken(request))
                .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from InteliJ credential")))
                .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
        }); MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
            when(powerShellCredential.getToken(request))
                .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from PowerShell credential")))
                .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getToken(request))
                .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure CLI credential")))
                .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
        }); MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock = mockConstruction(AzureDeveloperCliCredential.class, (azureDeveloperCliCredential, context) -> {
            when(azureDeveloperCliCredential.getToken(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        })) {

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();

            // Second call should return token from cached credential.
            StepVerifier.create(credential.getToken(request)).expectNextMatches(accessToken -> token1.equals(accessToken.getToken()) && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond()).verifyComplete();

            Assertions.assertNotNull(managedIdentityCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
            Assertions.assertNotNull(powerShellCredentialMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
        }
    }

    @Test
    public void testCredentialCachingSync() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        RuntimeException runtimeException =  new RuntimeException("Second call should not be made");

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
            when(managedIdentityCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Managed Identity credential")).thenThrow(runtimeException);
        }); MockedConstruction<IntelliJCredential> intelliJCredentialMock = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
            when(intelliJCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")).thenThrow(runtimeException);
        }); MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
            when(powerShellCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Powershell credential")).thenThrow(runtimeException);
        }); MockedConstruction<AzureCliCredential> azureCliCredentialMock = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
            when(azureCliCredential.getTokenSync(request)).thenThrow(new CredentialUnavailableException("Cannot get token from Cli credential")).thenThrow(runtimeException);
        }); MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock = mockConstruction(AzureDeveloperCliCredential.class, (azureDeveloperCliCredential, context) -> {
            when(azureDeveloperCliCredential.getTokenSync(request)).thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
        })) {
            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            AccessToken accessToken1 = credential.getTokenSync(request);
            Assertions.assertEquals(token1, accessToken1.getToken());
            Assertions.assertEquals(expiresAt.getSecond(), accessToken1.getExpiresAt().getSecond());

            // Second call should return token from cached credential.
            AccessToken accessToken2 = credential.getTokenSync(request);
            Assertions.assertEquals(token1, accessToken2.getToken());
            Assertions.assertEquals(expiresAt.getSecond(), accessToken2.getExpiresAt().getSecond());

            Assertions.assertNotNull(managedIdentityCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
            Assertions.assertNotNull(powerShellCredentialMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
        }
    }
}
