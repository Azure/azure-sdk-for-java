// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.ConfidentialClient;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.MsalServiceException;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class ClientSecretCredentialTest {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testValidSecrets() {
        // setup
        String secret = "secret";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<ConfidentialClient> identityClientMock
            = mockConstruction(ConfidentialClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithCache(any())).thenThrow(new IllegalStateException("Test"));
                when(identitySyncClient.authenticate(request1))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
                when(identitySyncClient.authenticate(request2))
                    .thenReturn(TestUtils.getMockAccessToken(token2, expiresAt));
            })) {
            // test
            ClientSecretCredential credential = new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret(secret)
                .additionallyAllowedTenants("*")
                .build();

            AccessToken accessToken = credential.getToken(request1);
            Assertions.assertEquals(token1, accessToken.getToken());
            Assertions.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());

            accessToken = credential.getToken(request2);
            Assertions.assertEquals(token2, accessToken.getToken());
            Assertions.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testValidSecretsCAE() {
        // setup
        String secret = "secret";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1
            = new TokenRequestContext().addScopes("https://management.azure.com").setCaeEnabled(true);
        TokenRequestContext request2
            = new TokenRequestContext().addScopes("https://vault.azure.net").setCaeEnabled(true);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<ConfidentialClient> identityClientMock
            = mockConstruction(ConfidentialClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithCache(any())).thenThrow(new IllegalStateException("Test"));
                when(identitySyncClient.authenticate(request1)).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalToken(token1, expiresAt);
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
                when(identitySyncClient.authenticate(request2)).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
            })) {
            // test
            ClientSecretCredential credential = new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret(secret)
                .additionallyAllowedTenants("*")
                .build();

            AccessToken accessToken = credential.getToken(request1);
            Assertions.assertEquals(token1, accessToken.getToken());
            Assertions.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());

            accessToken = credential.getToken(request2);
            Assertions.assertEquals(token2, accessToken.getToken());
            Assertions.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidSecrets() {
        // setup
        String secret = "secret";
        String badSecret = "badsecret";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        try (MockedConstruction<ConfidentialClient> identityClientMock
            = mockConstruction(ConfidentialClient.class, (confidentialClient, context) -> {
                when(confidentialClient.authenticateWithCache(any())).thenThrow(new IllegalStateException("Test"));
                when(confidentialClient.authenticate(request))
                    .thenThrow(new MsalServiceException("bad secret", "BadSecret"));
            })) {
            // test
            ClientSecretCredential credential = new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret(badSecret)
                .additionallyAllowedTenants("*")
                .build();

            CredentialAuthenticationException ex
                = assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
            assertTrue(ex.getMessage().startsWith("bad secret"));

            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidParameters() {
        // setup
        String secret = "secret";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<ConfidentialClient> identityClientMock
            = mockConstruction(ConfidentialClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithCache(any())).thenReturn(null);
                when(identityClient.authenticate(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {
            // test
            try {
                new ClientSecretCredentialBuilder().clientId(CLIENT_ID)
                    .clientSecret(secret)
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("tenantId"));
            }
            try {
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                    .clientSecret(secret)
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("clientId"));
            }
            try {
                new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                    .clientId(CLIENT_ID)
                    .additionallyAllowedTenants("*")
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("clientSecret"));
            }
            Assertions.assertNotNull(identityClientMock);
        }
    }
}
