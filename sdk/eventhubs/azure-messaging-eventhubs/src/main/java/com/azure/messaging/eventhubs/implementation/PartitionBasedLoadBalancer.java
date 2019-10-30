// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncConsumer;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.EventProcessorStore;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * This class is responsible for balancing the load of processing events from all partitions of an Event Hub by
 * distributing the number of partitions uniformly among all the  active {@link EventProcessor EventProcessors}.
 * <p>
 * This load balancer will retrieve partition ownership details from the {@link EventProcessorStore} to find the number
 * of active {@link EventProcessor EventProcessors}. It uses the last modified time to decide if an EventProcessor is
 * active. If a partition ownership entry has not be updated for a specified duration of time, the owner of that
 * partition is considered inactive and the partition is available for other EventProcessors to own.
 * </p>
 */
public final class PartitionBasedLoadBalancer {

    private static final Random RANDOM = new Random();
    private final ClientLogger logger = new ClientLogger(PartitionBasedLoadBalancer.class);

    private final String eventHubName;
    private final String consumerGroupName;
    private final EventProcessorStore eventProcessorStore;
    private final EventHubAsyncClient eventHubAsyncClient;
    private final String ownerId;
    private final long inactiveTimeLimitInSeconds;
    private final PartitionPumpManager partitionPumpManager;
    private final String fullyQualifiedNamespace;

    /**
     * Creates an instance of PartitionBasedLoadBalancer for the given Event Hub name and consumer group.
     *
     * @param eventProcessorStore The partition manager that this load balancer will use to read/update ownership
     * details.
     * @param eventHubAsyncClient The asynchronous Event Hub client used to consume events.
     * @param eventHubName The Event Hub name the {@link EventProcessor} is associated with.
     * @param consumerGroupName The consumer group name the {@link EventProcessor} is associated with.
     * @param ownerId The identifier of the {@link EventProcessor} that owns this load balancer.
     * @param inactiveTimeLimitInSeconds The time in seconds to wait for an update on an ownership record before
     * assuming the owner of the partition is inactive.
     * @param partitionPumpManager The partition pump manager that keeps track of all EventHubConsumers and partitions
     * that this {@link EventProcessor} is processing.
     */
    public PartitionBasedLoadBalancer(final EventProcessorStore eventProcessorStore,
        final EventHubAsyncClient eventHubAsyncClient, final String fullyQualifiedNamespace,
        final String eventHubName, final String consumerGroupName, final String ownerId,
        final long inactiveTimeLimitInSeconds, final PartitionPumpManager partitionPumpManager) {
        this.eventProcessorStore = eventProcessorStore;
        this.eventHubAsyncClient = eventHubAsyncClient;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
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
     * EventProcessor. Given that each invocation to this method results in ownership claim of at most one partition,
     * this algorithm converges gradually towards a steady state.
     * </p>
     * When a new partition is claimed, this method is also responsible for starting a partition pump that creates an
     * {@link EventHubAsyncConsumer} for processing events from that partition.
     */
    public void loadBalance() {
        /*
         * Retrieve current partition ownership details from the datastore.
         */
        final Mono<Map<String, PartitionOwnership>> partitionOwnershipMono = eventProcessorStore
            .listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroupName)
            .timeout(Duration.ofSeconds(2)) // TODO: configurable by the user
            .collectMap(PartitionOwnership::getPartitionId, Function.identity());

        /*
         * Retrieve the list of partition ids from the Event Hub.
         */
        final Mono<List<String>> partitionsMono = eventHubAsyncClient
            .getPartitionIds()
            .timeout(Duration.ofSeconds(5)) // TODO: configurable
            .collectList();

        Mono.zip(partitionOwnershipMono, partitionsMono)
            .flatMap(this::loadBalance)
            // if there was an error, log warning and TODO: call user provided error handler
            .doOnError(ex -> logger.warning("Load balancing for event processor failed - {}", ex.getMessage()))
            .subscribe();
    }

    /*
     * This method works with the given partition ownership details and Event Hub partitions to evaluate whether the
     * current Event Processor should take on the responsibility of processing more partitions.
     */
    private Mono<Void> loadBalance(final Tuple2<Map<String, PartitionOwnership>, List<String>> tuple) {
        return Mono.fromRunnable(() -> {
            logger.info("Starting load balancer");
            Map<String, PartitionOwnership> partitionOwnershipMap = tuple.getT1();

            List<String> partitionIds = tuple.getT2();

            if (ImplUtils.isNullOrEmpty(partitionIds)) {
                // This may be due to an error when getting Event Hub metadata.
                throw logger.logExceptionAsError(Exceptions.propagate(
                    new IllegalStateException("There are no partitions in Event Hub " + eventHubName)));
            }

            int numberOfPartitions = partitionIds.size();
            logger.info("Partition manager returned {} ownership records", partitionOwnershipMap.size());
            logger.info("EventHubAsyncClient returned {} partitions", numberOfPartitions);
            if (!isValid(partitionOwnershipMap)) {
                // User data is corrupt.
                throw logger.logExceptionAsError(Exceptions.propagate(
                    new IllegalStateException("Invalid partitionOwnership data from PartitionManager")));
            }

            /*
             * Remove all partitions' ownership that have not been modified for a configuration period of time. This
             * means that the previous EventProcessor that owned the partition is probably down and the partition is now
             * eligible to be claimed by other EventProcessors.
             */
            Map<String, PartitionOwnership> activePartitionOwnershipMap = removeInactivePartitionOwnerships(
                partitionOwnershipMap);
            logger.info("Number of active ownership records {}", activePartitionOwnershipMap.size());

            if (ImplUtils.isNullOrEmpty(activePartitionOwnershipMap)) {
                /*
                 * If the active partition ownership map is empty, this is the first time an event processor is
                 * running or all Event Processors are down for this Event Hub, consumer group combination. All
                 * partitions in this Event Hub are available to claim. Choose a random partition to claim ownership.
                 */
                claimOwnership(partitionOwnershipMap, partitionIds.get(RANDOM.nextInt(numberOfPartitions)));
                return;
            }

            /*
             * Create a map of owner id and a list of partitions it owns
             */
            Map<String, List<PartitionOwnership>> ownerPartitionMap = activePartitionOwnershipMap.values()
                .stream()
                .collect(
                    Collectors.groupingBy(PartitionOwnership::getOwnerId, mapping(Function.identity(), toList())));

            // add the current event processor to the map if it doesn't exist
            ownerPartitionMap.putIfAbsent(this.ownerId, new ArrayList<>());

            /*
             * Find the minimum number of partitions every event processor should own when the load is
             * evenly distributed.
             */
            int numberOfActiveEventProcessors = ownerPartitionMap.size();
            logger.info("Number of active event processors {}", ownerPartitionMap.size());

            int minPartitionsPerEventProcessor = numberOfPartitions / numberOfActiveEventProcessors;

            /*
             * If the number of partitions in Event Hub is not evenly divisible by number of active event processors,
             * a few Event Processors may own 1 additional partition than the minimum when the load is balanced.
             * Calculate the number of event processors that can own additional partition.
             */
            int numberOfEventProcessorsWithAdditionalPartition = numberOfPartitions % numberOfActiveEventProcessors;

            logger.info("Expected min partitions per event processor = {}, expected number of event "
                    + "processors with additional partition = {}", minPartitionsPerEventProcessor,
                numberOfEventProcessorsWithAdditionalPartition);

            if (isLoadBalanced(minPartitionsPerEventProcessor, numberOfEventProcessorsWithAdditionalPartition,
                ownerPartitionMap)) {
                // If the partitions are evenly distributed among all active event processors, no change required.
                logger.info("Load is balanced");
                return;
            }

            if (!shouldOwnMorePartitions(minPartitionsPerEventProcessor, ownerPartitionMap)) {
                // This event processor already has enough partitions and shouldn't own more.
                logger.info("This event processor owns {} partitions and shouldn't own more",
                    ownerPartitionMap.get(ownerId).size());
                return;
            }

            // If we have reached this stage, this event processor has to claim/steal ownership of at least 1
            // more partition
            logger.info(
                "Load is unbalanced and this event processor should own more partitions");
            /*
             * If some partitions are unclaimed, this could be because an event processor is down and
             * it's partitions are now available for others to own or because event processors are just
             * starting up and gradually claiming partitions to own or new partitions were added to Event Hub.
             * Find any partition that is not actively owned and claim it.
             *
             * OR
             *
             * Find a partition to steal from another event processor. Pick the event processor that has owns the
             * highest number of partitions.
             */
            String partitionToClaim = partitionIds.parallelStream()
                .filter(partitionId -> !activePartitionOwnershipMap.containsKey(partitionId))
                .findAny()
                .orElseGet(() -> {
                    logger.info("No unclaimed partitions, stealing from another event processor");
                    return findPartitionToSteal(ownerPartitionMap);
                });

            claimOwnership(partitionOwnershipMap, partitionToClaim);
        });
    }

    /*
     * Check if partition ownership data is valid before proceeding with load balancing.
     */
    private boolean isValid(final Map<String, PartitionOwnership> partitionOwnershipMap) {
        return partitionOwnershipMap.values()
            .stream()
            .noneMatch(partitionOwnership -> {
                return partitionOwnership.getEventHubName() == null
                    || !partitionOwnership.getEventHubName().equals(this.eventHubName)
                    || partitionOwnership.getConsumerGroupName() == null
                    || !partitionOwnership.getConsumerGroupName().equals(this.consumerGroupName)
                    || partitionOwnership.getPartitionId() == null
                    || partitionOwnership.getLastModifiedTime() == null
                    || partitionOwnership.getETag() == null;
            });
    }

    /*
     * Find the event processor that owns the maximum number of partitions and steal a random partition
     * from it.
     */
    private String findPartitionToSteal(final Map<String, List<PartitionOwnership>> ownerPartitionMap) {
        Map.Entry<String, List<PartitionOwnership>> ownerWithMaxPartitions = ownerPartitionMap.entrySet()
            .stream()
            .max(Comparator.comparingInt(entry -> entry.getValue().size()))
            .get();
        int numberOfPartitions = ownerWithMaxPartitions.getValue().size();
        logger.info("Owner id {} owns {} partitions, stealing a partition from it", ownerWithMaxPartitions.getKey(),
            numberOfPartitions);
        return ownerWithMaxPartitions.getValue().get(RANDOM.nextInt(numberOfPartitions)).getPartitionId();
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

        return numberOfPartitionsOwned < minPartitionsPerEventProcessor
            || numberOfPartitionsOwned == leastPartitionsOwnedByAnyEventProcessor;
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
                return (System.currentTimeMillis() - entry.getValue().getLastModifiedTime() < TimeUnit.SECONDS
                    .toMillis(inactiveTimeLimitInSeconds)) && !ImplUtils.isNullOrEmpty(entry.getValue().getOwnerId());
            }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void claimOwnership(final Map<String, PartitionOwnership> partitionOwnershipMap,
        final String partitionIdToClaim) {
        logger.info("Attempting to claim ownership of partition {}", partitionIdToClaim);
        PartitionOwnership ownershipRequest = createPartitionOwnershipRequest(partitionOwnershipMap,
            partitionIdToClaim);

        eventProcessorStore
            .claimOwnership(ownershipRequest)
            .timeout(Duration.ofSeconds(1)) // TODO: configurable
            .doOnNext(partitionOwnership -> logger.info("Successfully claimed ownership of partition {}",
                partitionOwnership.getPartitionId()))
            .doOnError(ex -> logger
                .warning("Failed to claim ownership of partition {} - {}", ownershipRequest.getPartitionId(),
                    ex.getMessage(), ex))
            .subscribe(partitionPumpManager::startPartitionPump);
    }

    private PartitionOwnership createPartitionOwnershipRequest(
        final Map<String, PartitionOwnership> partitionOwnershipMap,
        final String partitionIdToClaim) {
        PartitionOwnership previousPartitionOwnership = partitionOwnershipMap.get(partitionIdToClaim);
        PartitionOwnership partitionOwnershipRequest = new PartitionOwnership()
            .setOwnerId(this.ownerId)
            .setPartitionId(partitionIdToClaim)
            .setConsumerGroupName(this.consumerGroupName)
            .setEventHubName(this.eventHubName)
            .setSequenceNumber(previousPartitionOwnership == null
                ? null
                : previousPartitionOwnership.getSequenceNumber())
            .setOffset(previousPartitionOwnership == null ? null : previousPartitionOwnership.getOffset())
            .setETag(previousPartitionOwnership == null ? null : previousPartitionOwnership.getETag())
            .setOwnerLevel(0L);
        return partitionOwnershipRequest;
    }
}
