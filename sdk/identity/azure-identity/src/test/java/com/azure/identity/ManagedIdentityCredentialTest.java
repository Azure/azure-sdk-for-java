// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class ManagedIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testVirtualMachineMSICredentialConfigurations() {
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId("foo").build();
        Assertions.assertEquals("foo", credential.getClientId());
    }

    @Test
    public void testMSIEndpoint() {
        // setup
        String endpoint = "http://localhost";
        String secret = "secret";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("MSI_ENDPOINT", endpoint) // This must stay to signal we are in an app service context
            .put("MSI_SECRET", secret)
            .put("IDENTITY_ENDPOINT", endpoint)
            .put("IDENTITY_HEADER", secret));

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithManagedIdentityMsalClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        })) {
            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().configuration(configuration).clientId(CLIENT_ID).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testIMDS() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithManagedIdentityMsalClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));

        })) {
            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresOn.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testArcUserAssigned() {
        // setup
        String endpoint = "http://localhost";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", endpoint)
            .put("IMDS_ENDPOINT", endpoint));


        // test
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
            .configuration(configuration).clientId(CLIENT_ID).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof ClientAuthenticationException)
            .verify();
    }

    @Test
    public void testInvalidIdCombination() {
        // setup
        String resourceId = "/subscriptions/" + UUID.randomUUID() + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";
        String objectId = "2323-sd2323s-32323-32334-34343";

        // test
        Assertions.assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder()
                .clientId(CLIENT_ID).resourceId(resourceId).build());

        Assertions.assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder()
                .clientId(CLIENT_ID).resourceId(resourceId).objectId(objectId).build());

        Assertions.assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder()
                .clientId(CLIENT_ID).objectId(objectId).build());

        Assertions.assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder()
                .resourceId(resourceId).objectId(objectId).build());
    }

    @Test
    public void testArcIdentityCredentialCreated() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", "http://localhost")
            .put("IMDS_ENDPOINT", "http://localhost"))
            .put("USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI", "true");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(ArcIdentityCredential.class));
    }

    @Test
    public void testServiceFabricMsiCredentialCreated() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", "http://localhost")
            .put("IDENTITY_SERVER_THUMBPRINT", "thumbprint")
            .put("IDENTITY_HEADER", "header"))
            .put("USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI", "true");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(ServiceFabricMsiCredential.class));
    }

    @Test
    public void testAppServiceMsi2019CredentialCreated() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", "http://localhost")
            .put("IDENTITY_HEADER", "header"))
            .put("USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI", "true");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AppServiceMsiCredential.class));
    }

    @Test
    public void testAppServiceMsi2017CredentialCreated() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("MSI_ENDPOINT", "http://localhost")
            .put("MSI_SECRET", "secret"))
            .put("USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI", "true");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AppServiceMsiCredential.class));
    }

    @Test
    public void testCloudShellCredentialCreated() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("MSI_ENDPOINT", "http://localhost"))
            .put("USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI", "true");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AppServiceMsiCredential.class));
    }

    @Test
    public void testAksExchangeTokenCredentialCreated() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("AZURE_TENANT_ID", "tenantId")
            .put("AZURE_CLIENT_ID", "clientId")
            .put("AZURE_FEDERATED_TOKEN_FILE", "tokenFile"));

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AksExchangeTokenCredential.class));
    }

    @Test
    public void testIMDSPodIdentityV1Credential() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource());
        configuration.put("USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI", "true");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(VirtualMachineMsiCredential.class));
    }
}

