// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * This class is responsible for balancing the load of processing events from all partitions of an Event Hub by
 * distributing the number of partitions uniformly among all the  active {@link EventProcessorClient EventProcessors}.
 * <p>
 * This load balancer will retrieve partition ownership details from the {@link CheckpointStore} to find the number of
 * active {@link EventProcessorClient EventProcessorCients}. It uses the last modified time to decide if an
 * EventProcessor is active. If a partition ownership entry has not be updated for a specified duration of time, the
 * owner of that partition is considered inactive and the partition is available for other EventProcessors to own.
 * </p>
 */
final class PartitionBasedLoadBalancer {

    private static final Random RANDOM = new Random();
    private final ClientLogger logger = new ClientLogger(PartitionBasedLoadBalancer.class);

    private final String eventHubName;
    private final String consumerGroupName;
    private final CheckpointStore checkpointStore;
    private final EventHubAsyncClient eventHubAsyncClient;
    private final String ownerId;
    private final long inactiveTimeLimitInSeconds;
    private final PartitionPumpManager partitionPumpManager;
    private final String fullyQualifiedNamespace;
    private final Consumer<ErrorContext> processError;
    private final PartitionContext partitionAgnosticContext;
    private final AtomicBoolean isLoadBalancerRunning = new AtomicBoolean();
    private final LoadBalancingStrategy loadBalancingStrategy;
    private final AtomicBoolean morePartitionsToClaim = new AtomicBoolean();
    private final AtomicReference<List<String>> partitionsCache = new AtomicReference<>(new ArrayList<>());

    /**
     * Creates an instance of PartitionBasedLoadBalancer for the given Event Hub name and consumer group.
     * @param checkpointStore The partition manager that this load balancer will use to read/update ownership details.
     * @param eventHubAsyncClient The asynchronous Event Hub client used to consume events.
     * @param eventHubName The Event Hub name the {@link EventProcessorClient} is associated with.
     * @param consumerGroupName The consumer group name the {@link EventProcessorClient} is associated with.
     * @param ownerId The identifier of the {@link EventProcessorClient} that owns this load balancer.
     * @param inactiveTimeLimitInSeconds The time in seconds to wait for an update on an ownership record before
* assuming the owner of the partition is inactive.
     * @param partitionPumpManager The partition pump manager that keeps track of all EventHubConsumers and partitions
* that this {@link EventProcessorClient} is processing.
     * @param processError The callback that will be called when an error occurs while running the load balancer.
     * @param loadBalancingStrategy The load balancing strategy to use.
     */
    PartitionBasedLoadBalancer(final CheckpointStore checkpointStore,
        final EventHubAsyncClient eventHubAsyncClient, final String fullyQualifiedNamespace,
        final String eventHubName, final String consumerGroupName, final String ownerId,
        final long inactiveTimeLimitInSeconds, final PartitionPumpManager partitionPumpManager,
        final Consumer<ErrorContext> processError, LoadBalancingStrategy loadBalancingStrategy) {
        this.checkpointStore = checkpointStore;
        this.eventHubAsyncClient = eventHubAsyncClient;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
        this.ownerId = ownerId;
        this.inactiveTimeLimitInSeconds = inactiveTimeLimitInSeconds;
        this.partitionPumpManager = partitionPumpManager;
        this.processError = processError;
        this.partitionAgnosticContext = new PartitionContext(fullyQualifiedNamespace, eventHubName,
            consumerGroupName, "NONE");
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    /**
     * This is the main method responsible for load balancing. This method is expected to be invoked by the {@link
     * EventProcessorClient} periodically. Every call to this method will result in this {@link EventProcessorClient}
     * owning <b>at most one</b> new partition.
     * <p>
     * The load is considered balanced when no active EventProcessor owns 2 partitions more than any other active
     * EventProcessor. Given that each invocation to this method results in ownership claim of at most one partition,
     * this algorithm converges gradually towards a steady state.
     * </p>
     * When a new partition is claimed, this method is also responsible for starting a partition pump that creates an
     * {@link EventHubConsumerAsyncClient} for processing events from that partition.
     */
    void loadBalance() {

        if (!isLoadBalancerRunning.compareAndSet(false, true)) {
            logger.info("Load balancer already running");
            return;
        }

        logger.info("Starting load balancer for {}", this.ownerId);
        /*
         * Retrieve current partition ownership details from the datastore.
         */
        final Mono<Map<String, PartitionOwnership>> partitionOwnershipMono = checkpointStore
            .listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroupName)
            .timeout(Duration.ofMinutes(1))
            .collectMap(PartitionOwnership::getPartitionId, Function.identity());

        /*
         * Retrieve the list of partition ids from the Event Hub.
         */
        Mono<List<String>> partitionsMono;
        if (CoreUtils.isNullOrEmpty(partitionsCache.get())) {
            // Call Event Hubs service to get the partition ids if the cache is empty
            logger.info("Getting partitions from Event Hubs service for {}", eventHubName);
            partitionsMono = eventHubAsyncClient
                .getPartitionIds()
                .timeout(Duration.ofMinutes(1))
                .collectList();
        } else {
            partitionsMono = Mono.just(partitionsCache.get());
            // we have the partitions, the client can be closed now
            closeClient();
        }

        Mono.zip(partitionOwnershipMono, partitionsMono)
            .flatMap(this::loadBalance)
            .then()
            .repeat(() -> LoadBalancingStrategy.GREEDY == loadBalancingStrategy && morePartitionsToClaim.get())
            .subscribe(ignored -> { },
                ex -> {
                    logger.warning(Messages.LOAD_BALANCING_FAILED, ex.getMessage(), ex);
                    ErrorContext errorContext = new ErrorContext(partitionAgnosticContext, ex);
                    processError.accept(errorContext);
                    isLoadBalancerRunning.set(false);
                    morePartitionsToClaim.set(false);
                },
                () -> logger.info("Load balancing completed successfully"));

    }

    /*
     * This method works with the given partition ownership details and Event Hub partitions to evaluate whether the
     * current Event Processor should take on the responsibility of processing more partitions.
     */
    private Mono<Void> loadBalance(final Tuple2<Map<String, PartitionOwnership>, List<String>> tuple) {
        return Mono.fromRunnable(() -> {
            logger.info("Starting next iteration of load balancer");
            Map<String, PartitionOwnership> partitionOwnershipMap = tuple.getT1();

            List<String> partitionIds = tuple.getT2();

            if (CoreUtils.isNullOrEmpty(partitionIds)) {
                // This may be due to an error when getting Event Hub metadata.
                throw logger.logExceptionAsError(Exceptions.propagate(
                    new IllegalStateException("There are no partitions in Event Hub " + eventHubName)));
            }
            partitionsCache.set(partitionIds);
            int numberOfPartitions = partitionIds.size();
            logger.info("Number of ownership records {}, number of partitions {}", partitionOwnershipMap.size(),
                numberOfPartitions);

            if (!isValid(partitionOwnershipMap)) {
                // User data is corrupt.
                throw logger.logExceptionAsError(Exceptions.propagate(
                    new IllegalStateException("Invalid partitionOwnership data from CheckpointStore")));
            }

            /*
             * Remove all partitions' ownership that have not been modified for a configuration period of time. This
             * means that the previous EventProcessor that owned the partition is probably down and the partition is now
             * eligible to be claimed by other EventProcessors.
             */
            Map<String, PartitionOwnership> activePartitionOwnershipMap = removeInactivePartitionOwnerships(
                partitionOwnershipMap);
            logger.info("Number of active ownership records {}", activePartitionOwnershipMap.size());

            /*
             * Create a map of owner id and a list of partitions it owns
             */
            Map<String, List<PartitionOwnership>> ownerPartitionMap = activePartitionOwnershipMap.values()
                .stream()
                .collect(
                    Collectors.groupingBy(PartitionOwnership::getOwnerId, mapping(Function.identity(), toList())));

            // add the current event processor to the map if it doesn't exist
            ownerPartitionMap.putIfAbsent(this.ownerId, new ArrayList<>());
            logger.verbose("Current partition distribution {}", format(ownerPartitionMap));

            if (CoreUtils.isNullOrEmpty(activePartitionOwnershipMap)) {
                /*
                 * If the active partition ownership map is empty, this is the first time an event processor is
                 * running or all Event Processors are down for this Event Hub, consumer group combination. All
                 * partitions in this Event Hub are available to claim. Choose a random partition to claim ownership.
                 */
                claimOwnership(partitionOwnershipMap, ownerPartitionMap,
                    partitionIds.get(RANDOM.nextInt(numberOfPartitions)));
                return;
            }

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
                logger.info("Load is balanced with this event processor owning {} partitions",
                    ownerPartitionMap.get(ownerId).size());
                renewOwnership(partitionOwnershipMap);
                return;
            }

            if (!shouldOwnMorePartitions(minPartitionsPerEventProcessor, ownerPartitionMap)) {
                // This event processor already has enough partitions and shouldn't own more.
                logger.info("This event processor owns {} partitions and shouldn't own more",
                    ownerPartitionMap.get(ownerId).size());
                renewOwnership(partitionOwnershipMap);
                return;
            }

            // If we have reached this stage, this event processor has to claim/steal ownership of at least 1
            // more partition
            logger.info(
                "Load is unbalanced and this event processor owns {} partitions and should own more partitions",
                ownerPartitionMap.get(ownerId).size());
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

            claimOwnership(partitionOwnershipMap, ownerPartitionMap, partitionToClaim);
        });
    }

    /*
     * Closes the client used by load balancer to get the partitions.
     */
    private void closeClient() {
        try {
            // this is an idempotent operation, calling close on an already closed client is just a no-op.
            this.eventHubAsyncClient.close();
        } catch (Exception ex) {
            logger.warning("Failed to close the client", ex);
        }
    }

    /*
     * This method renews the ownership of currently owned partitions
     */
    private void renewOwnership(Map<String, PartitionOwnership> partitionOwnershipMap) {
        morePartitionsToClaim.set(false);
        // renew ownership of already owned partitions
        checkpointStore.claimOwnership(partitionPumpManager.getPartitionPumps().keySet()
            .stream()
            .filter(
                partitionId -> partitionOwnershipMap.containsKey(partitionId) && partitionOwnershipMap.get(partitionId)
                    .getOwnerId().equals(this.ownerId))
            .map(partitionId -> createPartitionOwnershipRequest(partitionOwnershipMap, partitionId))
            .collect(Collectors.toList()))
            .subscribe(partitionPumpManager::verifyPartitionConnection,
                ex -> {
                    logger.error("Error renewing partition ownership", ex);
                    isLoadBalancerRunning.set(false);
                },
                () -> isLoadBalancerRunning.set(false));
    }

    private String format(Map<String, List<PartitionOwnership>> ownerPartitionMap) {
        return ownerPartitionMap.entrySet()
            .stream()
            .map(entry -> {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey()).append("=[");
                sb.append(entry.getValue().stream().map(po -> po.getPartitionId()).collect(Collectors.joining(",")));
                sb.append("]");
                return sb.toString();
            }).collect(Collectors.joining(";"));
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
                    || partitionOwnership.getConsumerGroup() == null
                    || !partitionOwnership.getConsumerGroup().equals(this.consumerGroupName)
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
     * that are actively owned. All entries in the original map returned by CheckpointStore that haven't been
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
                    .toMillis(inactiveTimeLimitInSeconds))
                    && !CoreUtils.isNullOrEmpty(entry.getValue().getOwnerId());
            }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void claimOwnership(final Map<String, PartitionOwnership> partitionOwnershipMap, Map<String,
        List<PartitionOwnership>> ownerPartitionsMap, final String partitionIdToClaim) {
        logger.info("Attempting to claim ownership of partition {}", partitionIdToClaim);
        PartitionOwnership ownershipRequest = createPartitionOwnershipRequest(partitionOwnershipMap,
            partitionIdToClaim);

        List<PartitionOwnership> partitionsToClaim = new ArrayList<>();
        partitionsToClaim.add(ownershipRequest);
        partitionsToClaim.addAll(partitionPumpManager.getPartitionPumps()
            .keySet()
            .stream()
            .filter(
                partitionId -> partitionOwnershipMap.containsKey(partitionId) && partitionOwnershipMap.get(partitionId)
                    .getOwnerId().equals(this.ownerId))
            .map(partitionId -> createPartitionOwnershipRequest(partitionOwnershipMap, partitionId))
            .collect(Collectors.toList()));

        morePartitionsToClaim.set(true);
        checkpointStore
            .claimOwnership(partitionsToClaim)
            .doOnNext(partitionOwnership -> logger.info("Successfully claimed ownership of partition {}",
                partitionOwnership.getPartitionId()))
            .doOnError(ex -> logger
                .warning(Messages.FAILED_TO_CLAIM_OWNERSHIP, ownershipRequest.getPartitionId(),
                    ex.getMessage(), ex))
            .collectList()
            .zipWhen(ownershipList -> checkpointStore.listCheckpoints(fullyQualifiedNamespace, eventHubName,
                consumerGroupName)
                .collectMap(checkpoint -> checkpoint.getPartitionId(), Function.identity()))
            .subscribe(ownedPartitionCheckpointsTuple -> {
                ownedPartitionCheckpointsTuple.getT1()
                    .stream()
                    .forEach(po -> partitionPumpManager.startPartitionPump(po,
                        ownedPartitionCheckpointsTuple.getT2().get(po.getPartitionId())));
            },
                ex -> {
                    logger.warning("Error while listing checkpoints", ex);
                    ErrorContext errorContext = new ErrorContext(partitionAgnosticContext, ex);
                    processError.accept(errorContext);
                    if (loadBalancingStrategy == LoadBalancingStrategy.BALANCED) {
                        isLoadBalancerRunning.set(false);
                    }
                    throw logger.logExceptionAsError(new IllegalStateException("Error while listing checkpoints", ex));
                },
                () -> {
                    if (loadBalancingStrategy == LoadBalancingStrategy.BALANCED) {
                        isLoadBalancerRunning.set(false);
                    }
                });
    }

    private PartitionOwnership createPartitionOwnershipRequest(
        final Map<String, PartitionOwnership> partitionOwnershipMap,
        final String partitionIdToClaim) {
        PartitionOwnership previousPartitionOwnership = partitionOwnershipMap.get(partitionIdToClaim);
        PartitionOwnership partitionOwnershipRequest = new PartitionOwnership()
            .setFullyQualifiedNamespace(this.fullyQualifiedNamespace)
            .setOwnerId(this.ownerId)
            .setPartitionId(partitionIdToClaim)
            .setConsumerGroup(this.consumerGroupName)
            .setEventHubName(this.eventHubName)
            .setETag(previousPartitionOwnership == null ? null : previousPartitionOwnership.getETag());
        return partitionOwnershipRequest;
    }
}
