// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.SETTLE;

public final class InstrumentedCheckpointStore implements CheckpointStore {
    private final CheckpointStore checkpointStore;
    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    private InstrumentedCheckpointStore(CheckpointStore checkpointStore, EventHubsTracer tracer, EventHubsMetricsProvider meter) {
        this.checkpointStore = checkpointStore;
        this.tracer = tracer;
        this.meter = meter;
    }

    public static CheckpointStore create(CheckpointStore checkpointStore, EventHubsConsumerInstrumentation instrumentation) {
        if (!instrumentation.isEnabled()) {
            return checkpointStore;
        }

        return new InstrumentedCheckpointStore(checkpointStore, instrumentation.getTracer(), instrumentation.getMeter());
    }

    @Override
    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return checkpointStore.listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroup);
    }

    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {
        return checkpointStore.claimOwnership(requestedPartitionOwnerships);
    }

    @Override
    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return checkpointStore.listCheckpoints(fullyQualifiedNamespace, eventHubName, consumerGroup);
    }

    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        return Mono.using(
                () -> new InstrumentationScope(tracer, meter)
                        .recordStartTime()
                        .setSpan(startSpan(checkpoint.getPartitionId())),
                scope -> checkpointStore.updateCheckpoint(checkpoint)
                        .doOnError(scope::setError)
                        .doOnCancel(scope::setCancelled),
                scope -> {
                    meter.reportCheckpoint(checkpoint, scope);
                    scope.close();
                });
    }

    private Context startSpan(String partitionId) {
        return tracer.isEnabled()
                ? tracer.startSpan(SETTLE, tracer.createStartOption(SpanKind.INTERNAL, SETTLE, partitionId), Context.NONE)
                : Context.NONE;
    }
}
