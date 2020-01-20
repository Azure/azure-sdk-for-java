// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import reactor.test.StepVerifier;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*" })
public class AuthFileCredentialTests<T> {

    // @Test
    // public void SdkAuthFileEnsureCredentialParsesCorrectly() throws Exception {
        //ClientId,TenantId,ClientSecret in ClientSecretCredential is privated 
    // }

    @Test
    public void BadSdkAuthFilePathThrowsDuringGetToken()
    {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        // test
        AuthFileCredential credential =new AuthFileCredentialBuilder().filePath("Bougs*File*Path").build(); 
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof Exception && "Error parsing SDK Auth File".equals(e.getMessage()))
            .verify();  
    }
}
