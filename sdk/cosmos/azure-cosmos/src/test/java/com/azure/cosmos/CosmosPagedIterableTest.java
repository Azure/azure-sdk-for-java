/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosPagedIterableTest extends TestSuiteBase {

    private static final int NUM_OF_ITEMS = 10;

    private CosmosClient cosmosClient;
    private CosmosContainer cosmosContainer;

    @Factory(dataProvider = "clientBuilders")
    public CosmosPagedIterableTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosPagedIterableTest() {
        assertThat(this.cosmosClient).isNull();
        this.cosmosClient = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.cosmosClient.asyncClient());
        cosmosContainer =
            cosmosClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
        createItems(NUM_OF_ITEMS);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.cosmosClient).isNotNull();
        this.cosmosClient.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsByPageWithCosmosPagedIterableHandler() throws Exception {
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<InternalObjectNode> cosmosPagedIterable =
            cosmosContainer.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedIterable = cosmosPagedIterable.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            logger.info("Cosmos Diagnostics : {}", cosmosDiagnostics);
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        AtomicInteger itemCount = new AtomicInteger();
        AtomicInteger feedResponseCount = new AtomicInteger();
        cosmosPagedIterable.iterableByPage().forEach(feedResponse -> {
            feedResponseCount.incrementAndGet();
            int size = feedResponse.getResults().size();
            itemCount.addAndGet(size);
        });

        assertThat(handleCount.get() >= 1).isTrue();
        assertThat(handleCount.get()).isEqualTo(feedResponseCount.get());
        assertThat(itemCount.get()).isEqualTo(NUM_OF_ITEMS);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsBySubscribeWithCosmosPagedIterableHandler() throws Exception {

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<InternalObjectNode> cosmosPagedIterable =
            cosmosContainer.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedIterable = cosmosPagedIterable.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            logger.info("Cosmos Diagnostics : {}", cosmosDiagnostics);
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        AtomicInteger itemCount = new AtomicInteger();
        cosmosPagedIterable.forEach(internalObjectNode -> {
            itemCount.incrementAndGet();
        });

        assertThat(handleCount.get() >= 1).isTrue();
        assertThat(itemCount.get()).isEqualTo(NUM_OF_ITEMS);
    }

    private void createItems(int numOfItems) {
        for (int i = 0; i < numOfItems; i++) {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            cosmosContainer.createItem(properties);
        }
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, uuid));
        return properties;
    }

}
