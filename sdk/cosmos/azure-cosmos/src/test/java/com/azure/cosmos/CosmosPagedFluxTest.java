/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosPagedFluxTest extends TestSuiteBase {

    private static final int NUM_OF_ITEMS = 10;

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;

    @Factory(dataProvider = "clientBuilders")
    public CosmosPagedFluxTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosPagedFluxTest() {
        assertThat(this.cosmosAsyncClient).isNull();
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.cosmosAsyncClient);
        cosmosAsyncContainer = cosmosAsyncClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
        createItems(NUM_OF_ITEMS);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.cosmosAsyncClient).isNotNull();
        this.cosmosAsyncClient.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsByPageWithCosmosPagedFluxHandler() throws Exception {
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> cosmosPagedFlux =
            cosmosAsyncContainer.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        AtomicInteger itemCount = new AtomicInteger();
        cosmosPagedFlux.byPage().toIterable().forEach(feedResponse -> {
            int size = feedResponse.getResults().size();
            itemCount.addAndGet(size);
        });

        assertThat(handleCount.get() >= 1).isTrue();
        assertThat(itemCount.get()).isEqualTo(NUM_OF_ITEMS);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsBySubscribeWithCosmosPagedFluxHandler() throws Exception {

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> cosmosPagedFlux =
            cosmosAsyncContainer.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        AtomicInteger itemCount = new AtomicInteger();
        cosmosPagedFlux.toIterable().forEach(internalObjectNode -> {
            itemCount.incrementAndGet();
        });

        assertThat(handleCount.get() >= 1).isTrue();
        assertThat(itemCount.get()).isEqualTo(NUM_OF_ITEMS);
    }

    private void createItems(int numOfItems) {
        for(int i = 0; i < numOfItems; i++) {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            cosmosAsyncContainer.createItem(properties).block();
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
