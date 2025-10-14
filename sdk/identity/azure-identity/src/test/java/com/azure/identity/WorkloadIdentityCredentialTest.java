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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class WorkloadIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testWorkloadIdentityFlow(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // Create a temporary token file
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            // test
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testWorkloadIdentityFlowSync(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // Create a temporary token file
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        // mock
        try (MockedConstruction<IdentitySyncClient> identitySyncClientMock
            = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithConfidentialClientCache(any()))
                    .thenThrow(new IllegalStateException("Test"));
                when(identitySyncClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
            })) {
            // test
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .build();

            AccessToken token = credential.getTokenSync(request1);

            assertTrue(token1.equals(token.getToken()));
            assertTrue(expiresAt.getSecond() == token.getExpiresAt().getSecond());
            assertNotNull(identitySyncClientMock);
        }
    }

    @Test
    public void testWorkloadIdentityFlowFailureNoTenantId() {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

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
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // test
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new WorkloadIdentityCredentialBuilder().configuration(configuration)
                .tenantId("TENANT_ID")
                .tokenFilePath("dummy-path")
                .build());
    }

    @Test
    public void testWorkloadIdentityFlowFailureNoTokenPath() {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // test
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new WorkloadIdentityCredentialBuilder().configuration(configuration)
                .tenantId("tenant-id")
                .clientId("client-id")
                .build());
    }

    @Test
    public void testGetClientId(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));

        // test
        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
            .clientId(CLIENT_ID)
            .tokenFilePath("dummy-path")
            .configuration(configuration)
            .build();

        Assertions.assertEquals(CLIENT_ID, credential.getClientId());
    }

    @Test
    public void testFileReadingError(@TempDir Path tempDir) {
        // setup
        String endpoint = "https://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        String nonExistentFile = tempDir.resolve("non-existent-file.txt").toString();

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
            .clientId(CLIENT_ID)
            .tokenFilePath(nonExistentFile)
            .configuration(configuration)
            .build();

        StepVerifier.create(credential.getToken(request)).expectErrorSatisfies(error -> {
            assertTrue(error instanceof RuntimeException);
            assertTrue(error.getMessage().contains("Failed to read federated token from file"));
            assertTrue(error.getCause() instanceof IOException);  // Original IOException from Files.readAllBytes
        }).verify();
    }
}
