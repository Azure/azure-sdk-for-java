// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.EmulatorTest;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class CosmosItemBulkTests extends BatchTestBase {

    @Factory(dataProvider = "simpleClientBuilders")
    public CosmosItemBulkTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        CosmosAsyncContainer container = this.bulkContainer;

        List<Mono<CosmosItemResponse<TestDoc>>> responseMonos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            responseMonos.add(executeCreateAsync(container, this.populateTestDoc(String.valueOf(i), String.valueOf(i))));
        }

        for (int i = 0; i < 100; i++) {
            CosmosItemResponse<TestDoc> response = responseMonos.get(i).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            assertFalse(Strings.isNullOrEmpty(response.getDiagnostics().toString()));

            TestDoc document = response.getItem();
            assertEquals(String.valueOf(i), document.getId());
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void upsertItem_withbulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> responseMonos = new ArrayList<>();
        CosmosAsyncContainer container = this.bulkContainer;

        for (int i = 100; i < 200; i++) {
            responseMonos.add(executeUpsertAsync(container, this.populateTestDoc(String.valueOf(i), String.valueOf(i))));
        }

        for (int i = 100; i < 200; i++) {
            CosmosItemResponse<TestDoc> response = responseMonos.get(i - 100).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            assertFalse(Strings.isNullOrEmpty(response.getDiagnostics().toString()));

            TestDoc document = response.getItem();
            assertEquals(String.valueOf(i), document.getId());
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteItem_withBulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> insertMonos = new ArrayList<>();
        List<TestDoc> createdDocuments = new ArrayList<TestDoc>();
        CosmosAsyncContainer container = this.bulkContainer;

        // Create the items
        for (int i = 200; i < 300; i++) {
            TestDoc createdDocument = this.populateTestDoc(String.valueOf(i), String.valueOf(i));
            createdDocuments.add(createdDocument);
            insertMonos.add(executeCreateAsync(container, createdDocument));
        }

        List<CosmosItemResponse<TestDoc>> list = Flux.merge(insertMonos).collectList().block();

        List<Mono<CosmosItemResponse<Object>>> deletedMonos = new ArrayList<>();
        // Delete the items
        for (TestDoc createdDocument : createdDocuments) {
            deletedMonos.add(executeDeleteAsync(container, createdDocument));
        }

        for (int i = 200; i < 300; i++) {
            CosmosItemResponse<Object> response = deletedMonos.get(i - 200).block();
            assertEquals(HttpResponseStatus.NO_CONTENT.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            assertFalse(Strings.isNullOrEmpty(response.getDiagnostics().toString()));
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readItem_withBulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> insertMonos = new ArrayList<>();
        List<TestDoc> createdDocuments = new ArrayList<TestDoc>();
        CosmosAsyncContainer container = this.bulkContainer;

        // Create the items
        for (int i = 300; i < 400; i++) {
            TestDoc createdDocument = this.populateTestDoc(String.valueOf(i), String.valueOf(i));
            createdDocuments.add(createdDocument);
            insertMonos.add(executeCreateAsync(container, createdDocument));
        }

        List<CosmosItemResponse<TestDoc>> list = Flux.merge(insertMonos).collectList().block();

        List<Mono<CosmosItemResponse<TestDoc>>> readMonos = new ArrayList<>();
        // Read the items
        for (TestDoc createdDocument : createdDocuments) {
            readMonos.add(executeReadAsync(container, createdDocument));
        }

        for (int i = 300; i < 400; i++) {
            CosmosItemResponse<TestDoc> response = readMonos.get(i - 300).block();
            assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            assertFalse(Strings.isNullOrEmpty(response.getDiagnostics().toString()));

            TestDoc document = response.getItem();
            assertEquals(String.valueOf(i), document.getId());
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceItem_withBulk() {
        List<Mono<CosmosItemResponse<TestDoc>>> insertMonos = new ArrayList<>();
        List<TestDoc> createdDocuments = new ArrayList<TestDoc>();
        CosmosAsyncContainer container = this.bulkContainer;

        // Create the items
        for (int i = 400; i < 500; i++) {
            TestDoc createdDocument = this.populateTestDoc(String.valueOf(i), String.valueOf(i));
            createdDocuments.add(createdDocument);
            insertMonos.add(executeCreateAsync(container, createdDocument));
        }

        List<CosmosItemResponse<TestDoc>> list = Flux.merge(insertMonos).collectList().block();

        List<Mono<CosmosItemResponse<TestDoc>>> replaceMonos = new ArrayList<>();
        // Replace the items
        for (TestDoc createdDocument : createdDocuments) {
            createdDocument.setCost(createdDocument.getCost() + 1);
            replaceMonos.add(executeReplaceAsync(container, createdDocument));
        }

        for (int i = 400; i < 500; i++) {
            CosmosItemResponse<TestDoc> response = replaceMonos.get(i - 400).block();
            assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
            assertTrue(response.getRequestCharge() > 0);
            assertFalse(Strings.isNullOrEmpty(response.getDiagnostics().toString()));

            TestDoc document = response.getItem();
            assertEquals(String.valueOf(i), document.getId());
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
