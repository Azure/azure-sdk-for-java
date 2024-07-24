// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzurePowerShell(request))
                .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        })) {

            // test
            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                    .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                            && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                    .verifyComplete();
            assertNotNull(identityClientMock);
        }

    }

    @Test
    public void azurePowerShellCredentialNotInstalledException() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzurePSNotInstalled");

        // mock

        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
            when(identityClient.authenticateWithAzurePowerShell(request))
                .thenReturn(Mono.error(new Exception("Azure PowerShell not installed")));
            when(identityClient.getIdentityClientOptions()).thenReturn(new IdentityClientOptions());
        })) {
            // test
            AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request))
                .expectErrorMatches(e -> e instanceof Exception && e.getMessage()
                    .contains("Azure PowerShell not installed"))
                .verify();
            assertNotNull(identityClientMock);
        }
    }

    @Test
    @LiveOnly
    public void azurePowerShellCredentialLiveTest() {
        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
        TokenRequestContext request = new TokenRequestContext().addScopes("https://vault.azure.net/.default");
        StepVerifier.create(credential.getToken(request))
            .assertNext(accessToken -> {
                assertNotNull(accessToken.getToken());
                assertNotNull(accessToken.getExpiresAt());
            })
            .verifyComplete();
    }
}
