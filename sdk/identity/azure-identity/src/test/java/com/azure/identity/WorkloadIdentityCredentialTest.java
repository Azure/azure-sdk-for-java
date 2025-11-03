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
    private static final String ENV_PROXY_URL = "AZURE_KUBERNETES_TOKEN_PROXY";
    private static final String ENV_CA_FILE = "AZURE_KUBERNETES_CA_FILE";
    private static final String ENV_CA_DATA = "AZURE_KUBERNETES_CA_DATA";
    private static final String ENV_SNI_NAME = "AZURE_KUBERNETES_SNI_NAME";
    private static final String CA_DATA
        = "-----BEGIN CERTIFICATE-----\n" + "MIICMzCCAZygAwIBAgIJALiPnVsvq8dsMA0GCSqGSIb3DQEBBQUAMFMxCzAJBgNV\n"
            + "BAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNVBAcTA2ZvbzEMMAoGA1UEChMDZm9v\n"
            + "MQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2ZvbzAeFw0xMzAzMTkxNTQwMTlaFw0x\n"
            + "ODAzMTgxNTQwMTlaMFMxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNV\n"
            + "BAcTA2ZvbzEMMAoGA1UEChMDZm9vMQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2Zv\n"
            + "bzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAzdGfxi9CNbMf1UUcvDQh7MYB\n"
            + "OveIHyc0E0KIbhjK5FkCBU4CiZrbfHagaW7ZEcN0tt3EvpbOMxxc/ZQU2WN/s/wP\n"
            + "xph0pSfsfFsTKM4RhTWD2v4fgk+xZiKd1p0+L4hTtpwnEw0uXRVd0ki6muwV5y/P\n"
            + "+5FHUeldq+pgTcgzuK8CAwEAAaMPMA0wCwYDVR0PBAQDAgLkMA0GCSqGSIb3DQEB\n"
            + "BQUAA4GBAJiDAAtY0mQQeuxWdzLRzXmjvdSuL9GoyT3BF/jSnpxz5/58dba8pWen\n"
            + "v3pj4P3w5DoOso0rzkZy2jEsEitlVM2mLSbQpMM+MUVQCQoiG6W9xuCFuxSrwPIS\n"
            + "pAqEAuV4DNoxQKKWmhVv+J0ptMWD25Pnpxeq5sXzghfJnslJlQND\n" + "-----END CERTIFICATE-----\n";

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

    @Test
    public void testProxyEnabledWithProxyUrlGetsToken(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        String proxyUrl = "https://token-proxy.example.com";

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, proxyUrl));

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();

            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();

            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testProxyEnabledWithoutProxyUrlGetsToken(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint));
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();

            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();

            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testProxyEnabledInvalidProxyUrlSchemeFailure(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://localhost";

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "http://not-https.example.com"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

    @Test
    public void testProxyUrlWithQueryFailure(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "https://proxy.example.com?x=y"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

    @Test
    public void testProxyUrlWithFragmentFailure(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "https://proxy.example.com#frag"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

    @Test
    public void testProxyUrlWithUserInfoFailure(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "https://user:pass@proxy.example.com"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

    @Test
    public void testCaFileAndCaDataPresentFailure(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Path caFile = tempDir.resolve("ca.crt");
        Files.write(caFile,
            "-----BEGIN CERTIFICATE-----\nMIIB...==\n-----END CERTIFICATE-----\n".getBytes(StandardCharsets.UTF_8));

        String caData = "-----BEGIN CERTIFICATE-----\nMIIB...==\n-----END CERTIFICATE-----";

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "https://proxy.example.com")
                .put(ENV_CA_FILE, caFile.toString())
                .put(ENV_CA_DATA, caData));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

    @Test
    public void testProxyEnabledWithProxyUrlGetsTokenSync(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        String proxyUrl = "https://token-proxy.example.com";

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, proxyUrl));

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        try (MockedConstruction<IdentitySyncClient> identitySyncClientMock
            = mockConstruction(IdentitySyncClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any()))
                    .thenThrow(new IllegalStateException("Test"));
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
            })) {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();

            AccessToken token = credential.getTokenSync(request1);

            assertTrue(token1.equals(token.getToken()));
            assertTrue(expiresAt.getSecond() == token.getExpiresAt().getSecond());
            assertNotNull(identitySyncClientMock);
        }
    }

    @Test
    public void testProxyUrlWithCaDataAcquiresToken(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        String token1 = "token-ca-data";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "https://token-proxy.example.com")
                .put(ENV_CA_DATA, CA_DATA));

        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            WorkloadIdentityCredential cred = new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();

            StepVerifier.create(cred.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && token.getExpiresAt().getSecond() == expiresAt.getSecond())
                .verifyComplete();

            assertNotNull(mocked);
        }
    }

    @Test
    public void testProxyUrlWithCaFileGetsToken(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        String token1 = "tok-ca-file";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));
        Path caFile = tempDir.resolve("ca.crt");

        Files.write(caFile, CA_DATA.getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, "https://token-proxy.example.com")
                .put(ENV_CA_FILE, caFile.toString()));

        try (MockedConstruction<IdentityClient> mocked
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            WorkloadIdentityCredential cred = new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();

            StepVerifier.create(cred.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && token.getExpiresAt().getSecond() == expiresAt.getSecond())
                .verifyComplete();

            assertNotNull(mocked);
        }
    }

    @Test
    public void testProxyEnabledWithSniNameGetsToken(@TempDir Path tempDir) throws IOException {
        // setup
        String endpoint = "https://localhost";
        String token1 = "token1";
        String proxyUrl = "https://token-proxy.example.com";
        String sniName = "615f3b8ad7eb011a09ed3b762e404de43ebc7ade0802a34c9fd322b688c3a655.ests.aks";

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, proxyUrl)
                .put(ENV_SNI_NAME, sniName));

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId("dummy-tenantid")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();

            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();

            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testProxyWithInvalidCaDataThrowsAtConstruction(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        String proxyUrl = "https://token-proxy.example.com";
        String invalidCaData = "-----BEGIN CERTIFICATE-----\nINVALID_BASE64_DATA_HERE\n-----END CERTIFICATE-----";

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, proxyUrl)
                .put(ENV_CA_DATA, invalidCaData));

        Assertions.assertThrows(RuntimeException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

    @Test
    public void testProxyWithInvalidCaFileThrowsAtConstruction(@TempDir Path tempDir) throws IOException {
        String endpoint = "https://login.microsoftonline.com";
        String proxyUrl = "https://token-proxy.example.com";
        String invalidCaData = "-----BEGIN CERTIFICATE-----\nINVALID_BASE64_DATA_HERE\n-----END CERTIFICATE-----";

        Path tokenFile = tempDir.resolve("token.txt");
        Files.write(tokenFile, "dummy-token".getBytes(StandardCharsets.UTF_8));

        Path caFile = tempDir.resolve("invalid-ca.crt");
        Files.write(caFile, invalidCaData.getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint)
                .put(ENV_PROXY_URL, proxyUrl)
                .put(ENV_CA_FILE, caFile.toString()));

        Assertions.assertThrows(RuntimeException.class, () -> {
            new WorkloadIdentityCredentialBuilder().tenantId("tenant")
                .clientId(CLIENT_ID)
                .tokenFilePath(tokenFile.toString())
                .configuration(configuration)
                .enableAzureTokenProxy()
                .build();
        });
    }

}
