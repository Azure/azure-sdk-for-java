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
public class ClientSecretCredentialTest {


    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testValidSecrets() throws Exception {
        // setup
        String secret = "secret";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        when(identityClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ClientSecretCredential credential =
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
        StepVerifier.create(credential.getToken(request2))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }

    @Test
    public void testInvalidSecrets() throws Exception {
        // setup
        String secret = "secret";
        String badSecret = "badsecret";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        IdentityClient badIdentityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(badIdentityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(identityClient.authenticateWithConfidentialClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        when(badIdentityClient.authenticateWithConfidentialClient(request)).thenReturn(Mono.error(new MsalServiceException("bad secret", "BadSecret")));
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), eq(secret), isNull(), isNull(), isNull(), eq(false), any()).thenReturn(identityClient);
        PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), eq(badSecret), isNull(), isNull(), isNull(), eq(false), any()).thenReturn(badIdentityClient);

        // test
        ClientSecretCredential credential =
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret).build();
        StepVerifier.create(credential.getToken(request))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
        credential =
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(badSecret).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof MsalServiceException && "bad secret".equals(e.getMessage()))
            .verify();
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String secret = "secret";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
        when(identityClient.authenticateWithConfidentialClient(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        try {
            new ClientSecretCredentialBuilder().clientId(CLIENT_ID).clientSecret(secret).build();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("tenantId"));
        }
        try {
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientSecret(secret).build();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("clientId"));
        }
        try {
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).build();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("clientSecret"));
        }
    }
}
