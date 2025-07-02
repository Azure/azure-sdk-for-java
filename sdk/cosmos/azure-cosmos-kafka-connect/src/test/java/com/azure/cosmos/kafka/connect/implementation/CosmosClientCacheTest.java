// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestConfigurations;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for CosmosClientCache
 */
public class CosmosClientCacheTest extends KafkaCosmosTestSuiteBase {

    @Test(groups = "unit")
    public void shouldReturnSameInstance() {
        CosmosClientCache instance1 = CosmosClientCache.getInstance();
        CosmosClientCache instance2 = CosmosClientCache.getInstance();

        assertThat(instance1).isSameAs(instance2);
    }

    @Test(groups = "kafka-emulator")
    public void shouldUseConfigObjectAsKey() {
        // Create two identical configs
        CosmosClientCacheItem clientItem1 =
            getCosmosClientFromCache(
                "shouldUseConfigObjectAsKey",
                CosmosClientCache.getInstance());
        CosmosClientCacheItem clientItem2 =
            getCosmosClientFromCache(
                "shouldUseConfigObjectAsKey",
                CosmosClientCache.getInstance());

        assertThat(clientItem2.getClient()).isSameAs(clientItem1.getClient());
        CosmosClientCache.getInstance().purgeClient(clientItem1.getClientConfig());
    }

    @Test(groups = "kafka-emulator")
    public void shouldCreateNewClientForDifferentConfig() {
        CosmosClientCacheItem clientItem1 =
            getCosmosClientFromCache(
                "shouldCreateNewClientForDifferentConfig1",
                CosmosClientCache.getInstance());
        CosmosClientCacheItem clientItem2 =
            getCosmosClientFromCache(
                "shouldCreateNewClientForDifferentConfig2",
                CosmosClientCache.getInstance());

        assertThat(clientItem2.getClient()).isNotSameAs(clientItem1.getClient());

        CosmosClientCache.getInstance().purgeClient(clientItem1.getClientConfig());
        CosmosClientCache.getInstance().purgeClient(clientItem2.getClientConfig());
    }

    @Test(groups = "kafka-emulator")
    public void shouldReuseClientWithinCleanupTTl() {
        CosmosClientCacheItem clientItem =
            getCosmosClientFromCache(
                "shouldReuseClientWithinCleanupTTl",
                CosmosClientCache.getInstance());
        CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());

        // same client will be returned as the client is not cleaned up yet
        CosmosClientCacheItem newClientItem =
            getCosmosClientFromCache(
                "shouldReuseClientWithinCleanupTTl",
                CosmosClientCache.getInstance());
        assertThat(newClientItem.getClient()).isSameAs(clientItem.getClient());

        CosmosClientCache.getInstance().purgeClient(clientItem.getClientConfig());
    }

    @Test(groups = "kafka-emulator")
    public void shouldNotReuseClientAfterPurge() {
        CosmosClientCacheItem clientItem =
            getCosmosClientFromCache(
                "shouldNotReuseClientAfterPurge",
                CosmosClientCache.getInstance());
        CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());

        CosmosClientCache.getInstance().purgeClient(clientItem.getClientConfig());

        CosmosClientCacheItem newClientItem
            = getCosmosClientFromCache(
                "shouldNotReuseClientAfterPurge",
                 CosmosClientCache.getInstance());
        assertThat(newClientItem.getClient()).isNotSameAs(clientItem.getClient());

        CosmosClientCache.getInstance().purgeClient(newClientItem.getClientConfig());
    }

    @Test(groups = "kafka-emulator")
    public void shouldCloseAllClientsOnShutdown() {
        CosmosClientCache cache = new CosmosClientCache();

        CosmosClientCacheItem client1 =
            getCosmosClientFromCache(
                "shouldCloseAllClientsOnShutdown1",
                cache);
        CosmosClientCacheItem client2 =
            getCosmosClientFromCache(
                "shouldCloseAllClientsOnShutdown2",
                cache);

        cache.close();
        ((RxDocumentClientImpl)CosmosBridgeInternal.getAsyncDocumentClient(client1.getClient())).isClosed();
        ((RxDocumentClientImpl)CosmosBridgeInternal.getAsyncDocumentClient(client2.getClient())).isClosed();
    }

    @Test(groups = "kafka-emulator")
    public void clientWillBeClosedOncePurged() throws InterruptedException {
        CosmosClientCacheItem clientItem =
            getCosmosClientFromCache(
                "clientWillBeClosedOncePurged",
                CosmosClientCache.getInstance());
        CosmosClientCache.getInstance().releaseClient(clientItem.getClientConfig());
        CosmosClientCache.getInstance().purgeClient(clientItem.getClientConfig());

        // by default, the cleanup job will run every 1 min
        Thread.sleep(Duration.ofMinutes(2).toMillis());
        assertThat(((RxDocumentClientImpl)CosmosBridgeInternal.getAsyncDocumentClient(clientItem.getClient())).isClosed())
            .isTrue();
    }

    private CosmosClientCacheItem getCosmosClientFromCache(String context, CosmosClientCache clientCache) {
        CosmosMasterKeyAuthConfig authConfig = new CosmosMasterKeyAuthConfig(KafkaCosmosTestConfigurations.MASTER_KEY);
        CosmosAccountConfig accountConfig = new CosmosAccountConfig(
            KafkaCosmosTestConfigurations.HOST,
            authConfig,
            CosmosClientCacheTest.class.getName(),
            true,
            Arrays.asList("East US2")
        );

        return clientCache.getOrCreateClient(accountConfig, context, null);
    }
}
