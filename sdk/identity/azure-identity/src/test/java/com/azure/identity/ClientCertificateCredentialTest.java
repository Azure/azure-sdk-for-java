// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class ClientCertificateCredentialTest {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String TEST_PEM_CERT = "-----BEGIN CERTIFICATE-----\n"
        + "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\n"
        + "-----END CERTIFICATE-----\n"
        + "-----BEGIN PRIVATE KEY-----\n"
        + "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\n"
        + "GCW0/fPxstiXQaANvVLWIIih4VlLHg==\n"
        + "-----END PRIVATE KEY-----\n";

    @Test
    public void testValidPemCertificatePath() throws Exception {
        // setup
        String pemPath = "C:\\fakepath\\cert1.pem";
        String token1 = "token1";

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemPath).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testValidPfxCertificatePath() throws Exception {
        // setup
        String pfxPath = "C:\\fakepath\\cert2.pfx";
        String pfxPassword = "password";
        String token2 = "token2";
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));

        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxPath, pfxPassword).build();
            StepVerifier.create(credential.getToken(request2))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testValidPemCertificate() throws Exception {
        // setup
        InputStream pemCert = new ByteArrayInputStream("fakepem".getBytes(StandardCharsets.UTF_8));
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemCert).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }

    }

    @Test
    public void testValidPfxCertificate() throws Exception {
        // setup
        InputStream pfxCert = new ByteArrayInputStream("fakepfx".getBytes(StandardCharsets.UTF_8));
        String pfxPassword = "password";
        String token2 = "token2";
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxCert, pfxPassword).build();
            StepVerifier.create(credential.getToken(request2))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }


    }

    @Test
    public void testInvalidPemCertificatePath() throws Exception {
        // setup
        String pemPath = "C:\\fakepath\\cert1.pem";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(Mono.error(new MsalServiceException("bad pem", "BadPem")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemPath).build();
            StepVerifier.create(credential.getToken(request1))
                .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pem".equals(e.getMessage()))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidPfxCertificatePath() throws Exception {
        // setup
        String pfxPath = "C:\\fakepath\\cert2.pfx";
        String pfxPassword = "password";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request)).thenReturn(Mono.error(new MsalServiceException("bad pfx", "BadPfx")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxPath, pfxPassword).build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pfx".equals(e.getMessage()))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidPemCertificate() throws Exception {
        // setup
        InputStream pemCert = new ByteArrayInputStream("fakepem".getBytes(StandardCharsets.UTF_8));
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(Mono.error(new MsalServiceException("bad pem", "BadPem")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemCert).build();
            StepVerifier.create(credential.getToken(request1))
                .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pem".equals(e.getMessage()))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidPfxCertificate() throws Exception {
        // setup
        InputStream pfxCert = new ByteArrayInputStream("fakepfx".getBytes(StandardCharsets.UTF_8));
        String pfxPassword = "password";
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request2)).thenReturn(Mono.error(new MsalServiceException("bad pfx", "BadPfx")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxCert, pfxPassword).build();
            StepVerifier.create(credential.getToken(request2))
                .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pfx".equals(e.getMessage()))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String pemPath = "C:\\fakepath\\cert1.pem";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        })) {
            // test
            try {
                new ClientCertificateCredentialBuilder().clientId(CLIENT_ID).pemCertificate(pemPath).build();
                fail();
            } catch (IllegalArgumentException e) {
                Assert.assertTrue(e.getMessage().contains("tenantId"));
            }
            try {
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).pemCertificate(pemPath).build();
                fail();
            } catch (IllegalArgumentException e) {
                Assert.assertTrue(e.getMessage().contains("clientId"));
            }
            try {
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).build();
                fail();
            } catch (IllegalArgumentException e) {
                Assert.assertTrue(e.getMessage().contains("clientCertificate"));
            }
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidAdditionalTenant() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_PEM_CERT.getBytes(StandardCharsets.UTF_8))) {
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(inputStream)
                    .additionallyAllowedTenants("RANDOM").build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
                .verify();
        }
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_PEM_CERT.getBytes(StandardCharsets.UTF_8))) {
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(inputStream).build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
                .verify();
        }
    }

    @Test
    public void testValidMultiTenantAuth() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_PEM_CERT.getBytes(StandardCharsets.UTF_8))) {
            ClientCertificateCredential credential =
                new ClientCertificateCredentialBuilder()
                    .tenantId(TENANT_ID)
                    .clientId(CLIENT_ID)
                    .pemCertificate(inputStream)
                    .additionallyAllowedTenants("*").build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof MsalServiceException)
                .verify();
        }

    }
}
