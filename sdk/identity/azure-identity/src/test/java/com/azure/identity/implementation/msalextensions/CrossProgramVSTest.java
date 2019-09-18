// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.azure.identity.implementation.msalextensions.cachepersister.CachePersister;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/*
 * Before running these tests, log into Azure with the new Visual Studio 16.3.0 Preview 1
 * This should create a msal.cache file in the the user directory, and these tests should be able to read and
 * write from the same file.
 * Note that deleting this cache file will cause the user to have to re-log in with Visual Studio as this will delete
 * the tokens in the cache
 *
 * NOTE: These tests are written assuming that nothing else has written to the MSAL cache besides visual studio
 * */
public class CrossProgramVSTest {

    CachePersister cachePersister;
    PersistentTokenCacheAccessAspect accessAspect;

    private ConfidentialClientApplication confApp;
    private ClientCredentialParameters confParameters;

    private int count = 0;

    @Before
    public void setup() throws Exception {
        org.junit.Assume.assumeTrue("Skipping these tests until we mock or record it", false);
        //using the default cachepersister and accessAspect objects
        cachePersister = new CachePersister.Builder().build();
        accessAspect = new PersistentTokenCacheAccessAspect();

        confApp = ConfidentialClientApplication.builder(TestConfiguration.CONFIDENTIAL_CLIENT_ID,
                ClientCredentialFactory.create(TestConfiguration.CONFIDENTIAL_CLIENT_SECRET))
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        confParameters = ClientCredentialParameters.builder(
                Collections.singleton(TestConfiguration.GRAPH_DEFAULT_SCOPE))
                .build();
    }

    @Test
    public void readCacheAfterVSAzureLogin() {
        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        Assert.assertTrue(jsonObj.has("AccessToken"));
        Assert.assertTrue(jsonObj.has("RefreshToken"));
        Assert.assertTrue(jsonObj.has("IdToken"));
        Assert.assertTrue(jsonObj.has("Account"));
        Assert.assertTrue(jsonObj.has("AppMetadata"));

        System.out.println(currJson);

        count = jsonObj.get("AccessToken").getAsJsonObject().keySet().size();
    }

    @Test
    public void writeToSameCacheFileAfterVSAzureLogin() {
        String currJson = new String(cachePersister.readCache());
        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        int set = jsonObj.get("AccessToken").getAsJsonObject().keySet().size();

        CompletableFuture<IAuthenticationResult> result = confApp.acquireToken(confParameters);
        result.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        currJson = new String(cachePersister.readCache());
        jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        int newSet = jsonObj.get("AccessToken").getAsJsonObject().keySet().size();

        Assert.assertEquals(newSet, set + 1);
        count++;

        System.out.println(currJson);
    }

    @Test
    public void countCache() {
        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();
        int newSet = jsonObj.get("AccessToken").getAsJsonObject().keySet().size();
        System.out.println(newSet);
    }

    @Test
    public void readCacheAfterPowershellAzureLogin() {
        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();
        System.out.println(currJson);

        int newSet = jsonObj.get("AccessToken").getAsJsonObject().keySet().size();

        Assert.assertEquals(newSet, 6);
        count++;
    }

}
