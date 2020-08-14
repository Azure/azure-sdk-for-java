// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.EmulatorTest;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.batch.BatchAsyncContainerExecutor;
import com.azure.cosmos.batch.ItemBatchOperation;
import com.azure.cosmos.batch.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.azure.cosmos.batch.BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES;
import static org.testng.Assert.*;

public class BatchAsyncContainerExecutorTests extends BatchTestBase {

    @Factory(dataProvider = "simpleClientBuilders")
    public BatchAsyncContainerExecutorTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void addAsync() {
        CosmosAsyncContainer container = this.gatewayJsonContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        List<Mono<TransactionalBatchOperationResult<?>>> responseMonos = new ArrayList<>();
        for (int i = 500; i < 600; i++) {
            responseMonos.add(executor.addAsync(createItem(String.valueOf(i)), null));
        }

        for (int i = 500; i < 600; i++) {
            TransactionalBatchOperationResult<?> response = responseMonos.get(i - 500).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());

            TestDoc document = Utils.parse(response.getResourceObject().toString(), TestDoc.class);
            assertEquals(String.valueOf(i), document.getId());

            CosmosItemResponse<TestDoc> storedDoc = container.readItem(String.valueOf(i), new PartitionKey(String.valueOf(i)), TestDoc.class).block();
            assertNotNull(storedDoc.getItem());
        }

        executor.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void validateRequestOptions_Simple() {
        CosmosAsyncContainer container = this.gatewayJsonContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        String id = UUID.randomUUID().toString();
        TestDoc testDoc = this.populateTestDoc(id, id);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setProperties(new HashMap<>());

        ItemBatchOperation<TestDoc> operation = new ItemBatchOperation.Builder<TestDoc>(OperationType.Replace,0)
            .partitionKey(new PartitionKey(id))
            .resource(testDoc)
            .id(id)
            .build();

        CompletableFuture<Boolean> resp = executor.validateAndMaterializeOperation(operation, requestOptions);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void validateInvalidRequestOptions_sessionToken() {
        CosmosAsyncContainer container = this.gatewayJsonContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        String id = UUID.randomUUID().toString();
        TestDoc testDoc = this.populateTestDoc(id, id);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setSessionToken("something");

        ItemBatchOperation<TestDoc> operation = new ItemBatchOperation.Builder<TestDoc>(OperationType.Replace,0)
            .partitionKey(new PartitionKey(id))
            .resource(testDoc)
            .id(id)
            .build();

        try {
            CompletableFuture<Boolean> resp = executor.validateAndMaterializeOperation(operation, requestOptions);
            fail("Should throw exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalStateException && ex.getMessage().contains("UnsupportedBulkRequestOptions"), "Should fail");
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void validateInvalidRequestOptions_PartitionKey() {
        CosmosAsyncContainer container = this.gatewayJsonContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        String id = UUID.randomUUID().toString();
        TestDoc testDoc = this.populateTestDoc(id, id);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setProperties(
            new HashMap<String, Object>() {{
                put(HttpConstants.HttpHeaders.PARTITION_KEY, id);
            }});

        ItemBatchOperation<TestDoc> operation = new ItemBatchOperation.Builder<TestDoc>(OperationType.Replace,0)
            .partitionKey(new PartitionKey(id))
            .resource(testDoc)
            .id(id)
            .build();

        try {
            CompletableFuture<Boolean> resp = executor.validateAndMaterializeOperation(operation, requestOptions);
            fail("Should throw exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalStateException && ex.getMessage().contains("expected byte array value for"), "Should fail");
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void validateInvalidRequestOptions_partitionKeyAndEPK() {
        CosmosAsyncContainer container = this.gatewayJsonContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        String id = UUID.randomUUID().toString();
        TestDoc testDoc = this.populateTestDoc(id, id);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setProperties(
            new HashMap<String, Object>() {{
                put(HttpConstants.HttpHeaders.PARTITION_KEY, id);
                put(WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING, "0");
            }});

        ItemBatchOperation<TestDoc> operation = new ItemBatchOperation.Builder<TestDoc>(OperationType.Replace,0)
            .partitionKey(new PartitionKey(id))
            .resource(testDoc)
            .id(id)
            .build();

        try {
            CompletableFuture<Boolean> resp = executor.validateAndMaterializeOperation(operation, requestOptions);
            fail("Should throw exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalStateException && ex.getMessage().contains("partition key and effective partition key may not both be set"), "Should fail");
        }
    }

    private ItemBatchOperation<TestDoc> createItem(String id) {
        TestDoc testDoc = this.populateTestDoc(id, id);

        ItemBatchOperation<TestDoc> operation = new ItemBatchOperation.Builder<TestDoc>(OperationType.Create, 0)
            .partitionKey(new PartitionKey(id))
            .resource(testDoc)
            .build();
        return operation;
    }
}
