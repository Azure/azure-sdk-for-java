// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmulatorVNextWithHttpTest extends TestSuiteBase {
    private CosmosAsyncClient testClient;
    private CosmosAsyncContainer testContainer;

    @Factory(dataProvider = "clientBuilders")
    public EmulatorVNextWithHttpTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator-vnext" }, timeOut = SETUP_TIMEOUT)
    public void before_EmulatorWithHttpTest() {
        this.testClient = getClientBuilder().gatewayMode().buildAsyncClient();
        this.testContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.testClient);

    }

    @AfterClass(groups = { "emulator-vnext" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeClose(this.testClient);
    }

    @Test(groups = { "emulator-vnext" }, timeOut = 4 * SHUTDOWN_TIMEOUT)
    public void documentCrud() {
        // create item
        TestItem newItem = TestItem.createNewItem();
        this.testContainer.createItem(newItem).block();

        // read item
        this.testContainer.readItem(newItem.getId(), new PartitionKey(newItem.getId()), TestItem.class).block();

        // query item
        String query = "select * from c";
        this.testContainer.queryItems(query, new CosmosQueryRequestOptions(), TestItem.class)
            .byPage()
            .blockLast();

        // update item
        newItem.setProp(UUID.randomUUID().toString());
        this.testContainer.upsertItem(newItem).block();

        // replace item
        newItem.setProp(UUID.randomUUID().toString());
        this.testContainer.replaceItem(newItem, newItem.getId(), new PartitionKey(newItem.getId())).block();

        // read all items
        this.testContainer.readAllItems(new PartitionKey(newItem.getId()), TestItem.class).byPage().blockLast();

//        // patch item
//        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().set("/prop", UUID.randomUUID().toString());
//        this.testContainer.patchItem(newItem.getId(), new PartitionKey(newItem.getId()), cosmosPatchOperations, TestItem.class).block();

        // bulk
        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        cosmosItemOperations.add(CosmosBulkOperations.getUpsertItemOperation(newItem, new PartitionKey(newItem.getId())));
        this.testContainer.executeBulkOperations(Flux.fromIterable(cosmosItemOperations), new CosmosBulkExecutionOptions())
            .blockLast();

        // batch
        CosmosBatch batchForRead = CosmosBatch.createCosmosBatch(new PartitionKey(newItem.getId()));
        batchForRead.readItemOperation(newItem.getId());
        this.testContainer.executeCosmosBatch(batchForRead).block();

        // read many
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey(newItem.getId()), newItem.getId()));
        this.testContainer.readMany(cosmosItemIdentities, TestItem.class).block();

        // delete item
        this.testContainer.deleteItem(newItem.getId(), new PartitionKey(newItem.getId())).block();
    }
}
