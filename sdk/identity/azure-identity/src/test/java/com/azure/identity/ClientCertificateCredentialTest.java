// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class ClientCertificateCredentialTest {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testValidCertificatePaths() throws Exception {
        // setup
        String pemPath = "C:\\fakepath\\cert1.pem";
        String pfxPath = "C:\\fakepath\\cert2.pfx";
        String pfxPassword = "password";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient pemIdentityClient = PowerMockito.mock(IdentityClient.class);
        IdentityClient pfxIdentityClient = PowerMockito.mock(IdentityClient.class);
        when(pemIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pfxIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pemIdentityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        when(pfxIdentityClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), eq(pemPath), isNull(), isNull(), eq(false), any()).thenReturn(pemIdentityClient);
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), eq(pfxPath), isNull(), eq(pfxPassword), eq(false), any()).thenReturn(pfxIdentityClient);

        // test
        ClientCertificateCredential credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemPath).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
        credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxPath, pfxPassword).build();
        StepVerifier.create(credential.getToken(request2))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }


    @Test
    public void testValidCertificates() throws Exception {
        // setup
        InputStream pemCert = new ByteArrayInputStream("fakepem".getBytes(StandardCharsets.UTF_8));
        InputStream pfxCert = new ByteArrayInputStream("fakepfx".getBytes(StandardCharsets.UTF_8));
        String pfxPassword = "password";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient pemIdentityClient = PowerMockito.mock(IdentityClient.class);
        IdentityClient pfxIdentityClient = PowerMockito.mock(IdentityClient.class);
        when(pemIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pfxIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pemIdentityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        when(pfxIdentityClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), isNull(), eq(pemCert), isNull(), eq(false), any()).thenReturn(pemIdentityClient);
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), isNull(), eq(pfxCert), eq(pfxPassword), eq(false), any()).thenReturn(pfxIdentityClient);

        // test
        ClientCertificateCredential credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemCert).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
        credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxCert, pfxPassword).build();
        StepVerifier.create(credential.getToken(request2))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }

    @Test
    public void testInvalidCertificatePaths() throws Exception {
        // setup
        String pemPath = "C:\\fakepath\\cert1.pem";
        String pfxPath = "C:\\fakepath\\cert2.pfx";
        String pfxPassword = "password";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");

        // mock
        IdentityClient pemIdentityClient = PowerMockito.mock(IdentityClient.class);
        IdentityClient pfxIdentityClient = PowerMockito.mock(IdentityClient.class);
        when(pemIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pfxIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pemIdentityClient.authenticateWithConfidentialClient(request1)).thenReturn(Mono.error(new MsalServiceException("bad pem", "BadPem")));
        when(pfxIdentityClient.authenticateWithConfidentialClient(request2)).thenReturn(Mono.error(new MsalServiceException("bad pfx", "BadPfx")));
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), eq(pemPath), isNull(), isNull(), eq(false), any()).thenReturn(pemIdentityClient);
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), eq(pfxPath), isNull(), eq(pfxPassword), eq(false), any()).thenReturn(pfxIdentityClient);

        // test
        ClientCertificateCredential credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemPath).build();
        StepVerifier.create(credential.getToken(request1))
            .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pem".equals(e.getMessage()))
            .verify();

        credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxPath, pfxPassword).build();
        StepVerifier.create(credential.getToken(request2))
            .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pfx".equals(e.getMessage()))
            .verify();
    }

    @Test
    public void testInvalidCertificates() throws Exception {
        // setup
        InputStream pemCert = new ByteArrayInputStream("fakepem".getBytes(StandardCharsets.UTF_8));
        InputStream pfxCert = new ByteArrayInputStream("fakepfx".getBytes(StandardCharsets.UTF_8));
        String pfxPassword = "password";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");

        // mock
        IdentityClient pemIdentityClient = PowerMockito.mock(IdentityClient.class);
        IdentityClient pfxIdentityClient = PowerMockito.mock(IdentityClient.class);
        when(pemIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pfxIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(pemIdentityClient.authenticateWithConfidentialClient(request1)).thenReturn(Mono.error(new MsalServiceException("bad pem", "BadPem")));
        when(pfxIdentityClient.authenticateWithConfidentialClient(request2)).thenReturn(Mono.error(new MsalServiceException("bad pfx", "BadPfx")));
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), isNull(), eq(pemCert), isNull(), eq(false), any()).thenReturn(pemIdentityClient);
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), isNull(), eq(pfxCert), eq(pfxPassword), eq(false), any()).thenReturn(pfxIdentityClient);

        // test
        ClientCertificateCredential credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pemCertificate(pemCert).build();
        StepVerifier.create(credential.getToken(request1))
            .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pem".equals(e.getMessage()))
            .verify();

        credential =
            new ClientCertificateCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).pfxCertificate(pfxCert, pfxPassword).build();
        StepVerifier.create(credential.getToken(request2))
            .expectErrorMatches(e -> e instanceof MsalServiceException && "bad pfx".equals(e.getMessage()))
            .verify();
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String pemPath = "C:\\fakepath\\cert1.pem";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(identityClient.authenticateWithConfidentialClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), isNull(), eq(pemPath), isNull(), isNull(), eq(false), any()).thenReturn(identityClient);

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
    }
}
