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
public class AzureCliCredentialTest {


    @Test
    public void getTokenMockAsync() throws Exception {   
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("resourcename");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                        && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
    }

    @Test
    public void azureCliCredentialWinAzureCLINotInstalledException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotInstalled");
 
        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzureCli(request))
            .thenReturn(Mono.error(new Exception("Azure CLI not installed")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);
 
        // test
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("Azure CLI not installed"))
            .verify();
    }

    @Test
    public void azureCliCredentialAzNotLogInException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureNotLogin");
  
        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(Mono.error(new Exception("Azure not Login")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("Azure not Login"))
            .verify();
    }
    
    @Test
    public void azureCliCredentialAuthenticationFailedException() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("AzureCliCredentialAuthenticationFailed");
  
        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzureCli(request))
                .thenReturn(Mono.error(new Exception("other error")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        AzureCliCredential credential = new AzureCliCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof Exception && e.getMessage().contains("other error"))
            .verify();
    }

}
