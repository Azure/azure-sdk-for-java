// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import java.lang.reflect.Field;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import org.junit.Assert;
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

    @Test
    public void sdkAuthFileEnsureCredentialParsesCorrectly() throws Exception {
        // setup
        String authFilePath = getClass().getResource("/authfile.json").getPath();
        Field credential = Class.forName("com.azure.identity.AuthFileCredential").getDeclaredField("credential");
        credential.setAccessible(true);
        AuthFileCredential authFileCredential = new AuthFileCredentialBuilder().filePath(authFilePath).build();

        // test
        authFileCredential.ensureCredential();
        Field  clientSecret= Class.forName("com.azure.identity.ClientSecretCredential").getDeclaredField("clientSecret");
        clientSecret.setAccessible(true);
        ClientSecretCredential clientSecretCredential= (ClientSecretCredential) credential.get(authFileCredential);
        Assert.assertNotNull(clientSecretCredential);
        Assert.assertEquals("mockclientsecret",(String)clientSecret.get(clientSecretCredential));
     }

    @Test
    public void badSdkAuthFilePathThrowsDuringGetToken()
    {
        // setup 
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        // test
        AuthFileCredential credential =new AuthFileCredentialBuilder().filePath("Bougs*File*Path").build(); 
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof Exception && "Error parsing SDK Auth File".equals(e.getMessage()))
            .verify();  
    }
}
