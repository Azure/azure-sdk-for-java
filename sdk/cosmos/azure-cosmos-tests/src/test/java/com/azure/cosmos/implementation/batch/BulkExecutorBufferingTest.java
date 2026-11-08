// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.CosmosBulkExecutionOptionsImpl;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

public class BulkExecutorBufferingTest {
    private BulkExecutor<?> executor;

    @BeforeClass(groups = { "unit" })
    public void setup() throws ReflectiveOperationException {
        // Skip client construction and its account discovery; execute the real buffering implementation.
        executor = mock(BulkExecutor.class, CALLS_REAL_METHODS);
        Field interval = BulkExecutor.class.getDeclaredField("maxMicroBatchIntervalInMs");
        interval.setAccessible(true);
        interval.set(executor, Long.MAX_VALUE);
        Field payloadSize = BulkExecutor.class.getDeclaredField("maxMicroBatchPayloadSizeInBytes");
        payloadSize.setAccessible(true);
        payloadSize.setInt(executor, Integer.MAX_VALUE);
    }

    @Test(groups = { "unit" })
    public void idleFlushMarkersDoNotProduceBuffersOnCompletion() {
        assertThat(executor.bufferOperations(idleFlushMarkers(), thresholds(100))
            .collectList().block(Duration.ofSeconds(5))).isEmpty();
    }

    @Test(groups = { "unit" })
    public void operationsAfterIdleAreFlushedWithoutRetainedMarkers() {
        CosmosItemOperation first = CosmosBulkOperations.getReadItemOperation("first", new PartitionKey("pk"));
        CosmosItemOperation second = CosmosBulkOperations.getReadItemOperation("second", new PartitionKey("pk"));
        Tuple2<Long, CosmosItemOperation> flush = Tuples.of(1L, FlushBuffersItemOperation.singleton());

        List<List<Tuple2<Long, CosmosItemOperation>>> batches = executor.bufferOperations(
            Flux.concat(idleFlushMarkers(), Flux.just(Tuples.of(0L, first), flush),
                idleFlushMarkers(), Flux.just(Tuples.of(0L, second), flush), idleFlushMarkers()),
            thresholds(100)).collectList().block(Duration.ofSeconds(5));

        assertThat(batches).hasSize(2);
        assertThat(batches.get(0)).containsExactly(Tuples.of(0L, first), flush);
        assertThat(batches.get(1)).containsExactly(Tuples.of(0L, second), flush);
    }

    @Test(groups = { "unit" })
    public void idleMarkersAfterSizeFlushDoNotEnterNextBuffer() {
        CosmosItemOperation first = CosmosBulkOperations.getReadItemOperation("first", new PartitionKey("pk"));
        CosmosItemOperation second = CosmosBulkOperations.getReadItemOperation("second", new PartitionKey("pk"));
        CosmosItemOperation third = CosmosBulkOperations.getReadItemOperation("third", new PartitionKey("pk"));

        List<List<Tuple2<Long, CosmosItemOperation>>> batches = executor.bufferOperations(
            Flux.concat(Flux.just(Tuples.of(0L, first), Tuples.of(0L, second)),
                idleFlushMarkers(), Flux.just(Tuples.of(0L, third))),
            thresholds(2)).collectList().block(Duration.ofSeconds(5));

        assertThat(batches).hasSize(2);
        assertThat(batches.get(0)).containsExactly(Tuples.of(0L, first), Tuples.of(0L, second));
        assertThat(batches.get(1)).containsExactly(Tuples.of(0L, third));
    }

    private static Flux<Tuple2<Long, CosmosItemOperation>> idleFlushMarkers() {
        return Flux.range(0, 10_000)
            .map(tick -> Tuples.of(tick.longValue(), FlushBuffersItemOperation.singleton()));
    }

    private static PartitionScopeThresholds thresholds(int maxMicroBatchSize) {
        CosmosBulkExecutionOptionsImpl options = new CosmosBulkExecutionOptionsImpl();
        options.setInitialMicroBatchSize(maxMicroBatchSize);
        options.setMaxMicroBatchSize(maxMicroBatchSize);
        return new PartitionScopeThresholds("0", options);
    }
}
