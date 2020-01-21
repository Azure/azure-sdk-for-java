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

public class MultithreadedTokenCacheTest {

    private PersistentTokenCacheAccessAspect accessAspect;
    private CachePersister cachePersister;

    private ConfidentialClientApplication confApp;
    private ConfidentialClientApplication confApp2;
    private PublicClientApplication pubApp;
    private ClientCredentialParameters confParameters;
    private DeviceCodeFlowParameters pubParameters;

    @Before
    public void setup() throws Exception {
        org.junit.Assume.assumeTrue("Skipping these tests until we mock or record it", false);
        // custom MsalCacheStorage for testing purposes so we don't overwrite the real one
        cachePersister = new CachePersister.Builder()
                .cacheLocation(java.nio.file.Paths.get(System.getProperty("user.home"), "test.cache").toString())
                .build();

        accessAspect = new PersistentTokenCacheAccessAspect(cachePersister);

        confApp = ConfidentialClientApplication.builder(TestConfiguration.CONFIDENTIAL_CLIENT_ID,
                ClientCredentialFactory.createFromSecret(TestConfiguration.CONFIDENTIAL_CLIENT_SECRET))
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        confApp2 = ConfidentialClientApplication.builder(TestConfiguration.CONFIDENTIAL_CLIENT_ID_2,
                ClientCredentialFactory.createFromSecret(TestConfiguration.CONFIDENTIAL_CLIENT_SECRET_2))
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        confParameters = ClientCredentialParameters.builder(
                Collections.singleton(TestConfiguration.GRAPH_DEFAULT_SCOPE))
                .build();


        pubApp = PublicClientApplication.builder(TestConfiguration.PUBLIC_CLIENT_ID)
                .authority(TestConfiguration.TENANT_SPECIFIC_AUTHORITY)
                .setTokenCacheAccessAspect(accessAspect)
                .build();

        Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> System.out.println(deviceCode.message());

        pubParameters = DeviceCodeFlowParameters.builder(
                Collections.singleton(TestConfiguration.GRAPH_DEFAULT_SCOPE),
                deviceCodeConsumer)
                .build();
    }

    @After
    public void cleanup() {
        if (accessAspect != null) {
            accessAspect.deleteCache();
        }
    }

    @Test
    public void twoThreadsWritingTokens() {

        ConcurrentClient a = new ConcurrentClient("conf");
        ConcurrentClient b = new ConcurrentClient("pub");

        try {
            a.t.join();
            b.t.join();
        } catch (Exception e) {
            System.out.printf("Error with threads");
        }

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
    public void tenThreadsWritingSameConfTokens() {

        ConcurrentClient a = new ConcurrentClient("conf");
        ConcurrentClient b = new ConcurrentClient("conf");
        ConcurrentClient c = new ConcurrentClient("conf");
        ConcurrentClient d = new ConcurrentClient("conf");
        ConcurrentClient e = new ConcurrentClient("conf");
        ConcurrentClient f = new ConcurrentClient("conf");
        ConcurrentClient g = new ConcurrentClient("conf");
        ConcurrentClient h = new ConcurrentClient("conf");
        ConcurrentClient i = new ConcurrentClient("conf");
        ConcurrentClient j = new ConcurrentClient("conf");

        try {
            a.t.join();
            b.t.join();
            c.t.join();
            d.t.join();
            e.t.join();
            f.t.join();
            g.t.join();
            h.t.join();
            i.t.join();
            j.t.join();
        } catch (Exception ex) {
            System.out.printf("Error with threads");
        }


        byte[] currJsonBytes = cachePersister.readCache();
        String currJson = new String(currJsonBytes);

        JsonObject jsonObj = new JsonParser().parse(currJson).getAsJsonObject();


        System.out.println("keys: " + jsonObj.get("AccessToken").getAsJsonObject().keySet().size());

        Assert.assertTrue(jsonObj.get("AccessToken").getAsJsonObject().keySet().size() == 1);
        Assert.assertTrue(jsonObj.get("RefreshToken").getAsJsonObject().keySet().size() == 0);
        Assert.assertTrue(jsonObj.get("IdToken").getAsJsonObject().keySet().size() == 0);
        Assert.assertTrue(jsonObj.get("Account").getAsJsonObject().keySet().size() == 0);
        Assert.assertTrue(jsonObj.get("AppMetadata").getAsJsonObject().keySet().size() == 0);

        accessAspect.deleteCache();
    }

    class ConcurrentClient implements Runnable {

        String threadName;
        Thread t;

        ConcurrentClient(String threadName) {
            this.threadName = threadName;
            t = new Thread(this, threadName);
            t.start();
        }

        public void run() {

            if (threadName.equals("conf")) {
                CompletableFuture<IAuthenticationResult> result = confApp.acquireToken(confParameters);

                result.handle((res, ex) -> {
                    if (ex != null) {
                        System.out.println("Oops! We have an exception 1 - " + ex.getMessage());
                        return "Unknown!";
                    }
                    return res;

                }).join();
            } else if (threadName.equals("pub")) {

                CompletableFuture<IAuthenticationResult> result = pubApp.acquireToken(
                        pubParameters);

                result.handle((res, ex) -> {
                    if (ex != null) {
                        System.out.println("Oops! We have an exception 2 - " + ex.getMessage());
                        return "Unknown!";
                    }

                    return res;

                }).join();
            }
        }

    }
}
