// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.client.PublicClient;
import com.azure.v2.identity.models.AuthenticationRecord;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class InteractiveBrowserCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testValidInteractive() throws Exception {
        Random random = new Random();

        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        int port = random.nextInt(10000) + 10000;

        // mock

        try (MockedConstruction<PublicClient> identityClientMock
            = mockConstruction(PublicClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1)))
                    .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(identitySyncClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
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
            InteractiveBrowserCredential credential
                = new InteractiveBrowserCredentialBuilder().clientId(CLIENT_ID).build();
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
    public void testValidInteractiveCAE() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1
            = new TokenRequestContext().addScopes("https://management.azure.com").setCaeEnabled(true);
        TokenRequestContext request2
            = new TokenRequestContext().addScopes("https://vault.azure.net").setCaeEnabled(true);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        try (MockedConstruction<PublicClient> identityClientMock
            = mockConstruction(PublicClient.class, (publicClient, context) -> {
                when(publicClient.authenticateWithBrowserInteraction(eq(request1)))
                    .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(publicClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request2.getScopes().get(0))
                        && argument.isCaeEnabled()) {
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
            InteractiveBrowserCredential credential
                = new InteractiveBrowserCredentialBuilder().clientId(CLIENT_ID).build();
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
    public void testValidInteractiveViaRedirectUri() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        String redirectUrl = "http://localhost:3761";

        try (MockedConstruction<PublicClient> identityClientMock
            = mockConstruction(PublicClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1)))
                    .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(identitySyncClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
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
            InteractiveBrowserCredential credential
                = new InteractiveBrowserCredentialBuilder().redirectUrl(redirectUrl).clientId(CLIENT_ID).build();
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
    public void testValidInteractiveWithLoginHint() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        String username = "user@foo.com";

        // mock
        try (MockedConstruction<PublicClient> identityClientMock
            = mockConstruction(PublicClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1)))
                    .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(identitySyncClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
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
            InteractiveBrowserCredential credential
                = new InteractiveBrowserCredentialBuilder().loginHint(username).clientId(CLIENT_ID).build();
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
    public void testValidAuthenticate() throws Exception {
        Random random = new Random();

        // setup
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        int port = random.nextInt(10000) + 10000;

        // mock
        try (MockedConstruction<PublicClient> identityClientMock
            = mockConstruction(PublicClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithBrowserInteraction(eq(request1)))
                    .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
            })) {
            // test
            InteractiveBrowserCredential credential
                = new InteractiveBrowserCredentialBuilder().clientId(CLIENT_ID).build();
            AuthenticationRecord authenticationRecord = credential.authenticate(request1);

            Assertions.assertTrue(authenticationRecord.getAuthority().equals("http://login.microsoftonline.com")
                && authenticationRecord.getUsername().equals("testuser")
                && authenticationRecord.getHomeAccountId() != null);
            Assertions.assertNotNull(identityClientMock);
        }
    }
}
