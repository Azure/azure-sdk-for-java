// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
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
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureCliCredentialWinAzureCLINotInstalledException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotInstalled");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(Mono.error(new Exception("Azure CLI not installed")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("Azure CLI not installed"))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureCliCredentialAzNotLogInException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotLogin");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(Mono.error(new Exception("Azure not Login")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("Azure not Login"))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureCliCredentialAuthenticationFailedException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureCliCredentialAuthenticationFailed");

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(Mono.error(new Exception("other error")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("other error"))
                .verify();
            Assert.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testAdditionalTenantNoImpact() {
        // setup
        String username = "testuser";
        String password = "P@ssw0rd";

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        AzureCliCredential credential =
            new AzureCliCredentialBuilder().additionallyAllowedTenants("RANDOM").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof CredentialUnavailableException)
            .verify();
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        AzureCliCredential credential =
            new AzureCliCredentialBuilder().tenantId("tenant").build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException && (e.getMessage().startsWith("The current credential is not configured to")))
            .verify();
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup

        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
            .setTenantId("newTenant");

        AzureCliCredential credential =
            new AzureCliCredentialBuilder().tenantId("tenant")
                .additionallyAllowedTenants(IdentityUtil.ALL_TENANTS).build();

        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e  instanceof CredentialUnavailableException)
            .verify();
    }
}
