// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*" })
public class AuthFileCredentialTests<T> {

     @Test
     public void SdkAuthFileEnsureCredentialParsesCorrectly() throws Exception {
        // setup 
        String authFilePath = getClass().getResource("/authfile.json").getPath();
        AuthFileCredential credential = new AuthFileCredentialBuilder().filePath(authFilePath).build();

        // test
        ClientSecretCredential clientSecretCredential= (ClientSecretCredential) credential.credential;
        Assert.assertEquals("mockclientsecret", clientSecretCredential.clientSecret);
     }

    @Test
    public void BadSdkAuthFilePathThrowsDuringGetToken()
    {
        // test
        try{
        AuthFileCredential credential = new AuthFileCredentialBuilder().filePath("Bougs*File*Path").build();
        }catch(RuntimeException  ex){
            Assert.assertEquals(ex.getMessage(),  "Error parsing SDK Auth File");
        } 
    }
}
