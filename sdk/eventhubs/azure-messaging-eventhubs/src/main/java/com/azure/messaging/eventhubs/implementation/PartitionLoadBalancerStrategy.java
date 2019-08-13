// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.CheckpointManager;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumer;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.PartitionProcessor;
import com.azure.messaging.eventhubs.PartitionProcessorFactory;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class PartitionLoadBalancerStrategy {

    private static final Random RANDOM = new Random();
    private final ClientLogger logger = new ClientLogger(PartitionLoadBalancerStrategy.class);

    private final String eventHubName;
    private final String consumerGroupName;
    private final PartitionManager partitionManager;
    private final EventHubAsyncClient eventHubAsyncClient;
    private final Map<String, EventHubConsumer> partitionPumps = new ConcurrentHashMap<>();
    private final String ownerId;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final EventPosition initialEventPosition;
    private final long inactiveTimeLimitInSeconds;

    public PartitionLoadBalancerStrategy(final PartitionManager partitionManager,
        final EventHubAsyncClient eventHubAsyncClient,
        final String eventHubName, final String consumerGroupName, final String ownerId,
        final PartitionProcessorFactory partitionProcessorFactory, final EventPosition initialEventPosition,
        final long inactiveTimeLimitInSeconds) {
        this.partitionManager = partitionManager;
        this.eventHubAsyncClient = eventHubAsyncClient;
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
        this.ownerId = ownerId;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.initialEventPosition = initialEventPosition;
        this.inactiveTimeLimitInSeconds = inactiveTimeLimitInSeconds;
    }

    public synchronized void runOnce() {
        final Mono<Map<String, PartitionOwnership>> partitionOwnershipMono = partitionManager
            .listOwnership(eventHubName, consumerGroupName)
            .collectMap(PartitionOwnership::partitionId, Function.identity());
        final Mono<List<String>> partitionsMono = eventHubAsyncClient.getPartitionIds().collectList();

        Mono.zip(partitionOwnershipMono, partitionsMono)
            .map(this::loadBalance)
            .doOnError(ex -> logger.warning("Load balancing for event processor failed - {}", ex.getMessage()))
            .subscribe();
    }

    public synchronized void stopAllPartitionPumps() {
        this.partitionPumps.forEach((partitionId, eventHubConsumer) -> {
            try {
                eventHubConsumer.close();
            } catch (Exception ex) {
                logger.warning("Failed to close consumer for partition {}", partitionId, ex);
            } finally {
                partitionPumps.remove(partitionId);
            }
        });
    }


    private Mono<Void> loadBalance(final Tuple2<Map<String, PartitionOwnership>, List<String>> tuple) {

        Map<String, PartitionOwnership> partitionOwnershipMap = tuple.getT1();
        List<String> partitionIds = tuple.getT2();

        if (ImplUtils.isNullOrEmpty(partitionIds)) {
            // should ideally never happen
            return Mono.error(new IllegalStateException("There are no partitions in Event Hub " + this.eventHubName));
        }


        /*
         * Remove all partitions ownerships that have not be modified for a long time. This means that the previous
         * event processor that owned the partition is probably down and the partition is now eligible to be
         * claimed by other event processors.
         */
        Map<String, PartitionOwnership> activePartitionOwnershipMap = removeInactivePartitionOwnerships(
            partitionOwnershipMap);

        if (ImplUtils.isNullOrEmpty(activePartitionOwnershipMap)) {
            /*
             * If the active partition ownership map is empty, this is the first time an event processor is
             * running or all Event Processors are down for this Event Hub, consumer group combination. All
             * partitions in this Event Hub are available to claim. Choose a random partition to claim ownership.
             */
            claimOwnership(partitionOwnershipMap, partitionIds.get(RANDOM.nextInt(partitionIds.size())));
            return Mono.empty();
        }

        /*
         * Create a map of owner id and a list of partitions it owns
         */
        Map<String, List<PartitionOwnership>> ownerPartitionMap = activePartitionOwnershipMap.values()
            .stream()
            .collect(
                Collectors.groupingBy(PartitionOwnership::ownerId, mapping(Function.identity(), toList())));

        // add the current event processor to the map if it doesn't exist
        ownerPartitionMap.putIfAbsent(this.ownerId, new ArrayList<>());

        /*
         * Find the minimum number of partitions every event processor should own when the load is
         * evenly distributed.
         */
        int minPartitionsPerEventProcessor = partitionIds.size() / ownerPartitionMap.size();

        /*
         * Due to the number of partitions in Event Hub and number of event processors running,
         * a few Event Processors may own 1 additional partition than the minimum. Calculate
         * the number of event processors that can own additional partition.
         */
        int numberOfEventProcessorsWithAdditionalPartition = partitionIds.size() % ownerPartitionMap.size();

        if (isLoadBalanced(minPartitionsPerEventProcessor, numberOfEventProcessorsWithAdditionalPartition,
            ownerPartitionMap)) {
            return Mono.empty();
        }

        if (!shouldOwnMorePartitions(minPartitionsPerEventProcessor, ownerPartitionMap)) {
            // This event processor already has enough partitions and shouldn't own more yet
            return Mono.empty();
        }

        // If we have reached this stage, this event processor has to claim/steal ownership of at least 1 more partition

        /*
         * If some partitions are unclaimed, this could be because an event processor is down and
         * it's partitions are now available for others to own or because event processors are just
         * starting up and gradually claiming partitions to own or new partitions were added to Event Hub.
         * Find any partition that is not actively owned and claim it.
         *
         * OR
         *
         * Find a partition to steal from another event processor. Pick the event processor that has owns the highest
         * number of partitions.
         */
        String partitionToClaim = partitionIds.parallelStream()
            .filter(partitionId -> !activePartitionOwnershipMap.containsKey(partitionId))
            .findAny()
            .orElseGet(() -> findPartitionToSteal(ownerPartitionMap));

        claimOwnership(partitionOwnershipMap, partitionToClaim);

        return Mono.empty();
    }

    private String findPartitionToSteal(Map<String, List<PartitionOwnership>> ownerPartitionMap) {
        List<PartitionOwnership> maxList = ownerPartitionMap.values()
            .stream()
            .max(Comparator.comparingInt(List::size))
            .get();

        return maxList.get(RANDOM.nextInt(maxList.size())).partitionId();
    }

    private boolean isLoadBalanced(int minPartitionsPerEventProcessor,
        int numberOfEventProcessorsWithAdditionalPartition, Map<String, List<PartitionOwnership>> ownerPartitionMap) {
        if (ownerPartitionMap.values()
            .stream()
            .noneMatch(ownershipList -> {
                return ownershipList.size() < minPartitionsPerEventProcessor
                    || ownershipList.size() > minPartitionsPerEventProcessor + 1;
            })) {
            long count = ownerPartitionMap.values()
                .stream()
                .filter(ownershipList -> ownershipList.size() == minPartitionsPerEventProcessor + 1)
                .count();

            return count == numberOfEventProcessorsWithAdditionalPartition;
        }
        return false;
    }

    private boolean shouldOwnMorePartitions(final int minPartitionsPerEventProcessor,
        final Map<String, List<PartitionOwnership>> ownerPartitionMap) {
        int numberOfPartitionsOwned = ownerPartitionMap.get(this.ownerId).size();

        int leastPartitionsOwnedByAnyEventProcessor =
            ownerPartitionMap.values().stream().min(Comparator.comparingInt(List::size)).get().size();

        if (numberOfPartitionsOwned > minPartitionsPerEventProcessor
            || numberOfPartitionsOwned > leastPartitionsOwnedByAnyEventProcessor) {
            return false;
        }

        return true;
    }

    private Map<String, PartitionOwnership> removeInactivePartitionOwnerships(
        Map<String, PartitionOwnership> partitionOwnershipMap) {
        return partitionOwnershipMap.entrySet().stream().filter(entry -> {
            return System.currentTimeMillis() - entry.getValue().lastModifiedTime() < TimeUnit.SECONDS
                .toMillis(inactiveTimeLimitInSeconds);
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void claimOwnership(final Map<String, PartitionOwnership> partitionOwnershipMap,
        final String partitionIdToClaim) {
        PartitionOwnership ownershipRequest = createPartitionOwnershipRequest(partitionOwnershipMap,
            partitionIdToClaim);

        partitionManager.claimOwnership(ownershipRequest)
            .subscribe(claimedOwnership -> {
                if (!partitionPumps.containsKey(claimedOwnership.partitionId())) {
                    startPartitionPump(claimedOwnership);
                }
            }, ex -> logger
                .warning("Failed to claim ownership of partition {} - {}", ownershipRequest.partitionId(),
                    ex.getMessage(), ex));
    }

    private void startPartitionPump(PartitionOwnership claimedOwnership) {
        PartitionContext partitionContext = new PartitionContext(claimedOwnership.partitionId(),
            this.eventHubName, this.consumerGroupName);
        CheckpointManager checkpointManager = new CheckpointManager(this.ownerId, partitionContext,
            this.partitionManager, null);
        PartitionProcessor partitionProcessor = this.partitionProcessorFactory
            .createPartitionProcessor(partitionContext, checkpointManager);

        EventPosition startFromEventPosition =
            claimedOwnership.sequenceNumber() == null ? initialEventPosition
                : EventPosition.fromSequenceNumber(claimedOwnership.sequenceNumber());

        EventHubConsumerOptions eventHubConsumerOptions = new EventHubConsumerOptions().ownerLevel(0L);
        EventHubConsumer eventHubConsumer = eventHubAsyncClient
            .createConsumer(this.consumerGroupName, claimedOwnership.partitionId(), startFromEventPosition,
                eventHubConsumerOptions);

        partitionPumps.put(claimedOwnership.partitionId(), eventHubConsumer);
        eventHubConsumer.receive().subscribe(partitionProcessor::processEvent,
            ex -> handleReceiveError(claimedOwnership, eventHubConsumer, partitionProcessor, ex));
    }

    private void handleReceiveError(PartitionOwnership claimedOwnership, EventHubConsumer eventHubConsumer,
        PartitionProcessor partitionProcessor, Throwable error) {

        partitionProcessor.processError(error);

        // if there was an error, it also marks the end of the event data stream
        // close the consumer and remove from partition pump map
        try {
            eventHubConsumer.close();
        } catch (IOException ex) {
            logger.warning("Failed to close Event Hub consumer for partition {}", claimedOwnership.partitionId(), ex);
        } finally {
            partitionPumps.remove(claimedOwnership.partitionId());
        }
    }

    private PartitionOwnership createPartitionOwnershipRequest(Map<String, PartitionOwnership> partitionOwnershipMap,
        String partitionIdToClaim) {
        PartitionOwnership previousPartitionOwnership = partitionOwnershipMap.get(partitionIdToClaim);
        PartitionOwnership partitionOwnershipRequest = new PartitionOwnership()
            .ownerId(this.ownerId)
            .partitionId(partitionIdToClaim)
            .consumerGroupName(this.consumerGroupName)
            .eventHubName(this.eventHubName)
            .sequenceNumber(previousPartitionOwnership == null ? null : previousPartitionOwnership.sequenceNumber())
            .offset(previousPartitionOwnership == null ? null : previousPartitionOwnership.offset())
            .eTag(previousPartitionOwnership == null ? null : previousPartitionOwnership.eTag())
            .ownerLevel(0L);
        return partitionOwnershipRequest;
    }
}
