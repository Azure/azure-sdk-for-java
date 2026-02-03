// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class OnBehalfOfCredentialTest {

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
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithOBO(request1))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
                when(identityClient.authenticateWithOBO(request2))
                    .thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
            })) {
            // test
            OnBehalfOfCredential credential = new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret(secret)
                .additionallyAllowedTenants("*")
                .build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            StepVerifier.create(credential.getToken(request2))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }

        // mock
        try (MockedConstruction<IdentitySyncClient> identityClientMock
            = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithConfidentialClientCache(any()))
                    .thenThrow(new IllegalStateException("Test"));
                when(identitySyncClient.authenticateWithOBO(request1))
                    .thenReturn(TestUtils.getMockAccessTokenSync(token1, expiresAt));
                when(identitySyncClient.authenticateWithOBO(request2))
                    .thenReturn(TestUtils.getMockAccessTokenSync(token2, expiresAt));
            })) {
            // test
            OnBehalfOfCredential credential = new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret(secret)
                .additionallyAllowedTenants("*")
                .build();

            AccessToken accessToken = credential.getTokenSync(request1);
            Assertions.assertEquals(token1, accessToken.getToken());
            Assertions.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());

            accessToken = credential.getTokenSync(request2);
            Assertions.assertEquals(token2, accessToken.getToken());
            Assertions.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(identityClientMock);
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
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithOBO(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {
            // test
            try {
                new OnBehalfOfCredentialBuilder().clientId(CLIENT_ID)
                    .clientSecret(secret)
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("tenantId"));
            }
            try {
                new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                    .clientSecret(secret)
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("clientId"));
            }
            try {
                new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                    .clientId(CLIENT_ID)
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("client secret"));
            }

            try {
                new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                    .clientId(CLIENT_ID)
                    .clientSecret("testSecret")
                    .clientAssertion(() -> "testAssertion")
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("Exactly one of client secret"));
            }

            try {
                new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                    .clientId(CLIENT_ID)
                    .pemCertificate("testPath")
                    .clientAssertion(() -> "testAssertion")
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("Exactly one of client secret"));
            }

            try {
                new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
                    .clientId(CLIENT_ID)
                    .clientSecret("testSecret")
                    .pemCertificate("testPath")
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("Exactly one of client secret"));
            }
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidAdditionalTenant() throws Exception {
        // setup
        String badSecret = "badsecret";

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        OnBehalfOfCredential credential = new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
            .clientId(CLIENT_ID)
            .userAssertion("assertion")
            .clientSecret(badSecret)
            .additionallyAllowedTenants("RANDOM")
            .build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        OnBehalfOfCredential credential = new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
            .clientId(CLIENT_ID)
            .clientSecret(badSecret)
            .userAssertion("assertion")
            .build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        OnBehalfOfCredential credential = new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
            .clientId(CLIENT_ID)
            .clientSecret(badSecret)
            .userAssertion("assertion")
            .additionallyAllowedTenants("*")
            .build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof MsalServiceException)
            .verify();
    }
}
