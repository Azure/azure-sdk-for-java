// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.util.TestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class AzureCliCredentialTest {

    @Test
    public void getTokenMockAsync() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureCliCredentialWinAzureCLINotInstalledException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotInstalled");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(Mono.error(new Exception("Azure CLI not installed")));
                when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("Azure CLI not installed"))
                .verify();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureCliCredentialAzNotLogInException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotLogin");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(Mono.error(new Exception("Azure not Login")));
                when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("Azure not Login"))
                .verify();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureCliCredentialAuthenticationFailedException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureCliCredentialAuthenticationFailed");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(Mono.error(new Exception("other error")));
                when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("other error"))
                .verify();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testAdditionalTenantNoImpact() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AzureCliCredential credential = new AzureCliCredentialBuilder().additionallyAllowedTenants("RANDOM").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException)
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AzureCliCredential credential = new AzureCliCredentialBuilder().tenantId("tenant").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AzureCliCredential credential = new AzureCliCredentialBuilder().tenantId("tenant")
            .additionallyAllowedTenants(IdentityUtil.ALL_TENANTS)
            .build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException)
            .verify();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "test",
            "TEST",
            "Test123",
            "sub-123",
            "sub_456",
            "sub.name",
            "123456",
            "a.b-c_d",
            "A.B-C_D",
            "0-9a-zA-Z_",
            "valid.subscription",
            " " })
    public void testSubscriptionAccepted(String subscription) {
        // Setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default");

        // Mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(Mono.error(new Exception("other error")));
                when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {

            AzureCliCredential credential = new AzureCliCredentialBuilder().subscription(subscription).build();

            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("other error"))
                .verify();

            Assertions.assertNotNull(identityClientMock);
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "@",
            "#",
            "$",
            "%",
            "^",
            "&",
            "*",
            "(",
            ")",
            "+",
            "=",
            "/",
            ",",
            ";",
            ":",
            "<",
            ">",
            "?",
            "!",
            "[",
            "]",
            "{",
            "}" })
    public void testInvalidSubscriptionRejected(String invalidChar) {
        String invalidSubscription = "test" + invalidChar + "sub";

        // Expect validation exception when an invalid subscription name is used
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new AzureCliCredentialBuilder().subscription(invalidSubscription));
    }

    @Test
    public void testClaimsChallengeThrowsCredentialUnavailableException() {
        // Test with claims provided
        String claims = "{\"access_token\":{\"essential\":true}}";
        String encodedClaims = java.util.Base64.getEncoder().encodeToString(claims.getBytes(StandardCharsets.UTF_8));
        TokenRequestContext requestWithClaims
            = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default").setClaims(claims);

        AzureCliCredential credential = new AzureCliCredentialBuilder().build();

        // Test async version
        StepVerifier.create(credential.getToken(requestWithClaims))
            .expectErrorMatches(throwable -> throwable instanceof CredentialUnavailableException
                && throwable.getMessage().contains("Claims challenges are not supported")
                && throwable.getMessage().contains("az login --claims-challenge")
                && throwable.getMessage().contains(encodedClaims))
            .verify();

        // Test sync version
        CredentialUnavailableException exception = Assertions.assertThrows(CredentialUnavailableException.class,
            () -> credential.getTokenSync(requestWithClaims));

        Assertions.assertTrue(exception.getMessage().contains("Claims challenges are not supported"));
        Assertions.assertTrue(exception.getMessage().contains("az login --claims-challenge"));
        Assertions.assertTrue(exception.getMessage().contains(encodedClaims));
    }

    @Test
    public void testClaimsChallengeWithTenantAndScopes() {
        TokenRequestContext requestWithClaims = new TokenRequestContext()
            .addScopes("https://graph.microsoft.com/.default", "https://vault.azure.net/.default")
            .setClaims("{\"access_token\":{\"essential\":true}}")
            .setTenantId("tenant-id-123");

        AzureCliCredential credential = new AzureCliCredentialBuilder().tenantId("tenant-id-123").build();

        // Test that error message includes tenant and scopes
        StepVerifier.create(credential.getToken(requestWithClaims))
            .expectErrorMatches(throwable -> throwable instanceof CredentialUnavailableException
                && throwable.getMessage().contains("--tenant tenant-id-123")
                && throwable.getMessage().contains("--scope")
                && throwable.getMessage().contains("https://graph.microsoft.com/.default"))
            .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t", "\n" })
    public void testEmptyClaimsDoesNotThrowException(String claims) {
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default").setClaims(claims);

        // Mock successful token acquisition for empty claims
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(TestUtils.getMockAccessToken("token", OffsetDateTime.now().plusHours(1)));
            })) {

            AzureCliCredential credential = new AzureCliCredentialBuilder().build();

            // Should not throw exception for empty/whitespace claims
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> "token".equals(accessToken.getToken()))
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testNullClaimsDoesNotThrowException() {
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default").setClaims(null);

        // Mock successful token acquisition for null claims
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzureCli(request))
                    .thenReturn(TestUtils.getMockAccessToken("token", OffsetDateTime.now().plusHours(1)));
            })) {

            AzureCliCredential credential = new AzureCliCredentialBuilder().build();

            // Should not throw exception for null claims
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> "token".equals(accessToken.getToken()))
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }
}
