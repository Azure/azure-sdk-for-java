// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.batch.implementation.BatchRequestResponseConstant;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CosmosItemBulkTests extends BatchTestBase {

    private CosmosAsyncClient bulkClient;
    private CosmosAsyncContainer bulkContainer;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public CosmosItemBulkTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemBulkTests() {
        assertThat(this.bulkClient).isNull();
        this.bulkClient = getClientBuilder().bulkExecutionEnabled(true).buildAsyncClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.bulkClient);
        bulkContainer = bulkClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.bulkClient).isNotNull();
        this.bulkClient.close();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        CosmosAsyncContainer container = this.bulkContainer;

        List<String> idList = new ArrayList<>();
        List<Mono<CosmosItemResponse<TestDoc>>> responseMonos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            idList.add(id);
            responseMonos.add(executeCreateAsync(container, this.populateTestDoc(id, String.valueOf(i))));
        }

        for (int i = 0; i < 100; i++) {
            CosmosItemResponse<TestDoc> response = responseMonos.get(i).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            String diagnostic = response.getDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));
            TestDoc document = response.getItem();

            String id = idList.get(i);
            assertEquals(id, document.getId());
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createItemLargeOperationSize_withBulk() {
        CosmosAsyncContainer container = this.bulkContainer;
        int appxDocSize = 3 * BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES;

        List<Mono<CosmosItemResponse<TestDoc>>> responseMonos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestDoc item = this.populateTestDoc("TBD", appxDocSize);
            responseMonos.add(executeCreateAsync(container, item));
        }

        for (int i = 0; i < 10; i++) {
            CosmosItemResponse<TestDoc> response = responseMonos.get(i).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            String diagnostic = response.getDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));        }
    }


    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void upsertItem_withbulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> responseMonos = new ArrayList<>();
        CosmosAsyncContainer container = this.bulkContainer;
        List<String> idList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            idList.add(id);
            responseMonos.add(executeUpsertAsync(container, this.populateTestDoc(id, String.valueOf(i))));
        }

        for (int i = 0; i < 100; i++) {
            CosmosItemResponse<TestDoc> response = responseMonos.get(i).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            String diagnostic = response.getDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));

            TestDoc document = response.getItem();
            String id = idList.get(i);
            assertEquals(id, document.getId());        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteItem_withBulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> insertMonos = new ArrayList<>();
        List<TestDoc> createdDocuments = new ArrayList<TestDoc>();
        CosmosAsyncContainer container = this.bulkContainer;

        // Create the items
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            TestDoc createdDocument = this.populateTestDoc(id, String.valueOf(i));

            createdDocuments.add(createdDocument);
            insertMonos.add(executeCreateAsync(container, createdDocument));
        }

        List<CosmosItemResponse<TestDoc>> list = Flux.merge(insertMonos).collectList().block();

        List<Mono<CosmosItemResponse<Object>>> deletedMonos = new ArrayList<>();
        // Delete the items
        for (TestDoc createdDocument : createdDocuments) {
            deletedMonos.add(executeDeleteAsync(container, createdDocument));
        }

        for (int i = 0; i < 100; i++) {
            CosmosItemResponse<Object> response = deletedMonos.get(i).block();
            assertEquals(HttpResponseStatus.NO_CONTENT.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            String diagnostic = response.getDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void readItem_withBulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> insertMonos = new ArrayList<>();
        List<TestDoc> createdDocuments = new ArrayList<TestDoc>();
        CosmosAsyncContainer container = this.bulkContainer;
        List<String> idList = new ArrayList<>();

        // Create the items
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            idList.add(id);
            TestDoc createdDocument = this.populateTestDoc(id, String.valueOf(i));

            createdDocuments.add(createdDocument);
            insertMonos.add(executeCreateAsync(container, createdDocument));
        }

        Flux.merge(insertMonos).collectList().block();

        List<Mono<CosmosItemResponse<TestDoc>>> readMonos = new ArrayList<>();
        // Read the items
        for (TestDoc createdDocument : createdDocuments) {
            readMonos.add(executeReadAsync(container, createdDocument));
        }

        for (int i = 0; i < 100; i++) {
            CosmosItemResponse<TestDoc> response = readMonos.get(i).block();
            assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            String diagnostic = response.getDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));

            TestDoc document = response.getItem();

            String id = idList.get(i);
            assertEquals(id, document.getId());
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void replaceItem_withBulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> insertMonos = new ArrayList<>();
        List<TestDoc> createdDocuments = new ArrayList<TestDoc>();
        CosmosAsyncContainer container = this.bulkContainer;
        List<String> idList = new ArrayList<>();

        // Create the items
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            idList.add(id);
            TestDoc createdDocument = this.populateTestDoc(id, String.valueOf(i));

            createdDocuments.add(createdDocument);
            insertMonos.add(executeCreateAsync(container, createdDocument));
        }

        Flux.merge(insertMonos).collectList().block();

        List<Mono<CosmosItemResponse<TestDoc>>> replaceMonos = new ArrayList<>();
        // Replace the items
        for (TestDoc createdDocument : createdDocuments) {
            createdDocument.setCost(createdDocument.getCost() + 1);
            replaceMonos.add(executeReplaceAsync(container, createdDocument));
        }

        for (int i = 0; i < 100; i++) {
            CosmosItemResponse<TestDoc> response = replaceMonos.get(i).block();
            assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            String diagnostic = response.getDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));

            TestDoc document = response.getItem();

            String id = idList.get(i);
            assertEquals(id, document.getId());
        }
    }

    private static Mono<CosmosItemResponse<TestDoc>> executeCreateAsync(CosmosAsyncContainer container, TestDoc item) {
        return container.createItem(item, new PartitionKey(item.getStatus()), null);
    }

    private static Mono<CosmosItemResponse<TestDoc>> executeUpsertAsync(CosmosAsyncContainer container, TestDoc item) {
        return container.upsertItem(item, ModelBridgeInternal.createCosmosItemRequestOptions(new PartitionKey(item.getStatus())));
    }

    private static Mono<CosmosItemResponse<Object>> executeDeleteAsync(CosmosAsyncContainer container, TestDoc item) {
        return container.deleteItem(item.getId(), new PartitionKey(item.getStatus()), null);
    }

    private static Mono<CosmosItemResponse<TestDoc>> executeReadAsync(CosmosAsyncContainer container, TestDoc item) {
        return container.readItem(item.getId(), new PartitionKey(item.getStatus()), TestDoc.class);
    }

    private static Mono<CosmosItemResponse<TestDoc>> executeReplaceAsync(CosmosAsyncContainer container, TestDoc item) {
        return container.replaceItem(item, item.getId(), new PartitionKey(item.getStatus()), null);
    }
}
