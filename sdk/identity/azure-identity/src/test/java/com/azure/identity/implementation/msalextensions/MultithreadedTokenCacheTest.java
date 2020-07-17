// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.implementation.msalextensions.cachepersister.CachePersister;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MultithreadedTokenCacheTest {
    private final SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();

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
    public void twoThreadsWritingTokens() throws Exception {

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

        Map<String, Object> jsonObj = serializerAdapter.deserialize(currJson, Map.class, SerializerEncoding.JSON);
        Map<String, Object> accessTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("AccessToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> refreshTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("RefreshToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> idTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("IdToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> accountObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("Account"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> appMetadataObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("AppMetadata"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);


        Assert.assertEquals(2, accessTokenObj.size());
        Assert.assertEquals(1, refreshTokenObj.size());
        Assert.assertEquals(1, idTokenObj.size());
        Assert.assertEquals(1, accountObj.size());
        Assert.assertEquals(1, appMetadataObj.size());

        accessAspect.deleteCache();
    }

    @Test
    public void tenThreadsWritingSameConfTokens() throws Exception {

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

        Map<String, Object> jsonObj = serializerAdapter.deserialize(currJson, Map.class, SerializerEncoding.JSON);
        Map<String, Object> accessTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("AccessToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);

        System.out.println("keys: " + accessTokenObj.size());

        Map<String, Object> refreshTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("RefreshToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> idTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("IdToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> accountObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("Account"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
        Map<String, Object> appMetadataObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("AppMetadata"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);


        Assert.assertEquals(1, accessTokenObj.size());
        Assert.assertEquals(0, refreshTokenObj.size());
        Assert.assertEquals(0, idTokenObj.size());
        Assert.assertEquals(0, accountObj.size());
        Assert.assertEquals(0, appMetadataObj.size());

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
