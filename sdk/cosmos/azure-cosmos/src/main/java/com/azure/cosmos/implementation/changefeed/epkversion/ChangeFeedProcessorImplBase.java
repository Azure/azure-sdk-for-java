// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Strings;
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
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.CheckpointerObserverFactory;
import com.azure.cosmos.implementation.changefeed.common.DefaultObserverFactory;
import com.azure.cosmos.implementation.changefeed.common.EqualPartitionsBalancingStrategy;
import com.azure.cosmos.implementation.changefeed.common.PartitionedByIdCollectionRequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.common.TraceHealthMonitor;
import com.azure.cosmos.ChangeFeedProcessorContext;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ChangeFeedProcessorImplBase<T> implements ChangeFeedProcessor, AutoCloseable{
    private final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorImplBase.class);
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);
    private static final int DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE = 100;
    private final static int DEFAULT_DEGREE_OF_PARALLELISM = 25; // default

    private final String hostName;
    private final ChangeFeedContextClient feedContextClient;
    private final ChangeFeedContextClient leaseContextClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final ChangeFeedObserverFactory<T> observerFactory;
    private final ChangeFeedMode changeFeedMode;
    private final Scheduler scheduler;

    private volatile String databaseResourceId;
    private volatile String databaseId;
    private volatile String collectionResourceId;
    private volatile String collectionId;
    private PartitionLoadBalancingStrategy loadBalancingStrategy;
    private LeaseStoreManager leaseStoreManager;
    private HealthMonitor healthMonitor;
    private volatile PartitionManager partitionManager;

    public ChangeFeedProcessorImplBase(
            String hostName,
            CosmosAsyncContainer feedContainer,
            CosmosAsyncContainer leaseContainer,
            ChangeFeedProcessorOptions changeFeedProcessorOptions,
            Consumer<List<T>> consumer,
            ChangeFeedMode changeFeedMode) {

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
        this.changeFeedMode = changeFeedMode;
        this.observerFactory = new DefaultObserverFactory<>(consumer);
    }

    public ChangeFeedProcessorImplBase(String hostName,
                                       CosmosAsyncContainer feedContainer,
                                       CosmosAsyncContainer leaseContainer,
                                       ChangeFeedProcessorOptions changeFeedProcessorOptions,
                                       BiConsumer<List<T>, ChangeFeedProcessorContext> biConsumer,
                                       ChangeFeedMode changeFeedMode) {
        checkNotNull(hostName, "Argument 'hostName' can not be null");
        checkNotNull(feedContainer, "Argument 'feedContainer' can not be null");
        checkNotNull(biConsumer, "Argument 'biConsumer' can not be null");

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
        this.changeFeedMode = changeFeedMode;
        this.observerFactory = new DefaultObserverFactory<>(biConsumer);
    }

    abstract CosmosChangeFeedRequestOptions createRequestOptionsForProcessingFromNow(FeedRange feedRange);

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
                    .flatMap(value -> this.getLeaseStoreManager().flatMap(this::buildPartitionManager))
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
     * getEstimatedLag() API is not supported on v1 Change Feed Processor which use epk range based lease internally. Use getCurrentState() instead.
     *
     * @return throws an {@link UnsupportedOperationException}.
     */
    @Override
    public Mono<Map<String, Integer>> getEstimatedLag() {
        throw new UnsupportedOperationException("getEstimatedLag() API is not supported. Use getCurrentState() instead");
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
                .flatMap(leaseStoreManager1 -> leaseStoreManager1
                        .getAllLeases()
                        .flatMap(lease -> {
                            CosmosChangeFeedRequestOptions options = this.createRequestOptionsForProcessingFromNow(lease.getFeedRange());

                            return this.feedContextClient
                                    .createDocumentChangeFeedQuery(this.feedContextClient.getContainerClient(), options, ChangeFeedProcessorItem.class, false)
                                    .take(1)
                                    .map(feedResponse -> {
                                        ChangeFeedProcessorState changeFeedProcessorState = new ChangeFeedProcessorState()
                                                .setHostName(lease.getOwner())
                                                .setLeaseToken(lease.getLeaseToken());

                                        long latestLsn;
                                        long estimatedLag = 0;
                                        long currentLsn;
                                        try {
                                            latestLsn = getLsnFromEncodedContinuationToken(feedResponse.getContinuationToken());
                                            changeFeedProcessorState.setContinuationToken(feedResponse.getContinuationToken());
                                            if (Strings.isNullOrWhiteSpace(lease.getContinuationToken())) {
                                                //  Lease continuation token is null
                                                //  Lease is never initialized, which means CFP has not processed any
                                                //  documents
                                                //  Estimated lag will be (latest lsn) - 1
                                                estimatedLag = latestLsn - 1;
                                            } else {
                                                //  Otherwise, estimated lag will be latest lsn - current lsn
                                                currentLsn = getLsnFromEncodedContinuationToken(lease.getContinuationToken());
                                                estimatedLag = latestLsn - currentLsn;
                                            }
                                        } catch (NumberFormatException ex) {
                                            logger.warn("Unexpected Cosmos LSN found", ex);
                                            changeFeedProcessorState.setEstimatedLag(-1);
                                        }

                                        changeFeedProcessorState.setEstimatedLag((int)Math.min(estimatedLag, Integer.MAX_VALUE));
                                        return changeFeedProcessorState;
                                    });
                        })
                        .collectList()
                        .map(Collections::unmodifiableList)
                );
    }

    private long getLsnFromEncodedContinuationToken(String continuationToken) {
        long lsn;
        ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuationToken);
        String token = changeFeedState
                .getContinuation()
                .getCurrentContinuationToken()
                .getToken();
        //   Remove extra quotes from token.
        token = token.replace("\"", "");
        lsn = Long.parseLong(token);
        return lsn;
    }

    private Mono<ChangeFeedProcessor> initializeCollectionPropertiesForBuild() {
        return this.feedContextClient
                .readDatabase(this.feedContextClient.getDatabaseClient(), null)
                .map(databaseResourceResponse -> {
                    this.databaseResourceId = databaseResourceResponse.getProperties().getResourceId();
                    this.databaseId = databaseResourceResponse.getProperties().getId();
                    return this.databaseResourceId;
                })
                .flatMap( id -> this.feedContextClient
                        .readContainer(this.feedContextClient.getContainerClient(), null)
                        .map(documentCollectionResourceResponse -> {
                            this.collectionResourceId = documentCollectionResourceResponse.getProperties().getResourceId();
                            this.collectionId = documentCollectionResourceResponse.getProperties().getId();
                            return this;
                        }));
    }

    private Mono<LeaseStoreManager> getLeaseStoreManager() {
        if (this.leaseStoreManager == null) {

            return this.leaseContextClient.readContainerSettings(this.leaseContextClient.getContainerClient(), null)
                    .flatMap(collectionSettings -> {
                        if (!this.isContainerPartitionedById(collectionSettings)) {
                            return Mono.error(new IllegalArgumentException("The lease collection must have partition key equal to id."));
                        }

                        RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();

                        String leasePrefix = this.getLeasePrefix();
                        this.leaseStoreManager =
                                LeaseStoreManagerImpl.builder()
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

    private boolean isContainerPartitionedById(CosmosContainerProperties containerProperties) {
        return containerProperties != null
                && containerProperties.getPartitionKeyDefinition() != null
                && !containerProperties.getPartitionKeyDefinition().getPaths().isEmpty()
                && containerProperties.getPartitionKeyDefinition().getPaths().size() == 1
                && containerProperties.getPartitionKeyDefinition().getPaths().get(0).equals("/id");
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
                this.databaseResourceId,
                this.collectionResourceId);
    }

    private String getPkRangeIdVersionLeasePrefix() {
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

    abstract Class<T> getPartitionProcessorItemType();
    abstract boolean canBootstrapFromPkRangeIdVersionLeaseStore();

    private Mono<PartitionManager> buildPartitionManager(LeaseStoreManager leaseStoreManager) {
        CheckpointerObserverFactory<T> factory = new CheckpointerObserverFactory<>(this.observerFactory, new CheckpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
                this.feedContextClient,
                BridgeInternal.extractContainerSelfLink(this.feedContextClient.getContainerClient()),
                leaseStoreManager,
                leaseStoreManager,
                DEFAULT_DEGREE_OF_PARALLELISM,
                DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE,
                this.changeFeedProcessorOptions,
                this.changeFeedMode);

        Bootstrapper bootstrapper;
        if (this.canBootstrapFromPkRangeIdVersionLeaseStore()) {

            String pkRangeIdVersionLeasePrefix = this.getPkRangeIdVersionLeasePrefix();
            RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();
            LeaseStoreManager pkRangeIdVersionLeaseStoreManager =
                com.azure.cosmos.implementation.changefeed.pkversion.LeaseStoreManagerImpl.builder()
                    .leasePrefix(pkRangeIdVersionLeasePrefix)
                    .leaseCollectionLink(this.leaseContextClient.getContainerClient())
                    .leaseContextClient(this.leaseContextClient)
                    .requestOptionsFactory(requestOptionsFactory)
                    .hostName(this.hostName)
                    .build();

            bootstrapper = new PkRangeIdVersionLeaseStoreBootstrapperImpl(
                synchronizer,
                leaseStoreManager,
                this.lockTime,
                this.sleepTime,
                pkRangeIdVersionLeaseStoreManager,
                leaseStoreManager,
                this.changeFeedMode);
        } else {
            bootstrapper = new BootstrapperImpl(
                synchronizer,
                leaseStoreManager,
                this.lockTime,
                this.sleepTime,
                leaseStoreManager,
                this.changeFeedMode);
        }

        FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager = this.getFeedRangeThroughputControlConfigManager();

        PartitionSupervisorFactory partitionSupervisorFactory = new PartitionSupervisorFactoryImpl<>(
                factory,
                leaseStoreManager,
                new PartitionProcessorFactoryImpl<>(
                        this.feedContextClient,
                        this.changeFeedProcessorOptions,
                        leaseStoreManager,
                        this.feedContextClient.getContainerClient(),
                        this.collectionResourceId,
                        this.changeFeedMode,
                        feedRangeThroughputControlConfigManager),
                this.changeFeedProcessorOptions,
                this.scheduler,
                this.getPartitionProcessorItemType()
        );

        if (this.loadBalancingStrategy == null) {
            this.loadBalancingStrategy = new EqualPartitionsBalancingStrategy(
                    this.hostName,
                    this.changeFeedProcessorOptions.getMinScaleCount(),
                    this.changeFeedProcessorOptions.getMaxScaleCount(),
                    this.changeFeedProcessorOptions.getLeaseExpirationInterval());
        }

        PartitionController partitionController = new PartitionControllerImpl(leaseStoreManager, leaseStoreManager, partitionSupervisorFactory, synchronizer, scheduler);

        if (this.healthMonitor == null) {
            this.healthMonitor = new TraceHealthMonitor();
        }

        PartitionController partitionController2 = new HealthMonitoringPartitionControllerDecorator(partitionController, this.healthMonitor);

        PartitionLoadBalancer partitionLoadBalancer = new PartitionLoadBalancerImpl(
                partitionController2,
                leaseStoreManager,
                this.loadBalancingStrategy,
                this.changeFeedProcessorOptions.getLeaseAcquireInterval(),
                this.scheduler,
                feedRangeThroughputControlConfigManager
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

    @Override
    public void close() {
        this.stop().subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
}
