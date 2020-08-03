// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.HashedWheelTimer;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import static com.azure.cosmos.batch.EmulatorTest.BatchTestBase.BATCH_TEST_TIMEOUT;
import static org.testng.Assert.*;

public class BatchAsyncStreamerTests {

    private int MaxBatchByteSize = 100000;
    private int defaultMaxDegreeOfConcurrency = 10;
    private static Exception expectedException = new Exception("Failed exception");
    private HashedWheelTimer timer = new HashedWheelTimer();
    private Semaphore limiter = new Semaphore(1);

    public Mono<PartitionKeyRangeBatchExecutionResult> executeAsync(PartitionKeyRangeServerBatchRequest request) throws Exception {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[request.getOperations().size()];
        int index = 0;
        for (ItemBatchOperation<?> operation : request.getOperations()) {
            TransactionalBatchOperationResult<?> result = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);
            result.setETag(operation.getId());
            results.add(result);

            arrayOperations[index++] = operation;
        }

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        try {
            Thread.sleep(20);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        StoreResponse storeResponse = new StoreResponse(
            HttpResponseStatus.OK.code(),
            new ArrayList<>(),
            responseContent.getBytes(StandardCharsets.UTF_8));

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            new RxDocumentServiceResponse(storeResponse),
            batchRequest,
            true).block();

        return Mono.just(new PartitionKeyRangeBatchExecutionResult(request.getPartitionKeyRangeId(), request.getOperations(), batchresponse));
    }

    private Mono<PartitionKeyRangeBatchExecutionResult> executorWithFailure(PartitionKeyRangeServerBatchRequest request) throws Exception {
        throw expectedException;
     }

    private Mono<Void> reBatchAsync(ItemBatchOperation<?> operation) {
        return Mono.just(true).then();
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void validatesSize() {
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            -1,
            MaxBatchByteSize,
            this.timer,
            this.limiter,
            1,
            this::executeAsync,
            this::reBatchAsync);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = NullPointerException.class)
    public void validatesExecutor() {
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            1,
            MaxBatchByteSize,
            this.timer,
            this.limiter,
            1,
            null,
            this::reBatchAsync);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = NullPointerException.class)
    public void validatesRetrier() {
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            1,
            MaxBatchByteSize,
            this.timer,
            this.limiter,
            1,
            this::executeAsync,
            null);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT, expectedExceptions = NullPointerException.class)
    public void validatesLimiter() {
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            1,
            MaxBatchByteSize,
            this.timer,
            null,
            1,
            this::executeAsync,
            this::reBatchAsync);
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void exceptionsOnBatchBubbleUpAsync() {
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            2,
            MaxBatchByteSize,
            this.timer,
            this.limiter,
            1,
            this::executorWithFailure,
            this::reBatchAsync);

        ItemBatchOperation<?> itemBatchOperation =  new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        ItemBatchOperationContext context = attachContext(itemBatchOperation);
        batchAsyncStreamer.add(itemBatchOperation);

        try {
            TransactionalBatchOperationResult<?> result = context.getOperationResultFuture().get();
            fail("Should throw exception");
        } catch (Exception ex) {
            assertEquals(expectedException, ex.getCause());
        }
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void timerDispatchesAsync() throws Exception {
        // Bigger batch size than the amount of operations, timer should dispatch
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            2,
            MaxBatchByteSize,
            this.timer,
            this.limiter,
            1,
            this::executeAsync,
            this::reBatchAsync);

        ItemBatchOperation<?> itemBatchOperation =  new ItemBatchOperation.Builder<Object>(OperationType.Create,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        ItemBatchOperationContext context = attachContext(itemBatchOperation);
        batchAsyncStreamer.add(itemBatchOperation);
        TransactionalBatchOperationResult<?> result = context.getOperationResultFuture().get();

        assertEquals(itemBatchOperation.getId(), result.getETag());
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void validatesCongestionControlAsync() {
        Semaphore newLimiter = new Semaphore(1);
        BatchAsyncStreamer batchAsyncStreamer =  new BatchAsyncStreamer(
            2,
            MaxBatchByteSize,
            this.timer,
            newLimiter,
            defaultMaxDegreeOfConcurrency,
            this::executeAsync,
            this::reBatchAsync);

        assertEquals(newLimiter.availablePermits(), 1);

        List<CompletableFuture<TransactionalBatchOperationResult<?>>> contexts = new ArrayList<>(10);
        for (int i = 0; i < 600; i++) {
            ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create, i)
                .partitionKey(PartitionKey.NONE)
                .id(String.valueOf(i))
                .build();

            ItemBatchOperationContext context = attachContext(operation);
            batchAsyncStreamer.add(operation);
            contexts.add(context.getOperationResultFuture());
        }

        // 300 batch request should atleast sum up to 1000 ms barrier with wait time of 20ms in executor
        CompletableFuture.allOf(contexts.toArray(new CompletableFuture<?>[contexts.size()]));

        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        assertTrue(newLimiter.availablePermits() >= 2, "Count of threads that can enter into semaphore should increase atleast by 1");
    }

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void dispatchesAsync() throws Exception {
        // Expect all operations to complete as their batches get dispached
        BatchAsyncStreamer batchAsyncStreamer = new BatchAsyncStreamer(
            2,
            MaxBatchByteSize,
            this.timer,
            this.limiter,
            1,
            this::executeAsync,
            this::reBatchAsync);

        List<CompletableFuture<TransactionalBatchOperationResult<?>>> contexts = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Create, i)
                .partitionKey(PartitionKey.NONE)
                .id(String.valueOf(i))
                .build();

            ItemBatchOperationContext context = attachContext(operation);
            batchAsyncStreamer.add(operation);
            contexts.add(context.getOperationResultFuture());
        }

        for (int i = 0; i < 10; i++) {
            CompletableFuture<TransactionalBatchOperationResult<?>> context = contexts.get(i);
            TransactionalBatchOperationResult<?> response = context.get();

            assertTrue(context.isDone());
            assertEquals(String.valueOf(i), response.getETag());
        }
    }

    private static ItemBatchOperationContext attachContext(ItemBatchOperation<?> operation) {
        ItemBatchOperationContext context = new ItemBatchOperationContext("");
        operation.attachContext(context);
        return context;
    }
}
