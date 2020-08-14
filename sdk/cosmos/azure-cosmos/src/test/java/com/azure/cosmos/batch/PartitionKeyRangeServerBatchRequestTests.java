// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.testng.AssertJUnit.assertEquals;

public class PartitionKeyRangeServerBatchRequestTests {

    private static final int TIMEOUT = 40000;

    private ItemBatchOperation<?> createItemBatchOperation(String id) {
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(null)
            .id(id)
            .build();

        return operation;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void fitsAllOperations() throws Exception {
        List<ItemBatchOperation<?>> operations = new ArrayList<ItemBatchOperation<?>>() {{
            createItemBatchOperation("");
            createItemBatchOperation("");
        }};

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createAsync(
        "0",
            operations,
        200000,
        2,
        false).get();

        assertEquals(operations.size(), serverOperationBatchRequest.getBatchRequest().getOperations().size());
        assertEquals(operations, serverOperationBatchRequest.getBatchRequest().getOperations());
        assertEquals(0, serverOperationBatchRequest.getBatchPendingOperations().size());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void overflowsBasedOnCount()  throws Exception {
        List<ItemBatchOperation<?>> operations = new ArrayList<ItemBatchOperation<?>>() {{
            add(createItemBatchOperation("1"));
            add(createItemBatchOperation("2"));
            add(createItemBatchOperation("3"));
        }};

        // Setting max count to 1
        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createAsync(
            "0",
            operations,
            200000,
            1,
            false).get();

        assertEquals(1, serverOperationBatchRequest.getBatchRequest().getOperations().size());
        assertEquals(operations.get(0).getId(), serverOperationBatchRequest.getBatchRequest().getOperations().get(0).getId());
        assertEquals(2, serverOperationBatchRequest.getBatchPendingOperations().size());
        assertEquals(operations.get(1).getId(), serverOperationBatchRequest.getBatchPendingOperations().get(0).getId());
        assertEquals(operations.get(2).getId(), serverOperationBatchRequest.getBatchPendingOperations().get(1).getId());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void overflowsBasedOnCountWithOffset() throws Exception {
        List<ItemBatchOperation<?>> operations = new ArrayList<ItemBatchOperation<?>>() {{
            add(createItemBatchOperation("1"));
            add(createItemBatchOperation("2"));
            add(createItemBatchOperation("3"));
        }};

        // Setting max count to 1
        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createAsync(
            "0",
            operations.subList(1, 3),
            200000,
            1,
            false).get();

        assertEquals(1, serverOperationBatchRequest.getBatchRequest().getOperations().size());
        // The first element is not taken into account due to an Offset of 1
        assertEquals(operations.get(1).getId(), serverOperationBatchRequest.getBatchRequest().getOperations().get(0).getId());
        assertEquals(1, serverOperationBatchRequest.getBatchPendingOperations().size());
        assertEquals(operations.get(2).getId(), serverOperationBatchRequest.getBatchPendingOperations().get(0).getId());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void partitionKeyRangeServerBatchRequestSizeTests() throws Exception {

        int docSizeInBytes = 250;
        int operationCount = 10;

        for (int expectedOperationCount : new int[] { 1, 2, 5, 10 }) {
             PartitionKeyRangeServerBatchRequestTests.verifyServerRequestCreationsBySizeAsync(expectedOperationCount, operationCount, docSizeInBytes);
             PartitionKeyRangeServerBatchRequestTests.verifyServerRequestCreationsByCountAsync(expectedOperationCount, operationCount, docSizeInBytes);
        }
    }

    private static void verifyServerRequestCreationsBySizeAsync(
        int expectedOperationCount,
        int operationCount,
        int docSizeInBytes) throws Exception {

        int perRequestOverheadEstimateInBytes = 30;
        int perDocOverheadEstimateInBytes = 50;
        int maxServerRequestBodyLength = ((docSizeInBytes + perDocOverheadEstimateInBytes) * expectedOperationCount) + perRequestOverheadEstimateInBytes;
        int maxServerRequestOperationCount = Integer.MAX_VALUE;

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequestTests.getBatchWithCreateOperationsAsync(operationCount, maxServerRequestBodyLength, maxServerRequestOperationCount, docSizeInBytes);

        assertEquals(expectedOperationCount, serverOperationBatchRequest.getBatchRequest().getOperations().size());
        assertEquals(serverOperationBatchRequest.getBatchPendingOperations().size(), operationCount - serverOperationBatchRequest.getBatchRequest().getOperations().size());
    }

    private static void verifyServerRequestCreationsByCountAsync(
        int expectedOperationCount,
        int operationCount,
        int docSizeInBytes) throws Exception {

        int maxServerRequestBodyLength = Integer.MAX_VALUE;
        int maxServerRequestOperationCount = expectedOperationCount;

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequestTests.getBatchWithCreateOperationsAsync(operationCount, maxServerRequestBodyLength, maxServerRequestOperationCount, docSizeInBytes);

        assertEquals(expectedOperationCount, serverOperationBatchRequest.getBatchRequest().getOperations().size());
        assertEquals(serverOperationBatchRequest.getBatchPendingOperations().size(), operationCount - serverOperationBatchRequest.getBatchRequest().getOperations().size());
    }

    private static ServerOperationBatchRequest getBatchWithCreateOperationsAsync(
        int operationCount,
        int maxServerRequestBodyLength,
        int maxServerRequestOperationCount,
        int docSizeInBytes) throws Exception {
        List<ItemBatchOperation<?>> operations = new ArrayList<>();

        byte[] body = new byte[docSizeInBytes - 4];
        Random random = new Random();
        random.nextBytes(body);
        for (int i = 0; i < operationCount; i++) {
            ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
                .partitionKey(null)
                .id("")
                .build();

            operation.setMaterialisedResource("{\"" + StringUtils.repeat("x", docSizeInBytes - 11) + "\": \"ahd\""+ "}");

            operations.add(operation);
        }

        return PartitionKeyRangeServerBatchRequest.createAsync("0",
            operations,
            maxServerRequestBodyLength,
            maxServerRequestOperationCount,
            false).get();
    }

}
