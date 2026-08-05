// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class PartitionKeyRangeServerBatchRequestTests {

    private static final int TIMEOUT = 40000;

    private CosmosItemOperation createItemBulkOperation(String id) {
        ItemBulkOperation<?, ?> operation = new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE,
            id,
            PartitionKey.NONE,
            null,
            null,
            null
        );

        return operation;
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void fitsAllOperations() {
        List<CosmosItemOperation> operations = new ArrayList<CosmosItemOperation>() {{
            createItemBulkOperation("");
            createItemBulkOperation("");
        }};

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            operations,
            200000,
            2,
            null);

        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().size()).isEqualTo(operations.size());
        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations()).isEqualTo(operations);
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().size()).isZero();
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void overflowsBasedOnCount() {
        List<CosmosItemOperation> operations = new ArrayList<CosmosItemOperation>() {{
            add(createItemBulkOperation("1"));
            add(createItemBulkOperation("2"));
            add(createItemBulkOperation("3"));
        }};

        // Setting max count to 0, at least one element will always get added
        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            operations,
            200000,
            0,
            null);

        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().size()).isEqualTo(1);
        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().get(0).getId()).isEqualTo(operations.get(0).getId());

        assertThat(serverOperationBatchRequest.getBatchPendingOperations().size()).isEqualTo(2);
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().get(0).getId()).isEqualTo(operations.get(1).getId());
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().get(1).getId()).isEqualTo(operations.get(2).getId());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void overflowsBasedOnCountWithOffset() {
        List<CosmosItemOperation> operations = new ArrayList<CosmosItemOperation>() {{
            add(createItemBulkOperation("1"));
            add(createItemBulkOperation("2"));
            add(createItemBulkOperation("3"));
        }};

        // Setting max count to 1
        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            operations.subList(1, 3),
            200000,
            1,
            null);

        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().size()).isEqualTo(1);

        // The first element is not taken into account due to an Offset of 1
        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().get(0).getId()).isEqualTo(operations.get(1).getId());
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().size()).isEqualTo(1);
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().get(0).getId()).isEqualTo(operations.get(2).getId());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void hybridRowSerializesEachItemOnce() {
        AtomicInteger serializationCount = new AtomicInteger();
        CosmosItemSerializer serializer = new CosmosItemSerializer() {
            @Override
            public <T> Map<String, Object> serialize(T item) {
                serializationCount.incrementAndGet();
                return Collections.singletonMap("id", item);
            }

            @Override
            public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
                throw new UnsupportedOperationException();
            }
        };
        List<CosmosItemOperation> operations = Collections.singletonList(new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE, "one", PartitionKey.NONE, null, "one", null));

        PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0", operations, Integer.MAX_VALUE, 100, serializer, true);

        assertThat(serializationCount).hasValue(1);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void hybridRowUsesExactEncodedSizeForOverflow() {
        JsonSerializable item = new JsonSerializable();
        item.set("payload", StringUtils.repeat("x", 128));
        List<CosmosItemOperation> operations = new ArrayList<>();
        operations.add(new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE, "one", PartitionKey.NONE, null, item, null));
        operations.add(new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE, "two", PartitionKey.NONE, null, item, null));

        ServerOperationBatchRequest single = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0", operations.subList(0, 1), Integer.MAX_VALUE, 100, null, true);
        int exactOneOperationLength = single.getBatchRequest().getRequestBody().length;
        ServerOperationBatchRequest split = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0", operations, exactOneOperationLength, 100, null, true);

        assertThat(split.getBatchRequest().getOperations()).hasSize(1);
        assertThat(split.getBatchPendingOperations()).hasSize(1);
        assertThat(split.getBatchRequest().getRequestBody()).hasSize(exactOneOperationLength);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT * 10)
    public void partitionKeyRangeServerBatchRequestSizeTests() {

        int docSizeInBytes = 250;
        int operationCount = 10;

        for (int expectedOperationCount : new int[] { 1, 2, 5, 10 }) {
             PartitionKeyRangeServerBatchRequestTests.
                 verifyServerRequestCreationsBySizeAsync(expectedOperationCount, operationCount, docSizeInBytes);
             PartitionKeyRangeServerBatchRequestTests.
                 verifyServerRequestCreationsByCountAsync(expectedOperationCount, operationCount, docSizeInBytes);
        }
    }

    private static void verifyServerRequestCreationsBySizeAsync(
        int expectedOperationCount,
        int operationCount,
        int docSizeInBytes) {

        int perDocOverheadEstimateInBytes = 50;
        int maxServerRequestBodyLength = (docSizeInBytes + perDocOverheadEstimateInBytes) * expectedOperationCount;
        int maxServerRequestOperationCount = Integer.MAX_VALUE;

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequestTests.
            getBatchWithCreateOperationsAsync(operationCount, maxServerRequestBodyLength, maxServerRequestOperationCount, docSizeInBytes);

        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().size()).isEqualTo(expectedOperationCount);
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().size()).
            isEqualTo(operationCount - serverOperationBatchRequest.getBatchRequest().getOperations().size());
    }

    private static void verifyServerRequestCreationsByCountAsync(
        int expectedOperationCount,
        int operationCount,
        int docSizeInBytes) {

        int maxServerRequestBodyLength = Integer.MAX_VALUE;

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequestTests.
            getBatchWithCreateOperationsAsync(operationCount, maxServerRequestBodyLength, expectedOperationCount, docSizeInBytes);

        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().size()).isEqualTo(expectedOperationCount);
        assertThat(serverOperationBatchRequest.getBatchPendingOperations().size()).
            isEqualTo(operationCount - serverOperationBatchRequest.getBatchRequest().getOperations().size());
    }

    private static ServerOperationBatchRequest getBatchWithCreateOperationsAsync(
        int operationCount,
        int maxServerRequestBodyLength,
        int maxServerRequestOperationCount,
        int docSizeInBytes) {
        List<CosmosItemOperation> operations = new ArrayList<>();

        for (int i = 0; i < operationCount; i++) {
            JsonSerializable jsonSerializable = new JsonSerializable();
            jsonSerializable.set("abc", StringUtils.repeat("x", docSizeInBytes - 10));// {"abc":" + "} = 10

            ItemBulkOperation<?, ?> operation = new ItemBulkOperation<>(
                CosmosItemOperationType.CREATE,
                "",
                null,
                null,
                jsonSerializable,
                null
            );

            operations.add(operation);
        }

        return PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            operations,
            maxServerRequestBodyLength,
            maxServerRequestOperationCount,
            null);
    }
}
