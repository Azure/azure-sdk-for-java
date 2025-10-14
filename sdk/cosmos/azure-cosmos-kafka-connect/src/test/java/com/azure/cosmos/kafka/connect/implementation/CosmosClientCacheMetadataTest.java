// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestConfigurations;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for CosmosClientCacheMetadata
 */
public class CosmosClientCacheMetadataTest extends KafkaCosmosTestSuiteBase {
    private CosmosAsyncClient cosmosClient;

    @BeforeClass(groups = "kafka-emulator")
    void setUp() {
        cosmosClient = new CosmosClientBuilder()
            .endpoint(KafkaCosmosTestConfigurations.HOST)
            .key(KafkaCosmosTestConfigurations.MASTER_KEY)
            .buildAsyncClient();
    }

    @AfterClass(groups = "kafka-emulator")
    void cleanup() {
        if (this.cosmosClient != null) {
            this.cosmosClient.close();
        }
    }

    @Test(groups = "kafka-emulator")
    void shouldInitializeWithRefCountOne() {
        Instant createdTime = Instant.now();
        CosmosClientCacheMetadata metadata = new CosmosClientCacheMetadata(cosmosClient, createdTime);

        assertThat(metadata.getRefCount()).isEqualTo(1);
        assertThat(metadata.getLastAccessed()).isEqualTo(createdTime);
    }

    @Test(groups = "kafka-emulator")
    void shouldIncrementRefCount() {
        CosmosClientCacheMetadata metadata = new CosmosClientCacheMetadata(cosmosClient, Instant.now());

        long initialCount = metadata.getRefCount();
        metadata.incrementRefCount();

        assertThat(metadata.getRefCount())
            .isEqualTo(initialCount + 1)
            .isEqualTo(2);
    }

    @Test(groups = "kafka-emulator")
    void shouldDecrementRefCount() {
        CosmosClientCacheMetadata metadata = new CosmosClientCacheMetadata(cosmosClient, Instant.now());

        metadata.incrementRefCount(); // ref count = 2
        long countBeforeDecrement = metadata.getRefCount();
        metadata.decrementRefCount();

        assertThat(metadata.getRefCount())
            .isEqualTo(countBeforeDecrement - 1)
            .isEqualTo(1);
    }

    @Test(groups = "kafka-emulator")
    void shouldUpdateLastAccessedTime() {
        CosmosClientCacheMetadata metadata = new CosmosClientCacheMetadata(cosmosClient, Instant.now());

        Instant originalLastAccessed = metadata.getLastAccessed();
        // Sleep briefly to ensure time difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        metadata.updateLastAccessed();

        assertThat(metadata.getLastAccessed())
            .isAfter(originalLastAccessed)
            .isNotEqualTo(originalLastAccessed);
    }

    @Test(groups = "kafka-emulator")
    void shouldReturnClient() {
        CosmosClientCacheMetadata metadata = new CosmosClientCacheMetadata(cosmosClient, Instant.now());

        assertThat(metadata.getClient()).isSameAs(cosmosClient);
    }

    @Test(groups = "kafka-emulator")
    void shouldCloseClientOnClose() {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(KafkaCosmosTestConfigurations.HOST)
            .key(KafkaCosmosTestConfigurations.MASTER_KEY)
            .buildAsyncClient();
        CosmosClientCacheMetadata metadata = new CosmosClientCacheMetadata(cosmosAsyncClient, Instant.now());
        metadata.close();

        assertThat(((RxDocumentClientImpl)CosmosBridgeInternal.getAsyncDocumentClient(cosmosAsyncClient)).isClosed())
            .isTrue();
    }
}
