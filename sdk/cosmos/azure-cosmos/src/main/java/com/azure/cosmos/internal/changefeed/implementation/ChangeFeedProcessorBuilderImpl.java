// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.internal.changefeed.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.internal.changefeed.Bootstrapper;
import com.azure.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.cosmos.internal.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.internal.changefeed.CheckpointFrequency;
import com.azure.cosmos.internal.changefeed.HealthMonitor;
import com.azure.cosmos.internal.changefeed.LeaseStoreManager;
import com.azure.cosmos.internal.changefeed.PartitionController;
import com.azure.cosmos.internal.changefeed.PartitionLoadBalancer;
import com.azure.cosmos.internal.changefeed.PartitionLoadBalancingStrategy;
import com.azure.cosmos.internal.changefeed.PartitionManager;
import com.azure.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.cosmos.internal.changefeed.PartitionProcessorFactory;
import com.azure.cosmos.internal.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.internal.changefeed.RequestOptionsFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Helper class to buildAsyncClient {@link ChangeFeedProcessor} instances
 * as logical representation of the Azure Cosmos DB database service.
 *
 * <pre>
 * {@code
 *  ChangeFeedProcessor.Builder()
 *     .setHostName(setHostName)
 *     .setFeedContainer(setFeedContainer)
 *     .setLeaseContainer(setLeaseContainer)
 *     .setHandleChanges(docs -> {
 *         // Implementation for handling and processing CosmosItemProperties list goes here
 *      })
 *     .observer(SampleObserverImpl.class)
 *     .buildAsyncClient();
 * }
 * </pre>
 */
public class ChangeFeedProcessorBuilderImpl implements ChangeFeedProcessor.BuilderDefinition, ChangeFeedProcessor, AutoCloseable {
    private static final long DefaultUnhealthinessDuration = Duration.ofMinutes(15).toMillis();
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);
    private static final int DefaultQueryPartitionsMaxBatchSize = 100;

    private int queryPartitionsMaxBatchSize = DefaultQueryPartitionsMaxBatchSize;
    private int degreeOfParallelism = 25; // default


    private String hostName;
    private ChangeFeedContextClient feedContextClient;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private ChangeFeedObserverFactory observerFactory;
    private volatile String databaseResourceId;
    private volatile String collectionResourceId;
    private ChangeFeedContextClient leaseContextClient;
    private PartitionLoadBalancingStrategy loadBalancingStrategy;
    private PartitionProcessorFactory partitionProcessorFactory;
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
                    .flatMap(leaseStoreManager -> this.buildPartitionManager(leaseStoreManager)))
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
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique name.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored collection.
     *
     * @param feedDocumentClient the instance of {@link CosmosAsyncContainer} to be used.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl setFeedContainer(CosmosAsyncContainer feedDocumentClient) {
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
    @Override
    public ChangeFeedProcessorBuilderImpl setOptions(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
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
    public ChangeFeedProcessorBuilderImpl observerFactory(ChangeFeedObserverFactory observerFactory) {
        if (observerFactory == null) {
            throw new IllegalArgumentException("observerFactory");
        }

        this.observerFactory = observerFactory;
        return this;
    }

    /**
     * Sets an existing {@link ChangeFeedObserver} type to be used by a {@link ChangeFeedObserverFactory} to process changes.
     * @param type the type of {@link ChangeFeedObserver} to be used.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl observer(Class<? extends ChangeFeedObserver> type) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }

        this.observerFactory = new ChangeFeedObserverFactoryImpl(type);

        return this;
    }

    @Override
    public ChangeFeedProcessorBuilderImpl setHandleChanges(Consumer<List<CosmosItemProperties>> consumer) {
        return this.observerFactory(new DefaultObserverFactory(consumer));
    }

    /**
     * Sets the database resource ID of the monitored collection.
     *
     * @param databaseResourceId the database resource ID of the monitored collection.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withDatabaseResourceId(String databaseResourceId) {
        this.databaseResourceId = databaseResourceId;
        return this;
    }

    /**
     * Sets the collection resource ID of the monitored collection.
     * @param collectionResourceId the collection resource ID of the monitored collection.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withCollectionResourceId(String collectionResourceId) {
        this.collectionResourceId = collectionResourceId;
        return this;
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases collection.
     *
     * @param leaseDocumentClient the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl setLeaseContainer(CosmosAsyncContainer leaseDocumentClient) {
        if (leaseDocumentClient == null) {
            throw new IllegalArgumentException("leaseContextClient");
        }

        this.leaseContextClient = new ChangeFeedContextClientImpl(leaseDocumentClient);
        return this;
    }

    /**
     * Sets the {@link PartitionLoadBalancingStrategy} to be used for partition load balancing.
     *
     * @param loadBalancingStrategy the {@link PartitionLoadBalancingStrategy} to be used for partition load balancing.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withPartitionLoadBalancingStrategy(PartitionLoadBalancingStrategy loadBalancingStrategy) {
        if (loadBalancingStrategy == null) {
            throw new IllegalArgumentException("loadBalancingStrategy");
        }

        this.loadBalancingStrategy = loadBalancingStrategy;
        return this;
    }

    /**
     * Sets the {@link PartitionProcessorFactory} to be used to create {@link PartitionProcessor} for partition processing.
     *
     * @param partitionProcessorFactory the instance of {@link PartitionProcessorFactory} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withPartitionProcessorFactory(PartitionProcessorFactory partitionProcessorFactory) {
        if (partitionProcessorFactory == null) {
            throw new IllegalArgumentException("partitionProcessorFactory");
        }

        this.partitionProcessorFactory = partitionProcessorFactory;
        return this;
    }

    /**
     * Sets the {@link LeaseStoreManager} to be used to manage leases.
     *
     * @param leaseStoreManager the instance of {@link LeaseStoreManager} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withLeaseStoreManager(LeaseStoreManager leaseStoreManager) {
        if (leaseStoreManager == null) {
            throw new IllegalArgumentException("leaseStoreManager");
        }

        this.leaseStoreManager = leaseStoreManager;
        return this;
    }

    /**
     * Sets the {@link HealthMonitor} to be used to monitor unhealthiness situation.
     *
     * @param healthMonitor The instance of {@link HealthMonitor} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withHealthMonitor(HealthMonitor healthMonitor) {
        if (healthMonitor == null) {
            throw new IllegalArgumentException("healthMonitor");
        }

        this.healthMonitor = healthMonitor;
        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration asynchronously.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    @Override
    public ChangeFeedProcessor build() {
        if (this.hostName == null) {
            throw new IllegalArgumentException("Host name was not specified");
        }

        if (this.observerFactory == null) {
            throw new IllegalArgumentException("Observer was not specified");
        }

        if (this.scheduler == null) {
            this.scheduler = Schedulers.elastic();
        }

        return this;
    }

    public ChangeFeedProcessorBuilderImpl() {
        this.queryPartitionsMaxBatchSize = DefaultQueryPartitionsMaxBatchSize;
        this.degreeOfParallelism = 25; // default
    }

    public ChangeFeedProcessorBuilderImpl(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    private Mono<ChangeFeedProcessor> initializeCollectionPropertiesForBuild() {
        if (this.changeFeedProcessorOptions == null) {
            this.changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        }

        return this.feedContextClient
            .readDatabase(this.feedContextClient.getDatabaseClient(), null)
            .map( databaseResourceResponse -> {
                this.databaseResourceId = databaseResourceResponse.getDatabase().getId();
                return this.databaseResourceId;
            })
            .flatMap( id -> this.feedContextClient
                .readContainer(this.feedContextClient.getContainerClient(), null)
                .map(documentCollectionResourceResponse -> {
                    this.collectionResourceId = documentCollectionResourceResponse.getContainer().getId();
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

                    return LeaseStoreManager.Builder()
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
        CheckpointerObserverFactory factory = new CheckpointerObserverFactory(this.observerFactory, new CheckpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
            this.feedContextClient,
            this.feedContextClient.getContainerClient(),
            leaseStoreManager,
            leaseStoreManager,
            this.degreeOfParallelism,
            this.queryPartitionsMaxBatchSize
        );

        Bootstrapper bootstrapper = new BootstrapperImpl(synchronizer, leaseStoreManager, this.lockTime, this.sleepTime);
        PartitionSupervisorFactory partitionSupervisorFactory = new PartitionSupervisorFactoryImpl(
            factory,
            leaseStoreManager,
            this.partitionProcessorFactory != null ? this.partitionProcessorFactory : new PartitionProcessorFactoryImpl(
                this.feedContextClient,
                this.changeFeedProcessorOptions,
                leaseStoreManager,
                this.feedContextClient.getContainerClient()),
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

    @Override
    public void close() {
        this.stop().subscribeOn(Schedulers.elastic()).subscribe();
    }
}
