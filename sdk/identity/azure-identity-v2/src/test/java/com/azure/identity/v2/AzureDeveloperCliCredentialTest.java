// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.client.DevToolslClient;
import com.azure.identity.v2.implementation.models.DevToolsClientOptions;
import com.azure.identity.v2.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class AzureDeveloperCliCredentialTest {

    @Test
    public void getTokenMock() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<DevToolslClient> devToolsClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureDeveloperCli(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {
            // test
            AzureDeveloperCliCredential credential = new AzureDeveloperCliCredentialBuilder().build();
            AccessToken accessToken = credential.getToken(request);
            Assertions.assertTrue(token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond());

            Assertions.assertNotNull(devToolsClientMock);
        }
    }

    @Test
    public void azureDeveloperCliCredentialWinAzureCLINotInstalledException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotInstalled");

        // mock
        try (MockedConstruction<DevToolslClient> identityClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureDeveloperCli(request))
                    .thenThrow(new Exception("Azure CLI not installed"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzureDeveloperCliCredential credential = new AzureDeveloperCliCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureDeveloperCliCredentialAzNotLogInException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotLogin");

        // mock
        try (MockedConstruction<DevToolslClient> identityClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureDeveloperCli(request))
                    .thenThrow(new Exception("Azure not Login"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzureDeveloperCliCredential credential = new AzureDeveloperCliCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azureDeveloperCliCredentialAuthenticationFailedException() {
        // setup
        TokenRequestContext request
            = new TokenRequestContext().addScopes("AzureDeveloperCliCredentialAuthenticationFailed");

        // mock
        try (MockedConstruction<DevToolslClient> identityClientMock
            = mockConstruction(DevToolslClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzureDeveloperCli(request))
                    .thenThrow(new Exception("other error"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzureDeveloperCliCredential credential = new AzureDeveloperCliCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
            Assertions.assertNotNull(identityClientMock);
        }
    }
}
