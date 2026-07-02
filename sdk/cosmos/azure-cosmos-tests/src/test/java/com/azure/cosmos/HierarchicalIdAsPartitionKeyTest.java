/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates that when a container's (hierarchical) partition key definition ends with "/id" the SDK
 * automatically appends the item id to the partition key, so callers can address an item using only
 * the prefix of the partition key. This mirrors
 * https://github.com/Azure/azure-cosmos-dotnet-v3/pull/5600.
 */
public class HierarchicalIdAsPartitionKeyTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosDatabase database;

    // Hierarchical partition key ["/pk", "/id"].
    private CosmosContainer hpkContainer;
    // Single "/id" partition key.
    private CosmosContainer idContainer;

    @Factory(dataProvider = "clientBuilders")
    public HierarchicalIdAsPartitionKeyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_HierarchicalIdAsPartitionKeyTest() {
        client = getClientBuilder().buildClient();
        database = createSyncDatabase(client, CosmosDatabaseForTest.generateId());

        PartitionKeyDefinition hpkDefinition = new PartitionKeyDefinition();
        hpkDefinition.setKind(PartitionKind.MULTI_HASH);
        hpkDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        hpkDefinition.setPaths(Arrays.asList("/pk", "/id"));
        String hpkContainerId = UUID.randomUUID().toString();
        database.createContainer(new CosmosContainerProperties(hpkContainerId, hpkDefinition));
        hpkContainer = database.getContainer(hpkContainerId);

        PartitionKeyDefinition idDefinition = new PartitionKeyDefinition();
        idDefinition.setKind(PartitionKind.HASH);
        idDefinition.setPaths(Collections.singletonList("/id"));
        String idContainerId = UUID.randomUUID().toString();
        database.createContainer(new CosmosContainerProperties(idContainerId, idDefinition));
        idContainer = database.getContainer(idContainerId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteSyncDatabase(database);
        safeCloseSyncClient(client);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkPointOperationsWithPrefixPartitionKey() {
        String id = UUID.randomUUID().toString();
        TestItem item = new TestItem(id, "pkA", "v1");
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkA").build();

        // Create with only the partition key prefix - the id is appended from the item body.
        hpkContainer.createItem(item, prefixPartitionKey, new CosmosItemRequestOptions());

        // Read with only the partition key prefix - the id is appended from the item id.
        CosmosItemResponse<TestItem> readResponse = hpkContainer.readItem(id, prefixPartitionKey, TestItem.class);
        assertThat(readResponse.getItem().getProp()).isEqualTo("v1");

        // The item is addressable with the fully specified partition key too (backward compatible).
        PartitionKey fullPartitionKey = new PartitionKeyBuilder().add("pkA").add(id).build();
        assertThat(hpkContainer.readItem(id, fullPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("v1");

        // Replace with only the prefix.
        item.setProp("v2");
        hpkContainer.replaceItem(item, id, prefixPartitionKey, new CosmosItemRequestOptions());
        assertThat(hpkContainer.readItem(id, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("v2");

        // Patch with only the prefix.
        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().replace("/prop", "v3");
        hpkContainer.patchItem(id, prefixPartitionKey, patchOperations, TestItem.class);
        assertThat(hpkContainer.readItem(id, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("v3");

        // Upsert with only the prefix.
        item.setProp("v4");
        hpkContainer.upsertItem(item, prefixPartitionKey, new CosmosItemRequestOptions());
        assertThat(hpkContainer.readItem(id, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("v4");

        // Delete with only the prefix.
        hpkContainer.deleteItem(id, prefixPartitionKey, new CosmosItemRequestOptions());
        assertThatThrownBy(() -> hpkContainer.readItem(id, prefixPartitionKey, TestItem.class))
            .isInstanceOf(CosmosException.class);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkReadManyWithPrefixPartitionKey() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();

        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkR").build();
        hpkContainer.createItem(new TestItem(id1, "pkR", "v1"), prefixPartitionKey, new CosmosItemRequestOptions());
        hpkContainer.createItem(new TestItem(id2, "pkR", "v2"), prefixPartitionKey, new CosmosItemRequestOptions());
        hpkContainer.createItem(new TestItem(id3, "pkR", "v3"), prefixPartitionKey, new CosmosItemRequestOptions());

        // readMany with only the partition key prefix on each identity.
        List<CosmosItemIdentity> itemIdentities = Arrays.asList(
            new CosmosItemIdentity(prefixPartitionKey, id1),
            new CosmosItemIdentity(prefixPartitionKey, id2));

        FeedResponse<TestItem> feedResponse = hpkContainer.readMany(itemIdentities, TestItem.class);
        assertThat(feedResponse.getResults()).hasSize(2);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkBulkWithPrefixPartitionKey() {
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkBulk").build();

        List<CosmosItemOperation> operations = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            operations.add(CosmosBulkOperations.getCreateItemOperation(
                new TestItem(id, "pkBulk", "v" + i), prefixPartitionKey));
        }

        for (CosmosBulkOperationResponse<Object> response : hpkContainer.<Object>executeBulkOperations(operations)) {
            assertThat(response.getResponse().getStatusCode()).isEqualTo(201);
        }

        // The bulk-created items are addressable using only the prefix.
        for (String id : ids) {
            assertThat(hpkContainer.readItem(id, prefixPartitionKey, TestItem.class).getItem().getId())
                .isEqualTo(id);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkTransactionalBatchWithPrefixPartitionKeyThrows() {
        String id = UUID.randomUUID().toString();
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkBatch").build();

        CosmosBatch batch = CosmosBatch.createCosmosBatch(prefixPartitionKey);
        batch.createItemOperation(new TestItem(id, "pkBatch", "v1"));

        // A batch targets a single partition key and cannot carry a per-item id, so a prefix
        // partition key is rejected for a container whose last partition key path is "/id".
        assertThatThrownBy(() -> hpkContainer.executeCosmosBatch(batch))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("itemId needs to be specified");
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkTransactionalBatchWithFullPartitionKeySucceeds() {
        String id = UUID.randomUUID().toString();
        PartitionKey fullPartitionKey = new PartitionKeyBuilder().add("pkBatchFull").add(id).build();

        CosmosBatch batch = CosmosBatch.createCosmosBatch(fullPartitionKey);
        batch.createItemOperation(new TestItem(id, "pkBatchFull", "v1"));

        CosmosBatchResponse response = hpkContainer.executeCosmosBatch(batch);
        assertThat(response.isSuccessStatusCode()).isTrue();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void idOnlyContainerCreateWithoutPartitionKey() {
        String id = UUID.randomUUID().toString();
        // No partition key supplied - it is derived from the item's id.
        idContainer.createItem(new TestItem(id, null, "v1"));

        CosmosItemResponse<TestItem> readResponse =
            idContainer.readItem(id, new PartitionKey(id), TestItem.class);
        assertThat(readResponse.getItem().getProp()).isEqualTo("v1");

        idContainer.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
    }

    private static class TestItem {
        private String id;
        private String pk;
        private String prop;

        TestItem() {
        }

        TestItem(String id, String pk, String prop) {
            this.id = id;
            this.pk = pk;
            this.prop = prop;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPk() {
            return pk;
        }

        public void setPk(String pk) {
            this.pk = pk;
        }

        public String getProp() {
            return prop;
        }

        public void setProp(String prop) {
            this.prop = prop;
        }
    }
}
