// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncConsumer;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * This class is responsible for balancing the load of processing events from all partitions of an Event Hub by
 * distributing the number of partitions uniformly among all the  active {@link EventProcessor}s.
 * <p>
 * This load balancer will retrieve partition ownership details from the {@link PartitionManager} to find the number of
 * active {@link EventProcessor}s. It uses the last modified time to decide if an EventProcessor is active. If a
 * partition ownership entry has not be updated for a specified duration of time, the owner of that partition is
 * considered inactive and the partition is available for other EventProcessors to own.
 * </p>
 * More details can be found <a href="https://gist.github.com/srnagar/7ef8566cfef0673288275c450dc99590">here</a>.
 */
public final class PartitionBasedLoadBalancer {

    private static final Random RANDOM = new Random();
    private final ClientLogger logger = new ClientLogger(PartitionBasedLoadBalancer.class);

    private final String eventHubName;
    private final String consumerGroupName;
    private final PartitionManager partitionManager;
    private final EventHubAsyncClient eventHubAsyncClient;
    private final String ownerId;
    private final long inactiveTimeLimitInSeconds;
    private final PartitionPumpManager partitionPumpManager;

    /**
     * Creates an instance of PartitionBasedLoadBalancer for the given Event Hub name and consumer group.
     *
     * @param partitionManager The partition manager that this load balancer will use to read/update ownership details.
     * @param eventHubAsyncClient The asynchronous Event Hub client used to consume events.
     * @param eventHubName The Event Hub name the {@link EventProcessor} is associated with.
     * @param consumerGroupName The consumer group name the {@link EventProcessor} is associated with.
     * @param ownerId The owner identifier for the {@link EventProcessor} this load balancer is associated with.
     * @param inactiveTimeLimitInSeconds The time in seconds to wait for an update on an ownership record before
     * assuming the owner of the partition is inactive.
     * @param partitionPumpManager The partition pump manager that keeps track of all EventHubConsumers and partitions
     * that this {@link EventProcessor} is processing.
     */
    public PartitionBasedLoadBalancer(final PartitionManager partitionManager,
        final EventHubAsyncClient eventHubAsyncClient,
        final String eventHubName, final String consumerGroupName, final String ownerId,
        final long inactiveTimeLimitInSeconds, final PartitionPumpManager partitionPumpManager) {
        this.partitionManager = partitionManager;
        this.eventHubAsyncClient = eventHubAsyncClient;
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
        this.ownerId = ownerId;
        this.inactiveTimeLimitInSeconds = inactiveTimeLimitInSeconds;
        this.partitionPumpManager = partitionPumpManager;
    }

    /**
     * This is the main method responsible for load balancing. This method is expected to be invoked by the {@link
     * EventProcessor} periodically. Every call to this method will result in this {@link EventProcessor} owning <b>at
     * most one</b> new partition.
     * <p>
     * The load is considered balanced when no active EventProcessor owns 2 partitions more than any other active
     * EventProcessor.Given that each invocation to this method results in ownership claim of at most one partition,
     * this algorithm converges gradually towards a steady state.
     * </p>
     * When a new partition is claimed, this method is also responsible for starting a partition pump that creates an
     * {@link EventHubAsyncConsumer} for processing events from that partition.
     */
    public void loadBalance() {
        /*
         * Retrieve current partition ownership details from the datastore.
         */
        final Mono<Map<String, PartitionOwnership>> partitionOwnershipMono = partitionManager
            .listOwnership(eventHubName, consumerGroupName)
            .timeout(Duration.ofSeconds(1)) // TODO: configurable by the user
            .collectMap(PartitionOwnership::partitionId, Function.identity());

        /*
         * Retrieve the list of partition ids from the Event Hub.
         */
        final Mono<List<String>> partitionsMono = eventHubAsyncClient
            .getPartitionIds()
            .timeout(Duration.ofSeconds(1)) // TODO: configurable
            .collectList();

        Mono.zip(partitionOwnershipMono, partitionsMono)
            .map(this::loadBalance)
            // if there was an error, log warning and TODO: call user provided error handler
            .doOnError(ex -> logger.warning("Load balancing for event processor failed - {}", ex.getMessage()))
            .subscribe();
    }

    /*
     * This method works with the given partition ownership details and Event Hub partitions to evaluate whether the
     * current Event Processor should take on the responsibility of processing more partitions.
     */
    private Mono<Void> loadBalance(final Tuple2<Map<String, PartitionOwnership>, List<String>> tuple) {

        Map<String, PartitionOwnership> partitionOwnershipMap = tuple.getT1();
        List<String> partitionIds = tuple.getT2();

        if (ImplUtils.isNullOrEmpty(partitionIds)) {
            // ideally, should never happen.
            return Mono.error(new IllegalStateException("There are no partitions in Event Hub " + this.eventHubName));
        }

        if (!isValid(partitionOwnershipMap)) {
            // User data is corrupt.
            return Mono.error(new IllegalStateException("Invalid partitionOwnership data from PartitionManager"));
        }

        /*
         * Remove all partitions ownerships that have not be modified for a long time. This means that the previous
         * event processor that owned the partition is probably down and the partition is now eligible to be
         * claimed by other event processors.
         */
        Map<String, PartitionOwnership> activePartitionOwnershipMap = removeInactivePartitionOwnerships(
            partitionOwnershipMap);

        int numberOfPartitions = partitionIds.size();
        if (ImplUtils.isNullOrEmpty(activePartitionOwnershipMap)) {
            /*
             * If the active partition ownership map is empty, this is the first time an event processor is
             * running or all Event Processors are down for this Event Hub, consumer group combination. All
             * partitions in this Event Hub are available to claim. Choose a random partition to claim ownership.
             */
            claimOwnership(partitionOwnershipMap, partitionIds.get(RANDOM.nextInt(numberOfPartitions)));
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
        int numberOfActiveEventProcessors = ownerPartitionMap.size();
        int minPartitionsPerEventProcessor = numberOfPartitions / numberOfActiveEventProcessors;

        /*
         * If the number of partitions in Event Hub is not evenly divisible by number of active event processors,
         * a few Event Processors may own 1 additional partition than the minimum when the load is balanced. Calculate
         * the number of event processors that can own additional partition.
         */
        int numberOfEventProcessorsWithAdditionalPartition = numberOfPartitions % numberOfActiveEventProcessors;

        if (isLoadBalanced(minPartitionsPerEventProcessor, numberOfEventProcessorsWithAdditionalPartition,
            ownerPartitionMap)) {
            // If the partitions are evenly distributed among all active event processors, no change required.
            return Mono.empty();
        }

        if (!shouldOwnMorePartitions(minPartitionsPerEventProcessor, ownerPartitionMap)) {
            // This event processor already has enough partitions and shouldn't own more.
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

    /*
     * Check if partition ownership data is valid before proceeding with load balancing.
     */
    private boolean isValid(final Map<String, PartitionOwnership> partitionOwnershipMap) {
        return partitionOwnershipMap.values()
            .stream()
            .noneMatch(partitionOwnership -> {
                return partitionOwnership.ownerId() == null
                    || partitionOwnership.eventHubName() == null
                    || !partitionOwnership.eventHubName().equals(this.eventHubName)
                    || partitionOwnership.consumerGroupName() == null
                    || !partitionOwnership.consumerGroupName().equals(this.consumerGroupName)
                    || partitionOwnership.partitionId() == null
                    || partitionOwnership.lastModifiedTime() == null
                    || partitionOwnership.eTag() == null;
            });
    }

    /*
     * Find the event processor that owns the maximum number of partitions and steal a random partition
     * from it.
     */
    private String findPartitionToSteal(final Map<String, List<PartitionOwnership>> ownerPartitionMap) {
        List<PartitionOwnership> maxList = ownerPartitionMap.values()
            .stream()
            .max(Comparator.comparingInt(List::size))
            .get();

        return maxList.get(RANDOM.nextInt(maxList.size())).partitionId();
    }

    /*
     * When the load is balanced, all active event processors own at least {@code minPartitionsPerEventProcessor}
     * and only {@code numberOfEventProcessorsWithAdditionalPartition} event processors will own 1 additional
     * partition.
     */
    private boolean isLoadBalanced(final int minPartitionsPerEventProcessor,
        final int numberOfEventProcessorsWithAdditionalPartition,
        final Map<String, List<PartitionOwnership>> ownerPartitionMap) {

        int count = 0;
        for (List<PartitionOwnership> partitionOwnership : ownerPartitionMap.values()) {
            int numberOfPartitions = partitionOwnership.size();
            if (numberOfPartitions < minPartitionsPerEventProcessor
                || numberOfPartitions > minPartitionsPerEventProcessor + 1) {
                return false;
            }

            if (numberOfPartitions == minPartitionsPerEventProcessor + 1) {
                count++;
            }
        }
        return count == numberOfEventProcessorsWithAdditionalPartition;
    }

    /*
     * This method is called after determining that the load is not balanced. This method will evaluate
     * if the current event processor should own more partitions. Specifically, this method returns true if the
     * current event processor owns less than the minimum number of partitions or if it owns the minimum number
     * and no other event processor owns lesser number of partitions than this event processor.
     */
    private boolean shouldOwnMorePartitions(final int minPartitionsPerEventProcessor,
        final Map<String, List<PartitionOwnership>> ownerPartitionMap) {

        int numberOfPartitionsOwned = ownerPartitionMap.get(this.ownerId).size();

        int leastPartitionsOwnedByAnyEventProcessor =
            ownerPartitionMap.values().stream().min(Comparator.comparingInt(List::size)).get().size();

        if (numberOfPartitionsOwned < minPartitionsPerEventProcessor
            || numberOfPartitionsOwned == leastPartitionsOwnedByAnyEventProcessor) {
            return true;
        }
        return false;
    }

    /*
     * This method will create a new map of partition id and PartitionOwnership containing only those partitions
     * that are actively owned. All entries in the original map returned by PartitionManager that haven't been
     * modified for a duration of time greater than the allowed inactivity time limit are assumed to be owned by
     * dead event processors. These will not be included in the map returned by this method.
     */
    private Map<String, PartitionOwnership> removeInactivePartitionOwnerships(
        final Map<String, PartitionOwnership> partitionOwnershipMap) {
        return partitionOwnershipMap
            .entrySet()
            .stream()
            .filter(entry -> {
                return System.currentTimeMillis() - entry.getValue().lastModifiedTime() < TimeUnit.SECONDS
                    .toMillis(inactiveTimeLimitInSeconds);
            }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void claimOwnership(final Map<String, PartitionOwnership> partitionOwnershipMap,
        final String partitionIdToClaim) {
        PartitionOwnership ownershipRequest = createPartitionOwnershipRequest(partitionOwnershipMap,
            partitionIdToClaim);

        partitionManager
            .claimOwnership(ownershipRequest)
            .timeout(Duration.ofSeconds(1)) // TODO: configurable
            .doOnError(ex -> logger
                .warning("Failed to claim ownership of partition {} - {}", ownershipRequest.partitionId(),
                    ex.getMessage(), ex))
            .subscribe(partitionPumpManager::startPartitionPump);
    }

    private PartitionOwnership createPartitionOwnershipRequest(
        final Map<String, PartitionOwnership> partitionOwnershipMap,
        final String partitionIdToClaim) {
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
