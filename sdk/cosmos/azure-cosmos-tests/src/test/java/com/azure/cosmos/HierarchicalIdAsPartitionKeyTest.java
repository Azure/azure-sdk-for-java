/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.RMResources;
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
import com.azure.cosmos.models.CosmosQueryRequestOptions;
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

import java.nio.charset.StandardCharsets;
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
 * the prefix of the partition key. For a single-path "/id" definition, only APIs that support an
 * omitted partition key can use the empty-prefix shorthand; APIs requiring a partition key continue
 * to require the full id value.
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
        database = createTestSyncDatabase(client, "hierarchical-id-partition-key");

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
    public void hpkReplaceUsesAddressedItemIdForPartitionKey() {
        String addressedId = UUID.randomUUID().toString();
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkReplace").build();
        hpkContainer.createItem(
            new TestItem(addressedId, "pkReplace", "original"),
            prefixPartitionKey,
            new CosmosItemRequestOptions());

        TestItem replacement = new TestItem(UUID.randomUUID().toString(), "pkReplace", "replacement");
        assertThatThrownBy(() -> hpkContainer.replaceItem(
            replacement, addressedId, prefixPartitionKey, new CosmosItemRequestOptions()))
            .isInstanceOfSatisfying(CosmosException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(400));
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
        assertThat(feedResponse.getResults())
            .extracting(TestItem::getId)
            .containsExactlyInAnyOrder(id1, id2);

        FeedResponse<TestItem> singletonResponse = hpkContainer.readMany(
            Collections.singletonList(new CosmosItemIdentity(prefixPartitionKey, id3)),
            TestItem.class);
        assertThat(singletonResponse.getResults())
            .extracting(TestItem::getId)
            .containsExactly(id3);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkReadManyDoesNotRewriteNonePartitionKey() {
        String id = UUID.randomUUID().toString();
        hpkContainer.createItem(
            new TestItem(id, null, "v1"),
            new PartitionKeyBuilder().addNullValue().build(),
            new CosmosItemRequestOptions());

        List<CosmosItemIdentity> identities =
            Collections.singletonList(new CosmosItemIdentity(PartitionKey.NONE, id));

        assertThatThrownBy(() -> hpkContainer.readMany(identities, TestItem.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(RMResources.PartitionKeyMismatch);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkPointOperationsDoNotRewriteNonePartitionKey() {
        String id = UUID.randomUUID().toString();
        PartitionKey nullPrefixPartitionKey = new PartitionKeyBuilder().addNullValue().build();
        hpkContainer.createItem(
            new TestItem(id, null, "v1"),
            nullPrefixPartitionKey,
            new CosmosItemRequestOptions());

        assertThatThrownBy(() -> hpkContainer.readItem(id, PartitionKey.NONE, TestItem.class))
            .isInstanceOfSatisfying(CosmosException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(400));

        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().replace("/prop", "patched");
        assertThatThrownBy(() ->
            hpkContainer.patchItem(id, PartitionKey.NONE, patchOperations, TestItem.class))
            .isInstanceOfSatisfying(CosmosException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(400));

        assertThatThrownBy(() ->
            hpkContainer.deleteItem(id, PartitionKey.NONE, new CosmosItemRequestOptions()))
            .isInstanceOfSatisfying(CosmosException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(400));

        assertThat(hpkContainer.readItem(id, nullPrefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("v1");
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkBulkWithPrefixPartitionKey() {
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkBulk").build();

        // Seed items with bulk create using only the prefix partition key. Create operations have no
        // explicit operation id, so the id is resolved from the item body (item-body-fallback branch).
        List<CosmosItemOperation> createOperations = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            createOperations.add(CosmosBulkOperations.getCreateItemOperation(
                new TestItem(id, "pkBulk", "v" + i), prefixPartitionKey));
        }

        for (CosmosBulkOperationResponse<Object> response : hpkContainer.<Object>executeBulkOperations(createOperations)) {
            assertThat(response.getResponse().getStatusCode()).isEqualTo(201);
        }

        // Each bulk-created item is addressable via the fully specified partition key [pk, id], which
        // proves the id was appended in the right position (not only reachable through the prefix).
        for (String id : ids) {
            PartitionKey fullPartitionKey = new PartitionKeyBuilder().add("pkBulk").add(id).build();
            assertThat(hpkContainer.readItem(id, fullPartitionKey, TestItem.class).getItem().getId())
                .isEqualTo(id);
        }

        // Mixed bulk operations that carry an explicit operation id (read/replace/upsert/delete/patch)
        // exercised with only the prefix partition key - this covers the operation-id branch of id
        // resolution which the create-only path does not reach.
        String replaceId = ids.get(0);
        String upsertId = ids.get(1);
        String patchId = ids.get(2);
        String deleteId = ids.get(3);
        String readId = ids.get(4);
        String insertViaUpsertId = UUID.randomUUID().toString();

        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().replace("/prop", "patched");

        List<CosmosItemOperation> mixedOperations = Arrays.asList(
            CosmosBulkOperations.getReadItemOperation(readId, prefixPartitionKey),
            CosmosBulkOperations.getReplaceItemOperation(
                replaceId, new TestItem(replaceId, "pkBulk", "replaced"), prefixPartitionKey),
            CosmosBulkOperations.getUpsertItemOperation(
                new TestItem(upsertId, "pkBulk", "upserted"), prefixPartitionKey),
            CosmosBulkOperations.getUpsertItemOperation(
                new TestItem(insertViaUpsertId, "pkBulk", "insertedViaUpsert"), prefixPartitionKey),
            CosmosBulkOperations.getPatchItemOperation(patchId, prefixPartitionKey, patchOperations),
            CosmosBulkOperations.getDeleteItemOperation(deleteId, prefixPartitionKey));

        for (CosmosBulkOperationResponse<Object> response : hpkContainer.<Object>executeBulkOperations(mixedOperations)) {
            assertThat(response.getResponse().getStatusCode()).isIn(200, 201, 204);
        }

        // Every operation took effect and the items remain addressable with just the prefix.
        assertThat(hpkContainer.readItem(replaceId, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("replaced");
        assertThat(hpkContainer.readItem(upsertId, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("upserted");
        assertThat(hpkContainer.readItem(insertViaUpsertId, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("insertedViaUpsert");
        assertThat(hpkContainer.readItem(patchId, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("patched");
        assertThatThrownBy(() -> hpkContainer.readItem(deleteId, prefixPartitionKey, TestItem.class))
            .isInstanceOf(CosmosException.class);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkBulkDoesNotRewriteNonePartitionKey() {
        String id = UUID.randomUUID().toString();
        hpkContainer.createItem(
            new TestItem(id, null, "v1"),
            new PartitionKeyBuilder().addNullValue().build(),
            new CosmosItemRequestOptions());

        List<CosmosItemOperation> operations = Collections.singletonList(
            CosmosBulkOperations.getDeleteItemOperation(id, PartitionKey.NONE));

        assertThatThrownBy(() -> hpkContainer.executeBulkOperations(operations).iterator().hasNext())
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(hpkContainer.readItem(
            id, new PartitionKeyBuilder().addNullValue().build(), TestItem.class).getItem().getId())
            .isEqualTo(id);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkBulkPartitionKeyCompletionFailureTerminatesPipeline() {
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkBulkFailure").build();
        List<CosmosItemOperation> operations = Collections.singletonList(
            CosmosBulkOperations.getCreateItemOperation(
                new TestItem(null, "pkBulkFailure", "invalid"),
                prefixPartitionKey));

        assertThatThrownBy(() -> hpkContainer.executeBulkOperations(operations).iterator().hasNext())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkByteBufferCreateWithPrefixPartitionKey() {
        String id = UUID.randomUUID().toString();
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkBytes").build();

        String rawItem = "{\"id\":\"" + id + "\",\"pk\":\"pkBytes\",\"prop\":\"bytes\"}";
        byte[] itemBytes = rawItem.getBytes(StandardCharsets.UTF_8);

        // Create from a raw byte payload with only the prefix partition key. The id is read from the
        // byte-buffer body and appended to complete the [pk, id] partition key.
        hpkContainer.createItem(itemBytes, prefixPartitionKey, new CosmosItemRequestOptions());

        // The item is addressable with the prefix and with the fully specified partition key.
        assertThat(hpkContainer.readItem(id, prefixPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("bytes");
        PartitionKey fullPartitionKey = new PartitionKeyBuilder().add("pkBytes").add(id).build();
        assertThat(hpkContainer.readItem(id, fullPartitionKey, TestItem.class).getItem().getProp())
            .isEqualTo("bytes");
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkQueryWithPrefixPartitionKeyReturnsAllItemsSharingPrefix() {
        String prefix = "pkQuery-" + UUID.randomUUID();
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add(prefix).build();

        int itemCount = 3;
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            hpkContainer.createItem(new TestItem(id, prefix, "v" + i), prefixPartitionKey, new CosmosItemRequestOptions());
        }

        // A query scoped to the prefix partition key must treat it as a prefix and return ALL items
        // sharing it. The id must NOT be auto-appended for queries (that would return at most one
        // item) - this locks down the primary safety property the feature relies on.
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions().setPartitionKey(prefixPartitionKey);
        List<String> resultIds = new ArrayList<>();
        hpkContainer.queryItems("SELECT * FROM c", queryOptions, TestItem.class)
            .forEach(item -> resultIds.add(item.getId()));

        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void hpkTransactionalBatchWithPrefixPartitionKeyThrows() {
        String id = UUID.randomUUID().toString();
        PartitionKey prefixPartitionKey = new PartitionKeyBuilder().add("pkBatch").build();

        CosmosBatch batch = CosmosBatch.createCosmosBatch(prefixPartitionKey);
        batch.createItemOperation(new TestItem(id, "pkBatch", "v1"));

        // A batch targets a single logical partition, so a prefix partition key is rejected.
        assertThatThrownBy(() -> hpkContainer.executeCosmosBatch(batch))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(RMResources.PartitionKeyMismatch);
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
    public void hpkTransactionalBatchWithNonePartitionKeyThrows() {
        CosmosBatch batch = CosmosBatch.createCosmosBatch(PartitionKey.NONE);
        batch.createItemOperation(new TestItem(UUID.randomUUID().toString(), null, "v1"));

        assertThatThrownBy(() -> hpkContainer.executeCosmosBatch(batch))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(RMResources.PartitionKeyMismatch);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void idOnlyContainerCreateWithoutPartitionKey() {
        String id = UUID.randomUUID().toString();
        // Create permits the zero-component prefix to be represented by an omitted partition key.
        idContainer.createItem(new TestItem(id, null, "v1"));

        // APIs that require a partition key continue to use the full id value.
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
