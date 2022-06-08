// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
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
}
