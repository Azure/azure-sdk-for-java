// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

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
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            when(identityClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
        })) {
            // test
            ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret)
                    .additionallyAllowedTenants("*").build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            StepVerifier.create(credential.getToken(request2))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }

        // mock
        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithConfidentialClientCache(any())).thenThrow(new IllegalStateException("Test"));
            when(identitySyncClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
            when(identitySyncClient.authenticateWithConfidentialClient(request2)).thenReturn(TestUtils.getMockAccessTokenSync(token2, expiresAt));
        })) {
            // test
            ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret)
                    .additionallyAllowedTenants("*").build();

            AccessToken accessToken = credential.getTokenSync(request1);
            Assert.assertEquals(token1, accessToken.getToken());
            Assert.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());

            accessToken = credential.getTokenSync(request2);
            Assert.assertEquals(token2, accessToken.getToken());
            Assert.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testValidSecretsCAE() throws Exception {
        // setup
        String secret = "secret";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com").setEnableCae(true);
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net").setEnableCae(true);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalToken(token1, expiresAt);
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
            when(identityClient.authenticateWithConfidentialClient(request2))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret)
                    .additionallyAllowedTenants("*").build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            StepVerifier.create(credential.getToken(request2))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }

        // mock
        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithConfidentialClientCache(any())).thenThrow(new IllegalStateException("Test"));
            when(identitySyncClient.authenticateWithConfidentialClient(request1))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalTokenSync(token1, expiresAt);
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
            when(identitySyncClient.authenticateWithConfidentialClient(request2))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret)
                    .additionallyAllowedTenants("*").build();

            AccessToken accessToken = credential.getTokenSync(request1);
            Assert.assertEquals(token1, accessToken.getToken());
            Assert.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());

            accessToken = credential.getTokenSync(request2);
            Assert.assertEquals(token2, accessToken.getToken());
            Assert.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidSecrets() throws Exception {
        // setup
        String secret = "secret";
        String badSecret = "badsecret";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request)).thenReturn(Mono.error(new MsalServiceException("bad secret", "BadSecret")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(badSecret)
                    .additionallyAllowedTenants("*").build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof MsalServiceException && "bad secret".equals(e.getMessage()))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }

        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithConfidentialClientCache(any())).thenThrow(new IllegalStateException("Test"));
            when(identitySyncClient.authenticateWithConfidentialClient(request)).thenThrow(new MsalServiceException("bad secret", "BadSecret"));
            when(identitySyncClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(badSecret)
                    .additionallyAllowedTenants("*").build();
            try {
                credential.getTokenSync(request);
            } catch (Exception e) {
                Assert.assertTrue(e instanceof MsalServiceException && "bad secret".equals(e.getMessage()));
            }
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String secret = "secret";
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
                new ClientSecretCredentialBuilder().clientId(CLIENT_ID).clientSecret(secret)
                    .additionallyAllowedTenants("*").build();
                fail();
            } catch (IllegalArgumentException e) {
                Assert.assertTrue(e.getMessage().contains("tenantId"));
            }
            try {
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientSecret(secret)
                    .additionallyAllowedTenants("*").build();
                fail();
            } catch (IllegalArgumentException e) {
                Assert.assertTrue(e.getMessage().contains("clientId"));
            }
            try {
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID)
                    .additionallyAllowedTenants("*").build();
                fail();
            } catch (IllegalArgumentException e) {
                Assert.assertTrue(e.getMessage().contains("clientSecret"));
            }
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidAdditionalTenant() throws Exception {
        // setup
        String badSecret = "badsecret";

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        ClientSecretCredential credential =
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(badSecret)
                .additionallyAllowedTenants("RANDOM").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        ClientSecretCredential credential =
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(badSecret).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        ClientSecretCredential credential =
            new ClientSecretCredentialBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(badSecret)
                .additionallyAllowedTenants("*").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof MsalServiceException)
            .verify();
    }
}
