// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.client.DevToolslClient;
import com.azure.identity.v2.implementation.models.DevToolsClientOptions;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import com.azure.identity.v2.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class AzureCliCredentialTest {

    @Test
    public void getTokenMock() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<DevToolslClient> devToolsClientMock
            = mockConstruction(DevToolslClient.class, (devToolsClient, context) -> {
                when(devToolsClient.authenticateWithAzureCli(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            AccessToken accessToken = credential.getToken(request);
            Assertions.assertTrue(token1.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(devToolsClientMock);
        }
    }

    @Test
    public void azureCliCredentialWinAzureCLINotInstalledException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotInstalled");

        // mock
        try (MockedConstruction<DevToolslClient> devToolsClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureCli(request))
                    .thenThrow(new Exception("Azure CLI not installed"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
        }
    }

    @Test
    public void azureCliCredentialAzNotLogInException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotLogin");

        // mock
        try (MockedConstruction<DevToolslClient> devToolsClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureCli(request)).thenThrow(new Exception("Azure not Login"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
            Assertions.assertNotNull(devToolsClientMock);
        }
    }

    @Test
    public void azureCliCredentialAuthenticationFailedException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureCliCredentialAuthenticationFailed");

        // mock
        try (MockedConstruction<DevToolslClient> devToolsClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureCli(request)).thenThrow(new Exception("other error"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzureCliCredential credential = new AzureCliCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
            Assertions.assertNotNull(devToolsClientMock);
        }
    }

    @Test
    public void testAdditionalTenantNoImpact() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AzureCliCredential credential = new AzureCliCredentialBuilder().additionallyAllowedTenants("RANDOM").build();
        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @Test
    public void testInvalidMultiTenantAuth() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AzureCliCredential credential = new AzureCliCredentialBuilder().tenantId("tenant").build();
        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
    }

    @Test
    public void testValidMultiTenantAuth() {
        // setup

        TokenRequestContext request
            = new TokenRequestContext().addScopes("https://vault.azure.net/.default").setTenantId("newTenant");

        AzureCliCredential credential = new AzureCliCredentialBuilder().tenantId("tenant")
            .additionallyAllowedTenants(IdentityUtil.ALL_TENANTS)
            .build();

        Assertions.assertThrows(CredentialAuthenticationException.class, () -> credential.getToken(request));
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
        try (MockedConstruction<DevToolslClient> devToolsClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureCli(request)).thenThrow(new Exception("other error"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {

            AzureCliCredential credential = new AzureCliCredentialBuilder().subscription(subscription).build();

            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));

            Assertions.assertNotNull(devToolsClientMock);
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
