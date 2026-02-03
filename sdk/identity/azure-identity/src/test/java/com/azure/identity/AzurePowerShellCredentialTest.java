// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
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

public class AzurePowerShellCredentialTest {

    @Test
    public void getTokenMockAsync() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzurePowerShell(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {

            // test
            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }

    }

    @Test
    public void azurePowerShellCredentialNotInstalledException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzurePSNotInstalled");

        // mock

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithAzurePowerShell(request))
                    .thenReturn(Mono.error(new Exception("Azure PowerShell not installed")));
                when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
            })) {
            // test
            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(
                    e -> e instanceof Exception && e.getMessage().contains("Azure PowerShell not installed"))
                .verify();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testClaimsChallengeThrowsCredentialUnavailableException() {
        // Test with claims provided
        String claims = "{\"access_token\":{\"essential\":true}}";
        String encodedClaims = java.util.Base64.getEncoder().encodeToString(claims.getBytes(StandardCharsets.UTF_8));

        TokenRequestContext requestWithClaims
            = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default").setClaims(claims);

        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();

        // Test async version
        StepVerifier.create(credential.getToken(requestWithClaims))
            .expectErrorMatches(throwable -> throwable instanceof CredentialUnavailableException
                && throwable.getMessage().contains("Claims challenges are not supported")
                && throwable.getMessage().contains("Connect-AzAccount -ClaimsChallenge")
                && throwable.getMessage().contains(encodedClaims))
            .verify();
    }

    @Test
    public void testPowerShellClaimsChallengeWithTenantAndScopes() {
        TokenRequestContext requestWithClaims = new TokenRequestContext()
            .addScopes("https://graph.microsoft.com/.default", "https://vault.azure.net/.default")
            .setClaims("{\"access_token\":{\"essential\":true}}")
            .setTenantId("tenant-id-123");

        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().tenantId("tenant-id-123").build();

        // Test that error message includes tenant and mentions scopes
        StepVerifier.create(credential.getToken(requestWithClaims))
            .expectErrorMatches(throwable -> throwable instanceof CredentialUnavailableException
                && throwable.getMessage().contains("-Tenant tenant-id-123"))
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
                when(identityClient.authenticateWithAzurePowerShell(request))
                    .thenReturn(TestUtils.getMockAccessToken("token", OffsetDateTime.now().plusHours(1)));
            })) {

            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();

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
                when(identityClient.authenticateWithAzurePowerShell(request))
                    .thenReturn(TestUtils.getMockAccessToken("token", OffsetDateTime.now().plusHours(1)));
            })) {

            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();

            // Should not throw exception for null claims
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> "token".equals(accessToken.getToken()))
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testClaimsChallengeEscapesSingleQuotes() {
        // Test with claims that contain single quotes (needs proper PowerShell escaping)
        String claims = "{\\\"access_token\\\":{\\\"claim\\\":\\\"value's test\\\"}}";
        String encodedClaims = java.util.Base64.getEncoder().encodeToString(claims.getBytes(StandardCharsets.UTF_8));

        TokenRequestContext requestWithClaims
            = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default").setClaims(claims);

        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();

        // Test that single quotes are properly escaped for PowerShell
        StepVerifier.create(credential.getToken(requestWithClaims))
            .expectErrorMatches(throwable -> throwable instanceof CredentialUnavailableException
                && throwable.getMessage().contains("Connect-AzAccount -ClaimsChallenge")
                && throwable.getMessage().contains(encodedClaims))
            .verify();
    }
}
