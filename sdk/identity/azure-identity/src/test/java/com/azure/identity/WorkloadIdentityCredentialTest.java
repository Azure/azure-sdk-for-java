// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class WorkloadIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testWorkloadIdentityFlow() {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithWorkloadIdentityConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        })) {
            // test
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder()
                .tenantId("dummy-tenantid")
                .clientId("dummy-clientid")
                .tokenFilePath("dummy-path")
                .configuration(configuration).clientId(CLIENT_ID).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testWorkloadIdentityFlowSync() {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // mock
        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithWorkloadIdentityConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
        })) {
            // test
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder()
                .tenantId("dummy-tenantid")
                .clientId("dummy-clientid")
                .tokenFilePath("dummy-path")
                .configuration(configuration).clientId(CLIENT_ID).build();

            AccessToken token = credential.getTokenSync(request1);

            assertTrue(token1.equals(token.getToken()));
            assertTrue(expiresAt.getSecond() == token.getExpiresAt().getSecond());
            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testWorkloadIdentityFlowFailureNoTenantId() {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // test
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new WorkloadIdentityCredentialBuilder().configuration(configuration)
                .clientId(CLIENT_ID)
                .tokenFilePath("dummy-path")
                .build());
    }

    @Test
    public void testWorkloadIdentityFlowFailureNoClientId() {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // test
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new WorkloadIdentityCredentialBuilder().configuration(configuration)
                .tenantId("TENANT_ID")
                .tokenFilePath("dummy-path").
                build());
    }

    @Test
    public void testWorkloadIdentityFlowFailureNoTokenPath() {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // test
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new WorkloadIdentityCredentialBuilder().configuration(configuration)
                .tenantId("tenant-id")
                .clientId("client-id")
                .build());
    }
}

