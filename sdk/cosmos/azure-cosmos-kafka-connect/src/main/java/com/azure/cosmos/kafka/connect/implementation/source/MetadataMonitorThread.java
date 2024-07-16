// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class MetadataMonitorThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataMonitorThread.class);

    // TODO[GA]: using a threadPool with less threads or single thread
    public static final Scheduler CONTAINERS_MONITORING_SCHEDULER = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        "cosmos-source-metadata-monitoring-bounded-elastic",
        60,
        true
    );

    private final String connectorName;
    private final CosmosSourceContainersConfig sourceContainersConfig;
    private final CosmosMetadataConfig metadataConfig;
    private final SourceConnectorContext connectorContext;
    private final IMetadataReader metadataReader;
    private final CosmosAsyncClient cosmosClient;
    private final SqlQuerySpec containersQuerySpec;
    private final ContainersMetadataTopicPartition containersMetadataTopicPartition;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    public MetadataMonitorThread(
        String connectorName,
        CosmosSourceContainersConfig containersConfig,
        CosmosMetadataConfig metadataConfig,
        SourceConnectorContext connectorContext,
        IMetadataReader metadataReader,
        CosmosAsyncClient cosmosClient) {

        checkNotNull(containersConfig, "Argument 'containersConfig' can not be null");
        checkNotNull(metadataConfig, "Argument 'metadataConfig' can not be null");
        checkNotNull(connectorContext, "Argument 'connectorContext' can not be null");
        checkNotNull(metadataReader, "Argument 'metadataReader' can not be null");
        checkNotNull(cosmosClient, "Argument 'cosmosClient' can not be null");

        this.connectorName = connectorName;
        this.sourceContainersConfig = containersConfig;
        this.metadataConfig = metadataConfig;
        this.connectorContext = connectorContext;
        this.metadataReader = metadataReader;
        this.cosmosClient = cosmosClient;
        this.containersQuerySpec = this.getContainersQuerySpec();
        int numContainers = Objects.requireNonNull(this.getAllContainers().block()).size();
        if (numContainers == 0 || numContainers != sourceContainersConfig.getIncludedContainers().size()) {
            throw new IllegalStateException("Some of the containers specified in the config were not found in the database.");
        }
        this.containersMetadataTopicPartition = new ContainersMetadataTopicPartition(containersConfig.getDatabaseName(), connectorName);
    }

    @Override
    public void run() {
        LOGGER.info("Start containers monitoring task");

        int containersPollDelayInMs = this.metadataConfig.getMetadataPollDelayInMs();
        if (containersPollDelayInMs >= 0) {
            Mono
                .delay(Duration.ofMillis(containersPollDelayInMs))
                .flatMap(t -> {
                    if (this.isRunning.get()) {
                        LOGGER.trace("ValidateContainersMetadataChange...");
                        return shouldRequestTaskReconfiguration();
                    }
                    return Mono.empty();
                })
                .doOnNext(shouldRequestReconfiguration -> {
                    if (shouldRequestReconfiguration) {
                        LOGGER.info("Changes detected, requestTaskReconfiguration");
                        this.connectorContext.requestTaskReconfiguration();
                    }
                })
                .onErrorResume(throwable -> {
                    LOGGER.warn("Containers metadata checking failed. Will retry in next polling cycle", throwable);
                    return Mono.empty();
                })
                .repeat(() -> this.isRunning.get())
                .subscribeOn(CONTAINERS_MONITORING_SCHEDULER)
                .subscribe();
        } else {
            LOGGER.info("Containers monitoring task not started due to negative containers poll delay");
        }
    }

    private Mono<Boolean> shouldRequestTaskReconfiguration() {
        // First check any containers to be copied changes
        // Container re-created, add or remove will request task reconfiguration
        // If there are no changes on the containers, then check for each container any feedRanges change need to request task reconfiguration
        return containersMetadataOffsetExists()
            .flatMap(metadataOffsetExists -> {
                if (metadataOffsetExists) {
                    return this.getAllContainers()
                        .flatMap(containersList -> {
                            return hasContainersChange(containersList)
                                .flatMap(hasChange -> {
                                    if (hasChange) {
                                        return Mono.just(true);
                                    }

                                    return shouldRequestTaskReconfigurationOnFeedRanges(containersList);
                                });
                        });
                }

                // there is no existing containers offset for comparison.
                // Could be this is the first time for the connector to start and the metadata task has not been initialized.
                // will skip and validate in next cycle.
                return Mono.just(false);
            });
    }

    public Mono<Boolean> containersMetadataOffsetExists() {
        return this.metadataReader
            .getContainersMetadataOffset(this.sourceContainersConfig.getDatabaseName(), this.connectorName)
            .map(offsetValueHolder -> offsetValueHolder.v != null);
    }

    public Mono<List<CosmosContainerProperties>> getAllContainers() {
        return this.cosmosClient
            .getDatabase(this.sourceContainersConfig.getDatabaseName())
            .queryContainers(this.containersQuerySpec)
            .byPage()
            .flatMapIterable(response -> response.getResults())
            .collectList()
            .onErrorMap(throwable -> KafkaCosmosExceptionsHelper.convertToConnectException(throwable, "getAllContainers failed."));
    }

    public Mono<List<String>> getContainerRidsFromOffset() {
        return this.metadataReader
            .getContainersMetadataOffset(this.sourceContainersConfig.getDatabaseName(), this.connectorName)
            .map(offsetValueHolder -> {
                return offsetValueHolder.v == null ? new ArrayList<>() : offsetValueHolder.v.getContainerRids();
            });
    }

    private Mono<Boolean> hasContainersChange(List<CosmosContainerProperties> allContainers) {
        List<String> containersRidToBeCopied =
            allContainers
                .stream()
                .map(CosmosContainerProperties::getResourceId)
                .collect(Collectors.toList());

        return this.getContainerRidsFromOffset()
            .map(containerRidsFromOffset -> {
                return !(containerRidsFromOffset.size() == containersRidToBeCopied.size()
                    && containerRidsFromOffset.containsAll(containersRidToBeCopied));
            });
    }

    private Mono<Boolean> shouldRequestTaskReconfigurationOnFeedRanges(List<CosmosContainerProperties> allContainers) {
        AtomicBoolean shouldRequestTaskReconfiguration = new AtomicBoolean(false);
        AtomicInteger containerIndex = new AtomicInteger(0);

        // loop through containers to check any feedRanges change
        return Mono.just(allContainers.get(containerIndex.get()))
            .flatMap(containerProperties -> shouldRequestTaskReconfigurationOnFeedRanges(containerProperties))
            .doOnNext(hasChange -> {
                shouldRequestTaskReconfiguration.set(hasChange);
                containerIndex.incrementAndGet();
            })
            .repeat(() -> !shouldRequestTaskReconfiguration.get() && containerIndex.get() < allContainers.size())
            .then(Mono.defer(() -> Mono.just(shouldRequestTaskReconfiguration.get())));
    }

    private Mono<Boolean> shouldRequestTaskReconfigurationOnFeedRanges(CosmosContainerProperties containerProperties) {
        return feedRangesMetadataOffsetExists(containerProperties)
            .flatMap(offsetExists -> {
                if (offsetExists) {
                    CosmosAsyncContainer container =
                        this.cosmosClient
                            .getDatabase(this.sourceContainersConfig.getDatabaseName())
                            .getContainer(containerProperties.getId());

                    return container
                        .getFeedRanges()
                        .flatMap(feedRanges -> {
                            return this.metadataReader
                                .getFeedRangesMetadataOffset(
                                    this.sourceContainersConfig.getDatabaseName(),
                                    containerProperties.getResourceId(),
                                    this.connectorName)
                                .flatMap(offsetValueHolder -> {
                                    if (offsetValueHolder.v == null) {
                                        // the container may have recreated
                                        return Mono.just(true);
                                    }

                                    List<FeedRange> differences =
                                        offsetValueHolder.v
                                            .getFeedRanges()
                                            .stream()
                                            .filter(normalizedFeedRange -> !feedRanges.contains(normalizedFeedRange))
                                            .collect(Collectors.toList());

                                    if (differences.size() == 0) {
                                        // the feedRanges are exact the same
                                        return Mono.just(false);
                                    }

                                    // There are feedRanges change, but not all changes need to trigger a reconfiguration
                                    // Merge should not trigger task reconfiguration as we will continue pulling the data from the pre-merge feed ranges
                                    // Split should trigger task reconfiguration for load-balancing
                                    return shouldRequestTaskReconfigurationOnFeedRangeChanges(containerProperties, differences);
                                });
                        });
                }

                // there is no existing feedRanges offset for comparison.
                // Could be this is the first time for the connector to start and the metadata task has not been initialized.
                // will skip and validate in next cycle.
                return Mono.just(false);
            });
    }

    private Mono<Boolean> feedRangesMetadataOffsetExists(CosmosContainerProperties containerProperties) {
        return this.metadataReader
            .getFeedRangesMetadataOffset(
                this.sourceContainersConfig.getDatabaseName(),
                containerProperties.getResourceId(),
                this.connectorName)
            .map(offsetValueHolder -> offsetValueHolder.v != null);
    }

    private Mono<Boolean> shouldRequestTaskReconfigurationOnFeedRangeChanges(
        CosmosContainerProperties containerProperties,
        List<FeedRange> changes) {
        if (changes == null || changes.isEmpty()) {
            return Mono.just(false);
        }

        AtomicBoolean shouldRequestTaskReconfiguration = new AtomicBoolean(false);
        AtomicInteger feedRangeIndex = new AtomicInteger(0);

        return Mono.just(changes.get(feedRangeIndex.get()))
            .flatMap(feedRangeChanged -> shouldRequestTaskReconfigurationOnFeedRangeChange(containerProperties, feedRangeChanged))
            .doOnNext(shouldReconfig -> {
                shouldRequestTaskReconfiguration.compareAndSet(false, shouldReconfig);
                feedRangeIndex.incrementAndGet();
            })
            .repeat(() -> (!shouldRequestTaskReconfiguration.get()) && feedRangeIndex.get() < changes.size())
            .then(Mono.defer(() -> Mono.just(shouldRequestTaskReconfiguration.get())));
    }

    private Mono<Boolean> shouldRequestTaskReconfigurationOnFeedRangeChange(
        CosmosContainerProperties containerProperties,
        FeedRange feedRangeChanged) {

        CosmosAsyncContainer container =
            this.cosmosClient
                .getDatabase(this.sourceContainersConfig.getDatabaseName())
                .getContainer(containerProperties.getId());

        return ImplementationBridgeHelpers
                .CosmosAsyncContainerHelper
                .getCosmosAsyncContainerAccessor()
                // when comparing the differences, we already use container.feedRanges() to refresh the cache, so there is no need to refresh the cache again here
                .getOverlappingFeedRanges(container, feedRangeChanged, false)
            .map(matchedPkRanges -> {
                if (matchedPkRanges.size() == 0) {
                    LOGGER.warn(
                        "FeedRang {} on container {} is gone but we failed to find at least one matching pkRange",
                        feedRangeChanged,
                        containerProperties.getResourceId());

                    return true;
                }

                if (matchedPkRanges.size() == 1) {
                    LOGGER.info(
                        "FeedRange {} is merged into {} on container {}",
                        feedRangeChanged,
                        matchedPkRanges.get(0).toString(),
                        containerProperties.getResourceId());
                    return false;
                }

                LOGGER.info(
                    "FeedRange {} is split into [{}] on container {}",
                    feedRangeChanged,
                    matchedPkRanges.stream().map(FeedRange::toString).collect(Collectors.toList()),
                    containerProperties.getResourceId()
                );
                return true;
            });
    }

    private SqlQuerySpec getContainersQuerySpec() {
        boolean includeAllContainers = sourceContainersConfig.isIncludeAllContainers();
        if (includeAllContainers) {
            return new SqlQuerySpec("SELECT * FROM c");
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE c.id IN ( ");
        for (int i = 0; i < sourceContainersConfig.getIncludedContainers().size(); i++) {
            String idValue = sourceContainersConfig.getIncludedContainers().get(i);
            String idParamName = "@param" + i;

            parameters.add(new SqlParameter(idParamName, idValue));
            queryStringBuilder.append(idParamName);

            if (i < sourceContainersConfig.getIncludedContainers().size() - 1) {
                queryStringBuilder.append(", ");
            }
        }
        queryStringBuilder.append(" )");
        return new SqlQuerySpec(queryStringBuilder.toString(), parameters);
    }

    public void close() {
        this.isRunning.set(false);
    }
}
