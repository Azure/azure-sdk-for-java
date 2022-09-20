// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
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
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.CheckpointerObserverFactory;
import com.azure.cosmos.implementation.changefeed.common.DefaultObserverFactory;
import com.azure.cosmos.implementation.changefeed.common.EqualPartitionsBalancingStrategy;
import com.azure.cosmos.implementation.changefeed.common.PartitionedByIdCollectionRequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.common.TraceHealthMonitor;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
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
import java.util.function.Consumer;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;

/**
 * Helper class to buildAsyncClient {@link ChangeFeedProcessor} instances
 * as logical representation of the Azure Cosmos DB database service.
 *
 */
public class ChangeFeedProcessorBuilderImpl implements ChangeFeedProcessor, AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorBuilderImpl.class);
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);
    private static final int DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE = 100;
    private final static int DEFAULT_DEGREE_OF_PARALLELISM = 25; // default


    private String hostName;
    private ChangeFeedContextClient feedContextClient;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private ChangeFeedObserverFactory<ChangeFeedProcessorItem> observerFactory;
    private volatile String databaseResourceId;
    private volatile String collectionResourceId;
    private ChangeFeedContextClient leaseContextClient;
    private PartitionLoadBalancingStrategy loadBalancingStrategy;
    private LeaseStoreManager leaseStoreManager;
    private HealthMonitor healthMonitor;
    private volatile PartitionManager partitionManager;

    private Scheduler scheduler;

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
     * getEstimatedLag() API is not supported on Full Fidelity Change Feed Processor. Use getCurrentState() instead.
     *
     * @return throws an {@link UnsupportedOperationException}.
     */
    @Override
    public Mono<Map<String, Integer>> getEstimatedLag() {
        throw new UnsupportedOperationException("getEstimatedLag() API is not supported on Full Fidelity Change Feed Processor. "
            + "Use getCurrentState() instead");
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
                           CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                               .createForProcessingFromNow(lease.getFeedRange())
                               .setMaxItemCount(1)
                               .allVersionsAndDeletes();

                           return this.feedContextClient
                               .createDocumentChangeFeedQuery(this.feedContextClient.getContainerClient(), options, ChangeFeedProcessorItem.class)
                               .take(1)
                               .map(feedResponse -> {
                                   ChangeFeedProcessorState changeFeedProcessorState = new ChangeFeedProcessorState()
                                       .setHostName(lease.getOwner())
                                       .setLeaseToken(lease.getLeaseToken());

                                   int latestLsn = 0;
                                   int estimatedLag = 0;
                                   int currentLsn = 0;
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

                                   changeFeedProcessorState.setEstimatedLag(estimatedLag);
                                   return changeFeedProcessorState;
                              });
                       })
                       .collectList()
                       .map(Collections::unmodifiableList)
                   );
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique name.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored collection.
     *
     * @param feedDocumentClient the instance of {@link CosmosAsyncContainer} to be used.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl feedContainer(CosmosAsyncContainer feedDocumentClient) {
        if (feedDocumentClient == null) {
            throw new IllegalArgumentException("feedContextClient");
        }

        this.feedContextClient = new ChangeFeedContextClientImpl(feedDocumentClient);
        return this;
    }

    /**
     * Sets the {@link ChangeFeedProcessorOptions} to be used.
     *
     * @param changeFeedProcessorOptions the change feed processor options to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl options(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        if (changeFeedProcessorOptions == null) {
            throw new IllegalArgumentException("changeFeedProcessorOptions");
        }

        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        return this;
    }

    /**
     * Sets the {@link ChangeFeedObserverFactory} to be used to generate {@link ChangeFeedObserver}
     *
     * @param observerFactory The instance of {@link ChangeFeedObserverFactory} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl observerFactory(ChangeFeedObserverFactory<ChangeFeedProcessorItem> observerFactory) {
        if (observerFactory == null) {
            throw new IllegalArgumentException("observerFactory");
        }

        this.observerFactory = observerFactory;
        return this;
    }

    public ChangeFeedProcessorBuilderImpl handleChanges(Consumer<List<ChangeFeedProcessorItem>> consumer) {
        return this.observerFactory(new DefaultObserverFactory<>(consumer));
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases collection.
     *
     * @param leaseClient the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl leaseContainer(CosmosAsyncContainer leaseClient) {
        if (leaseClient == null) {
            throw new IllegalArgumentException("leaseClient");
        }

        if (!getContextClient(leaseClient).isContentResponseOnWriteEnabled()) {
            throw new IllegalArgumentException("leaseClient: content response on write setting must be enabled");
        }

        ConsistencyLevel consistencyLevel = getContextClient(leaseClient).getConsistencyLevel();
        if (consistencyLevel == ConsistencyLevel.CONSISTENT_PREFIX || consistencyLevel == ConsistencyLevel.EVENTUAL) {
            logger.warn("leaseClient consistency level setting are less then expected which is SESSION");
        }

        this.leaseContextClient = new ChangeFeedContextClientImpl(leaseClient);

        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration asynchronously.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor build() {
        if (this.hostName == null) {
            throw new IllegalArgumentException("Host name was not specified");
        }

        if (this.observerFactory == null) {
            throw new IllegalArgumentException("Observer was not specified");
        }

        if (this.changeFeedProcessorOptions != null && this.changeFeedProcessorOptions.getLeaseAcquireInterval().compareTo(ChangeFeedProcessorOptions.DEFAULT_ACQUIRE_INTERVAL) < 0) {
            logger.warn("Found lower than expected setting for leaseAcquireInterval");
        }

        if (this.changeFeedProcessorOptions == null) {
            this.changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        }

        this.scheduler = this.changeFeedProcessorOptions.getScheduler();
        this.feedContextClient.setScheduler(this.scheduler);
        this.leaseContextClient.setScheduler(this.scheduler);

        return this;
    }

    public ChangeFeedProcessorBuilderImpl() {
    }

    private Mono<ChangeFeedProcessor> initializeCollectionPropertiesForBuild() {
        return this.feedContextClient
            .readDatabase(this.feedContextClient.getDatabaseClient(), null)
            .map( databaseResourceResponse -> {
                this.databaseResourceId = databaseResourceResponse.getProperties().getResourceId();
                return this.databaseResourceId;
            })
            .flatMap( id -> this.feedContextClient
                .readContainer(this.feedContextClient.getContainerClient(), null)
                .map(documentCollectionResourceResponse -> {
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

                    return LeaseStoreManagerImpl.builder()
                        .leasePrefix(leasePrefix)
                        .leaseCollectionLink(this.leaseContextClient.getContainerClient())
                        .leaseContextClient(this.leaseContextClient)
                        .requestOptionsFactory(requestOptionsFactory)
                        .hostName(this.hostName)
                        .build()
                        .map(manager -> {
                            this.leaseStoreManager = manager;
                            return this.leaseStoreManager;
                        });
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
            this.databaseResourceId,
            this.collectionResourceId);
    }

    private Mono<PartitionManager> buildPartitionManager(LeaseStoreManager leaseStoreManager) {
        CheckpointerObserverFactory<ChangeFeedProcessorItem> factory =
            new CheckpointerObserverFactory<>(this.observerFactory, new CheckpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
            this.feedContextClient,
            this.feedContextClient.getContainerClient(),
            leaseStoreManager,
            leaseStoreManager,
            DEFAULT_DEGREE_OF_PARALLELISM,
            DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE,
            this.collectionResourceId
        );

        Bootstrapper bootstrapper = new BootstrapperImpl(synchronizer, leaseStoreManager, this.lockTime, this.sleepTime);
        PartitionSupervisorFactory partitionSupervisorFactory = new PartitionSupervisorFactoryImpl(
            factory,
            leaseStoreManager,
            new PartitionProcessorFactoryImpl(
                this.feedContextClient,
                this.changeFeedProcessorOptions,
                leaseStoreManager,
                this.feedContextClient.getContainerClient(),
                this.collectionResourceId),
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
            this.scheduler
        );

        PartitionManager partitionManager = new PartitionManagerImpl(bootstrapper, partitionController, partitionLoadBalancer);

        return Mono.just(partitionManager);
    }

    private int getLsnFromEncodedContinuationToken(String continuationToken) {
        int lsn;
        ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuationToken);
        String token = changeFeedState
            .getContinuation()
            .getCurrentContinuationToken()
            .getToken();
        //   Remove extra quotes from token.
        token = token.replace("\"", "");
        lsn = Integer.parseInt(token);
        return lsn;
    }

    @Override
    public void close() {
        this.stop().subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
}
