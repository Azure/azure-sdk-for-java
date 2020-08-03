// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.azure.cosmos.batch.EmulatorTest.BatchTestBase.BATCH_TEST_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class BatchAsyncBatcherTests {

    private static Exception expectedException = new Exception("expectedException");
    private static BatchPartitionMetric metric = new BatchPartitionMetric();

    private ItemBatchOperation<?> createItemBatchOperation(boolean withContext) {
        ItemBatchOperation<?> operation =
            new ItemBatchOperation.Builder<>(OperationType.Create, 0)
                .partitionKey(PartitionKey.NONE)
                .id("ac")
            .build();

        if (withContext) {
            operation.attachContext(new ItemBatchOperationContext(""));
        }

        return operation;
    }

    public Mono<PartitionKeyRangeBatchExecutionResult> executeAsync(PartitionKeyRangeServerBatchRequest request) throws Exception {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[request.getOperations().size()];
        int index = 0;
        for (ItemBatchOperation<?> operation : request.getOperations()) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<>(HttpResponseStatus.OK);
            result.setETag(operation.getId());
            results.add(result);

            arrayOperations[index++] = operation;
        }

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        StoreResponseBuilder storeResponseBuilder = new StoreResponseBuilder();
        storeResponseBuilder.withStatus(HttpResponseStatus.OK.code());
        storeResponseBuilder.withContent(responseContent);

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            new RxDocumentServiceResponse(storeResponseBuilder.build()),
            batchRequest,
            true).block();

        return Mono.just(new PartitionKeyRangeBatchExecutionResult(request.getPartitionKeyRangeId(), request.getOperations(), batchresponse));
    }

    public Mono<PartitionKeyRangeBatchExecutionResult> executorWithSplit(PartitionKeyRangeServerBatchRequest request) throws Exception {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[request.getOperations().size()];
        int index = 0;
        for (ItemBatchOperation<?> operation : request.getOperations()) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.GONE);
            result.setETag(operation.getId());
            result.setSubStatusCode(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);
            results.add(result);

            arrayOperations[index++] = operation;
        }

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        StoreResponseBuilder storeResponseBuilder = new StoreResponseBuilder();
        storeResponseBuilder.withStatus(HttpResponseStatus.GONE.code());
        storeResponseBuilder.withHeader(WFConstants.BackendHeaders.SUB_STATUS, String.valueOf(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE));
        storeResponseBuilder.withContent(responseContent);

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            new RxDocumentServiceResponse(storeResponseBuilder.build()),
            batchRequest,
            true).block();

        return Mono.just(new PartitionKeyRangeBatchExecutionResult(request.getPartitionKeyRangeId(), request.getOperations(), batchresponse));
    }

    public Mono<PartitionKeyRangeBatchExecutionResult> executorWithLessResponses(PartitionKeyRangeServerBatchRequest request) throws Exception {
        int operationCount = request.getOperations().size() - 2;
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[operationCount];
        int index = 0;
        List<ItemBatchOperation<?>> operationWithResponse = request.getOperations().subList(1, request.getOperations().size() - 1);

        for (ItemBatchOperation<?> operation : operationWithResponse) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);
            result.setETag(operation.getId());
            results.add(result);

            arrayOperations[index++] = operation;
        }

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        StoreResponseBuilder storeResponseBuilder = new StoreResponseBuilder();
        storeResponseBuilder.withStatus(HttpResponseStatus.OK.code());
        storeResponseBuilder.withContent(responseContent);

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            new RxDocumentServiceResponse(storeResponseBuilder.build()),
            batchRequest,
            true).block();

        return Mono.just(new PartitionKeyRangeBatchExecutionResult(request.getPartitionKeyRangeId(), request.getOperations(), batchresponse));
    }

    private Mono<PartitionKeyRangeBatchExecutionResult> executorWithFailure(PartitionKeyRangeServerBatchRequest request) throws Exception {
        throw expectedException;
    }

    private Mono<Void> reBatchAsync(ItemBatchOperation<?> operation)  {
        return Mono.just(true).then();
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void validatesSize() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(-1, 1, this::executeAsync, this::reBatchAsync);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void validatesByteSize() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(1, -1, this::executeAsync, this::reBatchAsync);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = NullPointerException.class)
    public void validatesExecutor() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(1, 1, null, this::reBatchAsync);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = NullPointerException.class)
    public void validatesRetrier() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(1, 1, this::executeAsync, null);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void hasFixedSize() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(2, 1000, this::executeAsync, this::reBatchAsync);
        assertTrue(batchAsyncBatcher.tryAdd(this.createItemBatchOperation(true)));
        assertTrue(batchAsyncBatcher.tryAdd(this.createItemBatchOperation(true)));
        assertFalse(batchAsyncBatcher.tryAdd(this.createItemBatchOperation(true)));
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void hasFixedByteSize() {
        ItemBatchOperation<?> itemBatchOperation = this.createItemBatchOperation(true);
        itemBatchOperation.materializeResource();
        // Each operation is 2 bytes
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(3, 4, this::executeAsync, this::reBatchAsync);
        assertTrue(batchAsyncBatcher.tryAdd(itemBatchOperation));
        assertTrue(batchAsyncBatcher.tryAdd(itemBatchOperation));
        assertFalse(batchAsyncBatcher.tryAdd(itemBatchOperation));
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void exceptionsFailOperationsAsync() throws Exception {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(2, 1000, this::executorWithFailure, this::reBatchAsync);
        ItemBatchOperation<?> operation1 = this.createItemBatchOperation(false);
        ItemBatchOperation<?> operation2 = this.createItemBatchOperation(false);

        ItemBatchOperationContext context1 = new ItemBatchOperationContext("");
        operation1.attachContext(context1);

        ItemBatchOperationContext context2 = new ItemBatchOperationContext("");
        operation2.attachContext(context2);

        batchAsyncBatcher.tryAdd(operation1);
        batchAsyncBatcher.tryAdd(operation2);

        batchAsyncBatcher.dispatchBatch(metric);

        try {
            TransactionalBatchOperationResult<?> result = context1.getOperationResultFuture().get();
            assertTrue(context1.getOperationResultFuture().isCompletedExceptionally());

            fail("Should throw exception");
        } catch (Exception ex) {
            assertEquals(expectedException, ex.getCause());
        }

        try {
            TransactionalBatchOperationResult<?> result = context2.getOperationResultFuture().get();
            assertTrue(context2.getOperationResultFuture().isCompletedExceptionally());

            fail("Should throw exception");
        } catch (Exception ex) {
            assertEquals(expectedException, ex.getCause());
        }
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void dispatchProcessInOrderAsync() throws Exception {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(10, 1000, this::executeAsync, this::reBatchAsync);
        List<ItemBatchOperation<?>> operations = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create, i)
                .partitionKey(new PartitionKey(String.valueOf(i)))
                .id(String.valueOf(i))
                .build();

            ItemBatchOperationContext context = new ItemBatchOperationContext("");
            operation.attachContext(context);
            operations.add(operation);
            assertTrue(batchAsyncBatcher.tryAdd(operation));
        }

        batchAsyncBatcher.dispatchBatch(metric);

        for (int i = 0; i < 10; i++) {
            ItemBatchOperation<?> operation = operations.get(i);
            TransactionalBatchOperationResult<?> result = operation.getContext().getOperationResultFuture().get();

            assertTrue(operation.getContext().getOperationResultFuture().isDone());
            assertEquals(String.valueOf(i), result.getETag());
         }
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void dispatchWithLessResponses() throws Exception {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(10, 1000, this::executorWithLessResponses, this::reBatchAsync);
        BatchAsyncBatcher secondAsyncBatcher = new BatchAsyncBatcher(10, 1000, this::executeAsync, this::reBatchAsync);
        List<ItemBatchOperation<?>> operations = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create, i)
                .partitionKey(new PartitionKey(String.valueOf(i)))
                .id(String.valueOf(i))
                .build();
            ItemBatchOperationContext context = new ItemBatchOperationContext("0");
            operation.attachContext(context);
            operations.add(operation);
            assertTrue(batchAsyncBatcher.tryAdd(operation));
        }

        batchAsyncBatcher.dispatchBatch(metric);

        try {
            // Wait for futures to complete
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        // Responses 1 and 10 should be missing
        for (int i = 0; i < 10; i++) {
            ItemBatchOperation<?> operation = operations.get(i);
            // Some tasks should not be resolved
            if (i == 0 || i == 9) {
                assertFalse(operation.getContext().getOperationResultFuture().isDone());
            } else {
                assertTrue(operation.getContext().getOperationResultFuture().isDone());
            }

            if (operation.getContext().getOperationResultFuture().isDone()) {
                TransactionalBatchOperationResult<?> result = operation.getContext().getOperationResultFuture().get();
                assertEquals(String.valueOf(i), result.getETag());
            } else {
                // Pass the pending one to another batcher
                assertTrue(secondAsyncBatcher.tryAdd(operation));
            }
        }

        secondAsyncBatcher.dispatchBatch(metric);
        // All tasks should be completed
        for (int i = 0; i < 10; i++) {
            ItemBatchOperation<?> operation = operations.get(i);
            TransactionalBatchOperationResult<?> result = operation.getContext().getOperationResultFuture().get();

            assertTrue(operation.getContext().getOperationResultFuture().isDone());
            assertEquals(String.valueOf(i), result.getETag());
        }
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void isEmptyWithNoOperations() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(10, 1000, this::executeAsync, this::reBatchAsync);
        assertTrue(batchAsyncBatcher.isEmpty());
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void isNotEmptyWithOperations() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(1, 1000, this::executeAsync, this::reBatchAsync);
        assertTrue(batchAsyncBatcher.tryAdd(this.createItemBatchOperation(true)));
        assertFalse(batchAsyncBatcher.isEmpty());
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void cannotAddToDispatchedBatch() {
        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(1, 1000, this::executeAsync, this::reBatchAsync);
        ItemBatchOperation<?> operation = this.createItemBatchOperation(false);
        operation.attachContext(new ItemBatchOperationContext(""));
        assertTrue(batchAsyncBatcher.tryAdd(operation));
        batchAsyncBatcher.dispatchBatch(metric);

        try {
            // Wait for batchAsyncBatcher to complete
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        assertFalse(batchAsyncBatcher.tryAdd(this.createItemBatchOperation(false)));
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void retrierGetsCalledOnSplit() {
        BatchPartitionKeyRangeGoneRetryPolicy retryPolicy1 = new BatchPartitionKeyRangeGoneRetryPolicy(
            new ResourceThrottleRetryPolicy(1));

        BatchPartitionKeyRangeGoneRetryPolicy retryPolicy2 = new BatchPartitionKeyRangeGoneRetryPolicy(
            new ResourceThrottleRetryPolicy(1));

        ItemBatchOperation<?> operation1 = this.createItemBatchOperation(false);
        ItemBatchOperation<?> operation2 = this.createItemBatchOperation(false);
        operation1.attachContext(new ItemBatchOperationContext("", retryPolicy1));
        operation2.attachContext(new ItemBatchOperationContext("", retryPolicy2));

        BatchAsyncBatcherRetrier retryDelegate = Mockito.mock(BatchAsyncBatcherRetrier.class);

        BatchAsyncBatcher batchAsyncBatcher = new BatchAsyncBatcher(2, 1000, this::executorWithSplit, retryDelegate);
        assertTrue(batchAsyncBatcher.tryAdd(operation1));
        assertTrue(batchAsyncBatcher.tryAdd(operation2));

        batchAsyncBatcher.dispatchBatch(metric);

        try {
            // Wait for batchAsyncBatcher to complete
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        verify(retryDelegate, times(1)).apply(operation1);
        verify(retryDelegate, times(1)).apply(operation2);
        verify(retryDelegate, times(2)).apply(any(ItemBatchOperation.class));
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void retrierGetsCalledOnOverFlow() {
        ItemBatchOperation<?> operation1 = this.createItemBatchOperation(false);
        ItemBatchOperation<?> operation2 = this.createItemBatchOperation(false);
        operation1.attachContext(new ItemBatchOperationContext(""));
        operation2.attachContext(new ItemBatchOperationContext(""));

        BatchAsyncBatcherRetrier retryDelegate = Mockito.mock(BatchAsyncBatcherRetrier.class);
        BatchAsyncBatcherExecutor executorDelegate = Mockito.mock(BatchAsyncBatcherExecutor.class);

        BatchAsyncBatcherThatOverflows batchAsyncBatcher = new BatchAsyncBatcherThatOverflows(2, 1000, executorDelegate, retryDelegate);
        assertTrue(batchAsyncBatcher.tryAdd(operation1));
        assertTrue(batchAsyncBatcher.tryAdd(operation2));
        batchAsyncBatcher.dispatchBatch(metric);

        try {
            // Wait for batchAsyncBatcher to complete
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        verify(retryDelegate, never()).apply(operation1);
        verify(retryDelegate, times(1)).apply(operation2);
        verify(retryDelegate, times(1)).apply(any(ItemBatchOperation.class));
    }

    private class BatchAsyncBatcherThatOverflows extends BatchAsyncBatcher {

        public BatchAsyncBatcherThatOverflows(
            int maxBatchOperationCount,
            int maxBatchByteSize,
            BatchAsyncBatcherExecutor executor,
            BatchAsyncBatcherRetrier retrier) {
            super(maxBatchOperationCount, maxBatchByteSize, executor, retrier);
        }

        @Override
        public  CompletableFuture<ServerOperationBatchRequest> createBatchRequestAsync() {
            CompletableFuture<ServerOperationBatchRequest> serverOperationBatchRequest = super.createBatchRequestAsync();

            return serverOperationBatchRequest
                .thenApplyAsync(batchRequest ->
                    new ServerOperationBatchRequest(
                        batchRequest.getBatchRequest(),
                        batchRequest.getBatchRequest().getOperations().subList(1, 2)));
        }
    }
}
