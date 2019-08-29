// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.azure.identity.implementation.msalextensions.cachepersister.CachePersister;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.aad.msal4j.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PersistentTokenCacheAccessAspectTest {

    private PersistentTokenCacheAccessAspect accessAspect;
    private CachePersister cachePersister;

    private ConfidentialClientApplication confApp;
    private ConfidentialClientApplication confApp2;
    private PublicClientApplication pubApp;
    private ClientCredentialParameters confParameters;
    private DeviceCodeFlowParameters pubParameters;

    @Before
    public void setup() throws Exception {
        // custom MsalCacheStorage for testing purposes so we don't overwrite the real one
        cachePersister = new CachePersister.Builder()
                .cacheLocation(java.nio.file.Paths.get(System.getProperty("user.home"), "test.cache").toString())
                .build();

        accessAspect = new PersistentTokenCacheAccessAspect(cachePersister);

        Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> {
            System.out.println(deviceCode.message());
        };

        confApp = ConfidentialClientApplication.builder(TestConfiguration.CONFIDENTIAL_CLIENT_ID,
                ClientCredentialFactory.create(TestConfiguration.CONFIDENTIAL_CLIENT_SECRET))
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        confApp2 = ConfidentialClientApplication.builder(TestConfiguration.CONFIDENTIAL_CLIENT_ID_2,
                ClientCredentialFactory.create(TestConfiguration.CONFIDENTIAL_CLIENT_SECRET_2))
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        pubApp = PublicClientApplication.builder(TestConfiguration.PUBLIC_CLIENT_ID)
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        confParameters = ClientCredentialParameters.builder(
                Collections.singleton(TestConfiguration.GRAPH_DEFAULT_SCOPE))
                .build();

        pubParameters = DeviceCodeFlowParameters.builder(
                Collections.singleton(TestConfiguration.GRAPH_DEFAULT_SCOPE),
                deviceCodeConsumer)
                .build();
    }

    @After
    public void cleanup() {
        accessAspect.deleteCache();
    }

    @Test
    public void checkIfWritesToFileFirstTimeConfidentialClient() {

        CompletableFuture<IAuthenticationResult> result = confApp.acquireToken(confParameters);

        result.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        Assert.assertTrue(jsonObj.has("AccessToken"));
        Assert.assertTrue(jsonObj.has("RefreshToken"));
        Assert.assertTrue(jsonObj.has("IdToken"));
        Assert.assertTrue(jsonObj.has("Account"));
        Assert.assertTrue(jsonObj.has("AppMetadata"));

        int set = jsonObj.get("AccessToken").getAsJsonObject().keySet().size();

        Assert.assertEquals(set, 1);

        accessAspect.deleteCache();
    }

    @Test
    public void checkIfWritesToFileFirstTimePublicClient() {

        CompletableFuture<IAuthenticationResult> result = pubApp.acquireToken(
                pubParameters);

        result.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        Assert.assertTrue(jsonObj.has("AccessToken"));
        Assert.assertTrue(jsonObj.has("RefreshToken"));
        Assert.assertTrue(jsonObj.has("IdToken"));
        Assert.assertTrue(jsonObj.has("Account"));
        Assert.assertTrue(jsonObj.has("AppMetadata"));

        Assert.assertTrue(jsonObj.get("AccessToken").getAsJsonObject().keySet().size() == 1);

        accessAspect.deleteCache();
    }

    @Test
    public void addsAccountToListPubClient() {

        CompletableFuture<IAuthenticationResult> result = pubApp.acquireToken(
                pubParameters);

        result.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception - " + ex.getMessage());
                return "Unknown!";
            }

            return res;

        }).join();

        Assert.assertEquals(pubApp.getAccounts().join().size(), 1);

        accessAspect.deleteCache();
    }

    @Test
    public void writesTwoTokensToCache() {
        CompletableFuture<IAuthenticationResult> result = pubApp.acquireToken(
                pubParameters);

        result.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception 1 - " + ex.getMessage());
                return "Unknown!";
            }

            return res;

        }).join();

        CompletableFuture<IAuthenticationResult> result2 = confApp.acquireToken(confParameters);

        result2.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception 2 - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        Assert.assertTrue(jsonObj.get("AccessToken").getAsJsonObject().keySet().size() == 2);
        Assert.assertTrue(jsonObj.get("RefreshToken").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("IdToken").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("Account").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("AppMetadata").getAsJsonObject().keySet().size() == 1);

        accessAspect.deleteCache();
    }

    @Test
    public void writesReadsMultipleTokensToCache() {
        CompletableFuture<IAuthenticationResult> result = pubApp.acquireToken(
                pubParameters);

        result.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception 1 - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        CompletableFuture<IAuthenticationResult> result2 = confApp.acquireToken(confParameters);

        result2.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception 2 - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        CompletableFuture<IAuthenticationResult> result3 = confApp2.acquireToken(confParameters);

        result3.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception 3 - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();

        Assert.assertTrue(jsonObj.get("AccessToken").getAsJsonObject().keySet().size() == 3);
        Assert.assertTrue(jsonObj.get("RefreshToken").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("IdToken").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("Account").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("AppMetadata").getAsJsonObject().keySet().size() == 1);

        accessAspect.deleteCache();
    }

    @Test
    public void syncsCacheWithExpiredTokens() {
        CompletableFuture<IAuthenticationResult> result3 = confApp2.acquireToken(confParameters);

        result3.handle((res, ex) -> {
            if (ex != null) {
                System.out.println("Oops! We have an exception 3 - " + ex.getMessage());
                return "Unknown!";
            }
            return res;

        }).join();

        accessAspect.deleteCache();
    }
}
