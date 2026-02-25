// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_CLIENT_ID", CLIENT_ID)
                .put("AZURE_CLIENT_SECRET", secret)
                .put("AZURE_TENANT_ID", TENANT_ID));

        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureDeveloperCli(request1)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(request1))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            });
            MockedConstruction<IntelliJCredential> ijcredential
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request1)).thenReturn(Mono.empty());
                })) {

            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
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
        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithManagedIdentityMsalClient(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            });
            MockedConstruction<IntelliJCredential> ijcredential
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
                })) {

            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
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
        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            });
            MockedConstruction<IntelliJCredential> ijcredential
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
                })) {

            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().managedIdentityClientId("dummy-client-id")
                    .configuration(configuration)
                    .build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
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
        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            // test
            String clientId = "dummy-client-id";
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().workloadIdentityClientId(clientId)
                .configuration(configuration)
                .build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
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
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().workloadIdentityClientId(clientId)
            .configuration(configuration)
            .build();
        WorkloadIdentityCredential workloadIdentityCredential = credential.getWorkloadIdentityCredentialIfPresent();
        Assertions.assertNotNull(workloadIdentityCredential);
        Assertions.assertEquals(clientId, workloadIdentityCredential.getClientId());

        credential = new DefaultAzureCredentialBuilder().managedIdentityClientId(clientId)
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
        credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
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
        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureDeveloperCli(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
                when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithSharedTokenCache(request, null)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithIntelliJ(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithVsCodeCredential(any(), any())).thenReturn(Mono.empty());
            });
            MockedConstruction<IntelliJCredential> ijcredential
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
                })) {

            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
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
        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureDeveloperCli(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
                when(identityClient.authenticateWithAzureCli(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithAzurePowerShell(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithSharedTokenCache(request, null)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithIntelliJ(request)).thenReturn(Mono.empty());
                when(identityClient.authenticateWithVsCodeCredential(any(), any())).thenReturn(Mono.empty());
            });
            MockedConstruction<IntelliJCredential> ijcredential
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request)).thenReturn(Mono.empty());
                })) {

            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
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
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(
                    Mono.error(new CredentialUnavailableException("Cannot get token from managed identity")));
            });
            MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock
                = mockConstruction(AzureDeveloperCliCredential.class, (AzureDeveloperCliCredential, context) -> {
                    when(AzureDeveloperCliCredential.getToken(request)).thenReturn(Mono.error(
                        new CredentialUnavailableException("Cannot get token from Azure Developer CLI credential")));
                });
            MockedConstruction<AzureCliCredential> azureCliCredentialMock
                = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
                    when(azureCliCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from Azure CLI credential")));
                });
            MockedConstruction<AzurePowerShellCredential> azurePowerShellCredentialMock
                = mockConstruction(AzurePowerShellCredential.class, (azurePowerShellCredential, context) -> {
                    when(azurePowerShellCredential.getToken(request)).thenReturn(Mono.error(
                        new CredentialUnavailableException("Cannot get token from Azure PowerShell credential")));
                });
            MockedConstruction<IntelliJCredential> intelliJCredentialMock
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
                });
            MockedConstruction<BrokerCredential> brokerCredentialMock
                = mockConstruction(BrokerCredential.class, (brokerCredential, context) -> {
                    when(brokerCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from OS Broker credential")));
                })) {
            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(t -> t instanceof CredentialUnavailableException
                    && t.getMessage().startsWith("EnvironmentCredential authentication unavailable. "))
                .verify();
            Assertions.assertNotNull(identityClientMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
            Assertions.assertNotNull(azurePowerShellCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
            Assertions.assertNotNull(brokerCredentialMock);
        }
    }

    @Test
    public void testCredentialUnavailable() {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock
            = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
                when(managedIdentityCredential.getToken(request)).thenReturn(Mono
                    .error(new CredentialUnavailableException("Cannot get token from Managed Identity credential")));
            });
            MockedConstruction<IntelliJCredential> intelliJCredentialMock
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
                });
            MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock
                = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
                    when(powerShellCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from Powershell credential")));
                });
            MockedConstruction<AzureCliCredential> azureCliCredentialMock
                = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
                    when(azureCliCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from Cli credential")));
                });
            MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock
                = mockConstruction(AzureDeveloperCliCredential.class, (AzureDeveloperCliCredential, context) -> {
                    when(AzureDeveloperCliCredential.getToken(request)).thenReturn(Mono.error(
                        new CredentialUnavailableException("Cannot get token from Azure Developer CLI credential")));
                });
            MockedConstruction<BrokerCredential> brokerCredentialMock
                = mockConstruction(BrokerCredential.class, (brokerCredential, context) -> {
                    when(brokerCredential.getToken(request)).thenReturn(
                        Mono.error(new CredentialUnavailableException("Cannot get token from OS Broker credential")));
                })) {

            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(t -> t instanceof CredentialUnavailableException
                    && t.getMessage().startsWith("EnvironmentCredential authentication unavailable. "))
                .verify();
            Assertions.assertNotNull(managedIdentityCredentialMock);
            Assertions.assertNotNull(intelliJCredentialMock);
            Assertions.assertNotNull(powerShellCredentialMock);
            Assertions.assertNotNull(azureCliCredentialMock);
            Assertions.assertNotNull(azureDeveloperCliCredentialMock);
            Assertions.assertNotNull(brokerCredentialMock);
        }
    }

    @Test
    public void testCredentialUnavailableSync() {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        EmptyEnvironmentConfigurationSource source = new EmptyEnvironmentConfigurationSource();
        Configuration configuration = new ConfigurationBuilder(source, source, source).build();

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock
            = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
                when(managedIdentityCredential.getTokenSync(request))
                    .thenThrow(new CredentialUnavailableException("Cannot get token from Managed Identity credential"));
            });
            MockedConstruction<IntelliJCredential> intelliJCredentialMock
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getTokenSync(request))
                        .thenThrow(new CredentialUnavailableException("Cannot get token from IntelliJ Credential"));
                });
            MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock
                = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
                    when(powerShellCredential.getTokenSync(request))
                        .thenThrow(new CredentialUnavailableException("Cannot get token from Powershell credential"));
                });
            MockedConstruction<AzureCliCredential> azureCliCredentialMock
                = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
                    when(azureCliCredential.getTokenSync(request))
                        .thenThrow(new CredentialUnavailableException("Cannot get token from Cli credential"));
                });
            MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock
                = mockConstruction(AzureDeveloperCliCredential.class, (azureDeveloperCliCredential, context) -> {
                    when(azureDeveloperCliCredential.getTokenSync(request)).thenThrow(
                        new CredentialUnavailableException("Cannot get token from Azure Developer Cli credential"));
                })) {
            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            try {
                credential.getTokenSync(request);
            } catch (Exception e) {
                Assertions.assertTrue(e instanceof CredentialUnavailableException
                    && e.getMessage().startsWith("EnvironmentCredential authentication unavailable. "));
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
        String resourceId = "/subscriptions/" + UUID.randomUUID()
            + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";

        // test
        Assertions.assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder().managedIdentityClientId(CLIENT_ID)
                .managedIdentityResourceId(resourceId)
                .build());
    }

    @Test
    public void testInvalidAdditionalTenant() {
        // setup
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("RANDOM")
            .configuration(configuration)
            .build();

        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().contains("The current credential is not configured to")));
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();

        StepVerifier.create(credential.getToken(request))
            .verifyErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().contains("The current credential is not configured to")));
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_CLIENT_ID, "foo")
                .put(Configuration.PROPERTY_AZURE_CLIENT_SECRET, "bar")
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, "baz"));

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        DefaultAzureCredential credential
            = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").configuration(configuration).build();

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

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock
            = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
                when(managedIdentityCredential.getToken(request))
                    .thenReturn(Mono
                        .error(new CredentialUnavailableException("Cannot get token from Managed Identity credential")))
                    .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
            });
            MockedConstruction<IntelliJCredential> intelliJCredentialMock
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getToken(request))
                        .thenReturn(
                            Mono.error(new CredentialUnavailableException("Cannot get token from InteliJ credential")))
                        .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
                });
            MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock
                = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
                    when(powerShellCredential.getToken(request))
                        .thenReturn(Mono
                            .error(new CredentialUnavailableException("Cannot get token from PowerShell credential")))
                        .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
                });
            MockedConstruction<AzureCliCredential> azureCliCredentialMock
                = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
                    when(azureCliCredential.getToken(request))
                        .thenReturn(Mono
                            .error(new CredentialUnavailableException("Cannot get token from Azure CLI credential")))
                        .thenReturn(Mono.error(new RuntimeException("Second call should not be made")));
                });
            MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock
                = mockConstruction(AzureDeveloperCliCredential.class, (azureDeveloperCliCredential, context) -> {
                    when(azureDeveloperCliCredential.getToken(request))
                        .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
                })) {

            // test
            DefaultAzureCredential credential
                = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();

            // Second call should return token from cached credential.
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();

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

        RuntimeException runtimeException = new RuntimeException("Second call should not be made");

        try (MockedConstruction<ManagedIdentityCredential> managedIdentityCredentialMock
            = mockConstruction(ManagedIdentityCredential.class, (managedIdentityCredential, context) -> {
                when(managedIdentityCredential.getTokenSync(request))
                    .thenThrow(new CredentialUnavailableException("Cannot get token from Managed Identity credential"))
                    .thenThrow(runtimeException);
            });
            MockedConstruction<IntelliJCredential> intelliJCredentialMock
                = mockConstruction(IntelliJCredential.class, (intelliJCredential, context) -> {
                    when(intelliJCredential.getTokenSync(request))
                        .thenThrow(new CredentialUnavailableException("Cannot get token from IntelliJ Credential"))
                        .thenThrow(runtimeException);
                });
            MockedConstruction<AzurePowerShellCredential> powerShellCredentialMock
                = mockConstruction(AzurePowerShellCredential.class, (powerShellCredential, context) -> {
                    when(powerShellCredential.getTokenSync(request))
                        .thenThrow(new CredentialUnavailableException("Cannot get token from Powershell credential"))
                        .thenThrow(runtimeException);
                });
            MockedConstruction<AzureCliCredential> azureCliCredentialMock
                = mockConstruction(AzureCliCredential.class, (azureCliCredential, context) -> {
                    when(azureCliCredential.getTokenSync(request))
                        .thenThrow(new CredentialUnavailableException("Cannot get token from Cli credential"))
                        .thenThrow(runtimeException);
                });
            MockedConstruction<AzureDeveloperCliCredential> azureDeveloperCliCredentialMock
                = mockConstruction(AzureDeveloperCliCredential.class, (azureDeveloperCliCredential, context) -> {
                    when(azureDeveloperCliCredential.getTokenSync(request))
                        .thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
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

    @ParameterizedTest
    @ValueSource(strings = { "prod", "PROD", "Prod", "pRoD" })
    public void testProductionOnlyCredentialsChain(String prodValue) {
        // Setup config with production-only setting using various case variants
        TestConfigurationSource configSource = new TestConfigurationSource().put("AZURE_TOKEN_CREDENTIALS", prodValue);
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Build the credential with the test configuration
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();

        List<TokenCredential> credentials = extractCredentials(credential);

        // Only production credentials should be present (3)
        assertEquals(3, credentials.size());

        // Verify production credentials
        assertInstanceOf(EnvironmentCredential.class, credentials.get(0));
        assertInstanceOf(WorkloadIdentityCredential.class, credentials.get(1));
        assertInstanceOf(ManagedIdentityCredential.class, credentials.get(2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "dev", "DEV", "Dev", "dEv" })
    public void testDeveloperOnlyCredentialsChain(String devValue) {
        // Setup config with developer-only setting using various case variants
        TestConfigurationSource configSource = new TestConfigurationSource().put("AZURE_TOKEN_CREDENTIALS", devValue);
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Build the credential with the test configuration
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();

        List<TokenCredential> credentials = extractCredentials(credential);

        // Only developer credentials should be present (4)
        assertEquals(6, credentials.size());

        // Verify developer credentials in order
        assertInstanceOf(IntelliJCredential.class, credentials.get(0));
        assertInstanceOf(VisualStudioCodeCredential.class, credentials.get(1));
        assertInstanceOf(AzureCliCredential.class, credentials.get(2));
        assertInstanceOf(AzurePowerShellCredential.class, credentials.get(3));
        assertInstanceOf(AzureDeveloperCliCredential.class, credentials.get(4));
        assertInstanceOf(BrokerCredential.class, credentials.get(5));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "AzureCliCredential",
            "azureclicredential",
            "AZURECLICREDENTIAL",
            "IntelliJCredential",
            "intellijcredential",
            "AzurePowerShellCredential",
            "azurepowershellcredential",
            "AzureDeveloperCliCredential",
            "azuredeveloperclicredential",
            "EnvironmentCredential",
            "environmentcredential",
            "WorkloadIdentityCredential",
            "workloadidentitycredential",
            "ManagedIdentityCredential",
            "managedidentitycredential",
            "VisualStudioCodeCredential",
            "visualstudiocodecredential" })
    public void testTargetedCredentialSelection(String credentialValue) {
        // Setup config with targeted credential value (case-insensitive)
        TestConfigurationSource configSource
            = new TestConfigurationSource().put("AZURE_TOKEN_CREDENTIALS", credentialValue);
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Build the credential with the test configuration
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
        List<TokenCredential> credentials = extractCredentials(credential);

        // Should contain exactly one credential
        assertEquals(1, credentials.size());

        // Assert that the only credential matches expected type
        Class<? extends TokenCredential> expectedType;
        switch (credentialValue.toLowerCase(Locale.ROOT)) {
            case "azureclicredential":
                expectedType = AzureCliCredential.class;
                break;

            case "intellijcredential":
                expectedType = IntelliJCredential.class;
                break;

            case "azurepowershellcredential":
                expectedType = AzurePowerShellCredential.class;
                break;

            case "azuredeveloperclicredential":
                expectedType = AzureDeveloperCliCredential.class;
                break;

            case "environmentcredential":
                expectedType = EnvironmentCredential.class;
                break;

            case "workloadidentitycredential":
                expectedType = WorkloadIdentityCredential.class;
                break;

            case "managedidentitycredential":
                expectedType = ManagedIdentityCredential.class;
                break;

            case "visualstudiocodecredential":
                expectedType = VisualStudioCodeCredential.class;
                break;

            default:
                throw new IllegalArgumentException("Unsupported test value: " + credentialValue);
        }

        assertInstanceOf(expectedType, credentials.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "PRODUCTION", "DEVELOPER", "both", "p r o d", "d e v" })
    public void testInvalidCredentialsConfiguration(String configValue) {
        // Setup config with invalid setting
        TestConfigurationSource configSource
            = new TestConfigurationSource().put("AZURE_TOKEN_CREDENTIALS", configValue);
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Build the credential with invalid configuration - should throw
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new DefaultAzureCredentialBuilder().configuration(configuration).build());

        // Verify error message
        assertTrue(exception.getMessage().contains("Invalid value for AZURE_TOKEN_CREDENTIALS"));
    }

    @Test
    public void testDefaultCredentialChainWithoutFilter() {
        // Create a test configuration with no AZURE_TOKEN_CREDENTIALS setting
        TestConfigurationSource configSource = new TestConfigurationSource();
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Build the credential with the test configuration
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().configuration(configuration).build();

        // Extract credentials to check their types and order
        List<TokenCredential> credentials = extractCredentials(credential);

        // Verify the complete chain with all 9 credentials
        assertEquals(9, credentials.size());
        assertInstanceOf(EnvironmentCredential.class, credentials.get(0));
        assertInstanceOf(WorkloadIdentityCredential.class, credentials.get(1));
        assertInstanceOf(ManagedIdentityCredential.class, credentials.get(2));
        assertInstanceOf(IntelliJCredential.class, credentials.get(3));
        assertInstanceOf(VisualStudioCodeCredential.class, credentials.get(4));
        assertInstanceOf(AzureCliCredential.class, credentials.get(5));
        assertInstanceOf(AzurePowerShellCredential.class, credentials.get(6));
        assertInstanceOf(AzureDeveloperCliCredential.class, credentials.get(7));
        assertInstanceOf(BrokerCredential.class, credentials.get(8));
    }

    /**
     * Helper method to extract the credentials list from a DefaultAzureCredential instance
     */
    @SuppressWarnings("unchecked")
    private List<TokenCredential> extractCredentials(DefaultAzureCredential credential) {
        try {
            // Use reflection to access the private credentials field
            Field credentialsField = ChainedTokenCredential.class.getDeclaredField("credentials");
            credentialsField.setAccessible(true);
            return new ArrayList<>((List<TokenCredential>) credentialsField.get(credential));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to extract credentials", e);
        }
    }

    @Test
    public void testRequireEnvVarsSuccess() {
        // Setup - create configuration with required environment variables present
        TestConfigurationSource configSource = new TestConfigurationSource().put("AZURE_CLIENT_ID", CLIENT_ID)
            .put("AZURE_TENANT_ID", TENANT_ID)
            .put("AZURE_CLIENT_SECRET", "test-secret");
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Test - should not throw when all required env vars are present
        DefaultAzureCredential credential
            = new DefaultAzureCredentialBuilder()
                .requireEnvVars(AzureIdentityEnvVars.AZURE_CLIENT_ID, AzureIdentityEnvVars.AZURE_TENANT_ID,
                    AzureIdentityEnvVars.AZURE_CLIENT_SECRET)
                .configuration(configuration)
                .build();

        // Verify the credential was created successfully
        Assertions.assertNotNull(credential);
    }

    @Test
    public void testRequireEnvVarsSingleMissing() {
        // Setup - create configuration missing one required environment variable
        TestConfigurationSource configSource
            = new TestConfigurationSource().put("AZURE_CLIENT_ID", CLIENT_ID).put("AZURE_TENANT_ID", TENANT_ID);
        // AZURE_CLIENT_SECRET is missing
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Test - should throw when required env var is missing
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder()
                .requireEnvVars(AzureIdentityEnvVars.AZURE_CLIENT_ID, AzureIdentityEnvVars.AZURE_TENANT_ID,
                    AzureIdentityEnvVars.AZURE_CLIENT_SECRET)
                .configuration(configuration)
                .build());

        // Verify error message
        assertTrue(exception.getMessage().contains("Required environment variable is missing: AZURE_CLIENT_SECRET"));
    }

    @Test
    public void testRequireEnvVarsMultipleMissing() {
        // Setup - create configuration missing multiple required environment variables
        TestConfigurationSource configSource = new TestConfigurationSource().put("AZURE_CLIENT_ID", CLIENT_ID);
        // AZURE_TENANT_ID and AZURE_CLIENT_SECRET are missing
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Test - should throw when multiple required env vars are missing
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder()
                .requireEnvVars(AzureIdentityEnvVars.AZURE_CLIENT_ID, AzureIdentityEnvVars.AZURE_TENANT_ID,
                    AzureIdentityEnvVars.AZURE_CLIENT_SECRET)
                .configuration(configuration)
                .build());

        // Verify error message contains all missing variables
        assertTrue(exception.getMessage().contains("Required environment variables are missing:"));
        assertTrue(exception.getMessage().contains("AZURE_TENANT_ID"));
        assertTrue(exception.getMessage().contains("AZURE_CLIENT_SECRET"));
        // Should not contain AZURE_CLIENT_ID since it is present
        String message = exception.getMessage();
        assertFalse(message.contains("AZURE_CLIENT_ID"));
    }

    @Test
    public void testRequireEnvVarsEmptyValue() {
        // Setup - create configuration with empty string for required environment variable
        TestConfigurationSource configSource = new TestConfigurationSource().put("AZURE_CLIENT_ID", CLIENT_ID)
            .put("AZURE_TENANT_ID", TENANT_ID)
            .put("AZURE_CLIENT_SECRET", ""); // Empty string should be treated as missing
        Configuration configuration = TestUtils.createTestConfiguration(configSource);

        // Test - should throw when required env var is empty
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> new DefaultAzureCredentialBuilder()
                .requireEnvVars(AzureIdentityEnvVars.AZURE_CLIENT_ID, AzureIdentityEnvVars.AZURE_TENANT_ID,
                    AzureIdentityEnvVars.AZURE_CLIENT_SECRET)
                .configuration(configuration)
                .build());

        // Verify error message
        assertTrue(exception.getMessage().contains("Required environment variable is missing: AZURE_CLIENT_SECRET"));
    }
}
