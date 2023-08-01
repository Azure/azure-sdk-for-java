// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithBrowserInteraction(eq(request1), eq(port), eq(null), eq(null))).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
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
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().port(port).clientId(CLIENT_ID).build();
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

        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1), eq(port), eq(null), eq(null))).thenReturn(TestUtils.getMockMsalTokenSync(token1, expiresAt));
            when(identitySyncClient.authenticateWithPublicClientCache(any(), any()))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder().port(port).clientId(CLIENT_ID).build();
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
    public void testValidInteractiveCAE() throws Exception {
        Random random = new Random();

        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com").setEnableCae(true);
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net").setEnableCae(true);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        int port = random.nextInt(10000) + 10000;

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithBrowserInteraction(eq(request1), eq(port), eq(null), eq(null))).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
            when(identityClient.authenticateWithPublicClientCache(any(), any()))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))
                        && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().port(port).clientId(CLIENT_ID).build();
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

        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1), eq(port), eq(null), eq(null))).thenReturn(TestUtils.getMockMsalTokenSync(token1, expiresAt));
            when(identitySyncClient.authenticateWithPublicClientCache(any(), any()))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))
                        && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder().port(port).clientId(CLIENT_ID).build();
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
    public void testValidInteractiveViaRedirectUri() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        String redirectUrl = "http://localhost:3761";

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithBrowserInteraction(eq(request1), eq(null), eq(redirectUrl), eq(null))).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
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
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().redirectUrl(redirectUrl).clientId(CLIENT_ID).build();
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

        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1), eq(null), eq(redirectUrl), eq(null))).thenReturn(TestUtils.getMockMsalTokenSync(token1, expiresAt));
            when(identitySyncClient.authenticateWithPublicClientCache(any(), any()))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().redirectUrl(redirectUrl).clientId(CLIENT_ID).build();
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
    public void testValidInteractiveWithLoginHint() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        String username = "user@foo.com";

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithBrowserInteraction(eq(request1), eq(null), eq(null), eq(username))).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
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
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().loginHint(username).clientId(CLIENT_ID).build();
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

        try (MockedConstruction<IdentitySyncClient> identityClientMock = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
            when(identitySyncClient.authenticateWithBrowserInteraction(eq(request1), eq(null), eq(null), eq(username))).thenReturn(TestUtils.getMockMsalTokenSync(token1, expiresAt));
            when(identitySyncClient.authenticateWithPublicClientCache(any(), any()))
                .thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                    }
                });
        })) {
            // test
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().loginHint(username).clientId(CLIENT_ID).build();
            AccessToken accessToken = credential.getTokenSync(request1);
            Assert.assertEquals(token1, accessToken.getToken());
            Assert.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            accessToken = credential.getTokenSync(request2);
            Assert.assertEquals(token2, accessToken.getToken());
            Assert.assertTrue(expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCredentialDoesnWorkWIthPortAndRedirectUrlConfigured() throws Exception {
        // setup
        new InteractiveBrowserCredentialBuilder()
            .clientId(CLIENT_ID)
            .port(8080)
            .redirectUrl("http://localhost:8080")
            .build();
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
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithBrowserInteraction(eq(request1), eq(port), eq(null), eq(null)))
                .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
        })) {
            // test
            InteractiveBrowserCredential credential =
                new InteractiveBrowserCredentialBuilder().port(port).clientId(CLIENT_ID).build();
            StepVerifier.create(credential.authenticate(request1))
                .expectNextMatches(authenticationRecord -> authenticationRecord.getAuthority()
                    .equals("http://login.microsoftonline.com")
                    && authenticationRecord.getUsername().equals("testuser")
                    && authenticationRecord.getHomeAccountId() != null)
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidMultiTenantAuth() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        InteractiveBrowserCredential credential =
            new InteractiveBrowserCredentialBuilder().tenantId("tenant").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }
}
