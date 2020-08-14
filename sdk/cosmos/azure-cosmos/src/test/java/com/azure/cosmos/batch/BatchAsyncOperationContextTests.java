// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class BatchAsyncOperationContextTests {

    private static final int TIMEOUT = 40000;

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void partitionKeyRangeIdIsSetOnInitialization() {
        String expectedPkRangeId = UUID.randomUUID().toString();
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        ItemBatchOperationContext batchAsyncOperationContext = new ItemBatchOperationContext(expectedPkRangeId);
        operation.attachContext(batchAsyncOperationContext);

        assertNotNull(batchAsyncOperationContext.getOperationResultFuture());
        assertEquals(batchAsyncOperationContext, operation.getContext());
        assertEquals(expectedPkRangeId, batchAsyncOperationContext.getPartitionKeyRangeId());
        assertFalse(batchAsyncOperationContext.getOperationResultFuture().isDone());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void taskIsCreatedOnInitialization() {
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();
        ItemBatchOperationContext batchAsyncOperationContext = new ItemBatchOperationContext("");
        operation.attachContext(batchAsyncOperationContext);

        assertNotNull(batchAsyncOperationContext.getOperationResultFuture());
        assertEquals(batchAsyncOperationContext, operation.getContext());
        assertFalse(batchAsyncOperationContext.getOperationResultFuture().isDone());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void taskResultIsSetOnCompleteAsync() throws Exception {
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();
        ItemBatchOperationContext batchAsyncOperationContext = new ItemBatchOperationContext("");
        operation.attachContext(batchAsyncOperationContext);

        TransactionalBatchOperationResult<?> expected = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);

        batchAsyncOperationContext.complete(null, expected);

        assertEquals(expected, batchAsyncOperationContext.getOperationResultFuture().get());
        assertTrue(batchAsyncOperationContext.getOperationResultFuture().isDone());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void exceptionIsSetOnFailAsync() {
        Exception failure = new Exception("It failed");
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();
        ItemBatchOperationContext batchAsyncOperationContext = new ItemBatchOperationContext("");
        operation.attachContext(batchAsyncOperationContext);

        batchAsyncOperationContext.fail(null, failure);


        try{
            batchAsyncOperationContext.getOperationResultFuture().get();
            fail("Should throw exception");
        } catch (Exception capturedException) {
            assertEquals(failure, capturedException.getCause());
        }

        assertTrue(batchAsyncOperationContext.getOperationResultFuture().isCompletedExceptionally());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void cannotAttachMoreThanOnce() {
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();
        operation.attachContext(new ItemBatchOperationContext(""));

        try{
            operation.attachContext(new ItemBatchOperationContext(""));
            fail("Should throw exception");
        } catch (IllegalArgumentException capturedException) {
            assertNotNull(capturedException);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void shouldRetry_NoPolicy() {
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        operation.attachContext(new ItemBatchOperationContext(""));
        IRetryPolicy.ShouldRetryResult shouldRetryResult = operation.getContext().shouldRetry(result).block();
        assertFalse(shouldRetryResult.shouldRetry);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void shouldRetry_WithPolicy_OnSuccess() {
        BatchPartitionKeyRangeGoneRetryPolicy retryPolicy = new BatchPartitionKeyRangeGoneRetryPolicy(
            new ResourceThrottleRetryPolicy(1));
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();
        operation.attachContext(new ItemBatchOperationContext("", retryPolicy));
        IRetryPolicy.ShouldRetryResult shouldRetryResult = operation.getContext().shouldRetry(result).block();
        assertFalse(shouldRetryResult.shouldRetry);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void shouldRetry_WithPolicy_On429() {
        BatchPartitionKeyRangeGoneRetryPolicy retryPolicy = new BatchPartitionKeyRangeGoneRetryPolicy(
            new ResourceThrottleRetryPolicy(1));
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.TOO_MANY_REQUESTS);
        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();
        operation.attachContext(new ItemBatchOperationContext("", retryPolicy));
        IRetryPolicy.ShouldRetryResult shouldRetryResult = operation.getContext().shouldRetry(result).block();
        assertTrue(shouldRetryResult.shouldRetry);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void shouldRetry_WithPolicy_OnSplit() {
        BatchPartitionKeyRangeGoneRetryPolicy retryPolicy = new BatchPartitionKeyRangeGoneRetryPolicy(
            new ResourceThrottleRetryPolicy(1));
        TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.GONE);
        result.setSubStatusCode(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);

        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        operation.attachContext(new ItemBatchOperationContext("", retryPolicy));
        IRetryPolicy.ShouldRetryResult shouldRetryResult = operation.getContext().shouldRetry(result).block();
        assertTrue(shouldRetryResult.shouldRetry);
    }

}
