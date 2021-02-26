// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.CosmosEncryptionAsyncClient;
import com.azure.cosmos.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReflectionUtils {

    private static <T> void set(Object object, T newValue, String fieldName) {
        try {
            FieldUtils.writeField(object, fieldName, newValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Object object, String fieldName) {
        try {
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static AtomicBoolean isEncryptionSettingsInitDone(EncryptionProcessor encryptionProcessor) {
        return get(encryptionProcessor, "isEncryptionSettingsInitDone");
    }

    public static AsyncCache<String, ClientEncryptionPolicy> getClientEncryptionPolicyCacheByContainerId(CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        return get(cosmosEncryptionAsyncClient, "clientEncryptionPolicyCacheByContainerId");
    }

    public static AsyncCache<String, CosmosClientEncryptionKeyProperties> getClientEncryptionKeyPropertiesCacheByKeyId(CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        return get(cosmosEncryptionAsyncClient, "clientEncryptionKeyPropertiesCacheByKeyId");
    }

    public static ConcurrentHashMap getValueMap(AsyncCache asyncCache) {
        return get(asyncCache, "values");
    }

    public static void setCosmosEncryptionAsyncClient(CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer, CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        set(cosmosEncryptionAsyncContainer, cosmosEncryptionAsyncClient,"cosmosEncryptionAsyncClient");
    }

    public static void setCosmosEncryptionAsyncClient(EncryptionProcessor encryptionProcessor, CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        set(encryptionProcessor, cosmosEncryptionAsyncClient,"encryptionCosmosClient");
    }
}
