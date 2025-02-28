// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class UsernamePasswordCredentialTest {

    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidUserCredential() throws Exception {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithUsernamePassword(request1, fakeUsernamePlaceholder,
                    fakePasswordPlaceholder)).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(identityClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
            })) {
            // test
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
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

        try (MockedConstruction<IdentitySyncClient> identityClientMock
            = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithUsernamePassword(request1, fakeUsernamePlaceholder,
                    fakePasswordPlaceholder)).thenReturn(TestUtils.getMockMsalTokenSync(token1, expiresAt));
                when(identitySyncClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
            })) {
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
                .build();
            // test
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
    public void testValidUserCredentialCAE() throws Exception {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1
            = new TokenRequestContext().addScopes("https://management.azure.com").setCaeEnabled(true);
        TokenRequestContext request2
            = new TokenRequestContext().addScopes("https://vault.azure.net").setCaeEnabled(true);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithUsernamePassword(request1, fakeUsernamePlaceholder,
                    fakePasswordPlaceholder)).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
                when(identityClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request2.getScopes().get(0))
                        && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalToken(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
            })) {
            // test
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
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

        try (MockedConstruction<IdentitySyncClient> identityClientMock
            = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithUsernamePassword(request1, fakeUsernamePlaceholder,
                    fakePasswordPlaceholder)).thenReturn(TestUtils.getMockMsalTokenSync(token1, expiresAt));
                when(identitySyncClient.authenticateWithPublicClientCache(any(), any())).thenAnswer(invocation -> {
                    TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                    if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request2.getScopes().get(0))
                        && argument.isCaeEnabled()) {
                        return TestUtils.getMockMsalTokenSync(token2, expiresAt);
                    } else if (argument.getScopes().size() == 1
                        && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                        return Mono.error(new UnsupportedOperationException("nothing cached"));
                    } else {
                        throw new InvalidUseOfMatchersException(
                            String.format("Argument %s does not match", (Object) argument));
                    }
                });
            })) {
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
                .build();
            // test
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
    public void testInvalidUserCredential() throws Exception {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String badPassword = "Password";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithUsernamePassword(request, fakeUsernamePlaceholder, badPassword))
                    .thenThrow(new MsalServiceException("bad credential", "BadCredential"));
                when(identityClient.authenticateWithPublicClientCache(any(), any()))
                    .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
                when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {
            // test
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(badPassword)
                .build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(t -> t instanceof MsalServiceException && "bad credential".equals(t.getMessage()))
                .verify();
            Assertions.assertNotNull(identityClientMock);
        }

        try (MockedConstruction<IdentitySyncClient> identityClientMock
            = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithUsernamePassword(request, fakeUsernamePlaceholder, badPassword))
                    .thenThrow(new MsalServiceException("bad credential", "BadCredential"));
                when(identitySyncClient.authenticateWithPublicClientCache(any(), any()))
                    .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
                when(identitySyncClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {
            // test
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(badPassword)
                .build();
            try {
                credential.getTokenSync(request);
            } catch (Exception e) {
                Assertions.assertTrue(e instanceof MsalServiceException && "bad credential".equals(e.getMessage()));
            }
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithUsernamePassword(request, fakeUsernamePlaceholder,
                    fakePasswordPlaceholder)).thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
                when(identityClient.authenticateWithPublicClientCache(any(), any()))
                    .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
            })) {
            // test
            try {
                new UsernamePasswordCredentialBuilder().username(fakeUsernamePlaceholder)
                    .password(fakePasswordPlaceholder)
                    .build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("clientId"));
            }
            try {
                new UsernamePasswordCredentialBuilder().clientId(clientId).username(fakeUsernamePlaceholder).build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("password"));
            }
            try {
                new UsernamePasswordCredentialBuilder().clientId(clientId).password(fakePasswordPlaceholder).build();
                fail();
            } catch (IllegalArgumentException e) {
                Assertions.assertTrue(e.getMessage().contains("username"));
            }
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testValidAuthenticate() throws Exception {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithUsernamePassword(eq(request1), eq(fakeUsernamePlaceholder),
                    eq(fakePasswordPlaceholder))).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
            })) {
            // test
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId)
                .username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
                .build();
            StepVerifier.create(credential.authenticate(request1))
                .expectNextMatches(authenticationRecord -> authenticationRecord.getAuthority()
                    .equals("http://login.microsoftonline.com")
                    && authenticationRecord.getUsername().equals("testuser")
                    && authenticationRecord.getHomeAccountId() != null)
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testAdditionalTenantNoImpact() {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        UsernamePasswordCredential credential
            = new UsernamePasswordCredentialBuilder().username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
                .clientId(clientId)
                .additionallyAllowedTenants("RANDOM")
                .build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e.getCause() instanceof MsalServiceException)
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().tenantId("tenant")
            .username(fakeUsernamePlaceholder)
            .password(fakePasswordPlaceholder)
            .clientId(clientId)
            .build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getCause().getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup
        String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
        String fakePasswordPlaceholder = "fakePasswordPlaceholder";

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        UsernamePasswordCredential credential
            = new UsernamePasswordCredentialBuilder().username(fakeUsernamePlaceholder)
                .password(fakePasswordPlaceholder)
                .tenantId("tenant")
                .clientId(clientId)
                .additionallyAllowedTenants(IdentityUtil.ALL_TENANTS)
                .build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e.getCause() instanceof MsalServiceException)
            .verify();
    }
}
