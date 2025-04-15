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
}
