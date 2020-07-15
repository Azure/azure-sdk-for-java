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

public class PersistentTokenCacheAccessAspectTest {
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

        Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> {
            System.out.println(deviceCode.message());
        };

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
        if (accessAspect != null) {
            accessAspect.deleteCache();
        }
    }

    @Test
    public void checkIfWritesToFileFirstTimeConfidentialClient() throws Exception {

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

        Map<String, Object> jsonObj = serializerAdapter.deserialize(currJson, Map.class, SerializerEncoding.JSON);

        Assert.assertTrue(jsonObj.containsKey("AccessToken"));
        Assert.assertTrue(jsonObj.containsKey("RefreshToken"));
        Assert.assertTrue(jsonObj.containsKey("IdToken"));
        Assert.assertTrue(jsonObj.containsKey("Account"));
        Assert.assertTrue(jsonObj.containsKey("AppMetadata"));

        Map<String, Object> accessTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("AccessToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);

        Assert.assertEquals(1, accessTokenObj.size());

        accessAspect.deleteCache();
    }

    @Test
    public void checkIfWritesToFileFirstTimePublicClient() throws Exception {

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

        Map<String, Object> jsonObj = serializerAdapter.deserialize(currJson, Map.class, SerializerEncoding.JSON);

        Assert.assertTrue(jsonObj.containsKey("AccessToken"));
        Assert.assertTrue(jsonObj.containsKey("RefreshToken"));
        Assert.assertTrue(jsonObj.containsKey("IdToken"));
        Assert.assertTrue(jsonObj.containsKey("Account"));
        Assert.assertTrue(jsonObj.containsKey("AppMetadata"));

        Map<String, Object> accessTokenObj = serializerAdapter.deserialize(
            serializerAdapter.serialize(jsonObj.get("AccessToken"), SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);

        Assert.assertEquals(1, accessTokenObj.size());

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
    public void writesTwoTokensToCache() throws Exception {
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
    public void writesReadsMultipleTokensToCache() throws Exception {
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


        Assert.assertEquals(3, accessTokenObj.size());
        Assert.assertEquals(1, refreshTokenObj.size());
        Assert.assertEquals(1, idTokenObj.size());
        Assert.assertEquals(1, accountObj.size());
        Assert.assertEquals(1, appMetadataObj.size());

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
