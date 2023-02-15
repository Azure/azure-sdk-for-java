// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        try (MockedConstruction<IdentityClient> identityclientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAuthorizationCode(eq(request1), eq(authCode1), eq(redirectUri)))
                .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
            when(identityClient.authenticateWithPublicClientCache(any(), any()))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {

            // test
            AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
                .clientId(clientId).authorizationCode(authCode1).redirectUrl(redirectUri.toString()).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            StepVerifier.create(credential.getToken(request2))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityclientMock);
        }
    }

    @Test
    public void testInvalidAdditionalTenant() throws Exception {
        // setup
        String badSecret = "badsecret";
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        AuthorizationCodeCredential credential =
            new AuthorizationCodeCredentialBuilder().tenantId("tenant").clientId("clientId").clientSecret(badSecret)
                .redirectUrl(redirectUri.toString()).authorizationCode(authCode1)
                .additionallyAllowedTenants("RANDOM").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        AuthorizationCodeCredential credential =
            new AuthorizationCodeCredentialBuilder().tenantId("tenant").clientId("clientId").clientSecret(badSecret)
                .authorizationCode(authCode1).redirectUrl(redirectUri.toString()).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() throws Exception {
        // setup
        String badSecret = "badsecret";
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        AuthorizationCodeCredential credential =
            new AuthorizationCodeCredentialBuilder().tenantId("tenant").clientId("clientId").clientSecret(badSecret)
                .additionallyAllowedTenants("*").authorizationCode(authCode1)
                .redirectUrl(redirectUri.toString()).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e.getCause() instanceof MsalServiceException)
            .verify();
    }
}
