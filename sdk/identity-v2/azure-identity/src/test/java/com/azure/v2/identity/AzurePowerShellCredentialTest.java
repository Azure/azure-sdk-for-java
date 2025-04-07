// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.DevToolsClient;
import com.azure.v2.identity.implementation.models.DevToolsClientOptions;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class AzurePowerShellCredentialTest {

    @Test
    public void getTokenMock() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<DevToolsClient> identityClientMock
            = mockConstruction(DevToolsClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzurePowerShell(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            })) {

            // test
            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
            AccessToken accessToken = credential.getToken(request);
            Assertions.assertTrue(token1.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void azurePowerShellCredentialNotInstalledException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzurePSNotInstalled");

        // mock

        try (MockedConstruction<DevToolsClient> devToolsClientMock
            = mockConstruction(DevToolsClient.class, (devToolslClient, context) -> {
                when(devToolslClient.authenticateWithAzurePowerShell(request))
                    .thenThrow(new CredentialAuthenticationException("Azure PowerShell not installed"));
                when(devToolslClient.getClientOptions()).thenReturn(new DevToolsClientOptions());
            })) {
            // test
            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
            Assertions.assertThrows(Exception.class, () -> credential.getToken(request));
            Assertions.assertNotNull(devToolsClientMock);
        }
    }
}
