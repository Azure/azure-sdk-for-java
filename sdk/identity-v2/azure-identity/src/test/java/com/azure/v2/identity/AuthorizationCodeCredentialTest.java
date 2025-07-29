// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.PublicClient;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class AuthorizationCodeCredentialTest {

    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidAuthorizationCode() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<PublicClient> identityclientMock
            = mockConstruction(PublicClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAuthorizationCode(eq(request1)))
                    .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(identityClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        throw new UnsupportedOperationException("nothing cached");
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
            })) {

            // test
            AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder().clientId(clientId)
                .authorizationCode(authCode1)
                .redirectUrl(redirectUri.toString())
                .build();
            AccessToken accessToken = credential.getToken(request1);
            Assertions.assertTrue(token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            accessToken = credential.getToken(request2);
            Assertions.assertTrue(token2.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(identityclientMock);
        }
    }

    @Test
    public void testInvalidAdditionalTenant() throws Exception {
        // setup
        String badSecret = "badsecret";
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder().tenantId("tenant")
            .clientId("clientId")
            .clientSecret(badSecret)
            .redirectUrl(redirectUri.toString())
            .authorizationCode(authCode1)
            .additionallyAllowedTenants("RANDOM")
            .build();

        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder().tenantId("tenant")
            .clientId("clientId")
            .clientSecret(badSecret)
            .authorizationCode(authCode1)
            .redirectUrl(redirectUri.toString())
            .build();
        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @Test
    public void testValidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder().tenantId("tenant")
            .clientId("clientId")
            .clientSecret(badSecret)
            .additionallyAllowedTenants("*")
            .authorizationCode(authCode1)
            .redirectUrl(redirectUri.toString())
            .build();

        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }
}
