// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class AzurePowerShellCredentialTest {

    @Test
    public void getTokenMockAsync() throws Exception {   
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzurePowerShell(request))
                .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                        && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
    }

    @Test
    public void azurePowerShellCredentialNotInstalledException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzurePSNotInstalled");
 
        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzurePowerShell(request))
            .thenReturn(Mono.error(new Exception("Azure PowerShell not installed")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);
 
        // test
        AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof Exception && e.getMessage()
                .contains("Azure PowerShell not installed"))
            .verify();
    }
}
