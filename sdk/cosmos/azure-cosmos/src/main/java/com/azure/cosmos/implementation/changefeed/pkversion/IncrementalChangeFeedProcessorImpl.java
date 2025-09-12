// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import com.azure.cosmos.implementation.changefeed.HealthMonitor;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancer;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancingStrategy;
import com.azure.cosmos.implementation.changefeed.PartitionManager;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedContextClientImpl;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.CheckpointerObserverFactory;
import com.azure.cosmos.implementation.changefeed.common.DefaultObserverFactory;
import com.azure.cosmos.implementation.changefeed.common.EqualPartitionsBalancingStrategy;
import com.azure.cosmos.implementation.changefeed.common.PartitionedByIdCollectionRequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.common.TraceHealthMonitor;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Helper class to buildAsyncClient {@link ChangeFeedProcessor} instances
 * as logical representation of the Azure Cosmos DB database service.
 *
 * <pre>
 * {@code
 * ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
 *     .hostName(hostName)
 *     .feedContainer(feedContainer)
 *     .leaseContainer(leaseContainer)
 *     .handleChanges(docs -> {
 *         for (JsonNode item : docs) {
 *             // Implementation for handling and processing of each JsonNode item goes here
 *         }
 *     })
 *     .buildChangeFeedProcessor();
 * }
 * </pre>
 */
public class IncrementalChangeFeedProcessorImpl implements ChangeFeedProcessor, AutoCloseable {
    private static final String PK_RANGE_ID_SEPARATOR = ":";
    private static final String SEGMENT_SEPARATOR = "#";
    private static final String PROPERTY_NAME_LSN = "_lsn";

    private final Logger logger = LoggerFactory.getLogger(IncrementalChangeFeedProcessorImpl.class);
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);
    private static final int DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE = 100;

    private final static int DEFAULT_DEGREE_OF_PARALLELISM = 25; // default


    private final String hostName;
    private final ChangeFeedContextClient feedContextClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final ChangeFeedObserverFactory<JsonNode> observerFactory;
    private volatile String databaseId;
    private volatile String collectionId;
    private volatile String databaseResourceId;
    private volatile String collectionResourceId;
    private final ChangeFeedContextClient leaseContextClient;
    private PartitionLoadBalancingStrategy loadBalancingStrategy;
    private LeaseStoreManager leaseStoreManager;
    private HealthMonitor healthMonitor;
    private volatile PartitionManager partitionManager;
    private final Scheduler scheduler;

    public IncrementalChangeFeedProcessorImpl(
            String hostName,
            CosmosAsyncContainer feedContainer,
            CosmosAsyncContainer leaseContainer,
            Consumer<List<JsonNode>> consumer,
            ChangeFeedProcessorOptions changeFeedProcessorOptions) {

        checkNotNull(hostName, "Argument 'hostName' can not be null");
        checkNotNull(feedContainer, "Argument 'feedContainer' can not be null");
        checkNotNull(consumer, "Argument 'consumer' can not be null");

        if (changeFeedProcessorOptions == null) {
            changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        }
        this.validateChangeFeedProcessorOptions(changeFeedProcessorOptions);
        this.validateLeaseContainer(leaseContainer);

        this.hostName = hostName;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.feedContextClient = new ChangeFeedContextClientImpl(feedContainer);
        this.leaseContextClient = new ChangeFeedContextClientImpl(leaseContainer);
        this.scheduler = this.changeFeedProcessorOptions.getScheduler();
        this.feedContextClient.setScheduler(this.scheduler);
        this.leaseContextClient.setScheduler(this.scheduler);
        this.observerFactory = new DefaultObserverFactory<>(consumer);
    }

    private void validateChangeFeedProcessorOptions(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        checkNotNull(changeFeedProcessorOptions, "Argument 'changeFeedProcessorOptions' can not be null");
        if (changeFeedProcessorOptions.getLeaseAcquireInterval().compareTo(ChangeFeedProcessorOptions.DEFAULT_ACQUIRE_INTERVAL) < 0) {
            logger.warn("Found lower than expected setting for leaseAcquireInterval");
        }
    }

    private void validateLeaseContainer(CosmosAsyncContainer leaseContainer) {
        checkNotNull(leaseContainer, "Argument 'leaseContainer' can not be null");

        if (!getContextClient(leaseContainer).isContentResponseOnWriteEnabled()) {
            throw new IllegalArgumentException("leaseClient: content response on write setting must be enabled");
        }

        ConsistencyLevel consistencyLevel = getContextClient(leaseContainer).getConsistencyLevel();
        if (consistencyLevel == ConsistencyLevel.CONSISTENT_PREFIX || consistencyLevel == ConsistencyLevel.EVENTUAL) {
            logger.warn("leaseClient consistency level setting are less then expected which is SESSION");
        }
    }

    /**
     * Start listening for changes asynchronously.
     *
     *  @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> start() {
        if (this.partitionManager == null) {
            return this.initializeCollectionPropertiesForBuild()
                .flatMap( value -> this.getLeaseStoreManager()
                    .flatMap(this::buildPartitionManager))
                .flatMap(partitionManager1 -> {
                    this.partitionManager = partitionManager1;
                    return this.partitionManager.start();
                });

        } else {
            return partitionManager.start();
        }
    }

    /**
     * Stops listening for changes asynchronously.
     *
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> stop() {
        if (this.partitionManager == null || !this.partitionManager.isRunning()) {
            throw new IllegalStateException("The ChangeFeedProcessor instance has not fully started");
        }
        return this.partitionManager.stop();
    }

    /**
     * Returns the state of the change feed processor.
     *
     * @return true if the change feed processor is currently active and running.
     */
    @Override
    public boolean isStarted() {
        return this.partitionManager != null && this.partitionManager.isRunning();
    }

    /**
     * Returns the current owner (host) and an approximation of the difference between the last processed item (defined
     *   by the state of the feed container) and the latest change in the container for each partition (lease
     *   document).
     * <p>
     * An empty map will be returned if the processor was not started or no lease documents matching the current
     *   {@link ChangeFeedProcessor} instance's lease prefix could be found.
     *
     * @return a map representing the current owner and lease token, the current LSN and latest LSN, and the estimated
     *         lag, asynchronously.
     */
    @Override
    public Mono<Map<String, Integer>> getEstimatedLag() {
        Map<String, Integer> earlyResult = new ConcurrentHashMap<>();

        if (this.leaseContextClient == null || this.feedContextClient == null) {
            return Mono.just(earlyResult);
        }

        return this.initializeCollectionPropertiesForBuild()
            .flatMap(value -> this.getLeaseStoreManager())
            .flatMap(leaseStoreManager1 ->
                leaseStoreManager1.getAllLeases()
                    .flatMap(lease -> {
                        final CosmosChangeFeedRequestOptions options =
                            ModelBridgeInternal.createChangeFeedRequestOptionsForChangeFeedState(
                                lease.getContinuationState(this.collectionId, ChangeFeedMode.INCREMENTAL));
                        options.setMaxItemCount(1);

                        return this.feedContextClient.createDocumentChangeFeedQuery(
                                this.feedContextClient.getContainerClient(),
                                options, JsonNode.class)
                            .take(1)
                            .map(feedResponse -> {
                                String ownerValue = lease.getOwner();
                                String sessionTokenLsn = feedResponse.getSessionToken();
                                String parsedSessionToken = sessionTokenLsn.substring(
                                    sessionTokenLsn.indexOf(PK_RANGE_ID_SEPARATOR));
                                String[] segments = StringUtils.split(parsedSessionToken, SEGMENT_SEPARATOR);
                                String latestLsn = segments[0];

                                if (segments.length >= 2) {
                                    // default to Global LSN
                                    latestLsn = segments[1];
                                }

                                if (ownerValue == null) {
                                    ownerValue = "";
                                }

                                // An empty list of documents returned means that we are current (zero lag)
                                if (feedResponse.getResults() == null || feedResponse.getResults().size() == 0) {
                                    return Pair.of(ownerValue + "_" + lease.getLeaseToken(), 0l);
                                }

                                long currentLsn = 0;
                                long estimatedLag;
                                try {
                                    currentLsn = Long.parseLong(feedResponse.getResults().get(0).get(PROPERTY_NAME_LSN).asText("0"));
                                    estimatedLag = Long.parseLong(latestLsn);
                                    estimatedLag = estimatedLag - currentLsn + 1;
                                } catch (NumberFormatException ex) {
                                    logger.warn("Unexpected Cosmos LSN found", ex);
                                    estimatedLag = -1l;
                                }

                                return Pair.of(
                                    ownerValue + "_" + lease.getLeaseToken() + "_" + currentLsn + "_" + latestLsn,
                                    estimatedLag);
                            });
                    })
                    .collectList()
                    .map(valueList -> {
                        Map<String, Integer> result = new ConcurrentHashMap<>();
                        for (Pair<String, Long> pair : valueList) {
                            result.put(pair.getKey(), (int)Math.min(pair.getValue(), Integer.MAX_VALUE));
                        }
                        return result;
                    })
            );
    }

    /**
     * Returns a list of states each representing one scoped worker item.
     * <p>
     * An empty list will be returned if the processor was not started or no lease items matching the current
     *   {@link ChangeFeedProcessor} instance's lease prefix could be found.
     *
     * @return a list of states each representing one scoped worker item.
     */
    @Override
    public Mono<List<ChangeFeedProcessorState>> getCurrentState() {

        if (this.leaseContextClient == null || this.feedContextClient == null) {
            return Mono.just(Collections.unmodifiableList(new ArrayList<>()));
        }

        return this.initializeCollectionPropertiesForBuild()
            .flatMap(value -> this.getLeaseStoreManager())
            .flatMap(leaseStoreManager1 ->
                leaseStoreManager1.getAllLeases()
                    .flatMap(lease -> {
                        final FeedRangeInternal feedRange = new FeedRangePartitionKeyRangeImpl(lease.getLeaseToken());
                        final CosmosChangeFeedRequestOptions options =
                            ModelBridgeInternal.createChangeFeedRequestOptionsForChangeFeedState(
                                lease.getContinuationState(this.collectionId, ChangeFeedMode.INCREMENTAL));
                        options.setMaxItemCount(1);

                        return this.feedContextClient.createDocumentChangeFeedQuery(
                            this.feedContextClient.getContainerClient(),
                            options, JsonNode.class)
                            .take(1)
                            .map(feedResponse -> {
                                String sessionTokenLsn = feedResponse.getSessionToken();
                                String parsedSessionToken = sessionTokenLsn.substring(
                                    sessionTokenLsn.indexOf(PK_RANGE_ID_SEPARATOR));
                                String[] segments = StringUtils.split(parsedSessionToken, SEGMENT_SEPARATOR);
                                String latestLsn = segments[0];

                                if (segments.length >= 2) {
                                    // default to Global LSN
                                    latestLsn = segments[1];
                                }

                                // lease.getId() - the ID of the lease item representing the persistent state of a
                                // change feed processor worker.
                                // latestLsn - a marker representing the latest item that will be processed.
                                ChangeFeedProcessorState changeFeedProcessorState = new ChangeFeedProcessorState()
                                    .setHostName(lease.getOwner())
                                    .setLeaseToken(lease.getLeaseToken());

                                // An empty list of documents returned means that we are current (zero lag)
                                if (feedResponse.getResults() == null || feedResponse.getResults().size() == 0) {
                                    changeFeedProcessorState.setEstimatedLag(0)
                                        .setContinuationToken(latestLsn);

                                    return changeFeedProcessorState;
                                }

                                changeFeedProcessorState.setContinuationToken(
                                    feedResponse.getResults().get(0).get(PROPERTY_NAME_LSN).asText(null));

                                long currentLsn;
                                long estimatedLag;
                                try {
                                    currentLsn = Long.parseLong(feedResponse.getResults().get(0).get(PROPERTY_NAME_LSN).asText("0"));
                                    estimatedLag = Long.parseLong(latestLsn);
                                    estimatedLag = estimatedLag - currentLsn + 1;
                                    changeFeedProcessorState.setEstimatedLag((int)Math.min(estimatedLag, Integer.MAX_VALUE));
                                } catch (NumberFormatException ex) {
                                    logger.warn("Unexpected Cosmos LSN found", ex);
                                    changeFeedProcessorState.setEstimatedLag(-1);
                                }

                                return changeFeedProcessorState;
                            });
                    })
                    .collectList()
                    .map(Collections::unmodifiableList)
            );
    }

    private Mono<ChangeFeedProcessor> initializeCollectionPropertiesForBuild() {
        return this.feedContextClient
            .readDatabase(this.feedContextClient.getDatabaseClient(), null)
            .map( databaseResourceResponse -> {
                this.databaseId = databaseResourceResponse.getProperties().getId();
                this.databaseResourceId = databaseResourceResponse.getProperties().getResourceId();
                return this.databaseId;
            })
            .flatMap( id -> this.feedContextClient
                .readContainer(this.feedContextClient.getContainerClient(), null)
                .map(documentCollectionResourceResponse -> {
                    this.collectionId = documentCollectionResourceResponse.getProperties().getId();
                    this.collectionResourceId = documentCollectionResourceResponse.getProperties().getResourceId();
                    return this;
                }));
    }

    private Mono<LeaseStoreManager> getLeaseStoreManager() {
        if (this.leaseStoreManager == null) {

            return this.leaseContextClient.readContainerSettings(this.leaseContextClient.getContainerClient(), null)
                .flatMap( collectionSettings -> {
                    boolean isPartitioned =
                        collectionSettings.getPartitionKeyDefinition() != null &&
                            collectionSettings.getPartitionKeyDefinition().getPaths() != null &&
                            collectionSettings.getPartitionKeyDefinition().getPaths().size() > 0;
                    if (!isPartitioned || (collectionSettings.getPartitionKeyDefinition().getPaths().size() != 1 || !collectionSettings.getPartitionKeyDefinition().getPaths().get(0).equals("/id"))) {
//                        throw new IllegalArgumentException("The lease collection, if partitioned, must have partition key equal to id.");
                        return Mono.error(new IllegalArgumentException("The lease collection must have partition key equal to id."));
                    }

                    RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();

                    String leasePrefix = this.getLeasePrefix();

                    this.leaseStoreManager = LeaseStoreManagerImpl.builder()
                            .leasePrefix(leasePrefix)
                            .leaseCollectionLink(this.leaseContextClient.getContainerClient())
                            .leaseContextClient(this.leaseContextClient)
                            .requestOptionsFactory(requestOptionsFactory)
                            .hostName(this.hostName)
                            .build();

                    return Mono.just(this.leaseStoreManager);
                });
        }

        return Mono.just(this.leaseStoreManager);
    }

    private String getLeasePrefix() {
        String optionsPrefix = this.changeFeedProcessorOptions.getLeasePrefix();

        if (optionsPrefix == null) {
            optionsPrefix = "";
        }

        URI uri = this.feedContextClient.getServiceEndpoint();

        return String.format(
            "%s%s_%s_%s",
            optionsPrefix,
            uri.getHost(),
            this.databaseId,
            this.collectionId);
    }

    private Mono<PartitionManager> buildPartitionManager(LeaseStoreManager leaseStoreManager) {
        CheckpointerObserverFactory<JsonNode> factory = new CheckpointerObserverFactory<>(this.observerFactory, new CheckpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
            this.feedContextClient,
            this.feedContextClient.getContainerClient(),
            leaseStoreManager,
            leaseStoreManager,
            DEFAULT_DEGREE_OF_PARALLELISM,
            DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE,
            this.collectionId,
            this.changeFeedProcessorOptions,
            this.hostName);

        RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();
        String epkRangeVersionLeasePrefix = this.getEpkRangeVersionLeasePrefix();

        LeaseStoreManager epkVersionLeaseStoreManager =
            com.azure.cosmos.implementation.changefeed.epkversion.LeaseStoreManagerImpl.builder()
                .leasePrefix(epkRangeVersionLeasePrefix)
                .leaseCollectionLink(this.leaseContextClient.getContainerClient())
                .leaseContextClient(this.leaseContextClient)
                .requestOptionsFactory(requestOptionsFactory)
                .hostName(this.hostName)
                .build();

        Bootstrapper bootstrapper = new BootstrapperImpl(
            synchronizer,
            leaseStoreManager,
            epkVersionLeaseStoreManager,
            this.changeFeedProcessorOptions,
            this.lockTime,
            this.sleepTime);

        FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager = this.getFeedRangeThroughputControlConfigManager();

        PartitionSupervisorFactory partitionSupervisorFactory = new PartitionSupervisorFactoryImpl(
            factory,
            leaseStoreManager,
            new PartitionProcessorFactoryImpl(
                this.feedContextClient,
                this.changeFeedProcessorOptions,
                leaseStoreManager,
                this.feedContextClient.getContainerClient(),
                this.collectionId,
                feedRangeThroughputControlConfigManager),
            this.changeFeedProcessorOptions,
            this.scheduler
        );

        if (this.loadBalancingStrategy == null) {
            this.loadBalancingStrategy = new EqualPartitionsBalancingStrategy(
                this.hostName,
                this.changeFeedProcessorOptions.getMinScaleCount(),
                this.changeFeedProcessorOptions.getMaxScaleCount(),
                this.changeFeedProcessorOptions.getLeaseExpirationInterval());
        }

        PartitionController partitionController =
            new PartitionControllerImpl(
                leaseStoreManager,
                leaseStoreManager,
                partitionSupervisorFactory,
                synchronizer,
                scheduler,
                this.changeFeedProcessorOptions.getMaxScaleCount());

        if (this.healthMonitor == null) {
            this.healthMonitor = new TraceHealthMonitor();
        }

        PartitionController partitionController2 = new HealthMonitoringPartitionControllerDecorator(partitionController, this.healthMonitor);

        PartitionLoadBalancer partitionLoadBalancer = new PartitionLoadBalancerImpl(
            partitionController2,
            leaseStoreManager,
            this.loadBalancingStrategy,
            this.changeFeedProcessorOptions.getLeaseAcquireInterval(),
            this.scheduler
        );

        PartitionManager partitionManager = new PartitionManagerImpl(bootstrapper, partitionController, partitionLoadBalancer);

        return Mono.just(partitionManager);
    }

    private FeedRangeThroughputControlConfigManager getFeedRangeThroughputControlConfigManager() {
        if (this.changeFeedProcessorOptions != null && this.changeFeedProcessorOptions.getFeedPollThroughputControlGroupConfig() != null) {
            return new FeedRangeThroughputControlConfigManager(
                this.changeFeedProcessorOptions.getFeedPollThroughputControlGroupConfig(),
                this.feedContextClient);
        }

        return null;
    }

    private String getEpkRangeVersionLeasePrefix() {
        String optionsPrefix = this.changeFeedProcessorOptions.getLeasePrefix();

        if (optionsPrefix == null) {
            optionsPrefix = "";
        }

        URI uri = this.feedContextClient.getServiceEndpoint();

        return String.format(
            "%s%s_%s_%s",
            optionsPrefix,
            uri.getHost(),
            this.databaseResourceId,
            this.collectionResourceId);
    }

    @Override
    public void close() {
        this.stop().subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
}
