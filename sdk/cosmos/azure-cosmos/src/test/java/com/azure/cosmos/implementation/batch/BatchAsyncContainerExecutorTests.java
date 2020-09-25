// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.BatchTestBase;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class BatchAsyncContainerExecutorTests extends BatchTestBase {

    private CosmosAsyncClient batchClient;
    private CosmosAsyncContainer batchContainer;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public BatchAsyncContainerExecutorTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_BatchAsyncContainerExecutorTests() {
        assertThat(this.batchClient).isNull();
        this.batchClient = getClientBuilder().buildAsyncClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.batchClient);
        batchContainer = batchClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.batchClient).isNotNull();
        this.batchClient.close();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void addAsync() {
        CosmosAsyncContainer container = batchContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        List<String> idList = new ArrayList<>();
        List<Mono<TransactionalBatchOperationResult<?>>> responseMonos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            idList.add(id);
            responseMonos.add(executor.addAsync(createItem(id), null));
        }

        for (int i = 0; i < 100; i++) {
            TransactionalBatchOperationResult<?> response = responseMonos.get(i).block();
            assertEquals(HttpResponseStatus.CREATED.code(), response.getResponseStatus());

            TestDoc document = Utils.parse(response.getResourceObject().toString(), TestDoc.class);
            assertEquals(idList.get(i), document.getId());

            CosmosItemResponse<TestDoc> storedDoc = container.readItem(idList.get(i), new PartitionKey(idList.get(i)), TestDoc.class).block();
            assertNotNull(storedDoc.getItem());

            String diagnostic = response.getCosmosDiagnostics().toString();
            assertFalse(Strings.isNullOrEmpty(diagnostic));
            assertTrue(diagnostic.contains("bulkSemaphoreStatistics"));
        }

        executor.close();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void validateRequestOptions_Simple() {
        CosmosAsyncContainer container = batchContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

        String id = UUID.randomUUID().toString();
        TestDoc testDoc = this.populateTestDoc(id, id);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setProperties(new HashMap<>());

        ItemBatchOperation<TestDoc> operation = new ItemBatchOperation.Builder<TestDoc>(OperationType.Replace,0)
            .partitionKey(new PartitionKey(id))
            .resource(testDoc)
            .id(id)
            .build();

        executor.validateAndMaterializeOperation(operation, requestOptions);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void validateInvalidRequestOptions_sessionToken() {
        CosmosAsyncContainer container = batchContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

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
            executor.validateAndMaterializeOperation(operation, requestOptions);
            fail("Should throw exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalStateException && ex.getMessage().contains("UnsupportedBulkRequestOptions"), "Should fail");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void validateInvalidRequestOptions_PartitionKey() {
        CosmosAsyncContainer container = batchContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

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
            executor.validateAndMaterializeOperation(operation, requestOptions);
            fail("Should throw exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalStateException && ex.getMessage().contains("expected byte array value for"), "Should fail");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void validateInvalidRequestOptions_partitionKeyAndEPK() {
        CosmosAsyncContainer container = batchContainer;
        BatchAsyncContainerExecutor executor = new BatchAsyncContainerExecutor(container, 20, BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES);

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
            executor.validateAndMaterializeOperation(operation, requestOptions);
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
